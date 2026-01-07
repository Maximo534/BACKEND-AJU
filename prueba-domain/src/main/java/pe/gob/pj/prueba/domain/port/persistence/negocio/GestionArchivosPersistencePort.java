package pe.gob.pj.prueba.domain.port.persistence.negocio;

import pe.gob.pj.prueba.domain.model.negocio.Archivo;

import java.util.List;

public interface GestionArchivosPersistencePort {
    void guardarReferenciaArchivo(Archivo archivo) throws Exception;
    Archivo buscarPorNombre(String nombre) throws Exception;
    void eliminarReferenciaArchivo(String nombre) throws Exception;
    List<Archivo> listarArchivosPorEvento(String idEvento) throws Exception;
}