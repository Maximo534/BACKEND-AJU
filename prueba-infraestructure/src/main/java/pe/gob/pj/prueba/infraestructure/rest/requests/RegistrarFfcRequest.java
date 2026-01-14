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

    @NotBlank(message = "Tipo evento obligatorio")
    private String tipoEvento;

    @NotBlank(message = "Nombre evento obligatorio")
    private String nombreEvento;

    @NotNull(message = "Fecha inicio obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

    private String resolucionPlanAnual;
    private String resolucionAdminPlan;
    private String documentoAutoriza;
    private String ejeId;
    private String modalidad;
    private Integer duracionHoras;
    private Integer numeroSesiones;
    private String docenteExpositor;
    private String interpreteSenias;
    private Integer numeroDiscapacitados;
    private String seDictoLenguaNativa;
    private String lenguaNativaDesc;
    private String publicoObjetivo;
    private String publicoObjetivoDetalle;
    private String nombreInstitucion;
    private String departamentoId;
    private String provinciaId;
    private String distritoGeograficoId;
    private String descripcionActividad;
    private String institucionesAliadas;
    private String observaciones;

    @Valid
    private List<DetalleParticipantesRequest> participantesPorGenero;

    @Valid
    private List<DetalleTareaRequest> tareasRealizadas;

    @Data
    public static class DetalleParticipantesRequest implements Serializable {
        @NotNull
        private Integer tipoParticipanteId;
        private String rangoEdad;
        @Min(0) private Integer cantidadFemenino;
        @Min(0) private Integer cantidadMasculino;
        @Min(0) private Integer cantidadLgtbiq;
    }

    @Data
    public static class DetalleTareaRequest implements Serializable {
        @NotBlank
        private String tareaId;
        private LocalDate fechaInicio;
    }
}