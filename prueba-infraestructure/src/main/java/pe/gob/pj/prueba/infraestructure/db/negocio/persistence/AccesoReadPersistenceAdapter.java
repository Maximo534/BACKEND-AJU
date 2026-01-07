package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import java.util.Objects;
import org.springframework.stereotype.Component;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.common.enums.Estado;
import pe.gob.pj.prueba.domain.common.enums.Formatos;
import pe.gob.pj.prueba.domain.common.utils.ProjectUtils;
import pe.gob.pj.prueba.domain.model.negocio.Opcion;
import pe.gob.pj.prueba.domain.model.negocio.PerfilOpcions;
import pe.gob.pj.prueba.domain.model.negocio.PerfilUsuario;
import pe.gob.pj.prueba.domain.model.negocio.Usuario;
import pe.gob.pj.prueba.domain.port.persistence.negocio.AccesoPersistenceReadPort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MaeOpcionEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MaePerfilRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovUsuarioRepository;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AccesoReadPersistenceAdapter implements AccesoPersistenceReadPort {

  MovUsuarioRepository movUsuarioRepository;
  MaePerfilRepository maePerfilRepository;

  @Override
  public Usuario iniciarSesion(String cuo, String usuario) {
    Usuario usuarioDTO = new Usuario();
    movUsuarioRepository.findByActivoAndUsuario(Estado.ACTIVO_NUMERICO.getNombre(), usuario)
        .ifPresent(movUsuario -> {
          usuarioDTO.setIdUsuario(movUsuario.getId());
          usuarioDTO.setUsuario(movUsuario.getUsuario());
          usuarioDTO.setClave(movUsuario.getClave());

          usuarioDTO.getPersona().setId(movUsuario.getPersona().getId());
          usuarioDTO.getPersona().setPrimerApellido(movUsuario.getPersona().getPrimerApellido());
          usuarioDTO.getPersona().setSegundoApellido(movUsuario.getPersona().getSegundoApellido());
          usuarioDTO.getPersona().setNombres(movUsuario.getPersona().getNombres());
          usuarioDTO.getPersona().setNumeroDocumento(movUsuario.getPersona().getNumeroDocumento());
          usuarioDTO.getPersona().setTelefono(movUsuario.getPersona().getTelefono());
          usuarioDTO.getPersona().setCorreo(movUsuario.getPersona().getCorreo());
          usuarioDTO.getPersona()
              .setIdTipoDocumento(movUsuario.getPersona().getTipoDocumento().getCodigo());
          usuarioDTO.getPersona()
              .setTipoDocumento(movUsuario.getPersona().getTipoDocumento().getAbreviatura());
          usuarioDTO.getPersona().setFechaNacimiento(
              ProjectUtils.convertDateToString(movUsuario.getPersona().getFechaNacimiento(),
                  Formatos.FECHA_DD_MM_YYYY.getFormato()));
          usuarioDTO.getPersona().setSexo(movUsuario.getPersona().getSexo());
          usuarioDTO.getPersona().setActivo(movUsuario.getPersona().getActivo());

          movUsuario.getPerfils().forEach(perfilUsuario -> {
            if (perfilUsuario.getActivo().equalsIgnoreCase(Estado.ACTIVO_NUMERICO.getNombre())) {
              usuarioDTO.getPerfiles()
                  .add(new PerfilUsuario(perfilUsuario.getId(), perfilUsuario.getPerfil().getId(),
                      perfilUsuario.getPerfil().getNombre(), perfilUsuario.getPerfil().getRol()));
            }
          });
        });
    return usuarioDTO;
  }

  @Override
  public PerfilOpcions obtenerOpciones(String cuo, Integer idPerfil) {
    PerfilOpcions perfilOpciones = new PerfilOpcions();
    maePerfilRepository.findById(idPerfil).ifPresent(maePerfil -> {
      perfilOpciones.setRol(maePerfil.getRol());
      maePerfil.getPerfilsOpcion().forEach(x -> {
        if (x.getActivo().equalsIgnoreCase(Estado.ACTIVO_NUMERICO.getNombre())) {
          MaeOpcionEntity maeOpcion = x.getOpcion();
          Opcion opcion = new Opcion();
          opcion.setId(maeOpcion.getId());
          opcion.setCodigo(maeOpcion.getCodigo());
          opcion.setUrl(maeOpcion.getUrl());
          opcion.setIcono(maeOpcion.getIcono());
          opcion.setNombre(maeOpcion.getNombre());
          opcion.setOrden(maeOpcion.getOrden());
          opcion.setActivo(maeOpcion.getActivo());
          opcion.setIdOpcionSuperior(
              Objects.nonNull(maeOpcion.getOpcionSuperior()) ? maeOpcion.getOpcionSuperior().getId()
                  : null);
          opcion.setNombreOpcionSuperior(Objects.nonNull(maeOpcion.getOpcionSuperior())
              ? maeOpcion.getOpcionSuperior().getNombre()
              : null);
          perfilOpciones.getOpciones().add(opcion);
        }
      });
    });
    return perfilOpciones;
  }

}
