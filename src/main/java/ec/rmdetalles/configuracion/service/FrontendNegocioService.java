package ec.rmdetalles.configuracion.service;

import ec.rmdetalles.configuracion.api.FrontendNegocioRequest;
import ec.rmdetalles.configuracion.api.FrontendNegocioResponse;
import ec.rmdetalles.configuracion.config.NegocioContext;
import ec.rmdetalles.configuracion.domain.FrontendNegocio;
import ec.rmdetalles.configuracion.repository.FrontendNegocioRepository;
import ec.rmdetalles.configuracion.repository.NegocioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FrontendNegocioService {
    private final FrontendNegocioRepository frontends;
    private final NegocioRepository negocios;

    public FrontendNegocioService(
            FrontendNegocioRepository frontends,
            NegocioRepository negocios
    ) {
        this.frontends = frontends;
        this.negocios = negocios;
    }

    @Transactional(readOnly = true)
    public List<FrontendNegocioResponse> listar() {
        return frontends.findByNegocio_IdOrderByOrigenAsc(NegocioContext.getRequired())
                .stream().map(FrontendNegocioResponse::from).toList();
    }

    public FrontendNegocioResponse crear(FrontendNegocioRequest request) {
        UUID negocioId = NegocioContext.getRequired();
        String origen = normalizarOrigen(request.origen());
        if (frontends.existsByNegocio_IdAndOrigenIgnoreCase(negocioId, origen)) {
            throw new IllegalArgumentException("Ese origen ya está registrado para este negocio.");
        }
        String host = resolverHost(origen, request.hostPublico());
        FrontendNegocio frontend = new FrontendNegocio(
                negocios.getReferenceById(negocioId), origen, host);
        if (Boolean.FALSE.equals(request.activo())) frontend.desactivar();
        return FrontendNegocioResponse.from(frontends.saveAndFlush(frontend));
    }

    public FrontendNegocioResponse actualizar(UUID id, FrontendNegocioRequest request) {
        UUID negocioId = NegocioContext.getRequired();
        FrontendNegocio frontend = buscar(id, negocioId);
        String origen = normalizarOrigen(request.origen());
        if (frontends.existsByNegocio_IdAndOrigenIgnoreCaseAndIdNot(negocioId, origen, id)) {
            throw new IllegalArgumentException("Ese origen ya está registrado para este negocio.");
        }
        frontend.actualizar(origen, resolverHost(origen, request.hostPublico()), request.activo());
        return FrontendNegocioResponse.from(frontends.saveAndFlush(frontend));
    }

    public void desactivar(UUID id) {
        FrontendNegocio frontend = buscar(id, NegocioContext.getRequired());
        frontend.desactivar();
        frontends.save(frontend);
    }

    private FrontendNegocio buscar(UUID id, UUID negocioId) {
        return frontends.findByIdAndNegocio_Id(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el frontend para el negocio actual."));
    }

    private String normalizarOrigen(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El origen es obligatorio.");
        }
        String origin = value.trim().replaceAll("/+$", "");
        URI uri;
        try {
            uri = URI.create(origin);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("El origen debe ser una URL válida, por ejemplo https://tienda.example.com.");
        }
        boolean schemeValid = "http".equalsIgnoreCase(uri.getScheme())
                || "https".equalsIgnoreCase(uri.getScheme());
        if (!schemeValid || uri.getHost() == null || uri.getRawUserInfo() != null
                || uri.getRawQuery() != null || uri.getRawFragment() != null
                || (uri.getRawPath() != null && !uri.getRawPath().isEmpty())) {
            throw new IllegalArgumentException(
                    "El origen debe incluir esquema y host, sin ruta, credenciales, consulta ni fragmento.");
        }
        return origin;
    }

    private String resolverHost(String origin, String hostPublico) {
        if (hostPublico == null || hostPublico.isBlank()) {
            return URI.create(origin).getHost();
        }
        String host = hostPublico.trim();
        if (host.contains("/") || host.contains(":") || host.contains(" ")) {
            throw new IllegalArgumentException("hostPublico debe contener solo el nombre de host, sin puerto ni esquema.");
        }
        return host;
    }
}
