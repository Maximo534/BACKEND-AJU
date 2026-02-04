package pe.gob.pj.prueba.infraestructure.rest.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LlapanchikpaqResponse implements Serializable {

    static final long serialVersionUID = 1L;

    // --- Identificadores ---
    Long id;
    String codigo;

    Long distritoJudicialId;
    String distritoJudicialNombre;

    LocalDate fechaInicio;
    LocalDate fechaFin;
    String estado;

    // --- Detalle ---
    String resolucionPlanAnual;
    String resolucionAdminPlan;
    String documentoAutoriza;
    String lugarActividad;

    // --- Ubigeo ---
    Long departamentoId;
    Long provinciaId;
    Long distritoGeograficoId;

    Long ejeId;

    // --- Población / Estadísticas ---
    Integer numMesasInstaladas;
    Integer numServidores;
    Integer numJueces;

    String usoLenguaNativa;
    String lenguaNativaDesc;

    // --- Textos ---
    String derivacion;
    String descripcionActividad;
    String institucionesAliadas;
    String observacion;

    // --- Auditoría ---
    LocalDate fechaRegistro;
    String usuarioRegistro;

    // --- Listas ---
    List<DetalleBeneficiadaResponse> beneficiadas;
    List<DetalleAtendidaResponse> atendidas;
    List<DetalleCasoResponse> casos;
    List<DetalleTareaResponse> tareas;

    // --- Archivos ---
    List<Archivo> archivos;

    // --- DTOs Internos ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DetalleBeneficiadaResponse implements Serializable {
        static final long serialVersionUID = 1L;
        String codigoRango;
        String descripcionRango;
        Integer cantFemenino;
        Integer cantMasculino;
        Integer cantLgtbiq;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DetalleAtendidaResponse implements Serializable {
        static final long serialVersionUID = 1L;
        Long tipoVulnerabilidadId;
        String rangoEdad;
        Integer cantidadFemenino;
        Integer cantidadMasculino;
        Integer cantidadLgtbiq;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DetalleCasoResponse implements Serializable {
        static final long serialVersionUID = 1L;
        Long materiaId;
        Integer cantidadDemandas;
        Integer cantidadAudiencias;
        Integer cantidadSentencias;
        Integer cantidadProcesos;
        Integer cantidadNotificaciones;
        Integer cantidadOrientaciones;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DetalleTareaResponse implements Serializable {
        static final long serialVersionUID = 1L;
        Long tareaId;
        LocalDate fechaInicio;
        String descripcion;
    }
}