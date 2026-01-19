package pe.gob.pj.prueba.infraestructure.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;

@Data
public class RegistrarDocumentoRequest implements Serializable {

    private String id;

    @NotBlank(message = "El tipo de documento es obligatorio.")
    private String tipo;

    @NotNull(message = "La categoría es obligatoria.")
    private Integer categoriaId;

}