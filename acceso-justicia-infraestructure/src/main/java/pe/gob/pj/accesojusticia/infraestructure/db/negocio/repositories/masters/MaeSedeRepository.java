package pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.masters;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters.MaeSedeEntity;

import java.util.List;

@Repository
public interface MaeSedeRepository extends JpaRepository<MaeSedeEntity, Long> {
    List<MaeSedeEntity> findByDistritoJudicialIdAndActivo(Long distritoJudicialId, String activo);
}