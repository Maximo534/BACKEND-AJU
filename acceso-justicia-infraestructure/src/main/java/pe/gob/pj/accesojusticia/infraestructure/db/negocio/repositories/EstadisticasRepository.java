package pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MovJusticiaItineranteEntity;

import java.util.List;

@Repository
public interface EstadisticasRepository extends JpaRepository<MovJusticiaItineranteEntity, String> {

    // --- QUERY 1: RANKING MAGISTRADOS ---
    @Query(value = """
        WITH Totales AS (
            SELECT n_usuario_reg_id AS usuario, COUNT(*) AS cantidad FROM acjust.mov_justicia_itinerante WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio GROUP BY n_usuario_reg_id
            UNION ALL
            SELECT n_usuario_reg_id, COUNT(*) FROM acjust.mov_actividad_promocion_cultura WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio GROUP BY n_usuario_reg_id
            UNION ALL
            SELECT n_usuario_reg_id, COUNT(*) FROM acjust.mov_llapanchikpaq_justicia WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio GROUP BY n_usuario_reg_id
            UNION ALL
            SELECT n_usuario_reg_id, COUNT(*) FROM acjust.mov_evento WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio GROUP BY n_usuario_reg_id
            UNION ALL
            SELECT n_usuario_reg_id, COUNT(*) FROM acjust.mov_meta_anual WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_reg_activ) = :anio GROUP BY n_usuario_reg_id
            UNION ALL
            SELECT n_usuario_reg_id, COUNT(*) FROM acjust.mov_jpe_caso_atendido WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_registro_caso) = :anio GROUP BY n_usuario_reg_id
        )
        SELECT u.x_nombre_completo, SUM(t.cantidad) AS total
        FROM Totales t
        INNER JOIN acjust.mov_usuario u ON t.usuario = u.n_usuario_id
        GROUP BY u.x_nombre_completo
        ORDER BY total DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Object[]> obtenerRankingTop10(@Param("anio") int anio);

    // --- QUERY 2: RANKING EJES ---
    // Se usa 'n_eje_id' y se cruza con 'acjust.mae_eje'
    @Query(value = """
        WITH EjesUnificados AS (
            SELECT n_eje_id FROM acjust.mov_justicia_itinerante WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            UNION ALL
            SELECT n_eje_id FROM acjust.mov_actividad_promocion_cultura WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            UNION ALL
            SELECT n_eje_id FROM acjust.mov_evento WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
        )
        SELECT m.x_descripcion, COUNT(*) AS cantidad
        FROM EjesUnificados u
        INNER JOIN acjust.mae_eje m ON u.n_eje_id = m.n_eje_id
        GROUP BY m.x_descripcion
        ORDER BY cantidad DESC
    """, nativeQuery = true)
    List<Object[]> obtenerRankingPorEje(@Param("anio") int anio);

    // --- QUERY 3: RESUMEN ACTIVIDADES POR USUARIO (MULTISERIE) ---
    @Query(value = """
        WITH Detalle AS (
            SELECT n_usuario_reg_id AS usuario, 'Justicia Itinerante' AS tipo FROM acjust.mov_justicia_itinerante WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            UNION ALL
            SELECT n_usuario_reg_id, 'Cultura Jurídica' FROM acjust.mov_actividad_promocion_cultura WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            UNION ALL
            SELECT n_usuario_reg_id, 'Fortalecimiento' FROM acjust.mov_evento WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
        )
        SELECT 
            u.x_nombre_completo,
            d.tipo,
            COUNT(*) as cantidad
        FROM Detalle d
        INNER JOIN acjust.mov_usuario u ON d.usuario = u.n_usuario_id
        GROUP BY u.x_nombre_completo, d.tipo
        ORDER BY u.x_nombre_completo
    """, nativeQuery = true)
    List<Object[]> obtenerResumenActividadMagistrado(@Param("anio") int anio);

    // --- QUERY 4: RANKING DISTRITOS ---
    // Se usa 'n_distrito_jud_id' y se cruza con 'acjust.mae_distrito_judicial'
    @Query(value = """
        WITH DistritosUnificados AS (
            -- 1. Justicia Itinerante
            SELECT n_distrito_jud_id AS id FROM acjust.mov_justicia_itinerante 
            WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            
            UNION ALL
            
            -- 2. Promoción Cultura
            SELECT n_distrito_jud_id FROM acjust.mov_actividad_promocion_cultura 
            WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            
            UNION ALL
            
            -- 3. Llapanchikpaq
            SELECT n_distrito_jud_id FROM acjust.mov_llapanchikpaq_justicia 
            WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            
            UNION ALL
            
            -- 4. Eventos
            SELECT n_distrito_jud_id FROM acjust.mov_evento 
            WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            
            UNION ALL
            
            -- 5. Orientación Jurídica (Tabla mov_meta_anual)
            SELECT n_distrito_jud_id FROM acjust.mov_meta_anual 
            WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_reg_activ) = :anio
            
            UNION ALL
            
            -- 6. Juez Paz Escolar (Tabla mov_jpe_caso_atendido)
            SELECT n_distrito_jud_id FROM acjust.mov_jpe_caso_atendido 
            WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_registro_caso) = :anio
        )
        SELECT 
            d.x_nom_corto AS distrito, 
            COUNT(*) AS cantidad
        FROM DistritosUnificados u
        INNER JOIN acjust.mae_distrito_judicial d ON u.id = d.n_distrito_jud_id
        GROUP BY d.x_nom_corto
        ORDER BY cantidad DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Object[]> obtenerRankingDistritos(@Param("anio") int anio);

    // --- QUERY 5: EVOLUCIÓN MENSUAL ---
    @Query(value = """
        WITH Mensual AS (
            -- 1. Justicia Itinerante
            SELECT EXTRACT(MONTH FROM f_inicio) as mes, 'Justicia Itinerante' as tipo 
            FROM acjust.mov_justicia_itinerante 
            WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            
            UNION ALL
            
            -- 2. Cultura Jurídica
            SELECT EXTRACT(MONTH FROM f_inicio), 'Cultura Jurídica' 
            FROM acjust.mov_actividad_promocion_cultura 
            WHERE l_activo = '1' AND EXTRACT(YEAR FROM f_inicio) = :anio
            
            UNION ALL
            
            -- 3. Fortalecimiento
            SELECT EXTRACT(MONTH FROM f_inicio), 'Fortalecimiento' 
            FROM acjust.mov_evento 
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