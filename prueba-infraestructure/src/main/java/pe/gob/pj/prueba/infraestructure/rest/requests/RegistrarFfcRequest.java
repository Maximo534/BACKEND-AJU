package pe.gob.pj.prueba.infraestructure.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegistrarFfcRequest implements Serializable {

    static final long serialVersionUID = 1L;

    Long id;
    String codigo;

    @NotNull(message = "El Distrito Judicial es obligatorio")
    Long distritoJudicialId;

    @NotNull(message = "El eje es obligatorio")
    Long ejeId;

    @NotBlank(message = "El tipo de evento es obligatorio")
    String tipoEvento;

    @NotBlank(message = "El nombre del evento es obligatorio")
    String nombreEvento;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaInicio;

    @NotNull(message = "La fecha fin es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaFin;

    @NotBlank(message = "La resolución del plan anual es obligatoria")
    String resolucionPlanAnual;

    @NotBlank(message = "La resolución administrativa es obligatoria")
    String resolucionAdminPlan;

    @NotBlank(message = "El documento que autoriza es obligatorio")
    String documentoAutoriza;

    @NotBlank(message = "La modalidad es obligatoria")
    String modalidad;

    @NotBlank(message = "El docente expositor es obligatorio")
    String docenteExpositor;

    // Estadísticas
    @NotNull(message = "La duración es obligatoria") @Min(0)
    Integer duracionHoras;

    @NotNull(message = "El número de sesiones es obligatorio") @Min(0)
    Integer numeroSesiones;

    @NotNull(message = "El número de discapacitados es obligatorio") @Min(0)
    Integer numeroDiscapacitados;

    // Flags (SI/NO)
    String interpreteSenias;
    String seDictoLenguaNativa;
    String lenguaNativaDesc;

    @NotBlank(message = "El público objetivo es obligatorio")
    String publicoObjetivo;

    String publicoObjetivoDetalle;

    @NotBlank(message = "El nombre de la institución es obligatorio")
    String nombreInstitucion;

    // Ubigeo
    @NotNull(message = "El departamento es obligatorio")
    Long departamentoId;

    @NotNull(message = "La provincia es obligatoria")
    Long provinciaId;

    @NotNull(message = "El distrito es obligatorio")
    Long distritoId;

    @NotBlank(message = "La descripción de la actividad es obligatoria")
    String descripcionActividad;

    @NotBlank(message = "Las instituciones aliadas son obligatorias")
    String institucionesAliadas;

    @NotBlank(message = "Las observaciones son obligatorias")
    String observaciones;

    // Listas
    @Valid
    @NotEmpty(message = "Debe registrar al menos un detalle de participantes")
    List<DetalleParticipantesRequest> participantes;

    @Valid
    @NotEmpty(message = "Debe registrar al menos una tarea realizada")
    List<DetalleTareaRequest> tareasRealizadas;

    // --- CLASES INTERNAS ---

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DetalleParticipantesRequest implements Serializable {
        static final long serialVersionUID = 1L;

        @NotNull(message = "El tipo de participante es obligatorio")
        Long tipoParticipanteId;

        @NotBlank(message = "El rango de edad es obligatorio")
        String rangoEdad;

        @NotNull(message = "Cantidad femenino obligatoria") @Min(0)
        Integer cantidadFemenino;

        @NotNull(message = "Cantidad masculino obligatoria") @Min(0)
        Integer cantidadMasculino;

        @NotNull(message = "Cantidad LGTBIQ obligatoria") @Min(0)
        Integer cantidadLgtbiq;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DetalleTareaRequest implements Serializable {
        static final long serialVersionUID = 1L;

        @NotNull(message = "El ID de la tarea es obligatorio")
        Long tareaId;

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate fechaInicio;
    }
}