package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import org.springframework.stereotype.Component;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.common.enums.Estado;
import pe.gob.pj.prueba.domain.common.enums.Formatos;
import pe.gob.pj.prueba.domain.common.utils.ProjectUtils;
import pe.gob.pj.prueba.domain.exceptions.negocio.TipoDocumentoNoExisteException;
import pe.gob.pj.prueba.domain.model.negocio.Persona;
import pe.gob.pj.prueba.domain.port.persistence.negocio.PersonaWritePersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MaeTipoDocumentoPersonaEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovPersonaEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MaeTipoDocumentoRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovPersonaRepository;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PersonaWritePersistenceAdapter implements PersonaWritePersistencePort {

  MovPersonaRepository movPersonaRepository;
  MaeTipoDocumentoRepository maeTipoDocumentoRepository;

  @Override
  public Integer registrarPersona(String cuo, Persona persona) {
    var maeTipoDocumento = maeTipoDocumentoRepository.findById(persona.getIdTipoDocumento())
        .orElseThrow(TipoDocumentoNoExisteException::new);
    var movPersona = new MovPersonaEntity();
    movPersona.setTipoDocumento(maeTipoDocumento);
    movPersona.setNumeroDocumento(persona.getNumeroDocumento());
    movPersona.setPrimerApellido(persona.getPrimerApellido());
    movPersona.setSegundoApellido(persona.getSegundoApellido());
    movPersona.setNombres(persona.getNombres());
    movPersona.setSexo(persona.getSexo());
    movPersona.setCorreo(persona.getCorreo());
    movPersona.setTelefono(persona.getTelefono());
    movPersona.setFechaNacimiento(ProjectUtils.parseStringToDate(persona.getFechaNacimiento(),
        Formatos.FECHA_DD_MM_YYYY.getFormato()));
    movPersona.setActivo(!Estado.INACTIVO_NUMERICO.getNombre().equals(persona.getActivo())
        ? Estado.ACTIVO_NUMERICO.getNombre()
        : Estado.INACTIVO_NUMERICO.getNombre());
    
    movPersona.setCAudIp(persona.getNumeroIp());
    movPersona.setCAudId(persona.getUsuario());
    movPersona.setCAudPc(persona.getNombrePc());
    movPersona.setCAudMcAddr(persona.getDireccionMac());
    movPersona.setCAudIdRed(persona.getRed());
    
    movPersonaRepository.save(movPersona);
    
    return movPersona.getId();
  }

  @Override
  public void actualizarPersona(String cuo, Persona persona) {

    var movPersona2 = movPersonaRepository.findById(persona.getId());
    movPersona2.ifPresent(mov -> {
      if (!mov.getTipoDocumento().getCodigo().equals(persona.getIdTipoDocumento())) {
        var maeTipoDocumento = new MaeTipoDocumentoPersonaEntity();
        maeTipoDocumento.setCodigo(persona.getIdTipoDocumento());
        mov.setTipoDocumento(maeTipoDocumento);
      }
      mov.setNumeroDocumento(persona.getNumeroDocumento());
      mov.setPrimerApellido(persona.getPrimerApellido());
      mov.setSegundoApellido(persona.getSegundoApellido());
      mov.setNombres(persona.getNombres());
      mov.setSexo(persona.getSexo());
      mov.setCorreo(persona.getCorreo());
      mov.setTelefono(persona.getTelefono());
      mov.setFechaNacimiento(ProjectUtils.parseStringToDate(persona.getFechaNacimiento(),
          Formatos.FECHA_DD_MM_YYYY.getFormato()));
      mov.setActivo(!Estado.INACTIVO_NUMERICO.getNombre().equals(persona.getActivo())
          ? Estado.ACTIVO_NUMERICO.getNombre()
          : Estado.INACTIVO_NUMERICO.getNombre());
      
      mov.setCAudIp(persona.getNumeroIp());
      mov.setCAudId(persona.getUsuario());
      mov.setCAudPc(persona.getNombrePc());
      mov.setCAudMcAddr(persona.getDireccionMac());
      mov.setCAudIdRed(persona.getRed());
    });

  }

}
