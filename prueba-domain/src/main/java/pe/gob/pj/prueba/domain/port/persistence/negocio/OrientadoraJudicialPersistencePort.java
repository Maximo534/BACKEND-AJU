package pe.gob.pj.prueba.domain.port.persistence.negocio;
import pe.gob.pj.prueba.domain.model.negocio.OrientadoraJudicial;

public interface OrientadoraJudicialPersistencePort {
    OrientadoraJudicial guardar(OrientadoraJudicial dominio) throws Exception;
    String obtenerUltimoId() throws Exception;

}