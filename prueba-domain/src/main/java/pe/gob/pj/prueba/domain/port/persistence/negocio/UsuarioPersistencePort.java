package pe.gob.pj.prueba.domain.port.persistence.negocio;

import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.Usuario;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarUsuarioQuery;

public interface UsuarioPersistencePort {

    Pagina<Usuario> listar(String cuo, ListarUsuarioQuery query, int pagina, int tamanio);

    Usuario registrar(String cuo, Usuario usuario);

    Usuario actualizar(String cuo, Usuario usuario);

    Usuario buscarPorId(String cuo, Integer id);

    boolean existeUsuarioPorLogin(String cuo, String login);

    void cambiarEstado(String cuo, Usuario usuario);
}