package pe.gob.pj.prueba.domain.exceptions.general;

import org.springframework.security.core.AuthenticationException;

public class IpTokenDifiereIpEndpointException extends AuthenticationException {

  private static final long serialVersionUID = 1L;

  public IpTokenDifiereIpEndpointException(String msg) {
    super(msg);
  }

}
