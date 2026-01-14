package pe.gob.pj.prueba.infraestructure.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class RegistrarCasoRequest implements Serializable {

    private String id; // Para updates

    @NotBlank(message = "Debe seleccionar un Juez Escolar")
    private String juezEscolarId;

    @NotBlank(message = "El Distrito Judicial es obligatorio")
    private String distritoJudicialId;

    @NotBlank(message = "Lugar es obligatorio")
    private String lugarActividad;

    @NotNull(message = "Fecha de registro obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaRegistro;

    private String departamentoId;
    private String provinciaId;
    private String distritoId;

    @NotBlank(message = "Nombre estudiante 1 obligatorio")
    private String nombreEstudiante1;
    private String dniEstudiante1;
    private String gradoEstudiante1;
    private String seccionEstudiante1;

    @NotBlank(message = "Nombre estudiante 2 obligatorio")
    private String nombreEstudiante2;
    private String dniEstudiante2;
    private String gradoEstudiante2;
    private String seccionEstudiante2;

    @NotBlank(message = "Resumen de hechos obligatorio")
    private String resumenHechos;

    @NotBlank(message = "Acuerdos obligatorios")
    private String acuerdos;
}