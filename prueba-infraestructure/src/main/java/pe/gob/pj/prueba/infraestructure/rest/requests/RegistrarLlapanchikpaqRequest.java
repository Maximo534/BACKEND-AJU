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
public class RegistrarLlapanchikpaqRequest implements Serializable {

    static final long serialVersionUID = 1L;

    Long id;
    String codigo;

    @NotNull(message = "El Distrito Judicial es obligatorio")
    Long distritoJudicialId;

    @NotNull(message = "Fecha inicio obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaInicio;

    @NotNull(message = "Fecha fin obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaFin;

    @NotBlank(message = "Resolución Plan Anual obligatoria")
    @Size(max = 50)
    String resolucionPlanAnual;

    @NotBlank(message = "Resolución Admin Plan obligatoria")
    @Size(max = 50)
    String resolucionAdminPlan;

    @NotBlank(message = "Documento Autoriza obligatorio")
    @Size(max = 60)
    String documentoAutoriza;

    @NotBlank(message = "Lugar Actividad obligatorio")
    @Size(max = 200)
    String lugarActividad;

    // --- Ubigeo (Long) ---
    @NotNull(message = "Departamento obligatorio")
    Long departamentoId;

    @NotNull(message = "Provincia obligatoria")
    Long provinciaId;

    @NotNull(message = "Distrito Geográfico obligatorio")
    Long distritoGeograficoId;

    @NotNull(message = "Eje es obligatorio")
    Long ejeId;

    // --- Datos Estadísticos ---
    @NotNull(message = "Num Mesas Instaladas obligatorio") @Min(0)
    Integer numMesasInstaladas;

    @NotNull(message = "Num Servidores obligatorio") @Min(0)
    Integer numServidores;

    @NotNull(message = "Num Jueces obligatorio") @Min(0)
    Integer numJueces;

    // --- Indicadores ---
    @NotBlank(message = "Uso de lengua nativa es obligatorio (SI/NO)")
    String usoLenguaNativa;

    String lenguaNativaDesc;

    // --- Textos ---
    String derivacion;

    @NotBlank(message = "Descripción Actividad obligatoria")
    String descripcionActividad;

    @NotBlank(message = "Instituciones Aliadas obligatoria")
    String institucionesAliadas;

    @NotBlank(message = "Observación obligatoria")
    String observacion;

    // --- Listas ---
    @Valid @NotEmpty(message = "Debe registrar beneficiadas")
    List<BeneficiadaRequest> beneficiadas;

    @Valid @NotEmpty(message = "Debe registrar atendidas")
    List<AtendidaRequest> atendidas;

    @Valid @NotEmpty(message = "Debe registrar casos")
    List<CasoRequest> casos;

    @Valid @NotEmpty(message = "Debe registrar tareas")
    List<TareaRequest> tareas;

    // --- DTOs Internos ---

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class BeneficiadaRequest implements Serializable {
        static final long serialVersionUID = 1L;

        @NotBlank String codigoRango;
        @NotBlank String descripcionRango;

        @NotNull @Min(0) Integer cantFemenino;
        @NotNull @Min(0) Integer cantMasculino;
        @NotNull @Min(0) Integer cantLgtbiq;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AtendidaRequest implements Serializable {
        static final long serialVersionUID = 1L;

        @NotNull(message = "Tipo vulnerabilidad obligatorio")
        Long tipoVulnerabilidadId;

        @NotBlank String rangoEdad;

        @NotNull @Min(0) Integer cantidadFemenino;
        @NotNull @Min(0) Integer cantidadMasculino;
        @NotNull @Min(0) Integer cantidadLgtbiq;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CasoRequest implements Serializable {
        static final long serialVersionUID = 1L;

        @NotNull(message = "Materia obligatoria")
        Long materiaId;

        @NotNull @Min(0) Integer cantidadDemandas;
        @NotNull @Min(0) Integer cantidadAudiencias;
        @NotNull @Min(0) Integer cantidadSentencias;
        @NotNull @Min(0) Integer cantidadProcesos;
        @NotNull @Min(0) Integer cantidadNotificaciones;
        @NotNull @Min(0) Integer cantidadOrientaciones;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TareaRequest implements Serializable {
        static final long serialVersionUID = 1L;

        @NotNull(message = "Tarea obligatoria")
        Long tareaId;

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate fechaInicio;
    }
}