package pe.gob.pj.accesojusticia.infraestructure.db.seguridad.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.seguridad.entities.MaeRolEntity;

public interface MaeRolRepository extends JpaRepository<MaeRolEntity, Integer> {

  List<MaeRolEntity> findByActivoAndMaeRolUsuariosMaeUsuarioNusuario(String activo, Integer nUsuario);

}
