package pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeEjeEntity;
import java.util.List;

public interface MaeEjeRepository extends JpaRepository<MaeEjeEntity, String> {
    List<MaeEjeEntity> findByActivo(String activo);
}