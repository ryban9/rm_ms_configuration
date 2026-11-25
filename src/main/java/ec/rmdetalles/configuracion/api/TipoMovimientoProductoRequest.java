package ec.rmdetalles.configuracion.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TipoMovimientoProductoRequest(
        @NotBlank @Size(max = 80) String nombre,
        @NotNull @Min(-1) @Max(1) Short factorStock,
        @NotNull Boolean activo
) { }
