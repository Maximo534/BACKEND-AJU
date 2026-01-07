package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MaePerfilEntity;

/**
 * 
 * Esta interfaz extiende {@link JpaRepository} y proporciona operaciones CRUD
 * básicas para la entidad mencionada.
 * 
 * @author oruizb
 * @version 1.0,07/02/2022
 */
public interface MaePerfilRepository extends JpaRepository<MaePerfilEntity, Integer> {

}
