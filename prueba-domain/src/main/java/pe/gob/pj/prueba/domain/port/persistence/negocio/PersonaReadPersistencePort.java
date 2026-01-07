package pe.gob.pj.prueba.domain.port.persistence.negocio;

import java.util.List;
import pe.gob.pj.prueba.domain.model.negocio.Persona;
import pe.gob.pj.prueba.domain.model.negocio.query.ConsultarPersonaQuery;

/**
 * @author oruizb
 * @version 1.0,07/02/2022
 */
public interface PersonaReadPersistencePort {

  /**
   * 
   * Método que permite obtener una lista de entidad MovPersona y encapsularlo en una lista del
   * modelo Persona, las cuales coinciden con los filtros enviados.
   * 
   * @param cuo Código unico de operación
   * @param filters Lista de clave valor, donde la clave son los parámetros declarados en el modelo
   *        Persona
   * @return Lista del modelo Persona que coinciden con los filtros enviados
   */
  public List<Persona> buscarPersona(String cuo, ConsultarPersonaQuery filters);

}
