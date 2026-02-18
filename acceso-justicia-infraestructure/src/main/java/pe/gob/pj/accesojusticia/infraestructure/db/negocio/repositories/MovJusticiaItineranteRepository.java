package pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MovJusticiaItineranteEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovJusticiaItineranteRepository extends JpaRepository<MovJusticiaItineranteEntity, Long> {

    boolean existsByCodigo(String codigo);

    Optional<MovJusticiaItineranteEntity> findByIdAndActivo(Long id, String activo);

    @Query("SELECT j FROM MovJusticiaItineranteEntity j " +
            "WHERE j.activo = '1' " +
            "AND (:distrito IS NULL OR j.distritoJudicialId = :distrito) " +
            "AND (cast(:fInicio as date) IS NULL OR j.fechaInicio >= :fInicio) " +
            "AND (cast(:fFin as date) IS NULL OR j.fechaInicio <= :fFin) " +
            "AND (:usuarioId IS NULL OR j.usuarioRegistroId = :usuarioId) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "     UPPER(j.codigo) LIKE UPPER(CONCAT('%', :search, '%')) OR " +
            "     UPPER(j.lugarActividad) LIKE UPPER(CONCAT('%', :search, '%'))) " +
            "ORDER BY j.id DESC")
    Page<MovJusticiaItineranteEntity> listarCompleto(
            @Param("search") String search,
            @Param("distrito") Long distrito,
            @Param("fInicio") LocalDate fInicio,
            @Param("fFin") LocalDate fFin,
            @Param("usuarioId") Long usuarioId,
            Pageable pageable
    );

    @Query("SELECT j FROM MovJusticiaItineranteEntity j " +
            "WHERE j.activo = '1' " +
            "AND (:distrito IS NULL OR j.distritoJudicialId = :distrito) " +
            "AND (cast(:fInicio as date) IS NULL OR j.fechaInicio >= :fInicio) " +
            "AND (cast(:fFin as date) IS NULL OR j.fechaInicio <= :fFin) " +
            "AND (:usuarioId IS NULL OR j.usuarioRegistroId = :usuarioId) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "     UPPER(j.codigo) LIKE UPPER(CONCAT('%', :search, '%')) OR " +
            "     UPPER(j.lugarActividad) LIKE UPPER(CONCAT('%', :search, '%'))) " +
            "ORDER BY j.id DESC")
    List<MovJusticiaItineranteEntity> listarSinPaginacion(
            @Param("search") String search,
            @Param("distrito") Long distrito,
            @Param("fInicio") LocalDate fInicio,
            @Param("fFin") LocalDate fFin,
            @Param("usuarioId") Long usuarioId
    );

    // Obtener último código para correlativo (ej: busca '%-2026-JI' en distrito X)
    @Query(value = "SELECT c_codigo FROM acjust.mov_justicia_itinerante " +
            "WHERE c_codigo LIKE %:sufijoAnio " +
            "AND n_distrito_jud_id = :distrito " +
            "ORDER BY n_just_itin_id DESC LIMIT 1", nativeQuery = true)
    String obtenerUltimoCodigo(@Param("distrito") Long distrito, @Param("sufijoAnio") String sufijoAnio);


    @Query("SELECT EXTRACT(MONTH FROM e.fechaInicio) as mes, COUNT(e) as cantidad " +
            "FROM MovJusticiaItineranteEntity e " +
            "WHERE e.activo = '1' " +
            "AND EXTRACT(YEAR FROM e.fechaInicio) = :anio " +
            "AND e.cAudId = :usuario " +
            "GROUP BY EXTRACT(MONTH FROM e.fechaInicio)")
    List<Object[]> contarPorMes(@Param("anio") int anio, @Param("usuario") String usuario);
}


