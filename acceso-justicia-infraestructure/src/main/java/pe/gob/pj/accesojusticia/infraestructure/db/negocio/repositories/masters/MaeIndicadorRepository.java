package pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.masters;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters.MaeIndicadorEntity;
import java.util.List;

public interface MaeIndicadorRepository extends JpaRepository<MaeIndicadorEntity, Long> {
    List<MaeIndicadorEntity> findByActividadIdAndActivo(Long idActividad, String activo);
}