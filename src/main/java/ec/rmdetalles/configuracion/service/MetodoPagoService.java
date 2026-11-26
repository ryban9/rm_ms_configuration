package ec.rmdetalles.configuracion.service;

import ec.rmdetalles.configuracion.api.MetodoPagoRequest;
import ec.rmdetalles.configuracion.api.MetodoPagoResponse;
import ec.rmdetalles.configuracion.config.NegocioContext;
import ec.rmdetalles.configuracion.repository.MetodoPagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class MetodoPagoService {
    private final MetodoPagoRepository repository;

    public MetodoPagoService(MetodoPagoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<MetodoPagoResponse> listar() {
        return repository.listar(NegocioContext.getRequired());
    }

    public MetodoPagoResponse guardar(String codigo, MetodoPagoRequest request) {
        if (codigo == null || !codigo.trim().matches("[A-Za-z][A-Za-z0-9_]{0,19}")) {
            throw new IllegalArgumentException(
                    "El código debe empezar con una letra y tener hasta 20 caracteres alfanuméricos o guion bajo.");
        }
        String normalizado = codigo.trim().toUpperCase(Locale.ROOT);
        String nombre = request.nombre().trim();
        repository.guardar(NegocioContext.getRequired(), normalizado, nombre, request.activo());
        return new MetodoPagoResponse(normalizado, nombre, request.activo());
    }
}
