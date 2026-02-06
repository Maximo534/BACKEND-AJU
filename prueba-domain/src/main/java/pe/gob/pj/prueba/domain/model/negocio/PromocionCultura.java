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
public class PromocionCultura extends Auditoria implements Serializable {

    static final long serialVersionUID = 1L;

    // --- Identificadores ---
    Long id;
    String codigo;

    // --- FKs y Maestros ---
    Long distritoJudicialId;
    String distritoJudicialNombre;

    // --- Datos Principales ---
    String nombreActividad;
    String tipoActividad;
    String tipoActividadOtros;
    String areaRiesgo;
    String zonaIntervencion;
    String modalidadProyecto;

    String publicoObjetivo;
    String publicoObjetivoOtros;

    LocalDate fechaInicio;
    LocalDate fechaFin;

    // --- Autorizaciones ---
    String resolucionPlanAnual;
    String resolucionAdminPlan;
    String documentoAutoriza;

    // --- Ubicación (Long) ---
    String lugarActividad;
    Long departamentoId;
    Long provinciaId;
    Long distritoGeograficoId;

    // --- Clasificación (Long) ---
    Long ejeId;

    // --- Indicadores ---
    String seDictoLenguaNativa;
    String lenguaNativa;

    String participaronDiscapacitados;
    Integer numeroDiscapacitados;

    // --- Textos ---
    String descripcionActividad;
    String institucionesAliadas;
    String observacion;

    // --- Auditoría Negocio ---
    LocalDate fechaRegistro;
    Long usuarioRegistroId;
    String activo;

    // --- Listas ---
    List<DetalleBeneficiada> personasBeneficiadas;
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

        String descripcionRango;
        String codigoRango;
        Integer cantidadFemenino;
        Integer cantidadMasculino;
        Integer cantidadLgtbiq;
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