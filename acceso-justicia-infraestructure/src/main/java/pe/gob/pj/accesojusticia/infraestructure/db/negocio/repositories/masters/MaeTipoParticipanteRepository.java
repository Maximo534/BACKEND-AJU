package pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.masters;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters.MaeTipoParticipanteEntity;
import java.util.List;

public interface MaeTipoParticipanteRepository extends JpaRepository<MaeTipoParticipanteEntity, Long> {
    List<MaeTipoParticipanteEntity> findByActivo(String activo);
}