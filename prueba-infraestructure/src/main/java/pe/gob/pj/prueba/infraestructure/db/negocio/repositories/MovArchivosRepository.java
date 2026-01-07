package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivosEntity;
import java.util.List;

public interface MovArchivosRepository extends JpaRepository<MovArchivosEntity, String> {

    List<MovArchivosEntity> findByNumeroIdentificacion(String numeroIdentificacion);
}