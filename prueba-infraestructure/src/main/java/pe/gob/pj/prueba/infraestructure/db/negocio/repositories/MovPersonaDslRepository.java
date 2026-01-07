package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import java.util.List;
import pe.gob.pj.prueba.domain.model.negocio.Persona;
import pe.gob.pj.prueba.domain.model.negocio.query.ConsultarPersonaQuery;

/**
 * 
 * Esta interfaz proporcion la manera de reazalizar busquedas dinamicas sobre la entidad mencionada
 * con QueryDsl
 * 
 * @author oruizb
 * @version 1.0,07/02/2022
 */
public interface MovPersonaDslRepository {
  List<Persona> buscarPersona(ConsultarPersonaQuery query);
}
