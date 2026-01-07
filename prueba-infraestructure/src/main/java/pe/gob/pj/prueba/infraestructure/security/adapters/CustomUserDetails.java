package pe.gob.pj.prueba.infraestructure.security.adapters;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.model.seguridad.RoleSecurity;

@EqualsAndHashCode(callSuper = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomUserDetails extends User {

  private static final long serialVersionUID = 1L;

  List<RoleSecurity> roles;

  public CustomUserDetails(String username, String password,
      Collection<? extends GrantedAuthority> authorities, List<RoleSecurity> roles) {
    super(username, password, authorities);
    this.roles = roles;
  }

}
