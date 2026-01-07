package pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeIndicadorEntity;

import java.util.List;

public interface MaeIndicadorRepository extends JpaRepository<MaeIndicadorEntity, String> {
    List<MaeIndicadorEntity> findByActividadIdAndActivo(String actividadId, String activo);
}
