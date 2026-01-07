package pe.gob.pj.prueba.domain.port.persistence.negocio;

import pe.gob.pj.prueba.domain.model.negocio.Persona;

/**
 * @author oruizb
 * @version 1.0,07/02/2022
 */
public interface PersonaWritePersistencePort {

  /**
   * 
   * Método que permite registrar entidad MovPersona con los datos encapsulados en el modelo Persona
   * 
   * @param cuo Código unico de operación
   * @param persona Modelo que contiene los atributos de una persona que se quieren guardar
   */
  public Integer registrarPersona(String cuo, Persona persona);

  /**
   * 
   * Método que permite actualizar entidad MovPersona con los datos encapsulados en el modelo
   * Persona
   * 
   * @param cuo Código unico de operación
   * @param persona Modelo que contiene los atributos de una persona que se quieren guardar
   *        incluyendo el identificador
   */
  public void actualizarPersona(String cuo, Persona persona);

}
