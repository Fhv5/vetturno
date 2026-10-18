package cl.huellitas.vetturno.service;

import cl.huellitas.vetturno.dto.*;
import cl.huellitas.vetturno.model.Veterinario;
import cl.huellitas.vetturno.repository.VeterinarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class VeterinarioService {
    private final VeterinarioRepository repository;

    public VeterinarioService(VeterinarioRepository repository) { this.repository = repository; }

    @Transactional
    public VeterinarioDTO crear(VeterinarioRequest r) {
        return DtoMapper.toDto(repository.save(new Veterinario(r.nombre().trim(), r.especialidad().trim())));
    }

    public List<VeterinarioDTO> listar() {
        return repository.findAllByOrderByIdAsc().stream().map(DtoMapper::toDto).toList();
    }
}
