package pe.gob.pj.prueba.domain.port.persistence.negocio;
import pe.gob.pj.prueba.domain.model.negocio.LlapanchikpaqJusticia;

public interface LlapanchikpaqPersistencePort {
    LlapanchikpaqJusticia guardar(LlapanchikpaqJusticia dominio) throws Exception;
    String obtenerUltimoId() throws Exception;
}