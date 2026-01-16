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
@JsonInclude(JsonInclude.Include.NON_NULL) // CLAVE: Oculta nulos en el listado
public class JusticiaItineranteResponse implements Serializable {

    // --- CAMPOS COMUNES (Siempre visibles) ---
    private String id;
    private String distritoJudicialNombre;
    private String distritoJudicialId; // Para editar (combo)
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;

    // --- CAMPOS DETALLE (Nulos al listar) ---
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

    // --- LISTAS HIJAS ---
    private List<DetallePAResponse> personasAtendidas;
    private List<DetallePCAResponse> casosAtendidos;
    private List<DetallePBResponse> personasBeneficiadas;
    private List<DetalleTRResponse> tareasRealizadas;

    // --- ARCHIVOS ---
    private List<Archivo> archivos;

    // SUB-DTOs
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetallePAResponse implements Serializable {
        private Integer tipoVulnerabilidadId;
        private String rangoEdad;
        private Integer cantFemenino;
        private Integer cantMasculino;
        private Integer cantLgtbiq;
    }
    // (Incluir aquí DetallePCAResponse, DetallePBResponse, DetalleTRResponse con estructura similar...)
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetallePCAResponse implements Serializable {
        private Integer materiaId;
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
        private String tareaId;
        private LocalDate fechaInicio;
    }
}