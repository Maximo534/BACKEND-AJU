package pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeTareaEntity;

import java.util.List;

public interface MaeTareaRepository extends JpaRepository<MaeTareaEntity, String> {
    List<MaeTareaEntity> findByIndicadorIdAndActivo(String indicadorId, String activo);
}