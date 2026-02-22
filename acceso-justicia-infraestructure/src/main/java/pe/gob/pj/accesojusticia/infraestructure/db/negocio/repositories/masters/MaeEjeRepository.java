package pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.masters;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters.MaeEjeEntity;
import java.util.List;

public interface MaeEjeRepository extends JpaRepository<MaeEjeEntity, Long> {
    List<MaeEjeEntity> findByActivo(String activo);
}