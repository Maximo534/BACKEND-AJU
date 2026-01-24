package pe.gob.pj.prueba.usecase.negocio;

import java.sql.SQLException;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.exceptions.negocio.PersonaYaExisteException;
import pe.gob.pj.prueba.domain.exceptions.negocio.UsuarioNoEsDePoderJudicialExcepcion;
import pe.gob.pj.prueba.domain.model.negocio.Persona;
import pe.gob.pj.prueba.domain.model.negocio.query.ConsultarPersonaQuery;
//import pe.gob.pj.prueba.domain.port.client.consultasiga.ConsultaUsuarioEstadoPort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.PersonaReadPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.PersonaWritePersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.RegistrarPersonaUseCasePort;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RegistrarPersonaUseCaseAdapter implements RegistrarPersonaUseCasePort {

  PersonaWritePersistencePort personaWritePersistencePort;
  PersonaReadPersistencePort personaReadPersistencePort;
//  ConsultaUsuarioEstadoPort consultaUsuarioEstadoPort;

  @Override
  @Transactional(transactionManager = "txManagerNegocio", propagation = Propagation.REQUIRES_NEW,
      readOnly = false, rollbackFor = {Exception.class, SQLException.class})
  public Integer registrarPersona(String cuo, Persona persona) {

//    var personaSiga =
//        consultaUsuarioEstadoPort.consultarPorDocumento(cuo, persona.getNumeroDocumento());
//
//    if (Objects.isNull(personaSiga.getData())) {
//      throw new UsuarioNoEsDePoderJudicialExcepcion(persona.getUsuario());
//    }
//
    if (!personaReadPersistencePort.buscarPersona(cuo,
        ConsultarPersonaQuery.builder().documentoIdentidad(persona.getNumeroDocumento()).build())
        .isEmpty()) {
      throw new PersonaYaExisteException();
    }
    return personaWritePersistencePort.registrarPersona(cuo, persona);
  }

}
