package pe.gob.pj.prueba.domain.port.persistence.negocio;

import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.JpeCasoAtendido;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarJpeCasosQuery;

import java.util.List;

public interface JusticiaPazPersistencePort {

    Pagina<JpeCasoAtendido> listar(String cuo, ListarJpeCasosQuery query, int pagina, int tamanio);

    JpeCasoAtendido guardar(String cuo, JpeCasoAtendido dominio);

    JpeCasoAtendido actualizar(String cuo, JpeCasoAtendido dominio);

    JpeCasoAtendido obtenerPorId(String cuo, Long id);

    String obtenerUltimoCodigo(String cuo, Long distritoId, String anio);

    List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;
}