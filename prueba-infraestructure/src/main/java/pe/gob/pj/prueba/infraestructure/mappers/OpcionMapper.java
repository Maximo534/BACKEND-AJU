package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import pe.gob.pj.prueba.domain.model.negocio.PerfilOpcions;
import pe.gob.pj.prueba.infraestructure.rest.responses.OpcionesPerfilResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OpcionMapper {


  @Mapping(target = "token", ignore = true)
  OpcionesPerfilResponse toOpcionesPerfilResponse(PerfilOpcions opciones);

}
