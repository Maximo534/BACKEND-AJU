package pe.gob.pj.accesojusticia.infraestructure.rest.responses;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.accesojusticia.infraestructure.common.enums.TipoError; // IMPORTANTE

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GlobalResponse implements Serializable {

  private static final long serialVersionUID = 1L;

  String codigo;
  String descripcion;
  String codigoOperacion;

  Object data;

  Long totalRegistros;
  Integer totalPaginas;
  Integer paginaActual;
  Integer tamanioPagina;

  public GlobalResponse(String codigoOperacion) {
    this.codigo = TipoError.OPERACION_EXITOSA.getCodigo();
    this.descripcion = TipoError.OPERACION_EXITOSA.getDescripcionUsuario();
    this.codigoOperacion = codigoOperacion;
  }

  public GlobalResponse(String codigo, String descripcion, String codigoOperacion, Object data) {
    this.codigo = codigo;
    this.descripcion = descripcion;
    this.codigoOperacion = codigoOperacion;
    this.data = data;
  }
}