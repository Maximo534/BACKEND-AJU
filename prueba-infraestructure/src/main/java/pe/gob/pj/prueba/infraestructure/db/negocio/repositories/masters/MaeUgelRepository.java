package pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeUgelEntity;
import java.util.List;

@Repository
public interface MaeUgelRepository extends JpaRepository<MaeUgelEntity, String> {
    List<MaeUgelEntity> findByDistritoJudicialId(String distritoJudicialId);
}