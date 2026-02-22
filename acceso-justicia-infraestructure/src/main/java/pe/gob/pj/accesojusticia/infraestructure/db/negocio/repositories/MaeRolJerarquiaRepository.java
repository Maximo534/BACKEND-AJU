package pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters.MaeRolJerarquiaEntity;

@Repository
public interface MaeRolJerarquiaRepository extends JpaRepository<MaeRolJerarquiaEntity, Long> {

    @Query("SELECT COUNT(j) > 0 FROM MaeRolJerarquiaEntity j " +
            "WHERE j.idRolPadre = :idPadre " +
            "AND j.idRolHijo = :idHijo " +
            "AND j.activo = '1'")
    boolean existeJerarquia(@Param("idPadre") Integer idPadre, @Param("idHijo") Integer idHijo);
}