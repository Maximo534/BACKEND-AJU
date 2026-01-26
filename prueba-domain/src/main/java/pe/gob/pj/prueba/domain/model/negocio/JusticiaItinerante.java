package pe.gob.pj.prueba.domain.model.negocio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JusticiaItinerante implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- CABECERA ---
    private String id;
    private String distritoJudicialId;
    private String distritoJudicialNombre;
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

    // Estadísticas
    private Integer numMesasInstaladas;
    private Integer numServidores;
    private Integer numJueces;
    private String codigoAdcPueblosIndigenas;
    private String tambo;
    private String codigoSaeLenguaNativa;
    private String lenguaNativa;

    // Textos
    private String descripcionActividad;
    private String institucionesAliadas;
    private String observaciones;

    // Auditoría
    private LocalDate fechaRegistro;
    private String usuarioRegistro;
    private String activo;

    // --- LISTAS DE DETALLE (Usando las Clases Internas) ---
    private List<DetalleBeneficiada> personasBeneficiadas = new ArrayList<>();
    private List<DetalleAtendida> personasAtendidas = new ArrayList<>();
    private List<DetalleCaso> casosAtendidos = new ArrayList<>();
    private List<DetalleTarea> tareasRealizadas = new ArrayList<>();
    private String search;
    // Archivos (Estándar)
    private List<Archivo> archivosGuardados;


    // =========================================================
    //    CLASES INTERNAS (ESTANDARIZADO)
    // =========================================================

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleBeneficiada implements Serializable {
        private String descripcionRango;
        private String codigoRango;
        private Integer cantFemenino;
        private Integer cantMasculino;
        private Integer cantLgtbiq;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleAtendida implements Serializable {
        private Integer tipoVulnerabilidadId;
        private String rangoEdad;
        private Integer cantFemenino;
        private Integer cantMasculino;
        private Integer cantLgtbiq;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleCaso implements Serializable {
        private Integer materiaId;
        private Integer numDemandas;
        private Integer numAudiencias;
        private Integer numSentencias;
        private Integer numProcesos;
        private Integer numNotificaciones;
        private Integer numOrientaciones;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleTarea implements Serializable {
        private String tareaId;
        private LocalDate fechaInicio;
        private String descripcion;
    }
}