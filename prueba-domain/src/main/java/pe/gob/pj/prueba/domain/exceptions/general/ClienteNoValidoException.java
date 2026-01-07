package pe.gob.pj.prueba.domain.exceptions.general;

import org.springframework.security.core.AuthenticationException;

public class ClienteNoValidoException extends AuthenticationException {

  private static final long serialVersionUID = 1L;

  public ClienteNoValidoException(String msg) {
    super(msg);
  }

}
