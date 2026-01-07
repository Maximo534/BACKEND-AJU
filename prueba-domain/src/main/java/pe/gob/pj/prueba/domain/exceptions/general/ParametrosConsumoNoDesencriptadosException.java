package pe.gob.pj.prueba.domain.exceptions.general;

import org.springframework.security.core.AuthenticationException;

public class ParametrosConsumoNoDesencriptadosException extends AuthenticationException {

  private static final long serialVersionUID = 1L;

  public ParametrosConsumoNoDesencriptadosException(String msg) {
    super(msg);
  }

}
