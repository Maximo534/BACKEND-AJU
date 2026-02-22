package pe.gob.pj.accesojusticia.infraestructure.mappers;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import pe.gob.pj.accesojusticia.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.accesojusticia.domain.model.negocio.Persona;
import pe.gob.pj.accesojusticia.infraestructure.rest.requests.PersonaRequest;
import pe.gob.pj.accesojusticia.infraestructure.rest.responses.PersonaResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PersonaMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "tipoDocumento", ignore = true)
  @Mapping(target = "usuario", source = "peticion.usuario")
  @Mapping(target = "nombrePc", source = "peticion.nombrePc")
  @Mapping(target = "direccionMac", source = "peticion.codigoMac")
  @Mapping(target = "numeroIp", source = "peticion.ip")
  @Mapping(target = "red", source = "peticion.red")
  Persona toPersona(PersonaRequest personaRequest, PeticionServicios peticion);

  List<PersonaResponse> toListPersonaResponse(List<Persona> personas);

}
