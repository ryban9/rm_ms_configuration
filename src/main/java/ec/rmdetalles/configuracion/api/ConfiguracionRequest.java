package ec.rmdetalles.configuracion.api;

import tools.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public record ConfiguracionRequest(@NotNull JsonNode valor) { }
