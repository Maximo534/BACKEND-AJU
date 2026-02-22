package pe.gob.pj.accesojusticia.infraestructure.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegistrarJuezRequest implements Serializable {

    static final long serialVersionUID = 1L;

    Long id;
    String codigo;

    @NotBlank(message = "El DNI es obligatorio.")
    @Size(min = 8, max = 8, message = "El DNI debe tener 8 dígitos.")
    String dni;

    @NotBlank(message = "El apellido paterno es obligatorio.")
    String apePaterno;

    @NotBlank(message = "El apellido materno es obligatorio.")
    String apeMaterno;

    @NotBlank(message = "Los nombres son obligatorios.")
    String nombres;

    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaNacimiento;

    @NotBlank(message = "El género es obligatorio.")
    String genero;

    @NotBlank(message = "El grado es obligatorio.")
    String grado;

    @NotBlank(message = "La sección es obligatoria.")
    String seccion;

    @NotBlank(message = "El email es obligatorio.")
    String email;

    @NotBlank(message = "El celular es obligatorio.")
    String celular;

    @NotBlank(message = "El cargo es obligatorio.")
    String cargo;

    @NotNull(message = "La fecha de juramentación es obligatoria.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaJuramentacion;

    @NotBlank(message = "La resolución de acreditación es obligatoria.")
    String resolucionAcreditacion;

    @NotNull(message = "La institución educativa es obligatoria.")
    Long institucionEducativaId;
}