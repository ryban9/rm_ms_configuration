package ec.rmdetalles.configuracion.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FrontendNegocioRequest(
        @NotBlank @Size(max = 500) String origen,
        @Size(max = 253) String hostPublico,
        Boolean activo
) { }
