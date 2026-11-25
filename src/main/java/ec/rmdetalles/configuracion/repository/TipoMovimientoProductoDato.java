package ec.rmdetalles.configuracion.repository;

public record TipoMovimientoProductoDato(
        String codigo, String nombre, short factorStock, boolean permiteNegativo, boolean activo
) { }
