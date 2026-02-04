package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovOrientadoraJudicialEntity;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovOrientadoraJudicialRepository extends JpaRepository<MovOrientadoraJudicialEntity, Long> {

    @Query(value = "SELECT c_codigo FROM acjust.mov_meta_anual " +
            "WHERE c_codigo LIKE %:sufijoAnio " +
            "AND n_distrito_jud_id = :distrito " +
            "ORDER BY n_meta_anual_id DESC LIMIT 1", nativeQuery = true)
    String obtenerUltimoCodigo(@Param("distrito") Long distrito, @Param("sufijoAnio") String sufijoAnio);

    @Query("SELECT e FROM MovOrientadoraJudicialEntity e " +
            "WHERE e.activo = '1' " +
            "AND (:distritoId IS NULL OR e.distritoJudicialId = :distritoId) " +
            "AND (cast(:fecIni as date) IS NULL OR e.fechaAtencion >= :fecIni) " +
            "AND (cast(:fecFin as date) IS NULL OR e.fechaAtencion <= :fecFin) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "     UPPER(e.codigo) LIKE UPPER(CONCAT('%', :search, '%')) OR " +
            "     UPPER(e.nombreCompleto) LIKE UPPER(CONCAT('%', :search, '%')) OR " +
            "     UPPER(e.numeroDocumento) LIKE UPPER(CONCAT('%', :search, '%')) OR " +
            "     UPPER(e.numeroExpediente) LIKE UPPER(CONCAT('%', :search, '%'))) " +
            "ORDER BY e.id DESC")
    Page<MovOrientadoraJudicialEntity> listarCompleto(
            @Param("search") String search,
            @Param("distritoId") Long distritoId,
            @Param("fecIni") LocalDate fecIni,
            @Param("fecFin") LocalDate fecFin,
            Pageable pageable);

    @Query("SELECT e.distritoJudicialId, COUNT(e) FROM MovOrientadoraJudicialEntity e WHERE e.activo = '1' GROUP BY e.distritoJudicialId")
    List<Object[]> obtenerEstadisticasPorCorte();
}