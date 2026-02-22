package pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.masters;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters.MaeDistritoJudicialEntity;

public interface MaeDistritoJudicialRepository extends JpaRepository<MaeDistritoJudicialEntity, Long> {
}