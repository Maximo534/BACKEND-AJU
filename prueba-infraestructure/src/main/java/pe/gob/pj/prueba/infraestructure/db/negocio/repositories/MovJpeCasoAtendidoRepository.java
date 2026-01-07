package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovJpeCasoAtendidoEntity;

import java.util.List;

@Repository
public interface MovJpeCasoAtendidoRepository extends JpaRepository<MovJpeCasoAtendidoEntity, String> {
    @Query("SELECT MAX(e.id) FROM MovJpeCasoAtendidoEntity e WHERE e.id LIKE '%-PE'")
    String obtenerUltimoId();
    @Query(value = """
        SELECT 
            pe.c_jpeca_id AS id,
            di.x_nom_corto AS corte,          
            ug.x_nombre AS ugel,          
            ie.x_nombre AS colegio,       
            pe.x_res_hecho AS incidente,
            pe.f_registro AS fecha
        FROM mov_aju_jpe_caso_atendidos pe
        INNER JOIN mae_aju_juez_paz_escolares ipe ON pe.c_cod_reg = ipe.c_cod_reg
        INNER JOIN mae_aju_institucion_educativas ie ON ipe.c_institucion_id = ie.c_institucion_id  
        INNER JOIN mae_aju_ugeles ug ON ie.c_ugel_id = ug.c_ugel_id                     
        INNER JOIN mae_aju_distrito_judiciales di ON ug.c_distrito_jud_id = di.c_distrito_jud_id       
        WHERE (:id IS NULL OR pe.c_jpeca_id = :id)
          AND (:anio IS NULL OR EXTRACT(YEAR FROM pe.f_registro) = :anio)
    """, countQuery = """
        SELECT count(*) 
        FROM mov_aju_jpe_caso_atendidos pe
        INNER JOIN mae_aju_juez_paz_escolares ipe ON pe.c_cod_reg = ipe.c_cod_reg
        INNER JOIN mae_aju_institucion_educativas ie ON ipe.c_institucion_id = ie.c_institucion_id
        INNER JOIN mae_aju_ugeles ug ON ie.c_ugel_id = ug.c_ugel_id
        INNER JOIN mae_aju_distrito_judiciales di ON ug.c_distrito_jud_id = di.c_distrito_jud_id
        WHERE (:id IS NULL OR pe.c_jpeca_id = :id)
          AND (:anio IS NULL OR EXTRACT(YEAR FROM pe.f_registro) = :anio)
    """, nativeQuery = true)
    Page<Object[]> listarDinamico(@Param("id") String id, @Param("anio") Integer anio, Pageable pageable);
    @Query("SELECT e.distritoJudicialId, COUNT(e) " +
            "FROM MovJpeCasoAtendidoEntity e " +
            "GROUP BY e.distritoJudicialId")
    List<Object[]> obtenerEstadisticasPorCorte();
}