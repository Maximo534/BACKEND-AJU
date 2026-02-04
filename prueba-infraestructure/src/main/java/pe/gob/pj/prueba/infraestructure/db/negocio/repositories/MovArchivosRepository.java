package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivoEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovArchivosRepository extends JpaRepository<MovArchivoEntity, Long> { // 1. ID es Long

    // Para buscar por nombre (usado al eliminar o descargar)
    // Agregamos 'AndActivo' para traer solo vigentes
    Optional<MovArchivoEntity> findByNombreAndActivo(String nombre, String activo);

    // Para auditoría (trae incluso los eliminados lógicamente)
    Optional<MovArchivoEntity> findByNombre(String nombre);

    // Para listar los archivos de un evento específico
    // Busca por el CÓDIGO (String) de enlace
    List<MovArchivoEntity> findByNumeroIdentificacionAndActivo(String numeroIdentificacion, String activo);

    // Consulta Nativa para Reportes Masivos
    @Query(value = """
    SELECT 
        a.x_ruta AS ruta, 
        a.x_nombre AS nombre
    FROM acjust.mov_archivo a             
    INNER JOIN acjust.mov_justicia_itinerante ji 
        ON a.c_num_identif = ji.c_codigo    
    WHERE 
      ji.l_activo = '1' 
      AND a.l_activo = '1'                
      AND a.x_tipo = :tipoArchivo 
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