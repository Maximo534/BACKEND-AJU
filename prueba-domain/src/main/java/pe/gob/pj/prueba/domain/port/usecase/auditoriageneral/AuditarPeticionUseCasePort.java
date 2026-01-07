package pe.gob.pj.prueba.domain.port.usecase.auditoriageneral;

import pe.gob.pj.prueba.domain.model.auditoriageneral.AuditoriaAplicativos;

/**
 * @author oruizb
 * @version 1.0,07/02/2022
 */
public interface AuditarPeticionUseCasePort {

  /**
   * Método que representa el caso de uso de guardar trazabilidad de las peticiones realizadas al
   * servicio
   * 
   * @param cuo String código unico de operación
   * @param auditoriaAplicativos AuditoriaAplicativos dto que encapsula lo datos de trazabilidad a
   *        guardar
   */
  public void crear(String cuo, AuditoriaAplicativos auditoriaAplicativos);
}
