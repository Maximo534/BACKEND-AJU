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

    @Query(value = "SELECT e FROM MovJusticiaItineranteEntity e " +
            "WHERE e.usuarioRegistro = :usuario " +
            "AND e.activo = '1' " +
            "AND (:distrito IS NULL OR e.distritoJudicialId = :distrito) " +
            "AND (cast(:fecIni as date) IS NULL OR e.fechaInicio >= :fecIni) " +
            "AND (cast(:fecFin as date) IS NULL OR e.fechaInicio <= :fecFin) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "UPPER(e.id) LIKE UPPER(CONCAT('%', :search, '%')) OR " +
            "UPPER(e.lugarActividad) LIKE UPPER(CONCAT('%', :search, '%')) OR " +
            "UPPER(e.publicoObjetivo) LIKE UPPER(CONCAT('%', :search, '%'))) " +
            "ORDER BY e.fechaInicio DESC")
    Page<MovJusticiaItineranteEntity> listarCompleto(
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
}