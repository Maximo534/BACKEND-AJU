package pe.gob.pj.accesojusticia.domain.port.persistence.negocio;

import pe.gob.pj.accesojusticia.domain.model.negocio.Documento;

import java.util.List;

public interface DocumentoPersistencePort {


    List<Documento> listarPorTipo(String cuo, String tipo);

    Documento guardar(String cuo, Documento dominio);

    Documento actualizar(String cuo, Documento dominio);

    Documento buscarPorId(String cuo, Long id);
}