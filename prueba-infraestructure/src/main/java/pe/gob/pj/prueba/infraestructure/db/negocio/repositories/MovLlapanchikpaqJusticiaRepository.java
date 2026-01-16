package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovLlapanchikpaqJusticia;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovLlapanchikpaqJusticiaRepository extends JpaRepository<MovLlapanchikpaqJusticia, String> {

    @Query("SELECT MAX(e.id) FROM MovLlapanchikpaqJusticia e WHERE e.id LIKE '%-LL'")
    String obtenerUltimoId();

    @Query(value = """
        SELECT 
            ll.c_llj_id AS id,
            ll.f_inicio AS fechaInicio,
            ll.l_activo AS estado,
            dj.x_nom_corto AS distritoJudicialNombre
        FROM prueba.mov_aju_llapanchikpaq_justicia ll -- ✅ AGREGADO 'prueba.'
        INNER JOIN prueba.mae_aju_distrito_judiciales dj ON ll.c_distrito_jud_id = dj.c_distrito_jud_id -- ✅ AGREGADO 'prueba.'
        WHERE ll.c_usuario_reg = :usuario
          
          -- FILTRO COMBO
          AND (:distrito IS NULL OR ll.c_distrito_jud_id = :distrito)
          
          -- FILTRO RANGO DE FECHAS (Sobre f_inicio)
          AND (CAST(:fecIni AS DATE) IS NULL OR ll.f_inicio >= :fecIni)
          AND (CAST(:fecFin AS DATE) IS NULL OR ll.f_inicio <= :fecFin)

          -- BUSCADOR GENERAL
          AND (
              :search IS NULL OR :search = '' OR
              UPPER(ll.c_llj_id) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(ll.x_lugar_activ) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(dj.x_nom_corto) LIKE UPPER(CONCAT('%', :search, '%'))
          )
        ORDER BY ll.f_inicio DESC
    """, countQuery = """
        SELECT count(*) 
        FROM prueba.mov_aju_llapanchikpaq_justicia ll -- ✅ AGREGADO 'prueba.'
        INNER JOIN prueba.mae_aju_distrito_judiciales dj ON ll.c_distrito_jud_id = dj.c_distrito_jud_id -- ✅ AGREGADO 'prueba.'
        WHERE ll.c_usuario_reg = :usuario
          AND (:distrito IS NULL OR ll.c_distrito_jud_id = :distrito)
          AND (CAST(:fecIni AS DATE) IS NULL OR ll.f_inicio >= :fecIni)
          AND (CAST(:fecFin AS DATE) IS NULL OR ll.f_inicio <= :fecFin)
          AND (
              :search IS NULL OR :search = '' OR
              UPPER(ll.c_llj_id) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(ll.x_lugar_activ) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(dj.x_nom_corto) LIKE UPPER(CONCAT('%', :search, '%'))
          )
    """, nativeQuery = true)
    Page<LlapanchikpaqResumenProjection> listar(
            @Param("usuario") String usuario,
            @Param("search") String search,
            @Param("distrito") String distrito,
            @Param("fecIni") LocalDate fecIni,
            @Param("fecFin") LocalDate fecFin,
            Pageable pageable);

    // Gráfico
    @Query("SELECT e.distritoJudicialId, COUNT(e) " +
            "FROM MovLlapanchikpaqJusticia e " +
            "GROUP BY e.distritoJudicialId")
    List<Object[]> obtenerEstadisticasPorCorte();

    interface LlapanchikpaqResumenProjection {
        String getId();
        LocalDate getFechaInicio();
        String getEstado();
        String getDistritoJudicialNombre();
    }
}