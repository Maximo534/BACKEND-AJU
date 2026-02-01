package pe.gob.pj.prueba.domain.model.negocio;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Perfil implements Serializable {

    static final long serialVersionUID = 1L;

    Integer id;
    String nombre;
    String rol;
    String activo;
}