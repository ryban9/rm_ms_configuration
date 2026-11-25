package ec.rmdetalles.configuracion.api;

public record EstadoPedidoResponse(
        String codigo, String nombre, short orden,
        boolean inicial, boolean finalizado, boolean activo
) {}
