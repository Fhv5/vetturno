package cl.huellitas.vetturno.service;

import cl.huellitas.vetturno.dto.*;
import cl.huellitas.vetturno.model.Propietario;
import cl.huellitas.vetturno.repository.PropietarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PropietarioService {
    private final PropietarioRepository repository;

    public PropietarioService(PropietarioRepository repository) { this.repository = repository; }

    @Transactional
    public PropietarioDTO crear(PropietarioRequest r) {
        return DtoMapper.toDto(repository.save(new Propietario(r.nombre().trim(), r.telefono().trim(), r.email())));
    }

    public List<PropietarioDTO> listar() {
        return repository.findAllByOrderByIdAsc().stream().map(DtoMapper::toDto).toList();
    }
}
