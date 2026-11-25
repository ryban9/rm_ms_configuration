package ec.rmdetalles.configuracion.api;

import jakarta.validation.constraints.*;

public record EstadoPedidoRequest(
        @NotBlank @Size(max = 80) String nombre,
        @Min(0) short orden,
        boolean inicial,
        boolean finalizado,
        boolean activo
) {}
