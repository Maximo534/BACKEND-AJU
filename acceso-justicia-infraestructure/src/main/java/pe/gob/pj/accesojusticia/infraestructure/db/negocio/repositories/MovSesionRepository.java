package pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MovSesionEntity;

public interface MovSesionRepository extends JpaRepository<MovSesionEntity, Long> {
}