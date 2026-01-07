package pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeMateriaEntity;
import java.util.List;

public interface MaeMateriaRepository extends JpaRepository<MaeMateriaEntity, Integer> {
    List<MaeMateriaEntity> findByActivo(String activo);
}