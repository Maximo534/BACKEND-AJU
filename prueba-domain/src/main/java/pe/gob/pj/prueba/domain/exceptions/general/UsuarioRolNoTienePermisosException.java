package pe.gob.pj.prueba.domain.exceptions.general;

import org.springframework.security.access.AccessDeniedException;

public class UsuarioRolNoTienePermisosException extends AccessDeniedException {

  private static final long serialVersionUID = 1L;

  public UsuarioRolNoTienePermisosException(String msg) {
    super(msg);
  }

}
