package pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeTipoUsuarioEntity;

@Repository
public interface MaeTipoUsuarioRepository extends JpaRepository<MaeTipoUsuarioEntity, String> {
    // No necesitamos métodos extra por ahora, con los de JpaRepository basta.
    // Ejemplo: findById("01") te devolverá el rol ADMIN si existe.
}