package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.DocumentoEntity;
import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<DocumentoEntity, String> {
    List<DocumentoEntity> findByTipoAndActivoOrderByPeriodoDesc(String tipo, String activo);
    @Query(value = """
    SELECT * FROM prueba.mov_aju_documentos d 
    WHERE d.l_activo = '1'
      AND (:tipo IS NULL OR d.c_tipo = :tipo)
      AND (:periodo IS NULL OR d.n_periodo = :periodo)
      AND (:catId IS NULL OR d.n_categoria_doc_id = :catId)
      AND (
          :nombre IS NULL OR 
          UPPER(d.x_nombre) LIKE UPPER(CONCAT('%', :nombre, '%'))
      )
    ORDER BY d.n_periodo DESC
""", countQuery = """
    SELECT count(*) 
    FROM prueba.mov_aju_documentos d 
    WHERE d.l_activo = '1'
      AND (:tipo IS NULL OR d.c_tipo = :tipo)
      AND (:periodo IS NULL OR d.n_periodo = :periodo)
      AND (:catId IS NULL OR d.n_categoria_doc_id = :catId)
      AND (:nombre IS NULL OR UPPER(d.x_nombre) LIKE UPPER(CONCAT('%', :nombre, '%')))
""", nativeQuery = true)
    Page<DocumentoEntity> listar(
            @Param("tipo") String tipo,
            @Param("periodo") Integer periodo,
            @Param("catId") Integer categoriaId,
            @Param("nombre") String nombre,
            Pageable pageable
    );
}