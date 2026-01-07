package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MaeJuezPazEscolarEntity;

import java.util.List;

@Repository
public interface MaeJuezPazEscolarRepository extends JpaRepository<MaeJuezPazEscolarEntity, String> {

    List<MaeJuezPazEscolarEntity> findByInstitucionEducativaIdAndActivo(String institucionEducativaId, String activo);
    boolean existsByDniAndInstitucionEducativaIdAndActivo(String dni, String institucionEducativaId, String activo);
}