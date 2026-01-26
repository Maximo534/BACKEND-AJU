package pe.gob.pj.prueba.infraestructure.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class RegistrarFfcRequest implements Serializable {

    private String id;

    @NotBlank(message = "El Distrito Judicial es obligatorio")
    private String distritoJudicialId;

    @NotBlank(message = "El tipo de evento es obligatorio")
    private String tipoEvento;

    @NotBlank(message = "El nombre del evento es obligatorio")
    private String nombreEvento;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha fin es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

    @NotBlank(message = "La resolución del plan anual es obligatoria")
    private String resolucionPlanAnual;

    @NotBlank(message = "La resolución administrativa es obligatoria")
    private String resolucionAdminPlan;

    @NotBlank(message = "El documento que autoriza es obligatorio")
    private String documentoAutoriza;

    @NotBlank(message = "El eje es obligatorio")
    private String ejeId;

    @NotBlank(message = "La modalidad es obligatoria")
    private String modalidad;

    @NotNull(message = "La duración en horas es obligatoria")
    @Min(value = 1, message = "La duración debe ser mayor a 0")
    private Integer duracionHoras;

    @NotNull(message = "El número de sesiones es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 sesión")
    private Integer numeroSesiones;

    @NotBlank(message = "El docente expositor es obligatorio")
    private String docenteExpositor;

    @NotBlank(message = "El indicador de intérprete de señas es obligatorio")
    private String interpreteSenias;

    @NotNull(message = "El número de discapacitados es obligatorio")
    @Min(0)
    private Integer numeroDiscapacitados;

    @NotBlank(message = "El indicador de lengua nativa es obligatorio")
    private String seDictoLenguaNativa;

    @NotBlank(message = "La descripción de lengua nativa es obligatoria")
    private String lenguaNativaDesc;

    @NotBlank(message = "El público objetivo es obligatorio")
    private String publicoObjetivo;

    @NotBlank(message = "El detalle del público objetivo es obligatorio")
    private String publicoObjetivoDetalle;

    @NotBlank(message = "El nombre de la institución es obligatorio")
    private String nombreInstitucion;

    @NotBlank(message = "El departamento es obligatorio")
    private String departamentoId;

    @NotBlank(message = "La provincia es obligatoria")
    private String provinciaId;

    @NotBlank(message = "El distrito geográfico es obligatorio")
    private String distritoGeograficoId;

    @NotBlank(message = "La descripción de la actividad es obligatoria")
    private String descripcionActividad;

    @NotBlank(message = "Las instituciones aliadas son obligatorias")
    private String institucionesAliadas;

    @NotBlank(message = "Las observaciones son obligatorias")
    private String observaciones;

    @Valid
    @NotEmpty(message = "Debe registrar al menos un detalle de participantes")
    private List<DetalleParticipantesRequest> participantes;

    @Valid
    @NotEmpty(message = "Debe registrar al menos una tarea realizada")
    private List<DetalleTareaRequest> tareas;

    @Data
    public static class DetalleParticipantesRequest implements Serializable {
        @NotNull(message = "El tipo de participante es obligatorio")
        private Integer tipoParticipanteId;

        @NotBlank(message = "El rango de edad es obligatorio")
        private String rangoEdad;

        @NotNull(message = "Cantidad femenino obligatoria") @Min(0)
        private Integer cantidadFemenino;

        @NotNull(message = "Cantidad masculino obligatoria") @Min(0)
        private Integer cantidadMasculino;

        @NotNull(message = "Cantidad LGTBIQ obligatoria") @Min(0)
        private Integer cantidadLgtbiq;
    }

    @Data
    public static class DetalleTareaRequest implements Serializable {
        @NotBlank(message = "El ID de la tarea es obligatorio")
        private String tareaId;

        private LocalDate fechaInicio;
    }
}