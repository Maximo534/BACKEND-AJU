package pe.gob.pj.prueba.domain.port.usecase.seguridad;

import java.util.Optional;
import pe.gob.pj.prueba.domain.model.seguridad.query.AutorizacionPeticionQuery;

/**
 * @author oruizb
 * @version 1.0,07/02/2022
 * 
 */
public interface ValidarAutorizacionUseCasePort {

  /**
   * 
   * @param cuo String código único de operación
   * @param query AutorizacionPeticionQuery bean que contiene los criterios para validar acceso
   * @return optional del endpoint u optional vacio
   */
  public Optional<String> validarAccesoMetodo(String cuo, AutorizacionPeticionQuery query);

}
