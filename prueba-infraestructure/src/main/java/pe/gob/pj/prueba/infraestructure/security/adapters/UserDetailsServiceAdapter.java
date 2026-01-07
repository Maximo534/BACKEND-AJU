package pe.gob.pj.prueba.infraestructure.security.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.common.enums.Estado;
import pe.gob.pj.prueba.domain.exceptions.general.ClienteNoValidoException;
import pe.gob.pj.prueba.domain.model.seguridad.RoleSecurity;
import pe.gob.pj.prueba.domain.model.seguridad.UserSecurity;
import pe.gob.pj.prueba.domain.port.usecase.seguridad.ObtenerInfoClienteUseCasePort;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserDetailsServiceAdapter implements UserDetailsService {

  ObtenerInfoClienteUseCasePort obtenerInfoClienteUseCasePort;
  PasswordEncoder passwordEncoder;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    var user = new UserSecurity();
    
    var clienteInfo = obtenerInfoClienteUseCasePort.recuperaInfoUsuario("", username);
    
    if (Objects.isNull(clienteInfo)
        || !Estado.ACTIVO_NUMERICO.getNombre().equalsIgnoreCase(clienteInfo.lActivo())) {
      log.error("Información del usuario [{}] es {}", username, Objects.toString(clienteInfo));
      throw new ClienteNoValidoException(String.format("Información del usuario [{}] es {}",
          username, Objects.toString(clienteInfo)));
    }
    
    user.setId(clienteInfo.id());
    user.setName(clienteInfo.cUsuario());
    user.setPassword(passwordEncoder.encode(clienteInfo.cClave()));
    
    var roles = new ArrayList<RoleSecurity>();
    var rolesB = obtenerInfoClienteUseCasePort.recuperarRoles("", username);
    rolesB.forEach(rol -> roles.add(new RoleSecurity(rol.id, rol.getCRol(), rol.getXNombre())));
    user.setRoles(roles);
    
    return new CustomUserDetails(user.getName(), user.getPassword(), getAuthorities(user), roles);
  }

  private Collection<? extends GrantedAuthority> getAuthorities(UserSecurity user) {
    String[] userRoles =
        user.getRoles().stream().map(RoleSecurity::getCodigo).toArray(String[]::new);
    return AuthorityUtils.createAuthorityList(userRoles);
  }

}
