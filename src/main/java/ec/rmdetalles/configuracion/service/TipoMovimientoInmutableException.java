package ec.rmdetalles.configuracion.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TipoMovimientoInmutableException extends RuntimeException {
    public TipoMovimientoInmutableException(String message) { super(message); }
}
