package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class RegistrarFjiRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- CABECERA ---
    private String id;
    private String distritoJudicialId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String resolucionPlanAnual;
    private String resolucionAdminPlan;
    private String documentoAutoriza;
    private String ejeId;
    private String publicoObjetivo;
    private String publicoObjetivoDetalle;
    private String lugarActividad;

    // Ubigeo
    private String departamentoId;
    private String provinciaId;
    private String distritoGeograficoId;

    // --- ESTADÍSTICAS ---
    private Integer numMujeresIndigenas;
    private Integer numPersonasNoIdiomaNacional;
    private Integer numJovenesQuechuaAymara;
    private String codigoAdcPueblosIndigenas;
    private String tambo;
    private String codigoSaeLenguaNativa;
    private String lenguaNativa;

    // --- TEXTOS ---
    private String descripcionActividad;
    private String institucionesAliadas;
    private String observaciones;

    // --- DETALLES  ---

    private List<DetallePARequest> personasAtendidas;

    private List<DetallePCARequest> casosAtendidos;

    private List<DetallePBRequest> personasBeneficiadas;

    private List<DetalleTRRequest> tareasRealizadas;



    @Data
    public static class DetallePARequest implements Serializable {
        private Integer tipoVulnerabilidadId;
        private String rangoEdad;
        private Integer cantFemenino;
        private Integer cantMasculino;
        private Integer cantLgtbiq;
    }

    @Data
    public static class DetallePCARequest implements Serializable {
        private Integer materiaId;
        private Integer numDemandas;
        private Integer numAudiencias;
        private Integer numSentencias;
        private Integer numProcesos;
        private Integer numNotificaciones;
        private Integer numOrientaciones;
    }

    @Data
    public static class DetallePBRequest implements Serializable {
        private String descripcionRango;
        private String codigoRango;
        private Integer cantFemenino;
        private Integer cantMasculino;
        private Integer cantLgtbiq;
    }

    @Data
    public static class DetalleTRRequest implements Serializable {
        private String tareaId;
        private LocalDate fechaInicio;
    }
}