package pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeDistritoEntity;
import java.util.List;

public interface MaeDistritoRepository extends JpaRepository<MaeDistritoEntity, String> {
    List<MaeDistritoEntity> findByProvinciaId(String provinciaId);
}