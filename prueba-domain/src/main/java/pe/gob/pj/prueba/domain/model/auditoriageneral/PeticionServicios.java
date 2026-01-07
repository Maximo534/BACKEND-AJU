package pe.gob.pj.prueba.domain.model.auditoriageneral;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/**
 * @author oruizb
 * @version 1.0,07/02/2022
 */
@Builder
@Getter @Setter
@ToString
public class PeticionServicios {

  String tipoMetodoHttp;
  String uri;
  String params;
  String usuarioAuth;
  String ips;
  String cuo;
  String herramienta;
  
  String ipPublica;

  String usuario;
  String ip;
  String nombrePc;
  String codigoMac;
  String red;
  
  String jwt;
  String peticionBody;
  String codigoRespuesta;
  String descripcionRespuesta;
  long inicio;
  long fin;
  
}
