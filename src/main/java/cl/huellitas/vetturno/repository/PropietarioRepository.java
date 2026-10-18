package cl.huellitas.vetturno.repository;

import cl.huellitas.vetturno.model.Propietario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PropietarioRepository extends JpaRepository<Propietario, Long> {
    List<Propietario> findAllByOrderByIdAsc();
}
