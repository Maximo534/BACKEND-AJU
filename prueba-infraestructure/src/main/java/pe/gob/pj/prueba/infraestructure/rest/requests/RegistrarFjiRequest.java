package pe.gob.pj.prueba.infraestructure.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class RegistrarFjiRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    @NotBlank(message = "El Distrito Judicial es obligatorio.")
    @Size(max = 2, message = "El Distrito Judicial debe tener 2 caracteres.")
    private String distritoJudicialId;

    @NotNull(message = "La fecha de inicio es obligatoria.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

    @NotBlank(message = "La resolución del plan anual es obligatoria.")
    @Size(max = 50, message = "La resolución del plan anual excede los 50 caracteres.")
    private String resolucionPlanAnual;

    @NotBlank(message = "La resolución administrativa es obligatoria.")
    @Size(max = 50, message = "La resolución administrativa excede los 50 caracteres.")
    private String resolucionAdminPlan;

    @NotBlank(message = "El documento de autorización es obligatorio.")
    @Size(max = 60, message = "El documento de autorización excede los 60 caracteres.")
    private String documentoAutoriza;

    @NotBlank(message = "El eje es obligatorio.")
    @Size(max = 5, message = "El ID del Eje excede los 5 caracteres.")
    private String ejeId;

    @NotBlank(message = "El público objetivo es obligatorio.")
    @Size(max = 150, message = "El público objetivo excede los 150 caracteres.")
    private String publicoObjetivo;

    @NotBlank(message = "El detalle del público objetivo es obligatorio.")
    @Size(max = 50, message = "El detalle del público objetivo excede los 50 caracteres.")
    private String publicoObjetivoDetalle;

    @NotBlank(message = "El lugar de actividad es obligatorio.")
    @Size(max = 150, message = "El lugar de actividad excede los 150 caracteres.")
    private String lugarActividad;

    // --- UBIGEO ---
    @NotBlank(message = "El departamento es obligatorio.")
    @Size(max = 2, message = "El departamento debe tener 2 caracteres.")
    private String departamentoId;

    @NotBlank(message = "La provincia es obligatoria.")
    @Size(max = 4, message = "La provincia debe tener 4 caracteres.")
    private String provinciaId;

    @NotBlank(message = "El distrito geográfico es obligatorio.")
    @Size(max = 6, message = "El distrito geográfico debe tener 6 caracteres.")
    private String distritoGeograficoId;

    // --- ESTADÍSTICAS ---
    @NotNull(message = "El número de mesas instaladas es obligatorio.")
    @Min(value = 0, message = "El número de mesas instaladas no puede ser negativo.")
    private Integer numMesasInstaladas;

    @NotNull(message = "El número de servidores es obligatorio.")
    @Min(value = 0, message = "El número de servidores no puede ser negativo.")
    private Integer numServidores;

    @NotNull(message = "El número de Jueces es obligatorio.")
    @Min(value = 0, message = "El número Jueces no puede ser negativo.")
    private Integer numJueces;

    @NotBlank(message = "El código ADC es obligatorio.")
    @Size(max = 2)
    private String codigoAdcPueblosIndigenas;

    @NotBlank(message = "El tambo es obligatorio.")
    @Size(max = 100)
    private String tambo;

    @NotBlank(message = "El código SAE es obligatorio.")
    @Size(max = 2)
    private String codigoSaeLenguaNativa;

    @NotBlank(message = "La lengua nativa es obligatoria.")
    @Size(max = 25)
    private String lenguaNativa;

    // --- TEXTOS ---
    @NotBlank(message = "La descripción de actividad es obligatoria.")
    private String descripcionActividad;

    @NotBlank(message = "Las instituciones aliadas son obligatorias.")
    private String institucionesAliadas;

    @NotBlank(message = "Las observaciones son obligatorias.")
    private String observaciones;

    // --- DETALLES (Listas) ---
    @Valid
    @NotEmpty(message = "Debe registrar al menos una persona atendida.")
    private List<DetallePARequest> personasAtendidas;

    @Valid
    @NotEmpty(message = "Debe registrar al menos un caso atendido.")
    private List<DetallePCARequest> casosAtendidos;

    @Valid
    @NotEmpty(message = "Debe registrar al menos una persona beneficiada.")
    private List<DetallePBRequest> personasBeneficiadas;

    @Valid
    @NotEmpty(message = "Debe registrar al menos una tarea realizada.")
    private List<DetalleTRRequest> tareasRealizadas;


    // --- SUB-CLASES DTO CON VALIDACIONES ---

    @Data
    public static class DetallePARequest implements Serializable {
        @NotNull(message = "El tipo de vulnerabilidad es obligatorio.")
        private Integer tipoVulnerabilidadId;

        @NotBlank(message = "El rango de edad es obligatorio.")
        private String rangoEdad;

        @NotNull(message = "Cant. Femenino obligatorio.") @Min(0) private Integer cantFemenino;
        @NotNull(message = "Cant. Masculino obligatorio.") @Min(0) private Integer cantMasculino;
        @NotNull(message = "Cant. LGTBIQ obligatorio.") @Min(0) private Integer cantLgtbiq;
    }

    @Data
    public static class DetallePCARequest implements Serializable {
        @NotNull(message = "La materia es obligatoria.")
        private Integer materiaId;

        @NotNull(message = "Num. Demandas obligatorio.") @Min(0) private Integer numDemandas;
        @NotNull(message = "Num. Audiencias obligatorio.") @Min(0) private Integer numAudiencias;
        @NotNull(message = "Num. Sentencias obligatorio.") @Min(0) private Integer numSentencias;
        @NotNull(message = "Num. Procesos obligatorio.") @Min(0) private Integer numProcesos;
        @NotNull(message = "Num. Notificaciones obligatorio.") @Min(0) private Integer numNotificaciones;
        @NotNull(message = "Num. Orientaciones obligatorio.") @Min(0) private Integer numOrientaciones;
    }

    @Data
    public static class DetallePBRequest implements Serializable {
        @NotBlank(message = "Descripción rango obligatoria.")
        private String descripcionRango;

        @NotBlank(message = "Código rango obligatorio.")
        private String codigoRango;

        @NotNull(message = "Cant. Femenino obligatorio.") @Min(0) private Integer cantFemenino;
        @NotNull(message = "Cant. Masculino obligatorio.") @Min(0) private Integer cantMasculino;
        @NotNull(message = "Cant. LGTBIQ obligatorio.") @Min(0) private Integer cantLgtbiq;
    }

    @Data
    public static class DetalleTRRequest implements Serializable {
        @NotBlank(message = "El ID de la tarea es obligatorio.")
        private String tareaId;

        private LocalDate fechaInicio;
    }
}