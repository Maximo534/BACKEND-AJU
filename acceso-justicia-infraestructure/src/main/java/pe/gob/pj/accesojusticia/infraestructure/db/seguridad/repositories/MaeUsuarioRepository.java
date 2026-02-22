package pe.gob.pj.accesojusticia.infraestructure.db.seguridad.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.seguridad.entities.MaeUsuarioEntity;

public interface MaeUsuarioRepository extends JpaRepository<MaeUsuarioEntity, Integer> {

  Optional<MaeUsuarioEntity> findByNusuarioAndActivo(Integer id, String activo);

}
