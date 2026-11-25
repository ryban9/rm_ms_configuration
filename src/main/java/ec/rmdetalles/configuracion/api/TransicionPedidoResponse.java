package ec.rmdetalles.configuracion.api;

public record TransicionPedidoResponse(String estadoOrigen, String estadoDestino, boolean activo) {}
