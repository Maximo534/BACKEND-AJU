package pe.gob.pj.prueba.usecase.seguridad;

import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.model.seguridad.Rol;
import pe.gob.pj.prueba.domain.model.seguridad.Usuario;
import pe.gob.pj.prueba.domain.port.persistence.seguridad.SeguridadPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.seguridad.ObtenerInfoClienteUseCasePort;

/**
 * 
 * @author oruizb
 * @version 1.0,31/01/2025
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ObtenerInfoClienteUseCaseAdapter implements ObtenerInfoClienteUseCasePort {

  SeguridadPersistencePort seguridadPersistencePort;

  @Override
  @Transactional(transactionManager = "txManagerSeguridad", propagation = Propagation.REQUIRED,
      readOnly = true, rollbackFor = {Exception.class, SQLException.class})
  public Usuario recuperaInfoUsuario(String cuo, String id) {
    return seguridadPersistencePort.recuperaInfoUsuario(cuo, id);
  }

  @Override
  @Transactional(transactionManager = "txManagerSeguridad", propagation = Propagation.REQUIRED,
      readOnly = true, rollbackFor = {Exception.class, SQLException.class})
  public List<Rol> recuperarRoles(String cuo, String id) {
    return seguridadPersistencePort.recuperarRoles(cuo, id);
  }

}
