package pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeCategoriaDocumentoEntity;
import java.util.List;

@Repository
public interface MaeCategoriaDocumentoRepository extends JpaRepository<MaeCategoriaDocumentoEntity, Integer> {
    List<MaeCategoriaDocumentoEntity> findByActivo(String activo);
}