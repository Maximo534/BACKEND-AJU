package pe.gob.pj.prueba.domain.model.client.consultasiga.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.model.client.GlobalClienteResponse;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Setter
@Getter
public class ConsultaPersonalEstadoResponse extends GlobalClienteResponse {

  Data data;

  @FieldDefaults(level = AccessLevel.PRIVATE)
  @Getter
  @Setter
  @NoArgsConstructor
  public static class Data {
    UsuarioEstadoResponse estadoUsuario;
  }

  @FieldDefaults(level = AccessLevel.PRIVATE)
  @Getter
  @Setter
  @NoArgsConstructor
  public static class UsuarioEstadoResponse {
    String codigoValidacion;
    String descripcionValidacion;
    String nombres;
    String apellidoPaterno;
    String apellidoMaterno;
    String apellidoCasado;
    String fotoB64;
  }

}
