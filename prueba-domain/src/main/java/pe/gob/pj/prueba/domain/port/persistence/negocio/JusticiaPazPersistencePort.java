package pe.gob.pj.prueba.domain.port.persistence.negocio;

import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.JpeCasoAtendido;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;

import java.util.List;

public interface JusticiaPazPersistencePort {
    Pagina<JpeCasoAtendido> listar(String usuario, JpeCasoAtendido filtros, int pagina, int tamanio)throws Exception;
    String obtenerUltimoId() throws Exception;
    JpeCasoAtendido guardar(JpeCasoAtendido caso) throws Exception;
    JpeCasoAtendido buscarPorId(String id) throws Exception;
    JpeCasoAtendido actualizar(JpeCasoAtendido dominio) throws Exception;
    List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;

}
