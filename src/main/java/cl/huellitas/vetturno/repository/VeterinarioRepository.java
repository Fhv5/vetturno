package cl.huellitas.vetturno.repository;

import cl.huellitas.vetturno.model.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {
    List<Veterinario> findAllByOrderByIdAsc();
}
