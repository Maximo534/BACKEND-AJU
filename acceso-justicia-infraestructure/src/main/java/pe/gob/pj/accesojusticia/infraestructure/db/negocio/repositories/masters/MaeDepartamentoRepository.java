package pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.masters;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters.MaeDepartamentoEntity;

public interface MaeDepartamentoRepository extends JpaRepository<MaeDepartamentoEntity, Long> {
}