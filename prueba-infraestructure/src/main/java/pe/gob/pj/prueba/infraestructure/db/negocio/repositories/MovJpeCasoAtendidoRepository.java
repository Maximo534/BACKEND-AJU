package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovJpeCasoAtendidoEntity;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovJpeCasoAtendidoRepository extends JpaRepository<MovJpeCasoAtendidoEntity, String> {

    @Query("SELECT MAX(e.id) FROM MovJpeCasoAtendidoEntity e WHERE e.id LIKE '%-PE'")
    String obtenerUltimoId();

    @Query(value = """
        SELECT 
            pe.c_jpeca_id AS id,
            di.x_nom_corto AS distritoJudicialNombre,          
            ug.x_nombre AS ugelNombre,          
            ie.x_nombre AS institucionNombre,       
            pe.x_res_hecho AS resumenHechos,
            pe.f_registro AS fechaRegistro,
            'REGISTRADO' AS estado
        FROM prueba.mov_aju_jpe_caso_atendidos pe
        INNER JOIN prueba.mae_aju_juez_paz_escolares ipe ON pe.c_cod_reg = ipe.c_cod_reg
        INNER JOIN prueba.mae_aju_institucion_educativas ie ON ipe.c_institucion_id = ie.c_institucion_id  
        INNER JOIN prueba.mae_aju_ugeles ug ON ie.c_ugel_id = ug.c_ugel_id                     
        INNER JOIN prueba.mae_aju_distrito_judiciales di ON ug.c_distrito_jud_id = di.c_distrito_jud_id       
        WHERE pe.c_usuario_reg = :usuario
          
          -- FILTROS COMBOS
          AND (:distrito IS NULL OR pe.c_distrito_jud_id = :distrito)
          AND (:ugel IS NULL OR ie.c_ugel_id = :ugel)
          AND (:institucion IS NULL OR ipe.c_institucion_id = :institucion)
          
          -- FILTRO EXACTO FECHA REGISTRO
          AND (CAST(:fecha AS DATE) IS NULL OR pe.f_registro = :fecha)

          -- BUSCADOR GENERAL
          AND (
              :search IS NULL OR :search = '' OR
              UPPER(pe.c_jpeca_id) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(pe.x_res_hecho) LIKE UPPER(CONCAT('%', :search, '%'))
          )
        ORDER BY pe.f_registro DESC
    """, countQuery = """
        SELECT count(*) 
        FROM prueba.mov_aju_jpe_caso_atendidos pe
        INNER JOIN prueba.mae_aju_juez_paz_escolares ipe ON pe.c_cod_reg = ipe.c_cod_reg
        INNER JOIN prueba.mae_aju_institucion_educativas ie ON ipe.c_institucion_id = ie.c_institucion_id
        INNER JOIN prueba.mae_aju_ugeles ug ON ie.c_ugel_id = ug.c_ugel_id
        INNER JOIN prueba.mae_aju_distrito_judiciales di ON ug.c_distrito_jud_id = di.c_distrito_jud_id       
        WHERE pe.c_usuario_reg = :usuario
          AND (:distrito IS NULL OR pe.c_distrito_jud_id = :distrito)
          AND (:ugel IS NULL OR ie.c_ugel_id = :ugel)
          AND (:institucion IS NULL OR ipe.c_institucion_id = :institucion)
          AND (CAST(:fecha AS DATE) IS NULL OR pe.f_registro = :fecha)
          AND (
              :search IS NULL OR :search = '' OR
              UPPER(pe.c_jpeca_id) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(pe.x_res_hecho) LIKE UPPER(CONCAT('%', :search, '%'))
          )
    """, nativeQuery = true)
    Page<JpeCasoProjection> listar(
            @Param("usuario") String usuario,
            @Param("search") String search,
            @Param("distrito") String distritoId,
            @Param("ugel") String ugelId,
            @Param("institucion") String institucionId,
            @Param("fecha") LocalDate fechaRegistro,
            Pageable pageable);

    @Query("SELECT e.distritoJudicialId, COUNT(e) FROM MovJpeCasoAtendidoEntity e GROUP BY e.distritoJudicialId")
    List<Object[]> obtenerEstadisticasPorCorte();

    interface JpeCasoProjection {
        String getId();
        String getDistritoJudicialNombre();
        String getUgelNombre();
        String getInstitucionNombre();
        String getResumenHechos();
        LocalDate getFechaRegistro();
        String getEstado();
    }
}