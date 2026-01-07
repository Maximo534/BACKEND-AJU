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
public interface MovPromocionCulturaRepository extends JpaRepository<MovPromocionCulturaEntity, String> {

    @Query("SELECT MAX(e.id) FROM MovPromocionCulturaEntity e WHERE e.id LIKE '%-CJ'")
    String obtenerUltimoId();

    @Query("SELECT e FROM MovPromocionCulturaEntity e " +
            "WHERE e.usuarioRegistro = :usuario " +
            "AND e.activo = '1' " +
            "AND (:codigo IS NULL OR :codigo = '' OR e.id LIKE %:codigo%) " +
            "AND (:descripcion IS NULL OR :descripcion = '' OR UPPER(e.descripcionActividad) LIKE UPPER(CONCAT('%', :descripcion, '%'))) " +
            "AND (:distrito IS NULL OR :distrito = '' OR e.distritoJudicialId = :distrito) " +
            "AND (cast(:fecIni as date) IS NULL OR e.fechaInicio >= :fecIni) " +
            "AND (cast(:fecFin as date) IS NULL OR e.fechaInicio <= :fecFin) " +
            "ORDER BY e.fechaInicio DESC")
    Page<MovPromocionCulturaEntity> listarDinamico(
            @Param("usuario") String usuario,
            @Param("codigo") String codigo,
            @Param("descripcion") String descripcion,
            @Param("distrito") String distrito,
            @Param("fecIni") LocalDate fecIni,
            @Param("fecFin") LocalDate fecFin,
            Pageable pageable);

    @Query("SELECT EXTRACT(MONTH FROM e.fechaInicio) as mes, COUNT(e) as cantidad " +
            "FROM MovPromocionCulturaEntity e " +
            "WHERE e.activo = '1' " +
            "AND EXTRACT(YEAR FROM e.fechaInicio) = :anio " +
            "AND e.usuarioRegistro = :usuario " +
            "GROUP BY EXTRACT(MONTH FROM e.fechaInicio)")
    List<Object[]> contarPorMes(@Param("anio") int anio, @Param("usuario") String usuario);

}