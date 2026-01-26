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
public class FortalecimientoCapacidades implements Serializable {

    private String id;
    private String distritoJudicialId;
    private String distritoJudicialNombre;
    private String tipoEvento;
    private String nombreEvento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private String resolucionPlanAnual;
    private String resolucionAdminPlan;
    private String documentoAutoriza;
    private String ejeId;

    private String modalidad;
    private Integer duracionHoras;
    private Integer numeroSesiones;
    private String docenteExpositor;

    private String interpreteSenias;
    private Integer numeroDiscapacitados;
    private String seDictoLenguaNativa;
    private String lenguaNativaDesc;

    private String publicoObjetivo;
    private String publicoObjetivoDetalle;
    private String nombreInstitucion;

    private String departamentoId;
    private String provinciaId;
    private String distritoGeograficoId;

    private String descripcionActividad;
    private String institucionesAliadas;
    private String observaciones;

    private LocalDate fechaRegistro;
    private String usuarioRegistro;
    private String activo;

    private List<DetalleParticipante> participantes;
    private List<DetalleTarea> tareasRealizadas;

    private String search;
    private List<Archivo> archivosGuardados;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleParticipante implements Serializable {
        private Integer tipoParticipanteId;
        private String rangoEdad;
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