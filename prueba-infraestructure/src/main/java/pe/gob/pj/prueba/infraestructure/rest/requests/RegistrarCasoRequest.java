package pe.gob.pj.prueba.infraestructure.rest.requests;

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
public class RegistrarCasoRequest implements Serializable {

    static final long serialVersionUID = 1L;

    Long id;

    @NotNull(message = "Debe seleccionar un Juez Escolar.")
    Long juezEscolarId;

    @NotNull(message = "El Distrito Judicial es obligatorio.")
    Long distritoJudicialId;

    @NotBlank(message = "El lugar de la actividad es obligatorio.")
    @Size(max = 150)
    String lugarActividad;

    @NotNull(message = "La fecha de registro es obligatoria.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaRegistro;

    // --- UBIGEO ---
    @NotNull(message = "El departamento es obligatorio.")
    Long departamentoId;

    @NotNull(message = "La provincia es obligatoria.")
    Long provinciaId;

    @NotNull(message = "El distrito es obligatorio.")
    Long distritoId;

    // --- ESTUDIANTE 1 ---
    @NotBlank(message = "El nombre del estudiante 1 es obligatorio.")
    @Size(max = 80)
    String nombreEstudiante1;

    @NotBlank(message = "El DNI del estudiante 1 es obligatorio.")
    @Size(max = 8)
    String dniEstudiante1;

    @NotBlank(message = "El grado del estudiante 1 es obligatorio.")
    @Size(max = 1)
    String gradoEstudiante1;

    @NotBlank(message = "La sección del estudiante 1 es obligatoria.")
    @Size(max = 1)
    String seccionEstudiante1;

    // --- ESTUDIANTE  ---
    @NotBlank(message = "El nombre del estudiante 2 es obligatorio.")
    @Size(max = 80)
    String nombreEstudiante2;

    @NotBlank(message = "El DNI del estudiante 2 es obligatorio.")
    @Size(max = 8)
    String dniEstudiante2;

    @NotBlank(message = "El grado del estudiante 2 es obligatorio.")
    @Size(max = 1)
    String gradoEstudiante2;

    @NotBlank(message = "La sección del estudiante 2 es obligatoria.")
    @Size(max = 1)
    String seccionEstudiante2;

    // --- DETALLE ---
    @NotBlank(message = "El resumen de los hechos es obligatorio.")
    String resumenHechos;

    @NotBlank(message = "Los acuerdos son obligatorios.")
    String acuerdos;
}