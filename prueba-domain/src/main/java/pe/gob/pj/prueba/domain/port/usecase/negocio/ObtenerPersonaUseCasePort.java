package pe.gob.pj.prueba.domain.port.usecase.negocio;

import java.util.List;
import pe.gob.pj.prueba.domain.model.negocio.Persona;
import pe.gob.pj.prueba.domain.model.negocio.query.ConsultarPersonaQuery;

public interface ObtenerPersonaUseCasePort {
  
  /**
   * 
   * Método que permite enviar filtros para obtener una lista del modelo persona que cumplan con los
   * criterios enviados.
   * <p>
   * Los filtros enviados deben estar envueltas en una clase y el modelo persona obtenido se mapeara
   * al ConsultaPersonaResponse response de la petición.
   * 
   * @param cuo String Código unico de operación
   * @param query ConsultarPersonaQuery envuelve los criterios de búsqueda. No debe ser núlo.
   * @return Lista del modelo Persona que coinciden con los filtros enviados
   */
  public List<Persona> buscarPersona(String cuo, ConsultarPersonaQuery query);
  
}
