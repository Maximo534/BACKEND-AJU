package pe.gob.pj.prueba.domain.port.persistence.auditoriageneral;

import pe.gob.pj.prueba.domain.model.auditoriageneral.AuditoriaAplicativos;

/**
 * @author oruizb
 * @version 1.0,07/02/2022
 */
public interface AuditoriaGeneralReadPersistencePort {

  /**
   * 
   * Método que permite registrar trazabilidad de las peticiones y su respuesta
   * 
   * @param auditoriaAplicativos AuditoriaAplicativos encapsula los datos de la petición con sus
   *        respectivas auditorias
   * @throws Exception si no logra registrar la trazabilidad
   */
  public void crear(AuditoriaAplicativos auditoriaAplicativos) throws Exception;
}
