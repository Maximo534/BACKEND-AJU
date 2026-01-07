package pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeDistritoJudicialEntity;

@Repository
public interface MaeDistritoJudicialRepository extends JpaRepository<MaeDistritoJudicialEntity, String> {
    //
}