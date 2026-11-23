package ec.rmdetalles.configuracion.api;

import ec.rmdetalles.configuracion.domain.FrontendNegocio;

import java.util.UUID;

public record FrontendNegocioResponse(
        UUID id,
        String origen,
        String hostPublico,
        boolean activo
) {
    public static FrontendNegocioResponse from(FrontendNegocio frontend) {
        return new FrontendNegocioResponse(frontend.getId(), frontend.getOrigen(),
                frontend.getHostPublico(), frontend.isActivo());
    }
}
