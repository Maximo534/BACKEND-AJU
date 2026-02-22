package pe.gob.pj.accesojusticia.domain.exceptions.general;

public class UsuarioTokenNoValidoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public UsuarioTokenNoValidoException(String file) {
    super(file);
  }

}
