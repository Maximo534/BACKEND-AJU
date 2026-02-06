package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MaePerfilEntity;

import java.util.List;
import java.util.Optional;

/**
 * 
 * Esta interfaz extiende {@link JpaRepository} y proporciona operaciones CRUD
 * básicas para la entidad mencionada.
 * 
 * @author oruizb
 * @version 1.0,07/02/2022
 */
public interface MaePerfilRepository extends JpaRepository<MaePerfilEntity, Integer> {

    List<MaePerfilEntity> findByActivo(String activo);
    Optional<MaePerfilEntity> findByRolAndActivo(String nombre, String activo);
    /**
     * Lista los perfiles que un rol específico (idRolPadre) tiene permiso de ver/crear.
     * Hace un cruce con la entidad MaeRolJerarquiaEntity sin necesidad de relacionarlas en Java.
     */
    @Query("SELECT p FROM MaePerfilEntity p " +
            "WHERE p.id IN (" +
            "    SELECT j.idRolHijo FROM MaeRolJerarquiaEntity j " +
            "    WHERE j.idRolPadre = :idRolLogueado " +
            "    AND j.activo = '1'" +
            ") " +
            "AND p.activo = '1'")
    List<MaePerfilEntity> listarPerfilesPermitidos(@Param("idRolLogueado") Integer idRolLogueado);
}
