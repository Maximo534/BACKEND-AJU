package pe.gob.pj.prueba.infraestructure.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class RegistrarLlapanchikpaqRequest implements Serializable {

    private String id; // Opcional (Update)

    @NotBlank(message = "El Distrito Judicial es obligatorio")
    private String distritoJudicialId;

    @NotNull(message = "Fecha inicio obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @NotBlank(message = "Resolución Plan Anual obligatoria")
    private String resolucionPlanAnual;

    @NotBlank(message = "Resolución Admin Plan obligatoria")
    private String resolucionAdminPlan;

    @NotBlank(message = "Documento Autoriza obligatorio")
    private String documentoAutoriza;

    @NotBlank(message = "Lugar Actividad obligatorio")
    private String lugarActividad;

    @NotBlank(message = "Departamento obligatorio")
    private String departamentoId;

    @NotBlank(message = "Provincia obligatoria")
    private String provinciaId;

    @NotBlank(message = "Distrito Geográfico obligatorio")
    private String distritoGeograficoId;

    @NotNull(message = "Num Mujeres Indígenas obligatorio") @Min(0)
    private Integer numMujeresIndigenas;

    @NotNull(message = "Num Quechua/Aymara obligatorio") @Min(0)
    private Integer numPersonasQuechuaAymara;

    @NotNull(message = "Num Jueces Quechua/Aymara obligatorio") @Min(0)
    private Integer numJuecesQuechuaAymara;

    @NotBlank(message = "Uso Lengua Nativa obligatorio")
    private String usoLenguaNativa;

    @NotBlank(message = "Desc. Lengua Nativa obligatorio")
    private String lenguaNativaDesc;

    @NotBlank(message = "Derivación obligatoria")
    private String derivacion;

    @NotBlank(message = "Impacto Actividad obligatorio")
    private String impactoActividad;

    @NotBlank(message = "Observación obligatoria")
    private String observacion;

    @Valid @NotEmpty(message = "Debe registrar beneficiadas")
    private List<BeneficiadaRequest> beneficiadas;

    @Valid @NotEmpty(message = "Debe registrar atendidas")
    private List<AtendidaRequest> atendidas;

    @Valid @NotEmpty(message = "Debe registrar casos")
    private List<CasoRequest> casos;

    @Valid @NotEmpty(message = "Debe registrar tareas")
    private List<TareaRequest> tareas;

    // --- DTOs Internos ---
    @Data
    public static class BeneficiadaRequest {
        @NotBlank private String codigoRango;
        @NotBlank private String descripcionRango;
        @NotNull @Min(0) private Integer cantFemenino;
        @NotNull @Min(0) private Integer cantMasculino;
        @NotNull @Min(0) private Integer cantLgtbiq;
    }

    @Data
    public static class AtendidaRequest {
        @NotNull private Integer tipoVulnerabilidadId;
        @NotBlank private String rangoEdad;
        @NotNull @Min(0) private Integer cantidadFemenino;
        @NotNull @Min(0) private Integer cantidadMasculino;
        @NotNull @Min(0) private Integer cantidadLgtbiq;
    }

    @Data
    public static class CasoRequest {
        @NotNull private Integer materiaId;
        @NotNull @Min(0) private Integer cantidadDemandas;
        @NotNull @Min(0) private Integer cantidadAudiencias;
        @NotNull @Min(0) private Integer cantidadSentencias;
        @NotNull @Min(0) private Integer cantidadProcesos;
        @NotNull @Min(0) private Integer cantidadNotificaciones;
        @NotNull @Min(0) private Integer cantidadOrientaciones;
    }

    @Data
    public static class TareaRequest {
        @NotBlank private String tareaId;
        @NotNull private LocalDate fechaInicio;
    }
}