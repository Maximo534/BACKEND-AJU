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
public interface MovOrientadoraJudicialRepository extends JpaRepository<MovOrientadoraJudicialEntity, String> {

    @Query("SELECT MAX(e.id) FROM MovOrientadoraJudicialEntity e WHERE e.id LIKE '%-OJ'")
    String obtenerUltimoId();

    @Query(value = """
        SELECT 
            oj.c_meta_anual_id AS id,
            oj.f_registro AS fechaAtencion,
            oj.x_nomb_apell AS nombrePersona,
            oj.c_num_exp AS numeroExpediente,
            oj.c_distrito_jud_id AS distritoJudicialId,
            dj.x_nom_corto AS distritoJudicialNombre
        FROM prueba.mov_aju_meta_anuales oj -- ✅ AGREGADO 'prueba.'
        LEFT JOIN prueba.mae_aju_distrito_judiciales dj ON oj.c_distrito_jud_id = dj.c_distrito_jud_id -- ✅ AGREGADO 'prueba.'
        WHERE oj.c_usuario_reg = :usuario
          
          -- FILTROS
          AND (:distrito IS NULL OR oj.c_distrito_jud_id = :distrito)
          AND (CAST(:fecIni AS DATE) IS NULL OR oj.f_registro >= :fecIni)
          AND (CAST(:fecFin AS DATE) IS NULL OR oj.f_registro <= :fecFin)

          -- BUSCADOR (ID, Nombre, DNI, Expediente)
          AND (
              :search IS NULL OR :search = '' OR
              UPPER(oj.c_meta_anual_id) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(oj.x_nomb_apell) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(oj.c_num_doc) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(oj.c_num_exp) LIKE UPPER(CONCAT('%', :search, '%'))
          )
        ORDER BY oj.f_registro DESC
    """, countQuery = """
        SELECT count(*) 
        FROM prueba.mov_aju_meta_anuales oj -- ✅ AGREGADO 'prueba.'
        WHERE oj.c_usuario_reg = :usuario
          AND (:distrito IS NULL OR oj.c_distrito_jud_id = :distrito)
          AND (CAST(:fecIni AS DATE) IS NULL OR oj.f_registro >= :fecIni)
          AND (CAST(:fecFin AS DATE) IS NULL OR oj.f_registro <= :fecFin)
          AND (
              :search IS NULL OR :search = '' OR
              UPPER(oj.c_meta_anual_id) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(oj.x_nomb_apell) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(oj.c_num_doc) LIKE UPPER(CONCAT('%', :search, '%')) OR
              UPPER(oj.c_num_exp) LIKE UPPER(CONCAT('%', :search, '%'))
          )
    """, nativeQuery = true)
    Page<OrientadoraProjection> listar(
            @Param("usuario") String usuario,
            @Param("search") String search,
            @Param("distrito") String distrito,
            @Param("fecIni") LocalDate fecIni,
            @Param("fecFin") LocalDate fecFin,
            Pageable pageable);

    @Query("SELECT e.distritoJudicialId, COUNT(e) FROM MovOrientadoraJudicialEntity e GROUP BY e.distritoJudicialId")
    List<Object[]> obtenerEstadisticasHistoricas();

    interface OrientadoraProjection {
        String getId();
        LocalDate getFechaAtencion();
        String getNombrePersona();
        String getNumeroExpediente();
        String getDistritoJudicialId();
        String getDistritoJudicialNombre();
    }
}