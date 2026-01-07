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

    @Query("SELECT e FROM MovEventoFcEntity e " +
            "WHERE e.usuarioRegistro = :usuario " +
            "AND e.activo = '1' " +
            "AND (:codigo IS NULL OR :codigo = '' OR e.id LIKE %:codigo%) " +
            "AND (:nombre IS NULL OR :nombre = '' OR e.nombreEvento LIKE %:nombre%) " +
            "AND (:distrito IS NULL OR :distrito = '' OR e.distritoJudicialId = :distrito) " +
            "AND (cast(:fecIni as date) IS NULL OR e.fechaInicio >= :fecIni) " +
            "AND (cast(:fecFin as date) IS NULL OR e.fechaInicio <= :fecFin) " +
            "ORDER BY e.fechaInicio DESC")
    Page<MovEventoFcEntity> listarDinamico(
            @Param("usuario") String usuario,
            @Param("codigo") String codigo,
            @Param("nombre") String nombre,
            @Param("distrito") String distrito,
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
}