package pe.gob.pj.prueba.infraestructure.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegistrarPromocionRequest implements Serializable {

    static final long serialVersionUID = 1L;

    Long id;

    @NotNull(message = "El Distrito Judicial es obligatorio")
    Long distritoJudicialId;

    @NotBlank(message = "Nombre actividad obligatorio")
    @Size(max = 100)
    String nombreActividad;

    @NotBlank(message = "Tipo de actividad obligatorio")
    @Size(max = 100)
    String tipoActividad;

    @NotBlank(message = "Otros tipo actividad obligatorio")
    @Size(max = 100)
    String tipoActividadOtros;

    @Size(max = 2)
    String areaRiesgo;

    @NotBlank(message = "Zona intervención obligatoria")
    @Size(max = 200)
    String zonaIntervencion;

    @NotBlank(message = "Modalidad obligatoria")
    @Size(max = 50)
    String modalidadProyecto;

    @NotBlank(message = "Público objetivo obligatorio")
    @Size(max = 150)
    String publicoObjetivo;

    @NotBlank(message = "Otros público objetivo obligatorio")
    @Size(max = 100)
    String publicoObjetivoOtros;

    @NotNull(message = "Fecha inicio obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaInicio;

    @NotNull(message = "Fecha fin obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaFin;

    // --- Autorizaciones ---
    @NotBlank(message = "Resolución Plan obligatoria")
    @Size(max = 50)
    String resolucionPlanAnual;

    @NotBlank(message = "Resolución Admin obligatoria")
    @Size(max = 50)
    String resolucionAdminPlan;

    @NotBlank(message = "Documento autoriza obligatorio")
    @Size(max = 60)
    String documentoAutoriza;

    // --- Ubicación ---
    @NotBlank(message = "Lugar actividad obligatorio")
    @Size(max = 60)
    String lugarActividad;

    @NotNull(message = "Departamento obligatorio")
    Long departamentoId;

    @NotNull(message = "Provincia obligatoria")
    Long provinciaId;

    @NotNull(message = "Distrito obligatorio")
    Long distritoGeograficoId;

    @NotNull(message = "Eje obligatorio")
    Long ejeId;

    // --- Indicadores ---
    @NotBlank(message = "Lengua nativa obligatorio")
    String seDictoLenguaNativa;

    @NotBlank(message = "Desc. Lengua nativa obligatorio")
    @Size(max = 25)
    String lenguaNativa;

    @NotBlank(message = "Participaron discap. obligatorio")
    String participaronDiscapacitados;

    @NotNull(message = "Número discap. obligatorio")
    @Min(0)
    Integer numeroDiscapacitados;

    // --- Textos ---
    @NotBlank(message = "Descripción actividad obligatoria")
    String descripcionActividad;

    @NotBlank(message = "Observación obligatoria")
    String observacion;

    @NotBlank(message = "Instituciones aliadas obligatoria")
    String institucionesAliadas;

    // --- Listas ---
    @Valid
    @NotEmpty(message = "Debe haber personas Beneficiadas")
    List<DetallePBRequest> personasBeneficiadas;

    @Valid
    @NotEmpty(message = "Debe haber tareas")
    List<DetalleTareaRequest> tareasRealizadas;

    // --- CLASES INTERNAS ---

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DetallePBRequest implements Serializable {
        static final long serialVersionUID = 1L;

        @NotBlank String descripcionRango;
        @NotBlank String codigoRango;

        @NotNull @Min(0) Integer cantidadFemenino;
        @NotNull @Min(0) Integer cantidadMasculino;
        @NotNull @Min(0) Integer cantidadLgtbiq;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DetalleTareaRequest implements Serializable {
        static final long serialVersionUID = 1L;

        @NotNull(message = "ID de tarea obligatorio")
        Long tareaId;
        LocalDate fechaInicio;
    }
}