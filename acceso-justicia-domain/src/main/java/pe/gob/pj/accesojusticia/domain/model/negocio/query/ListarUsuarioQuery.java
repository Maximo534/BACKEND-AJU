package pe.gob.pj.accesojusticia.domain.model.negocio.query;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Accessors(fluent = true)
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class ListarUsuarioQuery {

    Integer idUsuario;
    String usuario;
    String nombreCompleto;
    String activo;
    Integer idUsuarioSesion;
    String rolUsuarioSesion;
}