package cl.huellitas.vetturno.controller;

import cl.huellitas.vetturno.dto.*;
import cl.huellitas.vetturno.service.VeterinarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/veterinarios")
public class VeterinarioController {
    private final VeterinarioService service;

    public VeterinarioController(VeterinarioService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VeterinarioDTO crear(@Valid @RequestBody VeterinarioRequest request) {
        return service.crear(request);
    }

    @GetMapping
    public List<VeterinarioDTO> listar() { return service.listar(); }
}
