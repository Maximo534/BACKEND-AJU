package pe.gob.pj.prueba.infraestructure.rest.controllers;

import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.domain.model.negocio.query.ConsultarPersonaQuery;
import pe.gob.pj.prueba.domain.port.usecase.auditoriageneral.AuditarPeticionUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.ActualizarPersonaUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.ObtenerPersonaUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.RegistrarPersonaUseCasePort;
import pe.gob.pj.prueba.infraestructure.mappers.AuditoriaGeneralMapper;
import pe.gob.pj.prueba.infraestructure.mappers.PersonaMapper;
import pe.gob.pj.prueba.infraestructure.rest.requests.PersonaRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.ConsultaPersonaResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.IdentificadorPersonaResponse;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class GestionPersonaController
    implements GestionPersona, GenerarHttpHeader, MonitorearRequest {

  ObtenerPersonaUseCasePort obtenerPersonaUseCasePort;
  RegistrarPersonaUseCasePort registrarPersonaUseCasePort;
  ActualizarPersonaUseCasePort actualizarPersonaUseCasePort;
  PersonaMapper personaMapper;

  @Getter
  AuditarPeticionUseCasePort auditoriaGeneralUseCasePort;
  @Getter
  AuditoriaGeneralMapper auditoriaGeneralMapper;
  @Getter
  ObjectMapper objectMaper;

  @Override
  public ResponseEntity<ConsultaPersonaResponse> consultarPersonas(PeticionServicios peticion,
      String formatoRespuesta, String numeroDocumento) {
    return ResponseEntity.ok().headers(getHttpHeader(formatoRespuesta))
        .body(new ConsultaPersonaResponse(peticion.getCuo(),
            personaMapper
                .toListPersonaResponse(obtenerPersonaUseCasePort.buscarPersona(peticion.getCuo(),
                    ConsultarPersonaQuery.builder().documentoIdentidad(numeroDocumento).build()))));
  }

  @Override
  public ResponseEntity<IdentificadorPersonaResponse> registrarPersona(PeticionServicios peticion,
      PersonaRequest request) {
    cargarTramaPeticion(peticion, request);
    var identificadorPersona = registrarPersonaUseCasePort.registrarPersona(peticion.getCuo(),
        personaMapper.toPersona(request, peticion));
    guardarAuditoria(Optional.ofNullable(peticion));
    return ResponseEntity.ok().headers(getHttpHeader(request.getFormatoRespuesta()))
        .body(new IdentificadorPersonaResponse(peticion.getCuo(), identificadorPersona));
  }

  @Override
  public ResponseEntity<IdentificadorPersonaResponse> actualizarPersona(PeticionServicios peticion,
      Integer id, PersonaRequest request) {

    var personaDto = personaMapper.toPersona(request, peticion);
    personaDto.setId(id);
    actualizarPersonaUseCasePort.actualizarPersona(peticion.getCuo(), personaDto);

    return ResponseEntity.ok().headers(getHttpHeader(request.getFormatoRespuesta()))
        .body(new IdentificadorPersonaResponse(peticion.getCuo(), id));
  }

}
