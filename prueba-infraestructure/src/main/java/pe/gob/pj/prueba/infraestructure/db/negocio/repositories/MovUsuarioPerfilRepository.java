package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovUsuarioPerfilEntity;

import java.util.List;

public interface MovUsuarioPerfilRepository extends JpaRepository<MovUsuarioPerfilEntity, Integer> {
    List<MovUsuarioPerfilEntity> findByUsuarioId(Integer idUsuario);
}