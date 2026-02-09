package pe.gob.pj.prueba.usecase.negocio;

import java.sql.SQLException; // Importante

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation; // Importante
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.exceptions.negocio.AccesoDenegadoException;
import pe.gob.pj.prueba.domain.exceptions.negocio.MovimientoNoEncontradoException;
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
    private static final String CLAVE_DEFAULT_TEXTO = "123456";
    private static final String ROL_SYSADMIN = "ADMAJUPJ";
    private final PasswordEncoder passwordEncoder;
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
    public Usuario registrar(String cuo, Usuario usuario, String login) {

        // VALIDACIÓN DE DUPLICIDAD
        boolean yaExiste = persistencePort.existeUsuarioPorLogin(cuo, usuario.getNombreUsuario());

        if (yaExiste) {
            throw new UsuarioDuplicadoException("El usuario '" + usuario.getNombreUsuario() + "' ya está en uso.");
        }

        log.info("[{}] AQUI:{}",login);

        //Obtener el ID del Perfil del Creador
        Integer idPerfilCreador = persistencePort.obtenerIdPerfilPorLogin(login);

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

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public void resetearClave(String cuo, Integer idUsuarioObjetivo, String rolOperador, String loginOperador) {

        Usuario usuarioObjetivo = persistencePort.buscarPorId(cuo, idUsuarioObjetivo);
        if (usuarioObjetivo == null) {
            throw new MovimientoNoEncontradoException("El usuario objetivo no existe.");
        }

        // LÓGICA DE PERMISOS
        // Validamos usando la constante del rol administrador correcta
        if (!ROL_SYSADMIN.equalsIgnoreCase(rolOperador)) {

            // --- BLOQUE DE VALIDACIÓN JERÁRQUICA ---

            Integer idPerfilOperador = persistencePort.obtenerIdPerfilPorLogin(loginOperador);
            Integer idPerfilObjetivo = persistencePort.obtenerIdPerfilPorIdUsuario(idUsuarioObjetivo);

            if (idPerfilObjetivo == null) {
                throw new AccesoDenegadoException("El usuario objetivo no tiene perfil activo. Solo un Administrador puede resetearlo.");
            }

            boolean tienePermiso = persistencePort.validarJerarquia(idPerfilOperador, idPerfilObjetivo);

            if (!tienePermiso) {
                throw new AccesoDenegadoException("Su rol no tiene jerarquía suficiente para resetear a este usuario.");
            }
        }


        String hashSeguro = passwordEncoder.encode(CLAVE_DEFAULT_TEXTO);

        persistencePort.actualizarClave(cuo, idUsuarioObjetivo, hashSeguro);

        log.info("[{}] Clave reseteada a valor por defecto para usuario ID: {} por operador: {}", cuo, idUsuarioObjetivo, loginOperador);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public void cambiarContrasenaPropia(String cuo, String loginUsuario, String claveActual, String nuevaClave, String confirmacion) {

        // Validar que las nuevas claves coincidan entre sí
        if (!nuevaClave.equals(confirmacion)) {
            throw new IllegalArgumentException("La nueva contraseña y su confirmación no coinciden.");
        }

        // Buscar al usuario por el Login del Token
        log.info("[{}] Intentando buscar usuario para cambio de clave. Login recibido del Token: '{}'", cuo, loginUsuario);

        Usuario usuario = persistencePort.buscarPorLogin(cuo, loginUsuario);
        if (usuario == null) {
            throw new MovimientoNoEncontradoException("Usuario no encontrado.");
        }

        //VALIDAR CLAVE ACTUAL
        if (!passwordEncoder.matches(claveActual, usuario.getClave())) {
            log.warn("[{}] Intento fallido de cambio de clave. La clave actual no coincide para el usuario: {}", cuo, loginUsuario);
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        // ENCRIPTAR NUEVA CLAVE
        String nuevoHash = passwordEncoder.encode(nuevaClave);

        // Actualizar en Base de Datos
        persistencePort.actualizarClave(cuo, usuario.getId(), nuevoHash);

        log.info("[{}] El usuario {} cambió su contraseña exitosamente.", cuo, loginUsuario);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public Usuario obtenerDatosSesion(String cuo, String login) {

        Usuario usuario = persistencePort.buscarPorLoginConDetalle(cuo, login);

        if (usuario == null) {
            throw new MovimientoNoEncontradoException("No se encontraron datos para el usuario de la sesión.");
        }

        return usuario;
    }
}