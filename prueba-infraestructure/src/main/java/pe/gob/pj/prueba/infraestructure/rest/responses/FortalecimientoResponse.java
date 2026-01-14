package pe.gob.pj.prueba.infraestructure.rest.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Oculta nulos en el listado
public class FortalecimientoResponse implements Serializable {

    // Cabecera
    private String id;
    private String distritoJudicialId;
    private String distritoJudicialNombre;
    private String tipoEvento;
    private String nombreEvento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;

    // Detalle
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

    // Auditoría
    private LocalDate fechaRegistro;
    private String usuarioRegistro;

    // Listas anidadas
    private List<DetalleParticipanteResponse> participantes;
    private List<DetalleTareaResponse> tareas;

    // Archivos
    private List<Archivo> archivos;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DetalleParticipanteResponse implements Serializable {
        private Integer tipoParticipanteId;
        private String rangoEdad;
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