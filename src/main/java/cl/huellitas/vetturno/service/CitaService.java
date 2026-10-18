package cl.huellitas.vetturno.service;

import cl.huellitas.vetturno.dto.*;
import cl.huellitas.vetturno.model.Cita;
import cl.huellitas.vetturno.repository.*;
import cl.huellitas.vetturno.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CitaService {
    private final CitaRepository citas;
    private final MascotaRepository mascotas;
    private final VeterinarioRepository veterinarios;
    private final Clock clock;

    public CitaService(CitaRepository citas, MascotaRepository mascotas, VeterinarioRepository veterinarios, Clock clock) {
        this.citas = citas;
        this.mascotas = mascotas;
        this.veterinarios = veterinarios;
        this.clock = clock;
    }

    @Transactional
    public CitaDTO crear(CitaRequest r) {
        var fecha = r.fechaHora().truncatedTo(ChronoUnit.MICROS);
        if (!fecha.isAfter(LocalDateTime.now(clock))) {
            throw new BusinessException("La fecha debe ser futura");
        }
        var mascota = mascotas.findById(r.mascotaId())
            .orElseThrow(() -> new BusinessException("La mascota no existe"));
        var veterinario = veterinarios.findById(r.veterinarioId())
            .orElseThrow(() -> new BusinessException("El veterinario no existe"));
        if (citas.existsByVeterinarioIdAndFechaHora(veterinario.getId(), fecha)) {
            throw new BusinessException("El veterinario ya tiene una cita en ese horario");
        }
        return DtoMapper.toDto(citas.saveAndFlush(new Cita(fecha, r.motivo().trim(), mascota, veterinario)));
    }

    public List<CitaDTO> listar() {
        return citas.findAllByOrderByFechaHoraAscIdAsc().stream().map(DtoMapper::toDto).toList();
    }

    public List<CitaDTO> porVeterinario(Long id) {
        if (id <= 0 || !veterinarios.existsById(id)) {
            throw new BusinessException("El veterinario no existe");
        }
        return citas.findByVeterinarioIdOrderByFechaHoraAscIdAsc(id).stream().map(DtoMapper::toDto).toList();
    }
}
