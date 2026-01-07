package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.DocumentoEntity;
import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<DocumentoEntity, String> {
    List<DocumentoEntity> findByTipoAndActivoOrderByPeriodoDesc(String tipo, String activo);
}