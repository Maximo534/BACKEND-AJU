package pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.masters;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters.MaeTareaEntity;
import java.util.List;

public interface MaeTareaRepository extends JpaRepository<MaeTareaEntity, Long> {
    List<MaeTareaEntity> findByIndicadorIdAndActivo(Long idIndicador, String activo);
}