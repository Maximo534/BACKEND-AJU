package pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaePlanAnualEntity;
import java.util.List;

public interface MaePlanAnualRepository extends JpaRepository<MaePlanAnualEntity, Long> {
    List<MaePlanAnualEntity> findByDistritoJudicialIdAndPeriodo(Long idCorte, String periodo);
}