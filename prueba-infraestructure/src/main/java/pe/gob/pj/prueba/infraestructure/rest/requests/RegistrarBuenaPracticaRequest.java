package pe.gob.pj.prueba.infraestructure.rest.requests;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class RegistrarBuenaPracticaRequest implements Serializable {

    // El ID se envía nulo al registrar, pero si viene, validamos su tamaño
    @Size(max = 17, message = "El ID no puede exceder los 17 caracteres.")
    private String id;

    @NotBlank(message = "El Distrito Judicial es obligatorio.")
    @Size(max = 2, message = "El código de Distrito Judicial debe tener 2 caracteres.")
    private String distritoJudicialId;

    // --- DATOS DE CONTACTO ---
    @Size(max = 80, message = "El responsable no puede exceder los 80 caracteres.")
    private String responsable;

    @Size(max = 80, message = "El email no puede exceder los 80 caracteres.")
    @Email(message = "El formato del correo electrónico no es válido.")
    private String email;

    @Size(max = 9, message = "El teléfono no puede exceder los 9 caracteres.")
    private String telefono;

    // Integrantes es TEXT en BD, no ponemos Size estricto, pero podrías limitar si deseas
    private String integrantes;

    // --- DATOS GENERALES ---
    @NotNull(message = "La fecha de inicio es obligatoria.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @NotBlank(message = "El título de la Buena Práctica es obligatorio.")
    @Size(max = 200, message = "El título no puede exceder los 200 caracteres.")
    private String titulo;

    @Size(max = 150, message = "La categoría no puede exceder los 150 caracteres.")
    private String categoria;

    // --- CAMPOS DE TEXTO LARGO (TEXT en BD) ---
    // No ponemos @Size porque en BD son ilimitados (o muy grandes).
    // Si alguno fuera obligatorio para el negocio, agrégale @NotBlank.

    private String problema;
    private String causa;
    private String consecuencia;

    private String descripcionGeneral;
    private String logro;
    private String objetivo;
    private String aliado;
    private String dificultad;
    private String norma;

    private String desarrollo;
    private String ejecucion;
    private String actividad;

    private String aporte;
    private String resultado;
    private String impacto;
    private String publicoObjetivo;

    private String leccionAprendida;
    private String infoAdicional;
}