package pe.gob.pj.accesojusticia.infraestructure.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import pe.gob.pj.accesojusticia.domain.model.negocio.PerfilOpcions;
import pe.gob.pj.accesojusticia.infraestructure.rest.responses.OpcionesPerfilResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OpcionMapper {


  @Mapping(target = "token", ignore = true)
  OpcionesPerfilResponse toOpcionesPerfilResponse(PerfilOpcions opciones);

}
