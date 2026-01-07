package pe.gob.pj.prueba.domain.port.usecase.seguridad;

import pe.gob.pj.prueba.domain.model.seguridad.query.AutenticacionUsuarioQuery;

/**
 * @author oruizb
 * @version 1.0,07/02/2022
 * 
 */
public interface AutenticarClienteUseCasePort {
  
  /**
   * Método que representa el caso de uso de autenticacion de usuario de petición
   * 
   * @param cuo String código único de operación
   * @param query AutenticacionUsuarioQuery dto que envuelve los criterios de autenticación
   * @return String identificador de usuario
   */
  public String autenticarUsuario(String cuo, AutenticacionUsuarioQuery query);

}
