package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovProgramacionEjeEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovProgramacionEjeId;

import java.util.List;
import java.util.Optional;

public interface MovProgramacionEjeRepository extends JpaRepository<MovProgramacionEjeEntity, MovProgramacionEjeId> {
    List<MovProgramacionEjeEntity> findByIdUsuarioAndPeriodo(Integer idUsuario, String periodo);
    Optional<MovProgramacionEjeEntity> findFirstByIdUsuarioAndActivo(Integer idUsuario, String activo);
}