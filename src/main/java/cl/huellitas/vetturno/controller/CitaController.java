package cl.huellitas.vetturno.controller;

import cl.huellitas.vetturno.dto.*;
import cl.huellitas.vetturno.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {
    private final CitaService service;

    public CitaController(CitaService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitaDTO crear(@Valid @RequestBody CitaRequest request) {
        return service.crear(request);
    }

    @GetMapping
    public List<CitaDTO> listar() { return service.listar(); }

    @GetMapping("/veterinario/{id}")
    public List<CitaDTO> porVeterinario(@PathVariable Long id) {
        return service.porVeterinario(id);
    }
}
