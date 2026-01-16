package pe.gob.pj.prueba.infraestructure.rest.requests;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class RegistrarOrientadoraRequest implements Serializable {

    private String id; // Opcional (solo para Updates)

    @NotBlank(message = "El Distrito Judicial es obligatorio")
    @Size(max = 2, message = "El Distrito Judicial excede 2 caracteres")
    private String distritoJudicialId;

    @NotNull(message = "La fecha de atención es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaAtencion;

    // --- DATOS USUARIA ---
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 80, message = "El nombre excede los 80 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Size(max = 25, message = "El tipo de documento excede los 25 caracteres")
    private String tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 18, message = "El número de documento excede los 18 caracteres")
    private String numeroDocumento;

    @NotBlank(message = "La nacionalidad es obligatoria")
    @Size(max = 25, message = "La nacionalidad excede los 25 caracteres")
    private String nacionalidad;

    @NotNull(message = "La edad es obligatoria")
    @Min(value = 0, message = "La edad no puede ser negativa")
    private Integer edad;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 9, message = "El teléfono excede los 9 caracteres")
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 150, message = "La dirección excede los 150 caracteres")
    private String direccion;

    // --- UBIGEO ---
    @NotBlank(message = "El departamento es obligatorio")
    @Size(max = 2)
    private String departamentoId;

    @NotBlank(message = "La provincia es obligatoria")
    @Size(max = 4)
    private String provinciaId;

    @NotBlank(message = "El distrito es obligatorio")
    @Size(max = 6)
    private String distritoId;

    // --- DETALLE CASO ---
    @NotBlank(message = "El tipo de vulnerabilidad es obligatorio")
    @Size(max = 150, message = "El tipo de vulnerabilidad excede 150 caracteres")
    private String tipoVulnerabilidad;

    @NotBlank(message = "El género es obligatorio")
    @Size(max = 30, message = "El género excede 30 caracteres")
    private String genero;

    @NotBlank(message = "La lengua materna es obligatoria")
    @Size(max = 30, message = "La lengua materna excede 30 caracteres")
    private String lenguaMaterna;

    @NotBlank(message = "El tipo de caso atendido es obligatorio")
    @Size(max = 150, message = "El tipo de caso excede 150 caracteres")
    private String tipoCasoAtendido;

    @NotBlank(message = "El número de expediente es obligatorio")
    @Size(max = 26, message = "El expediente excede 26 caracteres")
    private String numeroExpediente;

    @NotBlank(message = "El tipo de violencia es obligatorio")
    @Size(max = 150, message = "El tipo de violencia excede 150 caracteres")
    private String tipoViolencia;

    @NotBlank(message = "La derivación es obligatoria")
    @Size(max = 150, message = "La derivación excede 150 caracteres")
    private String derivacionInstitucion;

    @NotBlank(message = "La reseña del caso es obligatoria")
    private String resenaCaso; // TEXT en BD, sin límite estricto en Java
}