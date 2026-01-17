package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MaeJuezPazEscolarEntity;

@Repository
public interface MaeJuezPazEscolarRepository extends JpaRepository<MaeJuezPazEscolarEntity, String> {

    boolean existsByDniAndInstitucionEducativaIdAndActivo(String dni, String institucionEducativaId, String activo);

    @Query(value = """
        SELECT 
            je.c_cod_reg AS id,
            je.c_dni AS dni,
            je.x_nombre AS nombres,
            je.x_ape_paterno AS apePaterno,
            je.x_ape_materno AS apeMaterno,
            je.x_cargo AS cargo,
            dj.x_nom_corto AS corteNombre,
            ug.x_nombre AS ugelNombre,
            ie.x_nombre AS colegioNombre
        FROM prueba.mae_aju_juez_paz_escolares je
        INNER JOIN prueba.mae_aju_institucion_educativas ie ON je.c_institucion_id = ie.c_institucion_id
        INNER JOIN prueba.mae_aju_ugeles ug ON ie.c_ugel_id = ug.c_ugel_id
        INNER JOIN prueba.mae_aju_distrito_judiciales dj ON ug.c_distrito_jud_id = dj.c_distrito_jud_id
        WHERE je.l_activo = '1'
        
        -- FILTROS
        AND (:corte IS NULL OR ug.c_distrito_jud_id = :corte)
        AND (:ugel IS NULL OR ie.c_ugel_id = :ugel)
        AND (:colegio IS NULL OR je.c_institucion_id = :colegio)
        
        -- BUSCADOR
        AND (
            :search IS NULL OR :search = '' OR
            UPPER(je.c_dni) LIKE UPPER(CONCAT('%', :search, '%')) OR
            UPPER(je.x_nombre) LIKE UPPER(CONCAT('%', :search, '%')) OR
            UPPER(je.x_ape_paterno) LIKE UPPER(CONCAT('%', :search, '%')) OR
            UPPER(je.x_res_acreditacion) LIKE UPPER(CONCAT('%', :search, '%'))
        )
        ORDER BY je.f_registro DESC
    """, countQuery = """
        SELECT count(*) 
        FROM prueba.mae_aju_juez_paz_escolares je
        INNER JOIN prueba.mae_aju_institucion_educativas ie ON je.c_institucion_id = ie.c_institucion_id
        INNER JOIN prueba.mae_aju_ugeles ug ON ie.c_ugel_id = ug.c_ugel_id
        INNER JOIN prueba.mae_aju_distrito_judiciales dj ON ug.c_distrito_jud_id = dj.c_distrito_jud_id
        WHERE je.l_activo = '1'
        AND (:corte IS NULL OR ug.c_distrito_jud_id = :corte)
        AND (:ugel IS NULL OR ie.c_ugel_id = :ugel)
        AND (:colegio IS NULL OR je.c_institucion_id = :colegio)
        AND (
            :search IS NULL OR :search = '' OR
            UPPER(je.c_dni) LIKE UPPER(CONCAT('%', :search, '%')) OR
            UPPER(je.x_nombre) LIKE UPPER(CONCAT('%', :search, '%')) OR
            UPPER(je.x_ape_paterno) LIKE UPPER(CONCAT('%', :search, '%'))
        )
    """, nativeQuery = true)
    Page<JuezProjection> listar(
            @Param("search") String search,
            @Param("corte") String corteId,
            @Param("ugel") String ugelId,
            @Param("colegio") String colegioId,
            Pageable pageable);

    interface JuezProjection {
        String getId();
        String getDni();
        String getNombres();
        String getApePaterno();
        String getApeMaterno();
        String getCargo();
        String getCorteNombre();
        String getUgelNombre();
        String getColegioNombre();
    }
}