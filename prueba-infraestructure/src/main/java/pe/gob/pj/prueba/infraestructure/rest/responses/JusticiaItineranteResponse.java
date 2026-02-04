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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JusticiaItineranteResponse implements Serializable {

    // --- IDENTIFICADORES ---
    private Long id;
    private String codigo;

    // --- DATOS ---
    private String distritoJudicialNombre;
    private Long distritoJudicialId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;

    // --- CAMPOS DETALLE ---
    private String resolucionPlanAnual;
    private String resolucionAdminPlan;
    private String documentoAutoriza;
    private Long ejeId;

    private String publicoObjetivo;
    private String publicoObjetivoDetalle;
    private String lugarActividad;

    // Ubigeo
    private Long departamentoId;
    private Long provinciaId;
    private Long distritoId;

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

    // --- LISTAS HIJAS ---
    private List<DetallePAResponse> personasAtendidas;
    private List<DetallePCAResponse> casosAtendidos;
    private List<DetallePBResponse> personasBeneficiadas;
    private List<DetalleTRResponse> tareasRealizadas;

    private LocalDate fRegistro;

    // --- ARCHIVOS ---
    private List<Archivo> archivos;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetallePAResponse implements Serializable {
        private Long tipoVulnerabilidadId;
        private String rangoEdad;
        private Integer cantFemenino;
        private Integer cantMasculino;
        private Integer cantLgtbiq;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetallePCAResponse implements Serializable {
        private Long materiaId;
        private Integer numDemandas;
        private Integer numAudiencias;
        private Integer numSentencias;
        private Integer numProcesos;
        private Integer numNotificaciones;
        private Integer numOrientaciones;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetallePBResponse implements Serializable {
        private String descripcionRango;
        private String codigoRango;
        private Integer cantFemenino;
        private Integer cantMasculino;
        private Integer cantLgtbiq;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleTRResponse implements Serializable {
        private Long tareaId;
        private LocalDate fechaInicio;
        private String descripcion;
    }
}