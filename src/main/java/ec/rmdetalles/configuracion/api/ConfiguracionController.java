package ec.rmdetalles.configuracion.api;

import ec.rmdetalles.configuracion.service.ConfiguracionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/configuracion/parametros")
public class ConfiguracionController {
    private final ConfiguracionService service;

    public ConfiguracionController(ConfiguracionService service) {
        this.service = service;
    }

    @GetMapping
    public List<ConfiguracionResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{clave:.+}")
    public ConfiguracionResponse obtener(@PathVariable String clave) {
        return service.obtener(clave);
    }

    @PutMapping("/{clave:.+}")
    public ConfiguracionResponse actualizar(
            @PathVariable String clave,
            @Valid @RequestBody ConfiguracionRequest request
    ) {
        return service.actualizar(clave, request.valor());
    }
}
