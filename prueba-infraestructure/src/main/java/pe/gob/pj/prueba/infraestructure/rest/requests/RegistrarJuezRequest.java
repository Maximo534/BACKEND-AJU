package pe.gob.pj.prueba.infraestructure.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class RegistrarJuezRequest implements Serializable {

    private String id;

    @NotBlank(message = "El DNI es obligatorio.")
    @Size(min = 8, max = 8)
    private String dni;

    @NotBlank(message = "El apellido paterno es obligatorio.")
    private String apePaterno;

    @NotBlank(message = "El apellido materno es obligatorio.")
    private String apeMaterno;

    @NotBlank(message = "Los nombres son obligatorios.")
    private String nombres;

    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El género es obligatorio.")
    private String genero;

    @NotBlank(message = "El grado es obligatorio.")
    private String grado;

    @NotBlank(message = "La sección es obligatoria.")
    private String seccion;

    @NotBlank(message = "El email es obligatorio.")
    private String email;

    @NotBlank(message = "El celular es obligatorio.")
    private String celular;

    @NotBlank(message = "El cargo es obligatorio.")
    private String cargo;

    @NotNull(message = "La fecha de juramentación es obligatoria.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaJuramentacion;

    @NotBlank(message = "La resolución de acreditación es obligatoria.")
    private String resolucionAcreditacion;

    @NotBlank(message = "La institución educativa es obligatoria.")
    private String institucionEducativaId;
}