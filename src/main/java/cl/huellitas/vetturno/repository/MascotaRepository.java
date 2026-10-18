package cl.huellitas.vetturno.repository;

import cl.huellitas.vetturno.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    @EntityGraph(attributePaths = "propietario")
    List<Mascota> findAllByOrderByIdAsc();
}
