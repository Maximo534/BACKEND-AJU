package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class RegistrarPromocionRequest implements Serializable {

    // Identificadores
    private String id;
    private String distritoJudicialId;

    // Datos Principales
    private String nombreActividad;
    private String tipoActividad;
    private String tipoActividadOtros;
    private String zonaIntervencion;
    private String modalidadProyecto;

    private String publicoObjetivo;
    private String publicoObjetivoOtros;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    // Autorizaciones y Documentos
    private String resolucionPlanAnual;
    private String resolucionAdminPlan;
    private String documentoAutoriza;     // OFICIO N°...

    // Ubicación Geográfica
    private String lugarActividad;
    private String departamentoId;
    private String provinciaId;
    private String distritoGeograficoId;

    // Clasificación
    private String ejeId;
    private String actividadOperativaId;

    // Indicadores de Inclusión
    private String seDictoLenguaNativa;   // "SI" / "NO"
    private String lenguaNativa;          // Descripcion (Quechua...)

    private String participaronDiscapacitados; // "SI" / "NO"
    private Integer numeroDiscapacitados;      // Cantidad

    private String requiereInterprete;    // "SI" / "NO"

    private String descripcionActividad;
    private String recursosUtilizados;
    private String observacion;
    private String institucionesAliadas;


    private List<DetalleParticipantesRequest> participantesPorGenero;
    private List<DetalleTareaRequest> tareasRealizadas;


    @Data
    public static class DetalleParticipantesRequest implements Serializable {
        private String descripcionRango;
        private String codigoRango;
        private Integer cantidadFemenino;
        private Integer cantidadMasculino;
        private Integer cantidadLgtbiq;
    }

    @Data
    public static class DetalleTareaRequest implements Serializable {
        private String tareaId;
        private LocalDate fechaInicio;
    }
}