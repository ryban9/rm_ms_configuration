package ec.rmdetalles.configuracion.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import ec.rmdetalles.configuracion.api.ConfiguracionResponse;
import ec.rmdetalles.configuracion.config.NegocioContext;
import ec.rmdetalles.configuracion.repository.ConfiguracionNegocioRepository;
import ec.rmdetalles.configuracion.repository.ConfiguracionNegocioRepository.ConfiguracionDato;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class ConfiguracionService {
    private static final Set<String> MIME_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private final ConfiguracionNegocioRepository configuraciones;
    private final JsonMapper objectMapper;

    public ConfiguracionService(
            ConfiguracionNegocioRepository configuraciones,
            JsonMapper objectMapper
    ) {
        this.configuraciones = configuraciones;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<ConfiguracionResponse> listar() {
        return configuraciones.listar(NegocioContext.getRequired())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ConfiguracionResponse obtener(String clave) {
        return configuraciones.buscar(NegocioContext.getRequired(), normalizarClave(clave))
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe esa configuración para el negocio actual."));
    }

    public ConfiguracionResponse actualizar(String clave, JsonNode valor) {
        String claveNormalizada = normalizarClave(clave);
        var existente = configuraciones.buscar(NegocioContext.getRequired(), claveNormalizada)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe esa configuración para el negocio actual."));

        validarValor(claveNormalizada, valor);
        String valorJson;
        try {
            valorJson = objectMapper.writeValueAsString(valor);
        } catch (JacksonException ex) {
            throw new IllegalArgumentException("El valor no se pudo convertir a JSON.");
        }

        UUID negocioId = NegocioContext.getRequired();
        if (!configuraciones.actualizar(negocioId, claveNormalizada, valorJson)) {
            throw new ResourceNotFoundException("No existe esa configuración para el negocio actual.");
        }
        return configuraciones.buscar(negocioId, claveNormalizada)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró la configuración después de actualizarla."));
    }

    private void validarValor(String clave, JsonNode valor) {
        if (valor == null || valor.isNull()) {
            throw new IllegalArgumentException("El valor de configuración no puede ser nulo.");
        }

        switch (clave) {
            case "general.moneda" -> {
                if (!valor.isTextual() || !valor.asText().matches("[A-Z]{3}")) {
                    throw new IllegalArgumentException("La moneda debe ser un código de tres letras en mayúsculas, por ejemplo USD.");
                }
            }
            case "general.zona_horaria" -> {
                if (!valor.isTextual()) {
                    throw new IllegalArgumentException("La zona horaria debe ser texto, por ejemplo America/Guayaquil.");
                }
                try {
                    ZoneId.of(valor.asText());
                } catch (DateTimeException ex) {
                    throw new IllegalArgumentException("La zona horaria no es válida.");
                }
            }
            case "catalogo.imagenes.max_por_producto" -> validarEntero(valor, 1, 20,
                    "El máximo de imágenes debe estar entre 1 y 20.");
            case "catalogo.imagenes.tamano_maximo_mb" -> validarEntero(valor, 1, 10,
                    "El tamaño máximo debe estar entre 1 y 10 MB.");
            case "catalogo.imagenes.mime_permitidos" -> validarMimes(valor);
            case "ventas.descuento_maximo_porcentaje" -> {
                if (!valor.isNumber()) {
                    throw new IllegalArgumentException("El descuento máximo debe ser numérico.");
                }
                BigDecimal porcentaje = valor.decimalValue();
                if (porcentaje.compareTo(BigDecimal.ZERO) < 0
                        || porcentaje.compareTo(BigDecimal.valueOf(100)) > 0) {
                    throw new IllegalArgumentException("El descuento máximo debe estar entre 0 y 100.");
                }
            }
            default -> throw new IllegalArgumentException(
                    "Esta clave todavía no tiene una regla de edición habilitada.");
        }
    }

    private void validarEntero(JsonNode valor, int minimo, int maximo, String mensaje) {
        if (!valor.isIntegralNumber() || !valor.canConvertToInt()
                || valor.intValue() < minimo || valor.intValue() > maximo) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private void validarMimes(JsonNode valor) {
        if (!valor.isArray() || valor.size() == 0) {
            throw new IllegalArgumentException("Debe permitirse al menos un tipo MIME.");
        }
        Set<String> vistos = new HashSet<>();
        for (JsonNode item : valor) {
            if (!item.isTextual() || !MIME_PERMITIDOS.contains(item.asText())) {
                throw new IllegalArgumentException("Solo se permiten image/jpeg, image/png y image/webp.");
            }
            if (!vistos.add(item.asText())) {
                throw new IllegalArgumentException("La lista de tipos MIME no debe contener duplicados.");
            }
        }
    }

    private String normalizarClave(String clave) {
        if (clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("La clave de configuración es obligatoria.");
        }
        return clave.trim().toLowerCase(Locale.ROOT);
    }

    private ConfiguracionResponse toResponse(ConfiguracionDato dato) {
        try {
            return new ConfiguracionResponse(
                    dato.clave(),
                    objectMapper.readTree(dato.valorJson()),
                    dato.descripcion(),
                    dato.actualizadoEn()
            );
        } catch (JacksonException ex) {
            throw new IllegalStateException("La configuración almacenada no contiene JSON válido.", ex);
        }
    }
}
