package pe.gob.pj.prueba.usecase.negocio;

import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.model.negocio.Persona;
import pe.gob.pj.prueba.domain.model.negocio.query.ConsultarPersonaQuery;
import pe.gob.pj.prueba.domain.port.persistence.negocio.PersonaReadPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.ObtenerPersonaUseCasePort;

/**
 * 
 * @author oruizb
 * @version 1.0,31/01/2025
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ObtenerPersonaUseCaseAdapter implements ObtenerPersonaUseCasePort {

  PersonaReadPersistencePort personaReadPersistencePort;

  @Override
  @Transactional(transactionManager = "txManagerNegocio", propagation = Propagation.REQUIRES_NEW,
      readOnly = true, rollbackFor = {Exception.class, SQLException.class})
  public List<Persona> buscarPersona(String cuo, ConsultarPersonaQuery query) {
    return personaReadPersistencePort.buscarPersona(cuo, query);
  }

}
