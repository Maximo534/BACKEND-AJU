package pe.gob.pj.accesojusticia.domain.exceptions.negocio;

import java.io.Serial;
import java.io.Serializable;

public class MaestroNoEncontradoException extends RuntimeException implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  public MaestroNoEncontradoException(String mensaje) {
    super(mensaje);
  }

  public MaestroNoEncontradoException() {
    super("El recurso maestro no fue encontrado.");
  }
}