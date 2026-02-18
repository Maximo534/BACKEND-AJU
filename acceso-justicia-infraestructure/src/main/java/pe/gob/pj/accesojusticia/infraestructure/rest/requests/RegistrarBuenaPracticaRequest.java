package pe.gob.pj.accesojusticia.infraestructure.rest.requests;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegistrarBuenaPracticaRequest implements Serializable {

    static final long serialVersionUID = 1L;

    Long id;
    String codigo;

    @NotNull(message = "El Distrito Judicial es obligatorio.")
    Long distritoJudicialId;

    // --- DATOS DE CONTACTO ---
    @NotBlank(message = "El responsable es obligatorio.")
    @Size(max = 80, message = "El responsable no puede exceder los 80 caracteres.")
    String responsable;

    @NotBlank(message = "El email es obligatorio.")
    @Size(max = 80, message = "El email no puede exceder los 80 caracteres.")
    @Email(message = "El formato del correo electrónico no es válido.")
    String email;

    @NotBlank(message = "El teléfono es obligatorio.")
    @Size(max = 9, message = "El teléfono no puede exceder los 9 caracteres.")
    String telefono;

    @NotBlank(message = "Los integrantes son obligatorios.")
    String integrantes;

    // --- DATOS GENERALES ---
    @NotNull(message = "La fecha de inicio es obligatoria.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaInicio;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaFin;

    @NotBlank(message = "El título de la Buena Práctica es obligatorio.")
    @Size(max = 200, message = "El título no puede exceder los 200 caracteres.")
    String titulo;

    @NotBlank(message = "La categoría es obligatoria.")
    @Size(max = 150, message = "La categoría no puede exceder los 150 caracteres.")
    String categoria;

    // --- TODOS LOS CAMPOS DE TEXTO (TEXT en BD) ---

    @NotBlank(message = "El problema es obligatorio.")
    String problema;

    @NotBlank(message = "La causa es obligatoria.")
    String causa;

    @NotBlank(message = "La consecuencia es obligatoria.")
    String consecuencia;

    @NotBlank(message = "La descripción general es obligatoria.")
    String descripcionGeneral;

    @NotBlank(message = "El logro es obligatorio.")
    String logro;

    @NotBlank(message = "El objetivo es obligatorio.")
    String objetivo;

    @NotBlank(message = "El aliado es obligatorio.")
    String aliado;

    @NotBlank(message = "La dificultad es obligatoria.")
    String dificultad;

    @NotBlank(message = "La norma es obligatoria.")
    String norma;

    @NotBlank(message = "El desarrollo es obligatorio.")
    String desarrollo;

    @NotBlank(message = "La ejecución es obligatoria.")
    String ejecucion;

    @NotBlank(message = "La actividad es obligatoria.")
    String actividad;

    @NotBlank(message = "El aporte es obligatorio.")
    String aporte;

    @NotBlank(message = "El resultado es obligatorio.")
    String resultado;

    @NotBlank(message = "El impacto es obligatorio.")
    String impacto;

    @NotBlank(message = "El público objetivo es obligatorio.")
    String publicoObjetivo;

    @NotBlank(message = "La lección aprendida es obligatoria.")
    String leccionAprendida;

    @NotBlank(message = "La información adicional es obligatoria.")
    String infoAdicional;

    @NotBlank(message = "El aporte relevante es obligatorio.")
    String aporteRelevante;

    @NotBlank(message = "La situación anterior es obligatoria.")
    String situacionAnterior;

    @NotBlank(message = "La situación después es obligatoria.")
    String situacionDespues;

    @NotBlank(message = "El impacto principal es obligatorio.")
    String impactoPrincipal;

    @NotBlank(message = "La mejora es obligatoria.")
    String mejora;

    @NotBlank(message = "La posibilidad de réplica es obligatoria.")
    String posibilidadReplica;

    @NotBlank(message = "Las acciones son obligatorias.")
    String acciones;

    @NotBlank(message = "El objetivo institucional es obligatorio.")
    String objInstitucional;

    @NotBlank(message = "La política pública es obligatoria.")
    String politicaPublica;

    @NotBlank(message = "La importancia es obligatoria.")
    String importancia;

    @NotBlank(message = "Los aspectos de implementación son obligatorios.")
    String aspectosImplementacion;

    @NotBlank(message = "El aporte a la sociedad es obligatorio.")
    String aporteSociedad;

    @NotBlank(message = "Las medidas son obligatorias.")
    String medidas;

    @NotBlank(message = "La norma interna es obligatoria.")
    String normaInterna;

    @NotBlank(message = "La dificultad interna es obligatoria.")
    String dificInterna;

    @NotBlank(message = "La dificultad externa es obligatoria.")
    String dificExterna;

    @NotBlank(message = "El aliado externo es obligatorio.")
    String aliadoExt;

    @NotBlank(message = "El aliado interno es obligatorio.")
    String aliadoInt;
}