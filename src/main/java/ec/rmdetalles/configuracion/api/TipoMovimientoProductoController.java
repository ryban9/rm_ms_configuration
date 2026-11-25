package ec.rmdetalles.configuracion.api;

import ec.rmdetalles.configuracion.service.TipoMovimientoProductoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/configuracion/inventario/productos/tipos-movimiento")
public class TipoMovimientoProductoController {
    private final TipoMovimientoProductoService service;
    public TipoMovimientoProductoController(TipoMovimientoProductoService service) { this.service = service; }

    @GetMapping
    public List<TipoMovimientoProductoResponse> listar() { return service.listar(); }

    @PutMapping("/{codigo}")
    public TipoMovimientoProductoResponse guardar(
            @PathVariable String codigo,
            @Valid @RequestBody TipoMovimientoProductoRequest request
    ) {
        return service.guardar(codigo, request);
    }
}
