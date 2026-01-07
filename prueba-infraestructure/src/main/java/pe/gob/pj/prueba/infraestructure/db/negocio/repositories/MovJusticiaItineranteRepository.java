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

    @Query("SELECT e FROM MovJusticiaItineranteEntity e " +
            "WHERE e.usuarioRegistro = :usuario " +
            "AND e.activo = '1' " +
            // Lógica de "Si el filtro es nulo, trae todo"
            "AND (:codigo IS NULL OR :codigo = '' OR e.id LIKE %:codigo%) " +
            "AND (:publico IS NULL OR :publico = '' OR e.publicoObjetivo LIKE %:publico%) " +
            "AND (:distrito IS NULL OR :distrito = '' OR e.distritoJudicialId = :distrito) " +
            "AND (cast(:fecIni as date) IS NULL OR e.fechaInicio >= :fecIni) " +
            "AND (cast(:fecFin as date) IS NULL OR e.fechaInicio <= :fecFin) " +
            "ORDER BY e.fechaInicio DESC")
    Page<MovJusticiaItineranteEntity> listarDinamico(
            @Param("usuario") String usuario,
            @Param("codigo") String codigo,
            @Param("publico") String publico,
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