package pe.gob.pj.prueba.domain.port.persistence.negocio;

import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.JuezPazEscolar;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarJuezEscolarQuery;

public interface JuezPazEscolarPersistencePort {

    Pagina<JuezPazEscolar> listar(String cuo, ListarJuezEscolarQuery query, int pagina, int tamanio);

    JuezPazEscolar guardar(String cuo, JuezPazEscolar dominio);

    JuezPazEscolar actualizar(String cuo, JuezPazEscolar dominio);

    JuezPazEscolar obtenerPorId(String cuo, Long id);

    // Métodos de validación y correlativo
    boolean existeDniEnColegio(String dni, Long colegioId);

    String obtenerUltimoCodigo(String cuo, String anio);
}