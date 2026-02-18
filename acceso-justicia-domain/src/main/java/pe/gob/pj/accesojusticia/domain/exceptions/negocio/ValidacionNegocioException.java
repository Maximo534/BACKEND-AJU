package pe.gob.pj.accesojusticia.domain.exceptions.negocio;

import java.io.Serial;
import java.io.Serializable;

public class ValidacionNegocioException extends RuntimeException implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  public ValidacionNegocioException(String mensaje) {
    super(mensaje);
  }

  public ValidacionNegocioException() {
    super("El movimiento solicitado no existe o no se encuentra activo.");
  }
}
