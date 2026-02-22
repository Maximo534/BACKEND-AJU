package pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MovJpeCasoAtendidoEntity;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovJpeCasoAtendidoRepository extends JpaRepository<MovJpeCasoAtendidoEntity, Long> {

    @Query(value = "SELECT c_codigo FROM acjust.mov_jpe_caso_atendido " +
            "WHERE c_codigo LIKE %:sufijoAnio " +
            "AND n_distrito_jud_id = :distrito " +
            "ORDER BY n_jpeca_id DESC LIMIT 1", nativeQuery = true)
    String obtenerUltimoCodigo(@Param("distrito") Long distrito, @Param("sufijoAnio") String sufijoAnio);

    @Query("SELECT e FROM MovJpeCasoAtendidoEntity e " +
            "LEFT JOIN FETCH e.juezEscolar je " +
            "LEFT JOIN FETCH je.institucionEducativa ie " +
            "LEFT JOIN FETCH ie.ugel u " +
            "WHERE e.activo = '1' " +
            "AND (:distritoId IS NULL OR e.distritoJudicialId = :distritoId) " +
            "AND (:ugelId IS NULL OR ie.ugelId = :ugelId) " +
            "AND (:colegioId IS NULL OR je.institucionEducativaId = :colegioId) " +
            "AND (cast(:fecha as date) IS NULL OR e.fechaRegistroCaso = :fecha) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "     UPPER(e.codigo) LIKE UPPER(CONCAT('%', :search, '%')) OR " +
            "     UPPER(e.resumenHechos) LIKE UPPER(CONCAT('%', :search, '%'))) " +
            "ORDER BY e.id DESC")
    Page<MovJpeCasoAtendidoEntity> listarCompleto(
            @Param("search") String search,
            @Param("distritoId") Long distritoId,
            @Param("ugelId") Long ugelId,
            @Param("colegioId") Long colegioId,
            @Param("fecha") LocalDate fechaRegistro,
            Pageable pageable);

    @Query("SELECT e.distritoJudicialId, COUNT(e) FROM MovJpeCasoAtendidoEntity e WHERE e.activo = '1' GROUP BY e.distritoJudicialId")
    List<Object[]> obtenerEstadisticasPorCorte();
}