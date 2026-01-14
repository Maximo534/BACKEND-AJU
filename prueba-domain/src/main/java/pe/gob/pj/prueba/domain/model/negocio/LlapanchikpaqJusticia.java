package pe.gob.pj.prueba.domain.model.negocio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LlapanchikpaqJusticia implements Serializable {

    private String id;
    private String distritoJudicialId;
    private String distritoJudicialNombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String resolucionPlanAnual;
    private String resolucionAdminPlan;
    private String documentoAutoriza;
    private String lugarActividad;

    // Ubigeo
    private String departamentoId;
    private String provinciaId;
    private String distritoGeograficoId;

    // Datos Población
    private Integer numMujeresIndigenas;
    private Integer numPersonasQuechuaAymara;
    private Integer numJuecesQuechuaAymara;
    private String usoLenguaNativa; // "SI"/"NO"
    private String lenguaNativaDesc;

    // Textos
    private String derivacion;
    private String impactoActividad;
    private String observacion;

    // Auditoría
    private LocalDate fechaRegistro;
    private String usuarioRegistro;
    private String activo;

    // --- LISTAS DETALLE ---
    private List<DetalleBeneficiada> beneficiadas;
    private List<DetalleAtendida> atendidas;
    private List<DetalleCaso> casos;
    private List<DetalleTarea> tareas;
    private String search;
    private List<Archivo> archivosGuardados;
    // --- CLASES INTERNAS PARA DETALLES ---

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleBeneficiada {
        private String codigoRango;
        private String descripcionRango;
        private Integer cantFemenino;
        private Integer cantMasculino;
        private Integer cantLgtbiq;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleAtendida {
        private Integer tipoVulnerabilidadId;
        private String rangoEdad;
        private Integer cantidadFemenino;
        private Integer cantidadMasculino;
        private Integer cantidadLgtbiq;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleCaso {
        private Integer materiaId;
        private Integer cantidadDemandas;
        private Integer cantidadAudiencias;
        private Integer cantidadSentencias;
        private Integer cantidadProcesos;
        private Integer cantidadNotificaciones;
        private Integer cantidadOrientaciones;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleTarea {
        private String tareaId;
        private LocalDate fechaInicio;
    }
}