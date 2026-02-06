package pe.gob.pj.prueba.domain.model.negocio;

import lombok.*;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.model.Auditoria; // Asegúrate de tener esta clase importada

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FortalecimientoCapacidades extends Auditoria implements Serializable {

    static final long serialVersionUID = 1L;

    // --- Identificadores ---
    Long id;
    String codigo;
    // --- FKs y Datos Maestros ---
    Long distritoJudicialId;
    String distritoJudicialNombre;

    String tipoEvento;
    String nombreEvento;
    LocalDate fechaInicio;
    LocalDate fechaFin;

    String resolucionPlanAnual;
    String resolucionAdminPlan;
    String documentoAutoriza;

    Long ejeId;

    String modalidad;
    Integer duracionHoras;
    Integer numeroSesiones;
    String docenteExpositor;

    String interpreteSenias;
    Integer numeroDiscapacitados;
    String seDictoLenguaNativa;
    String lenguaNativaDesc;

    String publicoObjetivo;
    String publicoObjetivoDetalle;
    String nombreInstitucion;

    // --- Ubigeo (Long) ---
    Long departamentoId;
    Long provinciaId;
    Long distritoId;

    String descripcionActividad;
    String institucionesAliadas;
    String observaciones;

    // --- Auditoría de Negocio ---
    LocalDate fechaRegistro;
    Long usuarioRegistroId;
    String activo;

    // --- Listas ---
    List<DetalleParticipante> participantes;
    List<DetalleTarea> tareasRealizadas;

    // --- Archivos ---
    List<Archivo> archivosGuardados;

    // --- CLASES INTERNAS ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DetalleParticipante implements Serializable {
        static final long serialVersionUID = 1L;

        Long tipoParticipanteId;
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
    public static class DetalleTarea implements Serializable {
        static final long serialVersionUID = 1L;

        Long tareaId;
        LocalDate fechaInicio;
        String descripcion;
    }
}