package pe.gob.pj.prueba.infraestructure.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegistrarDocumentoRequest implements Serializable {

    static final long serialVersionUID = 1L;

    Long id;

    @NotBlank(message = "El nombre del documento es obligatorio.")
    @Size(max = 250, message = "El nombre excede los 250 caracteres.")
    String nombre;

    @NotBlank(message = "El tipo de documento es obligatorio.")
    @Size(max = 60, message = "El tipo excede los 60 caracteres.")
    String tipo;

    @NotBlank(message = "El formato es obligatorio (Ej: PDF, DOCX).")
    @Size(max = 5, message = "El formato excede los 5 caracteres.")
    String formato;

    @NotNull(message = "El periodo (año) es obligatorio.")
    Integer periodo;

    @NotNull(message = "La categoría es obligatoria.")
    Long categoriaDocumentoId;
}