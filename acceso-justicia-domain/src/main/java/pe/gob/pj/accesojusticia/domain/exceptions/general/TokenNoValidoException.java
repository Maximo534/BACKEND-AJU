package pe.gob.pj.accesojusticia.domain.exceptions.general;

public class TokenNoValidoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public TokenNoValidoException(String msg) {
    super(msg);
  }

}
