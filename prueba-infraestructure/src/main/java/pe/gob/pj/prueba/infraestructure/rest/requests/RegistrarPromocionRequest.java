package pe.gob.pj.prueba.infraestructure.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class RegistrarPromocionRequest implements Serializable {

    private String id;

    @NotBlank(message = "El Distrito Judicial es obligatorio")
    private String distritoJudicialId;

    @NotBlank(message = "Nombre actividad obligatorio")
    @Size(max = 100)
    private String nombreActividad;

    @NotBlank(message = "Tipo de actividad obligatorio")
    @Size(max = 100)
    private String tipoActividad;

    @NotBlank(message = "Otros tipo actividad obligatorio")
    @Size(max = 100)
    private String tipoActividadOtros;

    @NotBlank(message = "Otros tipo actividad obligatorio")
    @Size(max = 02)
    private String areaRiesgo;

    @NotBlank(message = "Zona intervención obligatoria")
    @Size(max = 200)
    private String zonaIntervencion;

    @NotBlank(message = "Modalidad obligatoria")
    @Size(max = 50)
    private String modalidadProyecto;

    @NotBlank(message = "Público objetivo obligatorio")
    @Size(max = 150)
    private String publicoObjetivo;

    @NotBlank(message = "Otros público objetivo obligatorio")
    @Size(max = 100)
    private String publicoObjetivoOtros;

    @NotNull(message = "Fecha inicio obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @NotNull(message = "Fecha fin obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

    // Autorizaciones
    @NotBlank(message = "Resolución Plan obligatoria")
    @Size(max = 50)
    private String resolucionPlanAnual;

    @NotBlank(message = "Resolución Admin obligatoria")
    @Size(max = 50)
    private String resolucionAdminPlan;

    @NotBlank(message = "Documento autoriza obligatorio")
    @Size(max = 60)
    private String documentoAutoriza;

    // Ubigeo
    @NotBlank(message = "Lugar actividad obligatorio")
    @Size(max = 60)
    private String lugarActividad;

    @NotBlank(message = "Departamento obligatorio")
    @Size(max = 2)
    private String departamentoId;

    @NotBlank(message = "Provincia obligatoria")
    @Size(max = 4)
    private String provinciaId;

    @NotBlank(message = "Distrito obligatorio")
    @Size(max = 6)
    private String distritoGeograficoId;

    @NotBlank(message = "Eje obligatorio")
    @Size(max = 5)
    private String ejeId;

    @NotBlank(message = "Actividad operativa obligatoria")
    private String actividadOperativaId;

    // Indicadores
    @NotBlank(message = "Lengua nativa obligatorio")
    private String seDictoLenguaNativa;

    @NotBlank(message = "Desc. Lengua nativa obligatorio")
    @Size(max = 25)
    private String lenguaNativa;

    @NotBlank(message = "Participaron discap. obligatorio")
    private String participaronDiscapacitados;

    @NotNull(message = "Número discap. obligatorio")
    @Min(0)
    private Integer numeroDiscapacitados;

    // Textos
    @NotBlank(message = "Descripción actividad obligatoria")
    private String descripcionActividad;

    @NotBlank(message = "Observación obligatoria")
    private String observacion;

    @NotBlank(message = "Instituciones aliadas obligatoria")
    private String institucionesAliadas;

    // Listas
    @Valid
    @NotEmpty(message = "Debe haber participantes")
    private List<DetalleParticipantesRequest> participantesPorGenero;

    @Valid
    @NotEmpty(message = "Debe haber tareas")
    private List<DetalleTareaRequest> tareasRealizadas;

    @Data
    public static class DetalleParticipantesRequest implements Serializable {
        @NotBlank private String descripcionRango;
        @NotBlank private String codigoRango;
        @NotNull @Min(0) private Integer cantidadFemenino;
        @NotNull @Min(0) private Integer cantidadMasculino;
        @NotNull @Min(0) private Integer cantidadLgtbiq;
    }

    @Data
    public static class DetalleTareaRequest implements Serializable {
        @NotBlank private String tareaId;
        @NotNull private LocalDate fechaInicio;
    }
}