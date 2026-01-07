package pe.gob.pj.prueba.domain.port.persistence.seguridad;

import java.util.List;
import java.util.Optional;
import pe.gob.pj.prueba.domain.model.seguridad.Rol;
import pe.gob.pj.prueba.domain.model.seguridad.Usuario;
import pe.gob.pj.prueba.domain.model.seguridad.query.AutenticacionUsuarioQuery;
import pe.gob.pj.prueba.domain.model.seguridad.query.AutorizacionPeticionQuery;

/**
 * @author oruizb
 * @version 1.0,07/02/2022
 */
public interface SeguridadPersistencePort {

  /**
   * Método para autenticar los datos del usuario enviado en el jwt
   * 
   * @param cuo String código unico de operación
   * @param query AutenticacionUsuarioQuery encapsula el usuario, clave, rol y cliente
   * @return identifcador de usuario
   */
  public String autenticarUsuario(String cuo, AutenticacionUsuarioQuery query);

  /**
   * Método para recuperar datos del usuario en base al identificador enviado
   * 
   * @param cuo String código unico de operación
   * @param id String identificador de usuario
   * @return usario que coincidio con con identificador
   */
  public Usuario recuperaInfoUsuario(String cuo, String id);

  /**
   * Obtener los roles del usuario que coincida con el identificador enviado
   * 
   * @param cuo String código unico de operación
   * @param id String identificador de usuario
   * @return lista de roles
   */
  public List<Rol> recuperarRoles(String cuo, String id);

  /**
   * Método para validar la autorización de usuario enviado en el token hacia el endpoint consumido
   * 
   * @param cuo String código unico de operación
   * @param query AutorizacionPeticionQuery clase que contiene los criterios para validar acceso
   * @return optional con el valor del endpoint si coincide o caso contrario optional vacio
   */
  public Optional<String> validarAccesoMetodo(String cuo, AutorizacionPeticionQuery query);
}
