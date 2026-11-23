package ec.rmdetalles.configuracion.config;

import java.util.UUID;

public final class NegocioContext {
    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

    private NegocioContext() { }

    public static void set(UUID negocioId) { CURRENT.set(negocioId); }

    public static UUID getRequired() {
        UUID negocioId = CURRENT.get();
        if (negocioId == null) {
            throw new IllegalStateException("No hay un negocio asociado a la solicitud.");
        }
        return negocioId;
    }

    public static void clear() { CURRENT.remove(); }
}
