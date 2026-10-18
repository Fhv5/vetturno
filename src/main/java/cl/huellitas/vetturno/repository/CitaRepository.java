package cl.huellitas.vetturno.repository;

import cl.huellitas.vetturno.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;

public interface CitaRepository extends JpaRepository<Cita, Long> {
    boolean existsByVeterinarioIdAndFechaHora(Long veterinarioId, LocalDateTime fechaHora);

    @EntityGraph(attributePaths = {"mascota.propietario", "veterinario"})
    List<Cita> findAllByOrderByFechaHoraAscIdAsc();

    @EntityGraph(attributePaths = {"mascota.propietario", "veterinario"})
    List<Cita> findByVeterinarioIdOrderByFechaHoraAscIdAsc(Long veterinarioId);
}
