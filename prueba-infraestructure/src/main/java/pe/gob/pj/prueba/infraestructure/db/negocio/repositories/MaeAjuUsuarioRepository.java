// Archivo: MaeAjuUsuarioRepository.java (Ubicación: infraestructure.db.negocio.repositories)
package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MaeAjuUsuarioEntity;
import java.util.Optional;

public interface MaeAjuUsuarioRepository extends JpaRepository<MaeAjuUsuarioEntity, String> {

    Optional<MaeAjuUsuarioEntity> findByIdAndActivo(String id, String activo);
}