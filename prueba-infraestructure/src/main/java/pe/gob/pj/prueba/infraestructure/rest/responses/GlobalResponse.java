package pe.gob.pj.prueba.infraestructure.rest.responses;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude; // 1. Importar esto
import lombok.AccessLevel;
import lombok.AllArgsConstructor; // 2. Importar esto para el Builder
import lombok.Builder; // 3. Importar esto para facilitar el llenado
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.infraestructure.common.enums.TipoError;

@Data
@Builder // ✅ Permite llenar los campos de paginación fácilmente
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // ✅ CLAVE: Si no hay paginación, oculta estos campos
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GlobalResponse implements Serializable {

  private static final long serialVersionUID = 1L;

  String codigo;
  String descripcion;
  String codigoOperacion;

  Object data; // Aquí irá la LISTA pura (Array)

  // --- CAMPOS DE PAGINACIÓN (Ahora al nivel raíz) ---
  Long totalRegistros;
  Integer totalPaginas;
  Integer paginaActual;
  Integer tamanioPagina;

  // Puedes mantener tus constructores personalizados para respuestas simples
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