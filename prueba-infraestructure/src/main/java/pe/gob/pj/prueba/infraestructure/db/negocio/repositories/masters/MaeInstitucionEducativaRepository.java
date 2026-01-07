package pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeInstitucionEducativaEntity;
import java.util.List;

@Repository
public interface MaeInstitucionEducativaRepository extends JpaRepository<MaeInstitucionEducativaEntity, String> {
    List<MaeInstitucionEducativaEntity> findByUgelId(String ugelId);
}