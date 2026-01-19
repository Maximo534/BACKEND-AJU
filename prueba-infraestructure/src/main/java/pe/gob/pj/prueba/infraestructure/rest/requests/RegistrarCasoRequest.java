package pe.gob.pj.prueba.infraestructure.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class RegistrarCasoRequest implements Serializable {

    private String id;

    @NotBlank(message = "Debe seleccionar un Juez Escolar.")
    @Size(max = 36)
    private String juezEscolarId;

    @NotBlank(message = "El Distrito Judicial es obligatorio.")
    @Size(max = 2)
    private String distritoJudicialId;

    @NotBlank(message = "El lugar de la actividad es obligatorio.")
    @Size(max = 150)
    private String lugarActividad;

    @NotNull(message = "La fecha de registro es obligatoria.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaRegistro;

    // --- UBIGEO ---
    @NotBlank(message = "El departamento es obligatorio.")
    @Size(max = 2)
    private String departamentoId;

    @NotBlank(message = "La provincia es obligatoria.")
    @Size(max = 4)
    private String provinciaId;

    @NotBlank(message = "El distrito es obligatorio.")
    @Size(max = 6)
    private String distritoId;

    // --- ESTUDIANTE 1 ---
    @NotBlank(message = "El nombre del estudiante 1 es obligatorio.")
    @Size(max = 80)
    private String nombreEstudiante1;

    @NotBlank(message = "El DNI del estudiante 1 es obligatorio.")
    @Size(max = 8)
    private String dniEstudiante1;

    @NotBlank(message = "El grado del estudiante 1 es obligatorio.")
    @Size(max = 1)
    private String gradoEstudiante1;

    @NotBlank(message = "La sección del estudiante 1 es obligatoria.")
    @Size(max = 1)
    private String seccionEstudiante1;

    // --- ESTUDIANTE 2 ---
    @NotBlank(message = "El nombre del estudiante 2 es obligatorio.")
    @Size(max = 80)
    private String nombreEstudiante2;

    @NotBlank(message = "El DNI del estudiante 2 es obligatorio.")
    @Size(max = 8)
    private String dniEstudiante2;

    @NotBlank(message = "El grado del estudiante 2 es obligatorio.")
    @Size(max = 1)
    private String gradoEstudiante2;

    @NotBlank(message = "La sección del estudiante 2 es obligatoria.")
    @Size(max = 1)
    private String seccionEstudiante2;

    // --- DETALLE ---
    @NotBlank(message = "El resumen de los hechos es obligatorio.")
    private String resumenHechos;

    @NotBlank(message = "Los acuerdos son obligatorios.")
    private String acuerdos;
}