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

    // --- CABECERA ---

    // El ID es opcional (null al registrar, obligatorio al actualizar,
    // pero eso lo valida el controlador o el mapper según el endpoint)
    private String id;

    @NotBlank(message = "El Distrito Judicial es obligatorio.")
    @Size(max = 2, message = "El Distrito Judicial debe tener 2 caracteres.")
    private String distritoJudicialId;

    @NotNull(message = "La fecha de inicio es obligatoria.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin; // Puede ser nula si es un evento de un solo día

    @Size(max = 50, message = "La resolución del plan anual excede los 50 caracteres.")
    private String resolucionPlanAnual;

    @Size(max = 50, message = "La resolución administrativa excede los 50 caracteres.")
    private String resolucionAdminPlan;

    @Size(max = 60, message = "El documento de autorización excede los 60 caracteres.")
    private String documentoAutoriza;

    @Size(max = 5, message = "El ID del Eje excede los 5 caracteres.")
    private String ejeId;

    @Size(max = 150, message = "El público objetivo excede los 150 caracteres.")
    private String publicoObjetivo;

    @Size(max = 50, message = "El detalle del público objetivo excede los 50 caracteres.")
    private String publicoObjetivoDetalle;

    @Size(max = 150, message = "El lugar de actividad excede los 150 caracteres.")
    private String lugarActividad;

    // --- UBIGEO ---
    @Size(max = 2, message = "El departamento debe tener 2 caracteres.")
    private String departamentoId;

    @Size(max = 4, message = "La provincia debe tener 4 caracteres.")
    private String provinciaId;

    @Size(max = 6, message = "El distrito geográfico debe tener 6 caracteres.")
    private String distritoGeograficoId;

    // --- ESTADÍSTICAS ---
    // Usamos @Min(0) para evitar negativos.
    // No usamos @NotNull para permitir que el backend asuma 0 si viene null (vía Mapper).

    @Min(value = 0, message = "El número de mujeres indígenas no puede ser negativo.")
    private Integer numMujeresIndigenas;

    @Min(value = 0, message = "El número de personas no idioma nacional no puede ser negativo.")
    private Integer numPersonasNoIdiomaNacional;

    @Min(value = 0, message = "El número de jóvenes Quechua/Aymara no puede ser negativo.")
    private Integer numJovenesQuechuaAymara;

    @Size(max = 2)
    private String codigoAdcPueblosIndigenas;

    @Size(max = 100)
    private String tambo;

    @Size(max = 2)
    private String codigoSaeLenguaNativa;

    @Size(max = 25)
    private String lenguaNativa;

    // --- TEXTOS ---
    // No ponemos validación de tamaño estricta si son TEXT en BD,
    // pero es buena práctica limitar para no desbordar memoria.
    private String descripcionActividad;
    private String institucionesAliadas;
    private String observaciones;

    // --- DETALLES (Listas) ---
    // @Valid es CRUCIAL aquí. Dice: "Entra a cada objeto de la lista y valídalo también".

    @Valid
    private List<DetallePARequest> personasAtendidas;

    @Valid
    private List<DetallePCARequest> casosAtendidos;

    @Valid
    private List<DetallePBRequest> personasBeneficiadas;

    @Valid
    private List<DetalleTRRequest> tareasRealizadas;


    // --- SUB-CLASES DTO CON VALIDACIONES ---

    @Data
    public static class DetallePARequest implements Serializable {
        @NotNull(message = "El tipo de vulnerabilidad es obligatorio.")
        private Integer tipoVulnerabilidadId;

        private String rangoEdad; // Se valida tamaño o formato si es crítico

        @Min(0) private Integer cantFemenino;
        @Min(0) private Integer cantMasculino;
        @Min(0) private Integer cantLgtbiq;
    }

    @Data
    public static class DetallePCARequest implements Serializable {
        @NotNull(message = "La materia es obligatoria.")
        private Integer materiaId;

        @Min(0) private Integer numDemandas;
        @Min(0) private Integer numAudiencias;
        @Min(0) private Integer numSentencias;
        @Min(0) private Integer numProcesos;
        @Min(0) private Integer numNotificaciones;
        @Min(0) private Integer numOrientaciones;
    }

    @Data
    public static class DetallePBRequest implements Serializable {
        private String descripcionRango;
        private String codigoRango;

        @Min(0) private Integer cantFemenino;
        @Min(0) private Integer cantMasculino;
        @Min(0) private Integer cantLgtbiq;
    }

    @Data
    public static class DetalleTRRequest implements Serializable {
        @NotBlank(message = "El ID de la tarea es obligatorio.")
        private String tareaId;

        private LocalDate fechaInicio;
    }
}