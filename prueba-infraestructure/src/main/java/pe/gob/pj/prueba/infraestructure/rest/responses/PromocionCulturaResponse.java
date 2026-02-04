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
public class PromocionCulturaResponse implements Serializable {

    static final long serialVersionUID = 1L;

    // --- Identificadores ---
    Long id;
    String codigo;

    Long distritoJudicialId;
    String distritoJudicialNombre;

    // --- Datos Principales ---
    String nombreActividad;
    String tipoActividad;
    String tipoActividadOtros;
    String areaRiesgo;
    String zonaIntervencion;
    String modalidad;

    String publicoObjetivo;
    String publicoObjetivoOtros;

    LocalDate fechaInicio;
    LocalDate fechaFin;

    // --- Autorizaciones ---
    String resolucionPlanAnual;
    String resolucionAdminPlan;
    String documentoAutoriza;

    // --- Ubicación ---
    String lugarActividad;
    Long departamentoId;
    Long provinciaId;
    Long distritoGeograficoId;

    // --- Clasificación ---
    Long ejeId;

    // --- Indicadores ---
    String seDictoLenguaNativa;
    String lenguaNativaDesc;
    String participaronDiscapacitados;
    Integer numeroDiscapacitados;
    String requiereInterprete;

    // --- Descripciones ---
    String descripcionActividad;
    String institucionesAliadas;
    String observacion;

    // --- Auditoría ---
    LocalDate fechaRegistro;
    String usuarioRegistro;
    String estado;

    // --- Listas ---
    List<DetallePBResponse> personasBeneficiadas;
    List<DetalleTareaResponse> tareasRealizadas;

    // --- Archivos ---
    List<Archivo> archivos;

    // --- CLASES INTERNAS ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DetallePBResponse implements Serializable {
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
    public static class DetalleTareaResponse implements Serializable {
        static final long serialVersionUID = 1L;

        Long tareaId;
        LocalDate fechaInicio;
        String descripcion;
    }
}