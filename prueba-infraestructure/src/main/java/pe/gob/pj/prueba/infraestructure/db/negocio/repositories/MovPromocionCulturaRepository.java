package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovPromocionCulturaEntity;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovPromocionCulturaRepository extends JpaRepository<MovPromocionCulturaEntity, String> {

    @Query("SELECT MAX(e.id) FROM MovPromocionCulturaEntity e WHERE e.id LIKE '%-CJ'")
    String obtenerUltimoId();

    @Query(value = """
        SELECT 
            pc.c_actv_prom_cult_id AS id,
            pc.f_inicio AS fechaInicio,
            pc.f_fin AS fechaFin,
            pc.t_desc_activ AS tipoActividad, 
            pc.l_activo AS estado,
            dj.x_nom_corto AS distritoJudicialNombre
        FROM prueba.mov_aju_actv_prom_culturas pc -- ✅ AGREGADO 'prueba.'
        INNER JOIN prueba.mae_aju_distrito_judiciales dj ON pc.c_distrito_jud_id = dj.c_distrito_jud_id -- ✅ AGREGADO 'prueba.'
        WHERE pc.c_usuario_reg = :usuario
          
          -- FILTRO COMBO
          AND (:distrito IS NULL OR pc.c_distrito_jud_id = :distrito)
          
          -- FILTRO FECHAS 
          AND (CAST(:fecIni AS DATE) IS NULL OR pc.f_inicio >= :fecIni)
          AND (CAST(:fecFin AS DATE) IS NULL OR pc.f_inicio <= :fecFin)

          -- BUSCADOR GENERAL
          AND (
              :search IS NULL OR :search = '' OR
              UPPER(pc.c_actv_prom_cult_id) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(pc.t_desc_activ) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(dj.x_nom_corto) LIKE UPPER(CONCAT('%', :search, '%'))
          )
        ORDER BY pc.f_inicio DESC
    """, countQuery = """
        SELECT count(*) 
        FROM prueba.mov_aju_actv_prom_culturas pc -- ✅ AGREGADO 'prueba.'
        INNER JOIN prueba.mae_aju_distrito_judiciales dj ON pc.c_distrito_jud_id = dj.c_distrito_jud_id -- ✅ AGREGADO 'prueba.'
        WHERE pc.c_usuario_reg = :usuario
          AND (:distrito IS NULL OR pc.c_distrito_jud_id = :distrito)
          AND (CAST(:fecIni AS DATE) IS NULL OR pc.f_inicio >= :fecIni)
          AND (CAST(:fecFin AS DATE) IS NULL OR pc.f_inicio <= :fecFin)
          AND (
              :search IS NULL OR :search = '' OR
              UPPER(pc.c_actv_prom_cult_id) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(pc.t_desc_activ) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(dj.x_nom_corto) LIKE UPPER(CONCAT('%', :search, '%'))
          )
    """, nativeQuery = true)
    Page<PromocionCulturaProjection> listar(
            @Param("usuario") String usuario,
            @Param("search") String search,
            @Param("distrito") String distrito,
            @Param("fecIni") LocalDate fecIni,
            @Param("fecFin") LocalDate fecFin,
            Pageable pageable);

    @Query("SELECT EXTRACT(MONTH FROM e.fechaInicio) as mes, COUNT(e) as cantidad " +
            "FROM MovPromocionCulturaEntity e " +
            "WHERE e.activo = '1' " +
            "AND EXTRACT(YEAR FROM e.fechaInicio) = :anio " +
            "AND e.usuarioRegistro = :usuario " +
            "GROUP BY EXTRACT(MONTH FROM e.fechaInicio)")
    List<Object[]> contarPorMes(@Param("anio") int anio, @Param("usuario") String usuario);

    interface PromocionCulturaProjection {
        String getId();
        LocalDate getFechaInicio();
        LocalDate getFechaFin();
        String getTipoActividad();
        String getEstado();
        String getDistritoJudicialNombre();
    }
}