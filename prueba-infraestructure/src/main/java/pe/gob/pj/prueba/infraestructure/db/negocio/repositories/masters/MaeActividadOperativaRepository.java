package pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeActividadOperativaEntity;

import java.util.List;

public interface MaeActividadOperativaRepository extends JpaRepository<MaeActividadOperativaEntity, String> {
    List<MaeActividadOperativaEntity> findByActivo(String activo);
}
