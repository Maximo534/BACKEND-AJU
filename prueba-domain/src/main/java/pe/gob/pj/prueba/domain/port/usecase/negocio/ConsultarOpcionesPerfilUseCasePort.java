package pe.gob.pj.prueba.domain.port.usecase.negocio;

import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.domain.model.negocio.PerfilOpcions;

/**
 * @author oruizb
 * @version 1.0,07/02/2022
 */
public interface ConsultarOpcionesPerfilUseCasePort {

  /**
   * 
   * Método que permite enviar el identificador de perfil para ser validado si existe y devolver las
   * opciones asignadas a este perfil.
   * 
   * @param cuo Código unico de operación
   * @param idPerfil Identificador del perfil
   * @return PerfilOpcions que encapsula las opciones asignados al usuario y el rol del perfil
   */
  public PerfilOpcions obtenerOpciones(String cuo, String usuario, Integer idPerfil,
      PeticionServicios peticion);
  
}
