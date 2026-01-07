package pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeTipoVulnerabilidadEntity;
import java.util.List;

public interface MaeTipoVulnerabilidadRepository extends JpaRepository<MaeTipoVulnerabilidadEntity, Integer> {
    List<MaeTipoVulnerabilidadEntity> findByActivo(String activo);
}