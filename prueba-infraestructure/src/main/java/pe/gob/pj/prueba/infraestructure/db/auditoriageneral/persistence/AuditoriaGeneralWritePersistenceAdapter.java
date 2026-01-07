package pe.gob.pj.prueba.infraestructure.db.auditoriageneral.persistence;

import org.springframework.stereotype.Component;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.model.auditoriageneral.AuditoriaAplicativos;
import pe.gob.pj.prueba.domain.port.persistence.auditoriageneral.AuditoriaGeneralReadPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.auditoriageneral.entities.MovAuditoriaAplicativosEntity;
import pe.gob.pj.prueba.infraestructure.db.auditoriageneral.repositories.MovAuditoriaAplicativosRespository;
import pe.gob.pj.prueba.infraestructure.mappers.AuditoriaGeneralMapper;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuditoriaGeneralWritePersistenceAdapter implements AuditoriaGeneralReadPersistencePort {

  AuditoriaGeneralMapper auditoriaGeneralMapper;
  MovAuditoriaAplicativosRespository movAuditoriaAplicativosRespository;

  @Override
  public void crear(AuditoriaAplicativos auditoriaAplicativos) throws Exception {
    MovAuditoriaAplicativosEntity mov =
        auditoriaGeneralMapper.toMovAuditoriaAplicativos(auditoriaAplicativos);
    movAuditoriaAplicativosRespository.save(mov);
  }

}
