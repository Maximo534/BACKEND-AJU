package pe.gob.pj.accesojusticia.infraestructure.rest.responses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data // @Data incluye @Getter, @Setter, @ToString, etc.
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioResponse implements Serializable {

  static final long serialVersionUID = 1L;

  // --- Identificadores ---
  Integer id;
  String usuario;

  // --- Datos Personales---
  String nombreCompleto;
  String cargo;
  String sigla;
  String email;

  // IDs de ubicación
  Integer idDistritoJudicial;
  Integer idInstancia;

  String nombreDistritoJudicial;
  String nombreInstancia;

  String rutaFoto;
  String nomFoto;
  String activo;

  // --- Relaciones ---
  List<PerfilUsuarioResponse> perfiles = new ArrayList<>();

  // --- Extras ---

  String token;
}