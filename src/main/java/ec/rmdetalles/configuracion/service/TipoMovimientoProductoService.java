package ec.rmdetalles.configuracion.service;

import ec.rmdetalles.configuracion.api.TipoMovimientoProductoRequest;
import ec.rmdetalles.configuracion.api.TipoMovimientoProductoResponse;
import ec.rmdetalles.configuracion.config.NegocioContext;
import ec.rmdetalles.configuracion.repository.TipoMovimientoProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class TipoMovimientoProductoService {
    private final TipoMovimientoProductoRepository repository;
    public TipoMovimientoProductoService(TipoMovimientoProductoRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<TipoMovimientoProductoResponse> listar() {
        return repository.listar(NegocioContext.getRequired()).stream()
                .map(TipoMovimientoProductoResponse::from).toList();
    }

    public TipoMovimientoProductoResponse guardar(String codigoEntrada, TipoMovimientoProductoRequest request) {
        UUID negocioId = NegocioContext.getRequired();
        String codigo = normalizarCodigo(codigoEntrada);
        if (request.factorStock() != 1 && request.factorStock() != -1) {
            throw new IllegalArgumentException("factorStock solo puede ser 1 o -1.");
        }
        String nombre = request.nombre().trim();
        var existente = repository.buscar(negocioId, codigo);
        if (existente.isPresent() && existente.get().factorStock() != request.factorStock()
                && repository.tieneMovimientos(negocioId, codigo)) {
            throw new TipoMovimientoInmutableException(
                    "No se puede cambiar factorStock porque este tipo ya tiene movimientos registrados.");
        }
        repository.guardar(negocioId, codigo, nombre, request.factorStock(), request.activo());
        return repository.buscar(negocioId, codigo).map(TipoMovimientoProductoResponse::from)
                .orElseThrow(() -> new IllegalStateException("No se encontró el tipo de movimiento después de guardarlo."));
    }

    private String normalizarCodigo(String codigo) {
        if (codigo == null || !codigo.trim().matches("[A-Za-z][A-Za-z0-9_]{0,19}")) {
            throw new IllegalArgumentException("El código debe tener hasta 20 caracteres alfanuméricos o guion bajo y empezar con una letra.");
        }
        return codigo.trim().toUpperCase(Locale.ROOT);
    }
}
