package pe.gob.pj.prueba.usecase.negocio;

import java.sql.SQLException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.exceptions.negocio.OpcionesNoAsignadadException;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.domain.model.negocio.PerfilOpcions;
import pe.gob.pj.prueba.domain.port.persistence.negocio.AccesoPersistenceReadPort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.ConsultarOpcionesPerfilUseCasePort;

/**
 * 
 * @author oruizb
 * @version 1.0,31/01/2025
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ConsultarOpcionesPerfilUseCaseAdapter implements ConsultarOpcionesPerfilUseCasePort{

  AccesoPersistenceReadPort accesoPersistencePort;

  @Override
  @Transactional(transactionManager = "txManagerNegocio", propagation = Propagation.REQUIRES_NEW,
      readOnly = true, rollbackFor = {Exception.class, SQLException.class})
  public PerfilOpcions obtenerOpciones(String cuo, String usuario, Integer idPerfil,
      PeticionServicios peticion) {
    var perfilOpciones = accesoPersistencePort.obtenerOpciones(cuo, idPerfil);
    if (perfilOpciones.getOpciones().isEmpty()) {
      throw new OpcionesNoAsignadadException();
    }
    return perfilOpciones;
  }

}
