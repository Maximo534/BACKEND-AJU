package pe.gob.pj.prueba.domain.exceptions.negocio;

import java.io.Serial;
import java.io.Serializable;

public class MovimientoNoEncontradoException extends RuntimeException implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  public MovimientoNoEncontradoException(String mensaje) {
    super(mensaje);
  }

  public MovimientoNoEncontradoException() {
    super("El movimiento solicitado no existe o no se encuentra activo.");
  }
}