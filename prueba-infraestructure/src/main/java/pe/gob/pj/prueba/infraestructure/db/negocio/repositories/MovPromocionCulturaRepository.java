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
public interface MovPromocionCulturaRepository extends JpaRepository<MovPromocionCulturaEntity, Long> {

    boolean existsByCodigo(String codigo);

    @Query(value = "SELECT c_codigo FROM acjust.mov_actividad_promocion_cultura " +
            "WHERE c_codigo LIKE %:sufijoAnio " +
            "AND n_distrito_jud_id = :distrito " +
            "ORDER BY n_actv_prom_cult_id DESC LIMIT 1", nativeQuery = true)
    String obtenerUltimoCodigo(@Param("distrito") Long distrito, @Param("sufijoAnio") String sufijoAnio);

    @Query("SELECT e FROM MovPromocionCulturaEntity e " +
            "WHERE e.activo = '1' " +
            "AND (:distrito IS NULL OR e.distritoJudicialId = :distrito) " +
            "AND (cast(:fecIni as date) IS NULL OR e.fechaInicio >= :fecIni) " +
            "AND (cast(:fecFin as date) IS NULL OR e.fechaInicio <= :fecFin) " +
            "AND (:usuarioId IS NULL OR e.usuarioRegistroId = :usuarioId) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "     UPPER(e.codigo) LIKE UPPER(CONCAT('%', :search, '%')) OR " +
            "     UPPER(e.nombreActividad) LIKE UPPER(CONCAT('%', :search, '%'))) " +
            "ORDER BY e.id DESC")
    List<MovPromocionCulturaEntity> listarSinPaginacion(
            @Param("search") String search,
            @Param("distrito") Long distrito,
            @Param("fecIni") LocalDate fecIni,
            @Param("fecFin") LocalDate fecFin,
            @Param("usuarioId") Long usuarioId
    );

    @Query("SELECT e FROM MovPromocionCulturaEntity e " +
            "WHERE e.activo = '1' " +
            "AND (:distrito IS NULL OR e.distritoJudicialId = :distrito) " +
            "AND (cast(:fecIni as date) IS NULL OR e.fechaInicio >= :fecIni) " +
            "AND (cast(:fecFin as date) IS NULL OR e.fechaInicio <= :fecFin) " +
            "AND (:usuarioId IS NULL OR e.usuarioRegistroId = :usuarioId) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "     UPPER(e.codigo) LIKE UPPER(CONCAT('%', :search, '%')) OR " +
            "     UPPER(e.nombreActividad) LIKE UPPER(CONCAT('%', :search, '%'))) " +
            "ORDER BY e.id DESC")
    Page<MovPromocionCulturaEntity> listarCompleto(
            @Param("search") String search,
            @Param("distrito") Long distrito,
            @Param("fecIni") LocalDate fecIni,
            @Param("fecFin") LocalDate fecFin,
            @Param("usuarioId") Long usuarioId,
            Pageable pageable);

    @Query("SELECT EXTRACT(MONTH FROM e.fechaInicio) as mes, COUNT(e) as cantidad " +
            "FROM MovJusticiaItineranteEntity e " +
            "WHERE e.activo = '1' " +
            "AND EXTRACT(YEAR FROM e.fechaInicio) = :anio " +
            "AND e.cAudId = :usuario " +
            "GROUP BY EXTRACT(MONTH FROM e.fechaInicio)")
    List<Object[]> contarPorMes(@Param("anio") int anio, @Param("usuario") String usuario);
}



