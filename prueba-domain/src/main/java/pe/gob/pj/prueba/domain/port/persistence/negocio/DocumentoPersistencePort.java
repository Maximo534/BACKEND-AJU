package pe.gob.pj.prueba.domain.port.persistence.negocio;

import pe.gob.pj.prueba.domain.model.negocio.Documento;
import java.util.List;

public interface DocumentoPersistencePort {
    List<Documento> buscarPorTipoYActivo(String tipo, String activo);
    Documento guardar(Documento documento) throws Exception;
}