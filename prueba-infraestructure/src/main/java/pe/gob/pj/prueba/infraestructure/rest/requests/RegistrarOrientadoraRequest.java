package pe.gob.pj.prueba.infraestructure.rest.requests;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegistrarOrientadoraRequest implements Serializable {

    static final long serialVersionUID = 1L;

    Long id;

    @NotNull(message = "El Distrito Judicial es obligatorio")
    Long distritoJudicialId;

    @NotNull(message = "La fecha de atención es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaAtencion;

    // --- DATOS USUARIA ---
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 80, message = "El nombre excede los 80 caracteres")
    String nombreCompleto;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Size(max = 25, message = "El tipo de documento excede los 25 caracteres")
    String tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 18, message = "El número de documento excede los 18 caracteres")
    String numeroDocumento;

    @NotBlank(message = "La nacionalidad es obligatoria")
    @Size(max = 25, message = "La nacionalidad excede los 25 caracteres")
    String nacionalidad;

    @NotNull(message = "La edad es obligatoria")
    @Min(value = 0, message = "La edad no puede ser negativa")
    Integer edad;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 9, message = "El teléfono excede los 9 caracteres")
    String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 150, message = "La dirección excede los 150 caracteres")
    String direccion;

    // --- UBIGEO ---
    @NotNull(message = "El departamento es obligatorio")
    Long departamentoId;

    @NotNull(message = "La provincia es obligatoria")
    Long provinciaId;

    @NotNull(message = "El distrito es obligatorio")
    Long distritoId;

    // --- DETALLE CASO ---
    @NotBlank(message = "El tipo de vulnerabilidad es obligatorio")
    @Size(max = 150, message = "El tipo de vulnerabilidad excede 150 caracteres")
    String tipoVulnerabilidad;

    @NotBlank(message = "El género es obligatorio")
    @Size(max = 30, message = "El género excede 30 caracteres")
    String genero;

    @NotBlank(message = "La lengua materna es obligatoria")
    @Size(max = 30, message = "La lengua materna excede 30 caracteres")
    String lenguaMaterna;

    @NotBlank(message = "El tipo de caso atendido es obligatorio")
    @Size(max = 150, message = "El tipo de caso excede 150 caracteres")
    String tipoCasoAtendido;

    @NotBlank(message = "El número de expediente es obligatorio")
    @Size(max = 26, message = "El expediente excede 26 caracteres")
    String numeroExpediente;

    @NotBlank(message = "El tipo de violencia es obligatorio")
    @Size(max = 150, message = "El tipo de violencia excede 150 caracteres")
    String tipoViolencia;

    @NotBlank(message = "La derivación es obligatoria")
    @Size(max = 150, message = "La derivación excede 150 caracteres")
    String derivacionInstitucion;

    @NotBlank(message = "La reseña del caso es obligatoria")
    String resenaCaso;
}