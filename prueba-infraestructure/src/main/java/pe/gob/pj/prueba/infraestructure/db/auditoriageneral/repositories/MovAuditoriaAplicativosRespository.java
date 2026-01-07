package pe.gob.pj.prueba.infraestructure.db.auditoriageneral.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.prueba.infraestructure.db.auditoriageneral.entities.MovAuditoriaAplicativosEntity;

public interface MovAuditoriaAplicativosRespository
    extends JpaRepository<MovAuditoriaAplicativosEntity, Integer> {

}
