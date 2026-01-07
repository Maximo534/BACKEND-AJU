package pe.gob.pj.prueba.domain.port.usecase.negocio;

import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.domain.model.negocio.Usuario;
import pe.gob.pj.prueba.domain.model.negocio.query.IniciarSesionQuery;

/**
 * @author oruizb
 * @version 1.0,07/02/2022
 */
public interface IniciarSesionUseCasePort {

  /**
   * 
   * Método que permite enviar las credenciales para ser validadas y devolver un objeto con los
   * datos del usuario o lanza una ErrorException indicando que las credenciales no son válidas o el
   * usuario esta inactivo.
   * 
   * @param cuo Código unico de operación
   * @param usuario Usuario con el que se inicia sesión
   * @param clave Clave con la que se inicia sesión
   * @return Usuario
   */
  public Usuario iniciarSesion(String cuo, IniciarSesionQuery iniciarSesionQuery,
      PeticionServicios peticion);
  
}
