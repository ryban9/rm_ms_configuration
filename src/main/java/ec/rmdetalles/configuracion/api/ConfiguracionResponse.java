package ec.rmdetalles.configuracion.api;

import tools.jackson.databind.JsonNode;

import java.time.OffsetDateTime;

public record ConfiguracionResponse(
        String clave,
        JsonNode valor,
        String descripcion,
        OffsetDateTime actualizadoEn
) { }
