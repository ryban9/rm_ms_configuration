package ec.rmdetalles.configuracion.api;

import ec.rmdetalles.configuracion.repository.TipoMovimientoProductoDato;

public record TipoMovimientoProductoResponse(
        String codigo,
        String nombre,
        short factorStock,
        boolean permiteNegativo,
        boolean activo
) {
    public static TipoMovimientoProductoResponse from(TipoMovimientoProductoDato dato) {
        return new TipoMovimientoProductoResponse(dato.codigo(), dato.nombre(), dato.factorStock(),
                dato.permiteNegativo(), dato.activo());
    }
}
