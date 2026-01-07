package pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeTipoParticipanteEntity;
import java.util.List;

public interface MaeTipoParticipanteRepository extends JpaRepository<MaeTipoParticipanteEntity, Integer> {
    List<MaeTipoParticipanteEntity> findByActivo(String activo);
}