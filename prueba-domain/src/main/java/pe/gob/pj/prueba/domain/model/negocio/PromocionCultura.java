package pe.gob.pj.prueba.domain.model.negocio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromocionCultura implements Serializable {

    private String id;
    private String distritoJudicialId;
    private String distritoJudicialNombre;
    // Datos Principales
    private String nombreActividad;
    private String tipoActividad;         // (Charla, Feria, etc.)
    private String tipoActividadOtros;
    private String areaRiesgo;
    private String zonaIntervencion;
    private String modalidad;

    private String publicoObjetivo;
    private String publicoObjetivoOtros;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    // Autorizaciones
    private String resolucionPlanAnual;
    private String resolucionAdminPlan;
    private String documentoAutoriza;

    // Ubicación
    private String lugarActividad;
    private String departamentoId;
    private String provinciaId;
    private String distritoGeograficoId;

    // Clasificación
    private String ejeId;

    // Indicadores Inclusivos
    private String seDictoLenguaNativa;   //  (SI/NO)
    private String lenguaNativaDesc;

    private String participaronDiscapacitados; //  (SI/NO)
    private Integer numeroDiscapacitados;

    // Descripciones
    private String descripcionActividad;
    private String institucionesAliadas;
    private String observacion;

    // Auditoría
    private LocalDate fechaRegistro;
    private String usuarioRegistro;
    private String activo;


    // Listas
    private List<DetalleBeneficiada> personasBeneficiadas;
    private List<DetalleTarea> tareasRealizadas;
    private String search;
    private List<Archivo> archivosGuardados;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleBeneficiada implements Serializable {

        private String descripcionRango;
        private String codigoRango;

        private Integer cantidadFemenino;
        private Integer cantidadMasculino;
        private Integer cantidadLgtbiq;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleTarea implements Serializable {
        private String tareaId;
        private LocalDate fechaInicio;
        private String descripcion;
    }

}