package ec.rmdetalles.configuracion.api;

import ec.rmdetalles.configuracion.service.MetodoPagoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/configuracion/ventas/metodos-pago")
public class MetodoPagoController {
    private final MetodoPagoService service;

    public MetodoPagoController(MetodoPagoService service) {
        this.service = service;
    }

    @GetMapping
    public List<MetodoPagoResponse> listar() {
        return service.listar();
    }

    @PutMapping("/{codigo}")
    public MetodoPagoResponse guardar(
            @PathVariable String codigo, @Valid @RequestBody MetodoPagoRequest request
    ) {
        return service.guardar(codigo, request);
    }
}
