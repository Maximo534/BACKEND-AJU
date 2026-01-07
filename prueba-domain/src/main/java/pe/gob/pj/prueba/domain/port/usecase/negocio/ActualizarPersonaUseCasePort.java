package pe.gob.pj.prueba.domain.port.usecase.negocio;

import pe.gob.pj.prueba.domain.model.negocio.Persona;

/**
 * @author oruizb
 * @version 1.0,07/02/2022
 * 
 */
public interface ActualizarPersonaUseCasePort {

  /**
   * 
   * Método que permite enviar los datos encapsulados en el modelo Persona para ser actualizados
   * aplicando reglas propias del negocio
   * 
   * @param cuo Código unico de operación
   * @param persona Modelo que contiene los atributos de una persona que se quieren guardar
   *        incluyendo el identificador
   * @throws Exception
   */
  public void actualizarPersona(String cuo, Persona persona);

}
