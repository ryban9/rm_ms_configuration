package ec.rmdetalles.configuracion.api;

import ec.rmdetalles.configuracion.service.FrontendNegocioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/configuracion/frontends")
public class FrontendNegocioController {
    private final FrontendNegocioService service;

    public FrontendNegocioController(FrontendNegocioService service) {
        this.service = service;
    }

    @GetMapping
    public List<FrontendNegocioResponse> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FrontendNegocioResponse crear(@Valid @RequestBody FrontendNegocioRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public FrontendNegocioResponse actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody FrontendNegocioRequest request
    ) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable UUID id) {
        service.desactivar(id);
    }
}
