package ec.rmdetalles.configuracion.service;

import ec.rmdetalles.configuracion.api.*;
import ec.rmdetalles.configuracion.config.NegocioContext;
import ec.rmdetalles.configuracion.repository.ConfiguracionPedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class ConfiguracionPedidoService {
    private final ConfiguracionPedidoRepository repository;

    public ConfiguracionPedidoService(ConfiguracionPedidoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<EstadoPedidoResponse> listarEstados() {
        return repository.listarEstados(NegocioContext.getRequired());
    }

    public EstadoPedidoResponse guardarEstado(String codigo, EstadoPedidoRequest request) {
        UUID negocioId = NegocioContext.getRequired();
        String normalizado = normalizarCodigo(codigo);
        repository.bloquearNegocio(negocioId);
        List<EstadoPedidoResponse> estados = repository.listarEstados(negocioId);
        if (request.activo() && request.inicial() && estados.stream().anyMatch(
                estado -> estado.activo() && estado.inicial()
                        && !estado.codigo().equals(normalizado))) {
            throw new IllegalArgumentException("Ya existe un estado inicial activo para el negocio.");
        }
        repository.guardarEstado(negocioId, normalizado, request);
        return new EstadoPedidoResponse(normalizado, request.nombre().trim(), request.orden(),
                request.inicial(), request.finalizado(), request.activo());
    }

    @Transactional(readOnly = true)
    public List<TransicionPedidoResponse> listarTransiciones() {
        return repository.listarTransiciones(NegocioContext.getRequired());
    }

    public TransicionPedidoResponse guardarTransicion(
            String origen, String destino, boolean activo
    ) {
        UUID negocioId = NegocioContext.getRequired();
        String origenNormalizado = normalizarCodigo(origen);
        String destinoNormalizado = normalizarCodigo(destino);
        if (origenNormalizado.equals(destinoNormalizado)) {
            throw new IllegalArgumentException("Los estados de origen y destino deben ser distintos.");
        }
        repository.bloquearNegocio(negocioId);
        List<EstadoPedidoResponse> estados = repository.listarEstados(negocioId);
        validarEstado(estados, origenNormalizado, activo);
        validarEstado(estados, destinoNormalizado, activo);
        repository.guardarTransicion(negocioId, origenNormalizado, destinoNormalizado, activo);
        return new TransicionPedidoResponse(origenNormalizado, destinoNormalizado, activo);
    }

    private void validarEstado(List<EstadoPedidoResponse> estados, String codigo, boolean activo) {
        EstadoPedidoResponse estado = estados.stream()
                .filter(item -> item.codigo().equals(codigo)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el estado " + codigo + " para el negocio actual."));
        if (activo && !estado.activo()) {
            throw new IllegalArgumentException("Una transición activa requiere estados activos.");
        }
    }

    private String normalizarCodigo(String codigo) {
        String resultado = codigo.trim().toUpperCase(Locale.ROOT);
        if (resultado.isBlank() || resultado.length() > 30) {
            throw new IllegalArgumentException("El código debe tener entre 1 y 30 caracteres.");
        }
        return resultado;
    }
}
