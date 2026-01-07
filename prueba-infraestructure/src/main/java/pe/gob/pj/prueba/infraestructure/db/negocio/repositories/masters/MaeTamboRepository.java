package pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeTamboEntity;
import java.util.List;

public interface MaeTamboRepository extends JpaRepository<MaeTamboEntity, String> {
    List<MaeTamboEntity> findByDistritoJudicialIdAndActivo(String distritoJudicialId, String activo);
}