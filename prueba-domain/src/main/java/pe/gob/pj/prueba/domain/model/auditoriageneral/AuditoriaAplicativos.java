package pe.gob.pj.prueba.domain.model.auditoriageneral;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.model.Auditoria;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@EqualsAndHashCode(callSuper = true)
public class AuditoriaAplicativos extends Auditoria {

  Long id;

  LocalDateTime fechaRegistro;

  String uri;

  String peticionUrl;

  String peticionBody;

  String usuarioAplicativo;

  String usuarioAuth;

  String ips;

  String codigoUnicoOperacion;

  String codigoRespuesta;

  String descripcionRespuesta;

  Long duracionRespuesta;

  String herramientaConsume;

}
