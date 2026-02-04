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
public interface MovBuenaPracticaRepository extends JpaRepository<MovBuenaPracticaEntity, Long> {

    @Query(value = "SELECT c_codigo FROM acjust.mov_buena_practica " +
            "WHERE c_codigo LIKE %:sufijoAnio " +
            "AND n_distrito_jud_id = :distrito " +
            "ORDER BY n_buena_pract_id DESC LIMIT 1", nativeQuery = true)
    String obtenerUltimoCodigo(@Param("distrito") Long distrito, @Param("sufijoAnio") String sufijoAnio);

    @Query("SELECT e FROM MovBuenaPracticaEntity e " +
            "WHERE e.activo = '1' " +
            "AND (:distrito IS NULL OR e.distritoJudicialId = :distrito) " +
            "AND (cast(:fecIni as date) IS NULL OR e.fechaInicio >= :fecIni) " +
            "AND (cast(:fecFin as date) IS NULL OR e.fechaInicio <= :fecFin) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "     UPPER(e.codigo) LIKE UPPER(CONCAT('%', :search, '%')) OR " +
            "     UPPER(e.titulo) LIKE UPPER(CONCAT('%', :search, '%'))) " +
            "ORDER BY e.id DESC")
    Page<MovBuenaPracticaEntity> listarCompleto(
            @Param("search") String search,
            @Param("distrito") Long distrito,
            @Param("fecIni") LocalDate fecIni,
            @Param("fecFin") LocalDate fecFin,
            Pageable pageable);

    @Query("SELECT e.distritoJudicialId, COUNT(e) " +
            "FROM MovBuenaPracticaEntity e " +
            "WHERE e.activo = '1' " +
            "GROUP BY e.distritoJudicialId")
    List<Object[]> obtenerEstadisticasPorCorte();
}