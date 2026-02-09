package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.domain.model.negocio.PerfilUsuario;
import pe.gob.pj.prueba.domain.model.negocio.Usuario;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarUsuarioQuery; // IMPORTANTE
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovUsuarioEntity;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarUsuarioRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarUsuarioRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.UsuarioResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.UsuarioSesionResponse;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UsuarioMapper {


  // Mapeo de Filtros REST a Query de Dominio
  @Mapping(target = "idUsuario", source = "idUsuario")
  @Mapping(target = "usuario", source = "usuario")
  @Mapping(target = "nombreCompleto", source = "nombreCompleto")
  @Mapping(target = "activo", source = "activo")
  ListarUsuarioQuery toQuery(ListarUsuarioRequest request);

  // --- REST -> DOMINIO ENTIDAD ---
  @Mapping(target = "id", source = "request.id")
  @Mapping(target = "nombreUsuario", source = "request.usuario")
  @Mapping(target = "activo", ignore = true)
  @Mapping(target = "perfiles", source = "request.idPerfil", qualifiedByName = "mapPerfilUnico")
  // Auditoría
  @Mapping(target = "usuario", source = "peticion.usuarioAuth")
  @Mapping(target = "nombrePc", source = "peticion.nombrePc")
  @Mapping(target = "direccionMac", source = "peticion.codigoMac")
  @Mapping(target = "numeroIp", source = "peticion.ip")
  @Mapping(target = "red", source = "peticion.red")
  @Mapping(target = "idEje", source = "request.idEje")
  Usuario toUsuario(RegistrarUsuarioRequest request, PeticionServicios peticion);

  @Named("mapPerfilUnico")
  default List<PerfilUsuario> mapPerfilUnico(Integer idPerfil) {
    if (idPerfil == null) return new ArrayList<>();

    PerfilUsuario perfil = new PerfilUsuario();
    perfil.setIdPerfil(idPerfil);

    List<PerfilUsuario> lista = new ArrayList<>();
    lista.add(perfil);
    return lista;
  }

  // --- DOMINIO -> REST ---
  @Mapping(target = "token", ignore = true)
  @Mapping(target = "usuario", source = "nombreUsuario")
  UsuarioResponse toUsuarioResponse(Usuario usuario);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "usuario", source = "nombreUsuario")
  @Mapping(target = "nombreCompleto", source = "nombreCompleto")
  @Mapping(target = "nombreDistritoJudicial", source = "nombreDistritoJudicial")
  @Mapping(target = "nombreInstancia", source = "nombreInstancia")

  @Mapping(target = "rutaFoto", ignore = true) // Ignorar deja el valor en null
  @Mapping(target = "nomFoto", ignore = true)
  @Mapping(target = "perfiles", expression = "java(null)")
  @Mapping(target = "token", ignore = true)

  @Mapping(target = "activo", source = "activo")
  UsuarioResponse toResponseListado(Usuario domain);

  // --- PERSISTENCIA ---
  @Mapping(target = "nombreUsuario", source = "usuario")
  @Mapping(target = "usuario", source = "CAudId")
  @Mapping(target = "red", source = "CAudIdRed")
  @Mapping(target = "nombrePc", source = "CAudPc")
  @Mapping(target = "numeroIp", source = "CAudIp")
  @Mapping(target = "direccionMac", source = "CAudMcAddr")
  Usuario toUsuario(MovUsuarioEntity entity);

  @Mapping(target = "usuario", source = "nombreUsuario")
  @Mapping(target = "CAudId", source = "usuario")
  @Mapping(target = "CAudIdRed", source = "red")
  @Mapping(target = "CAudPc", source = "nombrePc")
  @Mapping(target = "CAudIp", source = "numeroIp")
  @Mapping(target = "CAudMcAddr", source = "direccionMac")
  @Mapping(target = "FAud", ignore = true)
  @Mapping(target = "BAud", ignore = true)
  MovUsuarioEntity toEntity(Usuario domain);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "usuario", ignore = true)
  @Mapping(target = "CAudId", source = "usuario")
  @Mapping(target = "CAudIdRed", source = "red")
  @Mapping(target = "CAudPc", source = "nombrePc")
  @Mapping(target = "CAudIp", source = "numeroIp")
  @Mapping(target = "CAudMcAddr", source = "direccionMac")
  @Mapping(target = "FAud", ignore = true)
  @Mapping(target = "BAud", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntity(Usuario domain, @MappingTarget MovUsuarioEntity entity);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "activo", source = "activo")
  @Mapping(target = "usuario", source = "peticion.usuarioAuth")
  @Mapping(target = "nombrePc", source = "peticion.nombrePc")
  @Mapping(target = "direccionMac", source = "peticion.codigoMac")
  @Mapping(target = "numeroIp", source = "peticion.ip")
  @Mapping(target = "red", source = "peticion.red")
  @Mapping(target = "nombreUsuario", ignore = true)
  @Mapping(target = "clave", ignore = true)
  @Mapping(target = "perfiles", ignore = true)
  Usuario toUsuarioEstado(Integer id, String activo, PeticionServicios peticion);

  // =========================================================
  // MAPEO PARA DATOS DE SESIÓN
  // =========================================================
  @Mapping(target = "usuario", source = "nombreUsuario")
  @Mapping(target = "nombreCompleto", source = "nombreCompleto")
  @Mapping(target = "cargo", source = "cargo")

  // --- DISTRITO JUDICIAL ---
  @Mapping(target = "nombreDistritoJudicial", source = "nombreDistritoJudicial")
  @Mapping(target = "idDistritoJudicial", source = "idDistritoJudicial")

  // --- EJE ---
  @Mapping(target = "eje", source = "nombreInstancia")
  @Mapping(target = "idEje", source = "idEje")
  UsuarioSesionResponse toSesionResponse(Usuario domain);
}