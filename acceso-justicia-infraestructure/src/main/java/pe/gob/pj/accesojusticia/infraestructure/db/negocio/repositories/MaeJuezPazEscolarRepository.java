package pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MaeJuezPazEscolarEntity;

@Repository
public interface MaeJuezPazEscolarRepository extends JpaRepository<MaeJuezPazEscolarEntity, Long> {

    boolean existsByDniAndInstitucionEducativaIdAndActivo(String dni, Long institucionEducativaId, String activo);

    @Query(value = "SELECT c_cod_reg FROM acjust.mae_juez_paz_escolar " +
            "WHERE c_cod_reg LIKE %:sufijoAnio " +
            "ORDER BY n_juez_paz_id DESC LIMIT 1", nativeQuery = true)
    String obtenerUltimoCodigo(@Param("sufijoAnio") String sufijoAnio);

    @Query("SELECT e FROM MaeJuezPazEscolarEntity e " +
            "LEFT JOIN FETCH e.institucionEducativa ie " +
            "LEFT JOIN FETCH ie.ugel ug " +
            "WHERE e.activo = '1' " +
            "AND (:colegioId IS NULL OR e.institucionEducativaId = :colegioId) " +
            "AND (:ugelId IS NULL OR ie.ugelId = :ugelId) " +
            "AND (:corteId IS NULL OR ug.distritoJudicialId = :corteId) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "     UPPER(e.codigo) LIKE UPPER(CONCAT('%', :search, '%')) OR " +
            "     UPPER(e.dni) LIKE UPPER(CONCAT('%', :search, '%')) OR " +
            "     UPPER(e.nombres) LIKE UPPER(CONCAT('%', :search, '%')) OR " +
            "     UPPER(e.apePaterno) LIKE UPPER(CONCAT('%', :search, '%'))) " +
            "ORDER BY e.id DESC")
    Page<MaeJuezPazEscolarEntity> listarCompleto(
            @Param("search") String search,
            @Param("corteId") Long corteId,
            @Param("ugelId") Long ugelId,
            @Param("colegioId") Long colegioId,
            Pageable pageable);
}