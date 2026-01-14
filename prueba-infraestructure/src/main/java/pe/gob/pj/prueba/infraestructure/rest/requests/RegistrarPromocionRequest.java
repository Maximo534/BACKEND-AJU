package pe.gob.pj.prueba.infraestructure.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class RegistrarPromocionRequest implements Serializable {

    private String id;
    @NotBlank(message = "Distrito Judicial obligatorio")
    private String distritoJudicialId;

    @NotBlank(message = "Nombre actividad obligatorio")
    private String nombreActividad;
    private String tipoActividad;
    private String tipoActividadOtros;
    private String zonaIntervencion;
    private String modalidadProyecto;

    private String publicoObjetivo;
    private String publicoObjetivoOtros;

    @NotNull(message = "Fecha inicio obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

    private String resolucionPlanAnual;
    private String resolucionAdminPlan;
    private String documentoAutoriza;

    private String lugarActividad;
    private String departamentoId;
    private String provinciaId;
    private String distritoGeograficoId;

    private String ejeId;
    private String actividadOperativaId;

    private String seDictoLenguaNativa;
    private String lenguaNativa;

    private String participaronDiscapacitados;
    private Integer numeroDiscapacitados;

    private String requiereInterprete;

    private String descripcionActividad;
    private String recursosUtilizados;
    private String observacion;
    private String institucionesAliadas;

    @Valid
    private List<DetalleParticipantesRequest> participantesPorGenero;
    @Valid
    private List<DetalleTareaRequest> tareasRealizadas;

    @Data
    public static class DetalleParticipantesRequest implements Serializable {
        private String descripcionRango;
        private String codigoRango;
        private Integer cantidadFemenino;
        private Integer cantidadMasculino;
        private Integer cantidadLgtbiq;
    }

    @Data
    public static class DetalleTareaRequest implements Serializable {
        private String tareaId;
        private LocalDate fechaInicio;
    }
}