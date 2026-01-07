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
public interface MovBuenaPracticaRepository extends JpaRepository<MovBuenaPracticaEntity, String> {

    @Query("SELECT MAX(e.id) FROM MovBuenaPracticaEntity e WHERE e.id LIKE '%-BP'")
    String obtenerUltimoId();

    @Query("SELECT e FROM MovBuenaPracticaEntity e " +
            "WHERE e.usuarioRegistro = :usuario " +
            // "AND e.activo = '1' " + // Descomentar si aplica
            "AND (:codigo IS NULL OR :codigo = '' OR e.id LIKE %:codigo%) " +
            "AND (:titulo IS NULL OR :titulo = '' OR e.titulo LIKE %:titulo%) " +
            "AND (:distrito IS NULL OR :distrito = '' OR e.distritoJudicialId = :distrito) " +

            // LÓGICA DE RANGO:
            // Buscamos registros donde su FECHA DE INICIO sea mayor o igual al filtro 'desde'
            "AND (cast(:fecIni as date) IS NULL OR e.fechaInicio >= :fecIni) " +
            // Y donde su FECHA DE INICIO sea menor o igual al filtro 'hasta'
            "AND (cast(:fecFin as date) IS NULL OR e.fechaInicio <= :fecFin) " +

            "ORDER BY e.fechaInicio DESC")
    Page<MovBuenaPracticaEntity> listarDinamico(
            @Param("usuario") String usuario,
            @Param("codigo") String codigo,
            @Param("titulo") String titulo,
            @Param("distrito") String distrito,
            @Param("fecIni") LocalDate fecIni,
            @Param("fecFin") LocalDate fecFin, // Recibimos el parámetro aunque la tabla no tenga la columna
            Pageable pageable);

    // Gráfico Histórico (Sin WHERE YEAR...)
    @Query("SELECT e.distritoJudicialId, COUNT(e) " +
            "FROM MovBuenaPracticaEntity e " +
            // "WHERE e.usuarioRegistro = :usuario " + // Descomenta si quieres que sea solo histórico DEL USUARIO
            "GROUP BY e.distritoJudicialId")
    List<Object[]> obtenerEstadisticasHistoricas();

}