package pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeInstanciaEntity;

import java.util.List;

@Repository
public interface MaeInstanciaRepository extends JpaRepository<MaeInstanciaEntity, Long> {

    List<MaeInstanciaEntity> findBySedeIdAndActivo(Long sedeId, String activo);
}