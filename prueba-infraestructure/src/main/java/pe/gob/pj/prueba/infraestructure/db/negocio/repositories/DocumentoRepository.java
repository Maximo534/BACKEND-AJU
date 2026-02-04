package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.DocumentoEntity;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<DocumentoEntity, Long> {

    @Query("SELECT e FROM DocumentoEntity e " +
            "LEFT JOIN FETCH e.categoria c " +
            "WHERE e.activo = '1' " +
            "AND (:tipo IS NULL OR e.tipo = :tipo) " +
            "ORDER BY e.periodo DESC")
    List<DocumentoEntity> listarActivosPorTipoConCategoria(@Param("tipo") String tipo);
}