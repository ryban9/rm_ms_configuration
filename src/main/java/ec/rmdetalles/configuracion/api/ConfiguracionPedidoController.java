package ec.rmdetalles.configuracion.api;

import ec.rmdetalles.configuracion.service.ConfiguracionPedidoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/configuracion/pedidos")
public class ConfiguracionPedidoController {
    private final ConfiguracionPedidoService service;

    public ConfiguracionPedidoController(ConfiguracionPedidoService service) {
        this.service = service;
    }

    @GetMapping("/estados")
    public List<EstadoPedidoResponse> estados() {
        return service.listarEstados();
    }

    @PutMapping("/estados/{codigo}")
    public EstadoPedidoResponse guardarEstado(
            @PathVariable String codigo, @Valid @RequestBody EstadoPedidoRequest request
    ) {
        return service.guardarEstado(codigo, request);
    }

    @GetMapping("/transiciones")
    public List<TransicionPedidoResponse> transiciones() {
        return service.listarTransiciones();
    }

    @PutMapping("/transiciones/{origen}/{destino}")
    public TransicionPedidoResponse guardarTransicion(
            @PathVariable String origen, @PathVariable String destino,
            @RequestBody TransicionPedidoRequest request
    ) {
        return service.guardarTransicion(origen, destino, request.activo());
    }
}
