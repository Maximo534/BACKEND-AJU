package pe.gob.pj.prueba.infraestructure.rest.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LlapanchikpaqResponse implements Serializable {

    private String id;
    private String distritoJudicialId;
    private String distritoJudicialNombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;

    // Campos Detalle
    private String resolucionPlanAnual;
    private String resolucionAdminPlan;
    private String documentoAutoriza;
    private String lugarActividad;

    // Ubigeo
    private String departamentoId;
    private String provinciaId;
    private String distritoGeograficoId;

    // Auditoría
    private LocalDate fechaRegistro;
    private String usuarioRegistro;

    // Listas
    private List<DetalleBeneficiadaResponse> beneficiadas;
    private List<DetalleAtendidaResponse> atendidas;
    private List<DetalleCasoResponse> casos;
    private List<DetalleTareaResponse> tareas;

    private List<Archivo> archivos;

    // DTOs internos (Misma estructura que el Request/Domain)
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleBeneficiadaResponse { String codigoRango; String descripcionRango; Integer cantFemenino; Integer cantMasculino; Integer cantLgtbiq; }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleAtendidaResponse { Integer tipoVulnerabilidadId; String rangoEdad; Integer cantidadFemenino; Integer cantidadMasculino; Integer cantidadLgtbiq; }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleCasoResponse { Integer materiaId; Integer cantidadDemandas; Integer cantidadAudiencias; Integer cantidadSentencias; Integer cantidadProcesos; Integer cantidadNotificaciones; Integer cantidadOrientaciones; }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleTareaResponse { String tareaId; LocalDate fechaInicio; }
}