package pe.gob.pj.prueba.domain.model.negocio;

import lombok.*;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.model.Auditoria;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LlapanchikpaqJusticia extends Auditoria implements Serializable {

    static final long serialVersionUID = 1L;

    // --- Identificadores ---
    Long id;
    String codigo;

    // --- FKs y Maestros  ---
    Long distritoJudicialId;
    String distritoJudicialNombre;

    // --- Datos Principales ---
    LocalDate fechaInicio;
    LocalDate fechaFin;
    String lugarActividad;

    String resolucionPlanAnual;
    String resolucionAdminPlan;
    String documentoAutoriza;

    // --- Ubicación ---
    Long departamentoId;
    Long provinciaId;
    Long distritoGeograficoId;

    // --- Clasificación ---
    Long ejeId;

    // --- Textos ---
    String descripcionActividad;
    String institucionesAliadas;
    String observacion;

    // --- Auditoría Negocio ---
    LocalDate fechaRegistro;
    Long usuarioRegistroId;
    String activo;

    // --- Listas de Detalle ---
    List<DetalleBeneficiada> personasBeneficiadas;
    List<DetalleAtendida> personasAtendidas;
    List<DetalleCaso> casosAtendidos;
    List<DetalleTarea> tareasRealizadas;

    // --- Archivos ---
    List<Archivo> archivosGuardados;

    // --- CLASES INTERNAS ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DetalleBeneficiada implements Serializable {
        static final long serialVersionUID = 1L;
        String codigoRango;
        String descripcionRango;
        Integer cantidadFemenino;
        Integer cantidadMasculino;
        Integer cantidadLgtbiq;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DetalleAtendida implements Serializable {
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
    public static class DetalleCaso implements Serializable {
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
    public static class DetalleTarea implements Serializable {
        static final long serialVersionUID = 1L;
        Long tareaId;
        LocalDate fechaInicio;
        String descripcion;
    }
}