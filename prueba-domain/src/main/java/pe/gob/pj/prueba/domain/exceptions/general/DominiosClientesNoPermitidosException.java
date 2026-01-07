package pe.gob.pj.prueba.domain.exceptions.general;

import org.springframework.security.core.AuthenticationException;

public class DominiosClientesNoPermitidosException extends AuthenticationException {

  private static final long serialVersionUID = 1L;

  public DominiosClientesNoPermitidosException(String msg) {
    super(msg);
  }

}
