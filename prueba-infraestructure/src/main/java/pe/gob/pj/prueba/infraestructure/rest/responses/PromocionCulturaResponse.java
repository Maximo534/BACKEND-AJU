package pe.gob.pj.prueba.infraestructure.rest.responses;

// import com.fasterxml.jackson.annotation.JsonInclude; // COMENTADO
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
// @JsonInclude(JsonInclude.Include.NON_NULL) // OJO: Comentado para ver nulos
public class PromocionCulturaResponse implements Serializable {

    // Identificadores
    private String id;
    private String distritoJudicialId;
    private String distritoJudicialNombre;

    // Datos Principales
    private String nombreActividad;
    private String tipoActividad;
    private String tipoActividadOtros;
    private String areaRiesgo;
    private String zonaIntervencion;
    private String modalidad;

    private String publicoObjetivo;
    private String publicoObjetivoOtros;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    // Documentos / Autorizaciones
    private String resolucionPlanAnual;
    private String resolucionAdminPlan;
    private String documentoAutoriza;

    // Ubicación Geográfica
    private String lugarActividad;
    private String departamentoId;
    private String provinciaId;
    private String distritoGeograficoId;

    // Clasificación
    private String ejeId;

    // Indicadores de Inclusión
    private String seDictoLenguaNativa;
    private String lenguaNativaDesc;
    private String participaronDiscapacitados;
    private Integer numeroDiscapacitados;
    private String requiereInterprete;

    // Descripciones
    private String descripcionActividad;
    private String institucionesAliadas;
    private String observacion;

    // Auditoría y Estado
    private LocalDate fechaRegistro;
    private String usuarioRegistro;
    private String estado;

    // Listas de Detalle
    private List<DetalleParticipanteResponse> participantesPorGenero;
    private List<DetalleTareaResponse> tareasRealizadas;

    // Archivos Adjuntos
    private List<Archivo> archivos;

    // --- DTOs Internos ---
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleParticipanteResponse implements Serializable {
        private String descripcionRango;
        private String codigoRango;
        private Integer cantidadFemenino;
        private Integer cantidadMasculino;
        private Integer cantidadLgtbiq;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleTareaResponse implements Serializable {
        private String tareaId;
        private LocalDate fechaInicio;
    }
}