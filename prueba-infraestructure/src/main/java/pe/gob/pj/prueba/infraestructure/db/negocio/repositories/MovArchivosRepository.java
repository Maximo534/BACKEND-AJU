package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivosEntity;
import java.util.List;

public interface MovArchivosRepository extends JpaRepository<MovArchivosEntity, String> {

    List<MovArchivosEntity> findByNumeroIdentificacion(String numeroIdentificacion);

    @Query(value = """
    SELECT a.x_ruta AS ruta, a.x_nombre AS nombre
    FROM prueba.mov_aju_archivos a  -- Verifica si es mov_archivos o mov_aju_archivos
    INNER JOIN prueba.mov_aju_justicia_itinerantes ji ON a.c_num_identif = ji.c_just_itin_id -- Verifica c_num_identif vs c_num_identificacion
    WHERE 
      ji.l_activo = '1' 
      AND a.x_tipo = :tipoArchivo  -- CORREGIDO: Usualmente es x_tipo según tu entity
      AND EXTRACT(YEAR FROM ji.f_inicio) = :anio
      AND EXTRACT(MONTH FROM ji.f_inicio) = :mes
""", nativeQuery = true)
    List<ArchivoDescargaProjection> listarParaDescargaMasiva(
            @Param("tipoArchivo") String tipoArchivo,
            @Param("anio") Integer anio,
            @Param("mes") Integer mes
    );

    interface ArchivoDescargaProjection {
        String getRuta();
        String getNombre();
    }
}