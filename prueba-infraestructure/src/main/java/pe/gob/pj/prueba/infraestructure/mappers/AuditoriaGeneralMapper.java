package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import pe.gob.pj.prueba.domain.model.auditoriageneral.AuditoriaAplicativos;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.infraestructure.db.auditoriageneral.entities.MovAuditoriaAplicativosEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuditoriaGeneralMapper {

  @Mapping(target = "usuarioAplicativo", source = "peticion.usuario")
  @Mapping(target = "fechaRegistro", expression = "java(java.time.LocalDateTime.now())")
  @Mapping(target = "codigoUnicoOperacion", source = "peticion.cuo")
  @Mapping(target = "ips", source = "peticion.ips")
  @Mapping(target = "usuarioAuth", source = "peticion.usuarioAuth")
  @Mapping(target = "uri", source = "peticion.uri")
  @Mapping(target = "peticionUrl", source = "peticion.params")
  @Mapping(target = "herramientaConsume", source = "peticion.herramienta")
  @Mapping(target = "codigoRespuesta", source = "peticion.codigoRespuesta")
  @Mapping(target = "descripcionRespuesta", source = "peticion.descripcionRespuesta")
  @Mapping(target = "duracionRespuesta",
      expression = "java(peticion.getFin() - peticion.getInicio())")
  @Mapping(target = "peticionBody", source = "peticion.peticionBody")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "usuario", source = "peticion.usuario")
  @Mapping(target = "nombrePc", source = "peticion.nombrePc")
  @Mapping(target = "direccionMac", source = "peticion.codigoMac")
  @Mapping(target = "numeroIp", source = "peticion.ip")
  @Mapping(target = "red", source = "peticion.red")
  AuditoriaAplicativos toAuditoriaAplicativos(PeticionServicios peticion);

  @Mapping(target = "activo", ignore = true)
  @Mapping(target = "FAud", ignore = true)
  @Mapping(target = "BAud", ignore = true)
  @Mapping(target = "CAudId", source = "auditoriaAplicativos.usuario")
  @Mapping(target = "CAudIdRed", source = "auditoriaAplicativos.red")
  @Mapping(target = "CAudIp", source = "auditoriaAplicativos.numeroIp")
  @Mapping(target = "CAudPc", source = "auditoriaAplicativos.nombrePc")
  @Mapping(target = "CAudMcAddr", source = "auditoriaAplicativos.direccionMac")
  MovAuditoriaAplicativosEntity toMovAuditoriaAplicativos(
      AuditoriaAplicativos auditoriaAplicativos);

}
