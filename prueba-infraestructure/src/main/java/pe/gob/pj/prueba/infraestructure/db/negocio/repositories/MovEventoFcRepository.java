package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovEventoFcEntity;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovEventoFcRepository extends JpaRepository<MovEventoFcEntity, String> {

    @Query("SELECT MAX(e.id) FROM MovEventoFcEntity e WHERE e.id LIKE '%-FC'")
    String obtenerUltimoId();

    @Query(value = """
        SELECT 
            fc.c_evento_id AS id,
            fc.f_inicio AS fechaInicio,
            fc.f_fin AS fechaFin,
            fc.c_tipo_evento AS tipoEvento,
            fc.l_activo AS estado,
            dj.x_nom_corto AS distritoJudicialNombre
        FROM prueba.mov_aju_eventos fc 
        INNER JOIN prueba.mae_aju_distrito_judiciales dj ON fc.c_distrito_jud_id = dj.c_distrito_jud_id 
        WHERE fc.c_usuario_reg = :usuario
          AND fc.l_activo = '1'
          
          -- FILTROS DE COMBOS
          AND (:distrito IS NULL OR fc.c_distrito_jud_id = :distrito)
          AND (:tipo IS NULL OR fc.c_tipo_evento = :tipo)
          
          -- FILTRO FECHAS
          AND (CAST(:fecIni AS DATE) IS NULL OR fc.f_inicio >= :fecIni)
          AND (CAST(:fecFin AS DATE) IS NULL OR fc.f_inicio <= :fecFin)

          -- BUSCADOR GENERAL
          AND (
              :search IS NULL OR :search = '' OR
              UPPER(fc.c_evento_id) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(fc.x_nombre_evento) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(dj.x_nom_corto) LIKE UPPER(CONCAT('%', :search, '%'))
          )
        ORDER BY fc.f_inicio DESC
    """, countQuery = """
        SELECT count(*) 
        FROM prueba.mov_aju_eventos fc 
        INNER JOIN prueba.mae_aju_distrito_judiciales dj ON fc.c_distrito_jud_id = dj.c_distrito_jud_id 
        WHERE fc.c_usuario_reg = :usuario
          AND fc.l_activo = '1'
          AND (:distrito IS NULL OR fc.c_distrito_jud_id = :distrito)
          AND (:tipo IS NULL OR fc.c_tipo_evento = :tipo)
          AND (CAST(:fecIni AS DATE) IS NULL OR fc.f_inicio >= :fecIni)
          AND (CAST(:fecFin AS DATE) IS NULL OR fc.f_inicio <= :fecFin)
          AND (
              :search IS NULL OR :search = '' OR
              UPPER(fc.c_evento_id) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(fc.x_nombre_evento) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(dj.x_nom_corto) LIKE UPPER(CONCAT('%', :search, '%'))
          )
    """, nativeQuery = true)
    Page<FortalecimientoProjection> listar(
            @Param("usuario") String usuario,
            @Param("search") String search,
            @Param("distrito") String distrito,
            @Param("tipo") String tipoEvento,
            @Param("fecIni") LocalDate fecIni,
            @Param("fecFin") LocalDate fecFin,
            Pageable pageable);

    @Query("SELECT EXTRACT(MONTH FROM e.fechaInicio) as mes, COUNT(e) as cantidad " +
            "FROM MovEventoFcEntity e " +
            "WHERE e.activo = '1' " +
            "AND EXTRACT(YEAR FROM e.fechaInicio) = :anio " +
            "AND e.usuarioRegistro = :usuario " +
            "GROUP BY EXTRACT(MONTH FROM e.fechaInicio)")
    List<Object[]> contarPorMes(@Param("anio") int anio, @Param("usuario") String usuario);

    interface FortalecimientoProjection {
        String getId();
        LocalDate getFechaInicio();
        LocalDate getFechaFin();
        String getTipoEvento();
        String getEstado();
        String getDistritoJudicialNombre();
    }
}