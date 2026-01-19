package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovBuenaPracticaEntity;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovBuenaPracticaRepository extends JpaRepository<MovBuenaPracticaEntity, String> {

    @Query("SELECT MAX(e.id) FROM MovBuenaPracticaEntity e WHERE e.id LIKE '%-BP'")
    String obtenerUltimoId();

    @Query(value = """
        SELECT 
            bp.c_buena_pract_id AS id,
            bp.c_distrito_jud_id AS distritoJudicialId,
            bp.x_titulo AS titulo,
            bp.f_inicio AS fechaInicio,
            dj.x_nom_corto AS distritoJudicialNombre
        FROM prueba.mov_aju_buena_practicas bp  
        INNER JOIN prueba.mae_aju_distrito_judiciales dj ON bp.c_distrito_jud_id = dj.c_distrito_jud_id
        WHERE bp.c_usuario_reg = :usuario
          
          -- FILTROS
          AND (:distrito IS NULL OR bp.c_distrito_jud_id = :distrito)
          AND (CAST(:fecIni AS DATE) IS NULL OR bp.f_inicio >= :fecIni)
          AND (CAST(:fecFin AS DATE) IS NULL OR bp.f_inicio <= :fecFin)

          -- BUSCADOR
          AND (
              :search IS NULL OR :search = '' OR
              UPPER(bp.c_buena_pract_id) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(bp.x_titulo) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(dj.x_nom_corto) LIKE UPPER(CONCAT('%', :search, '%'))
          )
        ORDER BY bp.f_inicio DESC
    """, countQuery = """
        SELECT count(*) 
        FROM prueba.mov_aju_buena_practicas bp 
        INNER JOIN prueba.mae_aju_distrito_judiciales dj ON bp.c_distrito_jud_id = dj.c_distrito_jud_id 
        WHERE bp.c_usuario_reg = :usuario
          AND (:distrito IS NULL OR bp.c_distrito_jud_id = :distrito)
          AND (CAST(:fecIni AS DATE) IS NULL OR bp.f_inicio >= :fecIni)
          AND (CAST(:fecFin AS DATE) IS NULL OR bp.f_inicio <= :fecFin)
          AND (
              :search IS NULL OR :search = '' OR
              UPPER(bp.c_buena_pract_id) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(bp.x_titulo) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(dj.x_nom_corto) LIKE UPPER(CONCAT('%', :search, '%'))
          )
    """, nativeQuery = true)
    Page<BuenaPracticaProjection> listar(
            @Param("usuario") String usuario,
            @Param("search") String search,
            @Param("distrito") String distritoId,
            @Param("fecIni") LocalDate fecIni,
            @Param("fecFin") LocalDate fecFin,
            Pageable pageable);

    @Query("SELECT e.distritoJudicialId, COUNT(e) FROM MovBuenaPracticaEntity e GROUP BY e.distritoJudicialId")
    List<Object[]> obtenerEstadisticasHistoricas();

    interface BuenaPracticaProjection {
        String getId();
        String getDistritoJudicialId();
        String getDistritoJudicialNombre();
        String getTitulo();
        LocalDate getFechaInicio();
    }
}