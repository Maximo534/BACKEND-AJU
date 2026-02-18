package pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.masters;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters.MaeInstitucionEducativaEntity;
import java.util.List;

public interface MaeInstitucionEducativaRepository extends JpaRepository<MaeInstitucionEducativaEntity, Long> {
    List<MaeInstitucionEducativaEntity> findByUgelId(Long idUgel);
}