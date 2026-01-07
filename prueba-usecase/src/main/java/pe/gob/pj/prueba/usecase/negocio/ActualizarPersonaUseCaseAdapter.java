package pe.gob.pj.prueba.usecase.negocio;

import java.sql.SQLException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.model.negocio.Persona;
import pe.gob.pj.prueba.domain.port.persistence.negocio.PersonaWritePersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.ActualizarPersonaUseCasePort;

/**
 * 
 * @author oruizb
 * @version 1.0,31/01/2025
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActualizarPersonaUseCaseAdapter implements ActualizarPersonaUseCasePort {

  PersonaWritePersistencePort personaWritePersistencePort;

  @Override
  @Transactional(transactionManager = "txManagerNegocio", propagation = Propagation.REQUIRES_NEW,
      readOnly = false, rollbackFor = {Exception.class, SQLException.class})
  public void actualizarPersona(String cuo, Persona persona) {
    personaWritePersistencePort.actualizarPersona(cuo, persona);
  }

}
