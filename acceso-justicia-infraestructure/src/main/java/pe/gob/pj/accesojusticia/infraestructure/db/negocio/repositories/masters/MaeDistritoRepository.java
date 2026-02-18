package pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.masters;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters.MaeDistritoEntity;
import java.util.List;

public interface MaeDistritoRepository extends JpaRepository<MaeDistritoEntity, Long> {
    List<MaeDistritoEntity> findByProvinciaId(Long provinciaId);
}