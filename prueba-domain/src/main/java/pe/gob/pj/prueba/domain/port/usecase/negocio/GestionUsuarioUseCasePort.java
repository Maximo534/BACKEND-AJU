package pe.gob.pj.prueba.domain.port.usecase.negocio;

import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.Usuario;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarUsuarioQuery;

public interface GestionUsuarioUseCasePort {

    Pagina<Usuario> listar(String cuo, ListarUsuarioQuery query, int pagina, int tamanio);

    Usuario registrar(String cuo, Usuario usuario);

    Usuario actualizar(String cuo, Usuario usuario);

    Usuario buscarPorId(String cuo, Integer id);

    boolean verificarDisponibilidadLogin(String cuo, String login);

    void cambiarEstado(String cuo, Usuario usuario);

    void resetearClave(String cuo, Integer idUsuarioObjetivo, String rolOperador, String loginOperador);

    void cambiarContrasenaPropia(String cuo, String loginUsuario, String claveActual, String nuevaClave, String confirmacion);
}