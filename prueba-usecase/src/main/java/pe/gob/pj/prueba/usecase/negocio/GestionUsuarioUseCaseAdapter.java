package pe.gob.pj.prueba.usecase.negocio;

import java.sql.SQLException; // Importante
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation; // Importante
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.exceptions.negocio.AccesoDenegadoException;
import pe.gob.pj.prueba.domain.exceptions.negocio.UsuarioDuplicadoException;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.Usuario;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarUsuarioQuery;
import pe.gob.pj.prueba.domain.port.persistence.negocio.UsuarioPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionUsuarioUseCasePort;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestionUsuarioUseCaseAdapter implements GestionUsuarioUseCasePort {

    private final UsuarioPersistencePort persistencePort;

    private static final String TX_MANAGER = "txManagerNegocio";

    @Override
    @Transactional(transactionManager = TX_MANAGER,
            propagation = Propagation.REQUIRES_NEW,
            readOnly = true,
            rollbackFor = {Exception.class, SQLException.class})
    public Pagina<Usuario> listar(String cuo, ListarUsuarioQuery query, int pagina, int tamanio) {
        return persistencePort.listar(cuo, query, pagina, tamanio);
    }
    @Override
    @Transactional(transactionManager = TX_MANAGER,
            propagation = Propagation.REQUIRES_NEW,
            readOnly = false,
            rollbackFor = {Exception.class, SQLException.class})
    public Usuario registrar(String cuo, Usuario usuario) {

        // VALIDACIÓN DE DUPLICIDAD
        boolean yaExiste = persistencePort.existeUsuarioPorLogin(cuo, usuario.getNombreUsuario());

        if (yaExiste) {
            throw new UsuarioDuplicadoException("El usuario '" + usuario.getNombreUsuario() + "' ya está en uso.");
        }

        String loginCreador = usuario.getUsuario();

        //Obtener el ID del Perfil del Creador
        Integer idPerfilCreador = persistencePort.obtenerIdPerfilPorLogin(loginCreador);

        //Verificar permiso para cada perfil que se intenta asignar
        if (usuario.getPerfiles() != null) {
            for (var perfilNuevo : usuario.getPerfiles()) {

                // Consultamos: ¿El perfil 5 (Distrital) puede crear el perfil 3 (Juez)?
                boolean esJerarquiaValida = persistencePort.validarJerarquia(idPerfilCreador, perfilNuevo.getIdPerfil());
                if (!esJerarquiaValida) {
                    throw new AccesoDenegadoException(
                            "Su perfil no tiene permisos para crear usuarios con el rol ID: " + perfilNuevo.getIdPerfil());
                }
            }
        }

        // REGLAS DE NEGOCIO
        usuario.setId(null);
        usuario.setActivo("1");
        usuario.setClave("$2a$12$nk2Lr/n7D2ozTptLiWuF.uwg5QLTV8/2DMFAPBMgmUr1zfogYkjgy");

        return persistencePort.registrar(cuo, usuario);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER,
            propagation = Propagation.REQUIRES_NEW,
            readOnly = false,
            rollbackFor = {Exception.class, SQLException.class})
    public Usuario actualizar(String cuo, Usuario usuario) {
        if (usuario.getId() == null) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio para actualizar.");
        }
        return persistencePort.actualizar(cuo, usuario);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER,
            propagation = Propagation.REQUIRES_NEW,
            readOnly = true,
            rollbackFor = {Exception.class, SQLException.class})
    public Usuario buscarPorId(String cuo, Integer id) {
        Usuario encontrado = persistencePort.buscarPorId(cuo, id);
        return encontrado;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verificarDisponibilidadLogin(String cuo, String login) {

        boolean existe = persistencePort.existeUsuarioPorLogin(cuo, login);
        return !existe;
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER,
            propagation = Propagation.REQUIRES_NEW,
            readOnly = false,
            rollbackFor = {Exception.class, SQLException.class})
    public void cambiarEstado(String cuo, Usuario usuario) {

        if (usuario.getId() == null) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio.");
        }

        if (!"1".equals(usuario.getActivo()) && !"0".equals(usuario.getActivo())) {
            throw new IllegalArgumentException("El estado debe ser '1' (Activo) o '0' (Inactivo).");
        }

        persistencePort.cambiarEstado(cuo, usuario);
    }
}