package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import java.util.List;
import org.springframework.stereotype.Component;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.model.negocio.Persona;
import pe.gob.pj.prueba.domain.model.negocio.query.ConsultarPersonaQuery;
import pe.gob.pj.prueba.domain.port.persistence.negocio.PersonaReadPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovPersonaRepository;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PersonaReadPersistenceAdapter implements PersonaReadPersistencePort {

  MovPersonaRepository movPersonaRepository;

  /**
   * TODO Agreagr paginación en la respuesta de la búsqueda
   * <p>
   * Se debe realizar la paginación antes de devolver el resultado (01/01/2025) Ricardo Ruiz
   */
  @Override
  public List<Persona> buscarPersona(String cuo, ConsultarPersonaQuery filters) {
    return movPersonaRepository.buscarPersona(filters);
  }

}
