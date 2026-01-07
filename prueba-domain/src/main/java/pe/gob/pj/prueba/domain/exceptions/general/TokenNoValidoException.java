package pe.gob.pj.prueba.domain.exceptions.general;

public class TokenNoValidoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public TokenNoValidoException(String msg) {
    super(msg);
  }

}
