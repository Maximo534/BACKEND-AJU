package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class RegistrarFfcRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String distritoJudicialId; // corte
    private String tipoEvento;         // tipoevento
    private String nombreEvento;       // nombreevento
    private LocalDate fechaInicio;     // fechainicio
    private LocalDate fechaFin;        // fechafin

    private String resolucionPlanAnual; // resanual
    private String resolucionAdminPlan; // resadmi
    private String documentoAutoriza;   // docauto
    private String ejeId;               // eje

    private String modalidad;           // modalidad
    private Integer duracionHoras;      // duracionhora
    private Integer numeroSesiones;     // numsesiones
    private String docenteExpositor;    // docenteexpo

    private String interpreteSenias;    // rbis (SI/NO)
    private Integer numeroDiscapacitados; // numperdiscap
    private String seDictoLenguaNativa; // chbxsdeln (SI/NO)
    private String lenguaNativaDesc;    // menlengnat

    private String publicoObjetivo;     // pubobjet
    private String publicoObjetivoDetalle; // detallepo
    private String nombreInstitucion;   // nombreinsti (Lugar actividad presencial)

    private String departamentoId;
    private String provinciaId;
    private String distritoGeograficoId;

    private String descripcionActividad; // descacti
    private String institucionesAliadas; // instaliadas
    private String observaciones;        // observaciones

    private List<DetalleParticipantesRequest> participantesPorGenero;

    private List<DetalleTareaRequest> tareasRealizadas;

    @Data
    public static class DetalleParticipantesRequest implements Serializable {
        private Integer tipoParticipanteId;
        private String rangoEdad;
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