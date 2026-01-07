package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class RegistrarLlapanchikpaqRequest implements Serializable {

    private String distritoJudicialId;

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

    // --- LISTAS DETALLE (Deben coincidir con el JSON del Front) ---
    private List<BeneficiadaRequest> beneficiadas;
    private List<AtendidaRequest> atendidas;
    private List<CasoRequest> casos;
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

    @Data
    public static class AtendidaRequest {
        private Integer tipoVulnerabilidadId;
        private String rangoEdad;
        private Integer cantidadFemenino;
        private Integer cantidadMasculino;
        private Integer cantidadLgtbiq;
    }

    @Data
    public static class CasoRequest {
        private Integer materiaId;
        private Integer cantidadDemandas;
        private Integer cantidadAudiencias;
        private Integer cantidadSentencias;
        private Integer cantidadProcesos;
        private Integer cantidadNotificaciones;
        private Integer cantidadOrientaciones;
    }

    @Data
    public static class TareaRequest {
        private String tareaId;
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate fechaInicio;
    }
}