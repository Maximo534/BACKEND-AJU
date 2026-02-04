package pe.gob.pj.prueba.domain.port.persistence.negocio;

import pe.gob.pj.prueba.domain.model.negocio.Archivo;

import java.util.List;

public interface GestionArchivosPersistencePort {
    void guardarReferenciaArchivo(Archivo archivoDomain) throws Exception;
    Archivo buscarPorId(Long id) throws Exception;
    Archivo buscarPorNombre(String nombre) throws Exception;
    void eliminarReferenciaPorId(Long id, String usuario, String ip, String pc, String mac) throws Exception;;
    List<Archivo> listarArchivosPorEvento(String codigoIdentificacion) throws Exception;

    List<Archivo> listarParaDescargaMasiva(String tipoArchivo, Integer anio, Integer mes) throws Exception;
}