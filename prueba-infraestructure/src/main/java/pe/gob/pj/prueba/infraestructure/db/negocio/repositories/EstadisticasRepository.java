package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovJusticiaItineranteEntity;

import java.util.List;

@Repository
public interface EstadisticasRepository extends JpaRepository<MovJusticiaItineranteEntity, String> {

    // --- QUERY 1: RANKING MAGISTRADOS (Ya corregida y probada) ---
    @Query(value = """
        WITH Totales AS (
            SELECT c_usuario_reg AS usuario, COUNT(*) AS cantidad FROM prueba.mov_aju_justicia_itinerantes WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio GROUP BY c_usuario_reg
            UNION ALL
            SELECT c_usuario_reg, COUNT(*) FROM prueba.mov_aju_actv_prom_culturas WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio GROUP BY c_usuario_reg
            UNION ALL
            SELECT c_usuario_reg, COUNT(*) FROM prueba.mov_aju_llapanchikpaq_justicia WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio GROUP BY c_usuario_reg
            UNION ALL
            SELECT c_usuario_reg, COUNT(*) FROM prueba.mov_aju_eventos WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio GROUP BY c_usuario_reg
            UNION ALL
            SELECT c_usuario_reg, COUNT(*) FROM prueba.mov_aju_meta_anuales WHERE EXTRACT(YEAR FROM f_registro) = :anio GROUP BY c_usuario_reg
            UNION ALL
            SELECT c_usuario_reg, COUNT(*) FROM prueba.mov_aju_jpe_caso_atendidos WHERE EXTRACT(YEAR FROM f_registro) = :anio GROUP BY c_usuario_reg
        )
        SELECT u.x_nombre_completo, SUM(t.cantidad) AS total
        FROM Totales t
        INNER JOIN prueba.mae_aju_usuarios u ON t.usuario = u.c_usuario_id
        GROUP BY u.x_nombre_completo
        ORDER BY total DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Object[]> obtenerRankingTop10(@Param("anio") int anio);


    // --- QUERY 2: RANKING EJES
    @Query(value = """
        WITH EjesUnificados AS (
            -- 1. Justicia Itinerante
            SELECT c_eje_id 
            FROM prueba.mov_aju_justicia_itinerantes 
            WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            
            UNION ALL
            
            -- 2. Promoción Cultura
            SELECT c_eje_id 
            FROM prueba.mov_aju_actv_prom_culturas 
            WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            
            UNION ALL
            
            -- 3. Eventos
            SELECT c_eje_id 
            FROM prueba.mov_aju_eventos 
            WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
        )
        SELECT 
            m.x_descripcion, 
            COUNT(*) AS cantidad
        FROM EjesUnificados u
        INNER JOIN prueba.mae_aju_ejes m ON u.c_eje_id = m.c_eje_id
        GROUP BY m.x_descripcion
        ORDER BY cantidad DESC
    """, nativeQuery = true)
    List<Object[]> obtenerRankingPorEje(@Param("anio") int anio);

    // --- QUERY 3: RESUMEN ACTIVIDADES POR USUARIO (MULTISERIE) ---
    @Query(value = """
        WITH Detalle AS (
            SELECT c_usuario_reg AS usuario, 'Justicia Itinerante' AS tipo FROM prueba.mov_aju_justicia_itinerantes WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            UNION ALL
            SELECT c_usuario_reg, 'Cultura Jurídica' FROM prueba.mov_aju_actv_prom_culturas WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            UNION ALL
            SELECT c_usuario_reg, 'Fortalecimiento' FROM prueba.mov_aju_eventos WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
        )
        SELECT 
            u.x_nombre_completo,
            d.tipo,
            COUNT(*) as cantidad
        FROM Detalle d
        INNER JOIN prueba.mae_aju_usuarios u ON d.usuario = u.c_usuario_id
        GROUP BY u.x_nombre_completo, d.tipo
        ORDER BY u.x_nombre_completo
    """, nativeQuery = true)
    List<Object[]> obtenerResumenActividadMagistrado(@Param("anio") int anio);

    @Query(value = """
        WITH DistritosUnificados AS (
            -- 1. Justicia Itinerante
            SELECT c_distrito_jud_id AS id FROM prueba.mov_aju_justicia_itinerantes 
            WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            
            UNION ALL
            
            -- 2. Promoción Cultura
            SELECT c_distrito_jud_id FROM prueba.mov_aju_actv_prom_culturas 
            WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            
            UNION ALL
            
            -- 3. Llapanchikpaq
            SELECT c_distrito_jud_id FROM prueba.mov_aju_llapanchikpaq_justicia 
            WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            
            UNION ALL
            
            -- 4. Eventos
            SELECT c_distrito_jud_id FROM prueba.mov_aju_eventos 
            WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            
            UNION ALL
            
            -- 5. Orientación Jurídica (Usa f_registro)
            SELECT c_distrito_jud_id FROM prueba.mov_aju_meta_anuales 
            WHERE EXTRACT(YEAR FROM f_registro) = :anio
            
            UNION ALL
            
            -- 6. Juez Paz Escolar (Usa f_registro)
            SELECT c_distrito_jud_id FROM prueba.mov_aju_jpe_caso_atendidos 
            WHERE EXTRACT(YEAR FROM f_registro) = :anio
        )
        SELECT 
            d.x_nom_corto AS distrito, 
            COUNT(*) AS cantidad
        FROM DistritosUnificados u
        INNER JOIN prueba.mae_aju_distrito_judiciales d ON u.id = d.c_distrito_jud_id
        GROUP BY d.x_nom_corto
        ORDER BY cantidad DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Object[]> obtenerRankingDistritos(@Param("anio") int anio);

    @Query(value = """
        WITH Mensual AS (
            -- 1. Justicia Itinerante
            SELECT EXTRACT(MONTH FROM f_inicio) as mes, 'Justicia Itinerante' as tipo 
            FROM prueba.mov_aju_justicia_itinerantes 
            WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            
            UNION ALL
            
            -- 2. Cultura Jurídica
            SELECT EXTRACT(MONTH FROM f_inicio), 'Cultura Jurídica' 
            FROM prueba.mov_aju_actv_prom_culturas 
            WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            
            UNION ALL
            
            -- 3. Fortalecimiento
            SELECT EXTRACT(MONTH FROM f_inicio), 'Fortalecimiento' 
            FROM prueba.mov_aju_eventos 
            WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
        )
        SELECT 
            mes, 
            tipo, 
            COUNT(*) as cantidad
        FROM Mensual
        GROUP BY mes, tipo
        ORDER BY mes
    """, nativeQuery = true)
    List<Object[]> obtenerEvolucionMensual(@Param("anio") int anio);

}