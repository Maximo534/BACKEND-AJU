package pe.gob.pj.accesojusticia.infraestructure.rest.controllers;

import java.util.Optional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import pe.gob.pj.accesojusticia.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.accesojusticia.domain.port.usecase.auditoriageneral.AuditarPeticionUseCasePort;
import pe.gob.pj.accesojusticia.infraestructure.mappers.AuditoriaGeneralMapper;

public interface MonitorearRequest {

  AuditarPeticionUseCasePort getAuditoriaGeneralUseCasePort();
  AuditoriaGeneralMapper getAuditoriaGeneralMapper();
  ObjectMapper getObjectMaper();

  default void cargarTramaPeticion(PeticionServicios peticion, Object request) {
    try {
      peticion.setPeticionBody(getObjectMaper().writeValueAsString(request));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  default void guardarAuditoria(Optional<PeticionServicios> peticion) {
    peticion.ifPresent(p->{
      p.setFin(System.currentTimeMillis());
      getAuditoriaGeneralUseCasePort().crear(p.getCuo(),
          getAuditoriaGeneralMapper().toAuditoriaAplicativos(p));
    });
  }

}
