package pe.gob.pj.accesojusticia.infraestructure.rest.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegistrarDocumentoRequest implements Serializable {

    Long id;

    @NotNull(message = "El tipo de documento es obligatorio")
    String tipo;

    Integer periodo;

    @NotNull(message = "La categoría del documento es obligatoria")
    Long categoriaDocumentoId;

}