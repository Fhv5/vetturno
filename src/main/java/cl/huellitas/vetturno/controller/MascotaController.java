package cl.huellitas.vetturno.controller;

import cl.huellitas.vetturno.dto.*;
import cl.huellitas.vetturno.service.MascotaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {
    private final MascotaService service;

    public MascotaController(MascotaService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MascotaDTO crear(@Valid @RequestBody MascotaRequest request) {
        return service.crear(request);
    }

    @GetMapping
    public List<MascotaDTO> listar() { return service.listar(); }
}
