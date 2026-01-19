package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovJusticiaItineranteEntity;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovJusticiaItineranteRepository extends JpaRepository<MovJusticiaItineranteEntity, String> {
    @Query("SELECT MAX(e.id) FROM MovJusticiaItineranteEntity e WHERE e.id LIKE '%-JI'")
    String obtenerUltimoId();

    @Query(value = """
        SELECT 
            ji.c_just_itin_id AS id,
            ji.f_inicio AS fechaInicio,
            ji.f_fin AS fechaFin,
            ji.f_reg_activ AS fechaRegistro,
            ji.x_lugar_activ AS lugar,
            ji.x_publico_obj AS publicoObjetivo,
            ji.l_activo AS estado,
            dj.x_nom_corto AS distritoJudicialNombre
        FROM prueba.mov_aju_justicia_itinerantes ji 
        INNER JOIN prueba.mae_aju_distrito_judiciales dj ON ji.c_distrito_jud_id = dj.c_distrito_jud_id 
        WHERE ji.c_usuario_reg = :usuario
          AND ji.l_activo = '1'
          
          -- FILTRO COMBO
          AND (:distrito IS NULL OR ji.c_distrito_jud_id = :distrito)
          
          -- FILTRO FECHAS
          AND (CAST(:fecIni AS DATE) IS NULL OR ji.f_inicio >= :fecIni)
          AND (CAST(:fecFin AS DATE) IS NULL OR ji.f_inicio <= :fecFin)

          -- BUSCADOR GENERAL
          AND (
              :search IS NULL OR :search = '' OR
              UPPER(ji.c_just_itin_id) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(ji.x_lugar_activ) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(ji.x_publico_obj) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(dj.x_nom_corto) LIKE UPPER(CONCAT('%', :search, '%'))
          )
        ORDER BY ji.f_inicio DESC
    """, countQuery = """
        SELECT count(*) 
        FROM prueba.mov_aju_justicia_itinerantes ji 
        INNER JOIN prueba.mae_aju_distrito_judiciales dj ON ji.c_distrito_jud_id = dj.c_distrito_jud_id 
        WHERE ji.c_usuario_reg = :usuario
          AND ji.l_activo = '1'
          AND (:distrito IS NULL OR ji.c_distrito_jud_id = :distrito)
          AND (CAST(:fecIni AS DATE) IS NULL OR ji.f_inicio >= :fecIni)
          AND (CAST(:fecFin AS DATE) IS NULL OR ji.f_inicio <= :fecFin)
          AND (
              :search IS NULL OR :search = '' OR
              UPPER(ji.c_just_itin_id) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(ji.x_lugar_activ) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(ji.x_publico_obj) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(dj.x_nom_corto) LIKE UPPER(CONCAT('%', :search, '%'))
          )
    """, nativeQuery = true)
    Page<JusticiaItineranteResumenProjection> listar(
            @Param("usuario") String usuario,
            @Param("search") String search,
            @Param("distrito") String distrito,
            @Param("fecIni") LocalDate fecIni,
            @Param("fecFin") LocalDate fecFin,
            Pageable pageable);

    @Query("SELECT EXTRACT(MONTH FROM e.fechaInicio) as mes, COUNT(e) as cantidad " +
            "FROM MovJusticiaItineranteEntity e " +
            "WHERE e.activo = '1' " +
            "AND EXTRACT(YEAR FROM e.fechaInicio) = :anio " +
            "AND e.usuarioRegistro = :usuario " +
            "GROUP BY EXTRACT(MONTH FROM e.fechaInicio)")
    List<Object[]> contarPorMes(@Param("anio") int anio, @Param("usuario") String usuario);

    interface JusticiaItineranteResumenProjection {
        String getId();
        LocalDate getFechaInicio();
        LocalDate getFechaFin();
        LocalDate getFechaRegistro();
        String getLugar();
        String getPublicoObjetivo();
        String getEstado();
        String getDistritoJudicialNombre();
    }
}