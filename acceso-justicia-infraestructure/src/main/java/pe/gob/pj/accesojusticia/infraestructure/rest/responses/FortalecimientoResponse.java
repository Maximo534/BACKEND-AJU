package pe.gob.pj.accesojusticia.infraestructure.rest.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.accesojusticia.domain.model.negocio.Archivo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FortalecimientoResponse implements Serializable {

    static final long serialVersionUID = 1L;

    // Cabecera
    Long id;
    String codigo;

    Long distritoJudicialId;
    String distritoJudicialNombre;

    String tipoEvento;
    String nombreEvento;
    LocalDate fechaInicio;
    LocalDate fechaFin;
    String estado;

    // Detalle
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

    // Ubigeo
    Long departamentoId;
    Long provinciaId;
    Long distritoId;

    String descripcionActividad;
    String institucionesAliadas;
    String observaciones;

    // Auditoría
    LocalDate fechaRegistro;
    String usuarioRegistro;

    // Listas anidadas
    List<DetalleParticipanteResponse> participantes;
    List<DetalleTareaResponse> tareasRealizadas;

    // Archivos
    List<Archivo> archivos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DetalleParticipanteResponse implements Serializable {
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
    public static class DetalleTareaResponse implements Serializable {
        static final long serialVersionUID = 1L;

        Long tareaId;
        LocalDate fechaInicio;
        String descripcion;
    }
}