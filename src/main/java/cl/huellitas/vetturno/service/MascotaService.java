package cl.huellitas.vetturno.service;

import cl.huellitas.vetturno.dto.*;
import cl.huellitas.vetturno.model.Mascota;
import cl.huellitas.vetturno.repository.*;
import cl.huellitas.vetturno.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MascotaService {
    private final MascotaRepository mascotas;
    private final PropietarioRepository propietarios;

    public MascotaService(MascotaRepository mascotas, PropietarioRepository propietarios) {
        this.mascotas = mascotas;
        this.propietarios = propietarios;
    }

    @Transactional
    public MascotaDTO crear(MascotaRequest r) {
        var propietario = propietarios.findById(r.propietarioId())
            .orElseThrow(() -> new BusinessException("El propietario no existe"));
        return DtoMapper.toDto(mascotas.save(new Mascota(r.nombre().trim(), r.especie().trim(), r.raza(), propietario)));
    }

    public List<MascotaDTO> listar() {
        return mascotas.findAllByOrderByIdAsc().stream().map(DtoMapper::toDto).toList();
    }
}
