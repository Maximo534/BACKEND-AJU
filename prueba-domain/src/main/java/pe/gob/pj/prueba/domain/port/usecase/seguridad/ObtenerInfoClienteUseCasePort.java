package pe.gob.pj.prueba.domain.port.usecase.seguridad;

import java.util.List;
import pe.gob.pj.prueba.domain.model.seguridad.Rol;
import pe.gob.pj.prueba.domain.model.seguridad.Usuario;

/**
 * @author oruizb
 * @version 1.0,07/02/2022
 * 
 */
public interface ObtenerInfoClienteUseCasePort {

  /**
   * Método que representa el caso de uso recuperar información de usuario de petición
   * 
   * @param cuo String código único de operación
   * @param id String identificador del usuario que
   * @return modelo de usuario
   */
  public Usuario recuperaInfoUsuario(String cuo, String id);

  /**
   * Método que representa el caso de uso de recuperación de roles
   * 
   * @param cuo String código único de operación
   * @param id String identificador del usuario que
   * @return colección de roles
   */
  public List<Rol> recuperarRoles(String cuo, String id);

}
