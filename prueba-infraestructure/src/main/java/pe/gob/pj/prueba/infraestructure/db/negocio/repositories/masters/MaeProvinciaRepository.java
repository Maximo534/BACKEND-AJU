package pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeProvinciaEntity;
import java.util.List;

public interface MaeProvinciaRepository extends JpaRepository<MaeProvinciaEntity, String> {
    List<MaeProvinciaEntity> findByDepartamentoId(String departamentoId);
}