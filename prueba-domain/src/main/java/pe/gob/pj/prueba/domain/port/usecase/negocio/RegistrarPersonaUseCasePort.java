package pe.gob.pj.prueba.domain.port.usecase.negocio;

import pe.gob.pj.prueba.domain.model.negocio.Persona;


/**
 * @author oruizb
 * @version 1.0,07/02/2022
 * 
 */
public interface RegistrarPersonaUseCasePort {
  
  /**
   * 
   * Método que permite enviar los datos encapsulados en el modelo Persona para ser guardados
   * aplicando reglas propias del negocio
   * 
   * @param cuo String código unico de operación
   * @param persona Persona modelo que contiene los atributos de una persona que se quieren guardar
   */
  public Integer registrarPersona(String cuo, Persona persona);
  
}
