package cl.huellitas.vetturno.controller;

import cl.huellitas.vetturno.dto.*;
import cl.huellitas.vetturno.service.PropietarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/propietarios")
public class PropietarioController {
    private final PropietarioService service;

    public PropietarioController(PropietarioService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PropietarioDTO crear(@Valid @RequestBody PropietarioRequest request) {
        return service.crear(request);
    }

    @GetMapping
    public List<PropietarioDTO> listar() { return service.listar(); }
}
