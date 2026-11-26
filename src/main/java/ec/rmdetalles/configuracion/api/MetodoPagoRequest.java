package ec.rmdetalles.configuracion.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MetodoPagoRequest(
        @NotBlank @Size(max = 80) String nombre,
        @NotNull Boolean activo
) {}
