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
public class RegistrarLlapanchikpaqRequest implements Serializable {

    private String id;

    @NotBlank(message = "Distrito Judicial obligatorio")
    private String distritoJudicialId;

    @NotNull(message = "Fecha inicio obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    private String resolucionPlanAnual;
    private String resolucionAdminPlan;
    private String documentoAutoriza;
    private String lugarActividad;

    private String departamentoId;
    private String provinciaId;
    private String distritoGeograficoId;

    private Integer numMujeresIndigenas;
    private Integer numPersonasQuechuaAymara;
    private Integer numJuecesQuechuaAymara;

    private String usoLenguaNativa;
    private String lenguaNativaDesc;

    private String derivacion;
    private String impactoActividad;
    private String observacion;

    @Valid
    private List<BeneficiadaRequest> beneficiadas;
    @Valid
    private List<AtendidaRequest> atendidas;
    @Valid
    private List<CasoRequest> casos;
    @Valid
    private List<TareaRequest> tareas;

    // --- DTOs Internos ---
    @Data
    public static class BeneficiadaRequest {
        private String codigoRango;
        private String descripcionRango;
        private Integer cantFemenino;
        private Integer cantMasculino;
        private Integer cantLgtbiq;
    }
    // ... otros DTOs internos (sin cambios) ...
    @Data public static class AtendidaRequest { Integer tipoVulnerabilidadId; String rangoEdad; Integer cantidadFemenino; Integer cantidadMasculino; Integer cantidadLgtbiq; }
    @Data public static class CasoRequest { Integer materiaId; Integer cantidadDemandas; Integer cantidadAudiencias; Integer cantidadSentencias; Integer cantidadProcesos; Integer cantidadNotificaciones; Integer cantidadOrientaciones; }
    @Data public static class TareaRequest { String tareaId; LocalDate fechaInicio; }
}