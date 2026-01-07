package pe.gob.pj.prueba.usecase.seguridad;

import java.sql.SQLException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.model.seguridad.query.AutorizacionPeticionQuery;
import pe.gob.pj.prueba.domain.port.persistence.seguridad.SeguridadPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.seguridad.ValidarAutorizacionUseCasePort;

/**
 * 
 * @author oruizb
 * @version 1.0,31/01/2025
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ValidarAutorizacionUseCaseAdapter implements ValidarAutorizacionUseCasePort {

  SeguridadPersistencePort seguridadPersistencePort;

  @Override
  @Transactional(transactionManager = "txManagerSeguridad", propagation = Propagation.REQUIRED,
      readOnly = true, rollbackFor = {Exception.class, SQLException.class})
  public Optional<String> validarAccesoMetodo(String cuo, AutorizacionPeticionQuery query) {
    return seguridadPersistencePort.validarAccesoMetodo(cuo, query);
  }

}
