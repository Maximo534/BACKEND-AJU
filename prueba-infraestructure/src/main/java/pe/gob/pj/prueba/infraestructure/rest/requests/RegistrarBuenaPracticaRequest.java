package pe.gob.pj.prueba.infraestructure.rest.requests;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class RegistrarBuenaPracticaRequest implements Serializable {

    // El ID sigue siendo opcional para el registro (solo validamos tamaño si viene)
    @Size(max = 17, message = "El ID no puede exceder los 17 caracteres.")
    private String id;

    @NotBlank(message = "El Distrito Judicial es obligatorio.")
    @Size(max = 2, message = "El código de Distrito Judicial debe tener 2 caracteres.")
    private String distritoJudicialId;

    // --- DATOS DE CONTACTO ---
    @NotBlank(message = "El responsable es obligatorio.")
    @Size(max = 80, message = "El responsable no puede exceder los 80 caracteres.")
    private String responsable;

    @NotBlank(message = "El email es obligatorio.")
    @Size(max = 80, message = "El email no puede exceder los 80 caracteres.")
    @Email(message = "El formato del correo electrónico no es válido.")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio.")
    @Size(max = 9, message = "El teléfono no puede exceder los 9 caracteres.")
    private String telefono;

    @NotBlank(message = "Los integrantes son obligatorios.")
    private String integrantes;

    // --- DATOS GENERALES ---
    @NotNull(message = "La fecha de inicio es obligatoria.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @NotBlank(message = "El título de la Buena Práctica es obligatorio.")
    @Size(max = 200, message = "El título no puede exceder los 200 caracteres.")
    private String titulo;

    @NotBlank(message = "La categoría es obligatoria.")
    @Size(max = 150, message = "La categoría no puede exceder los 150 caracteres.")
    private String categoria;

    // --- TODOS LOS CAMPOS DE TEXTO (TEXT en BD) ---

    @NotBlank(message = "El problema es obligatorio.")
    private String problema;

    @NotBlank(message = "La causa es obligatoria.")
    private String causa;

    @NotBlank(message = "La consecuencia es obligatoria.")
    private String consecuencia;

    @NotBlank(message = "La descripción general es obligatoria.")
    private String descripcionGeneral;

    @NotBlank(message = "El logro es obligatorio.")
    private String logro;

    @NotBlank(message = "El objetivo es obligatorio.")
    private String objetivo;

    @NotBlank(message = "El aliado es obligatorio.")
    private String aliado; // Aliado general

    @NotBlank(message = "La dificultad es obligatoria.")
    private String dificultad;

    @NotBlank(message = "La norma es obligatoria.")
    private String norma;

    @NotBlank(message = "El desarrollo es obligatorio.")
    private String desarrollo;

    @NotBlank(message = "La ejecución es obligatoria.")
    private String ejecucion;

    @NotBlank(message = "La actividad es obligatoria.")
    private String actividad;

    @NotBlank(message = "El aporte es obligatorio.")
    private String aporte;

    @NotBlank(message = "El resultado es obligatorio.")
    private String resultado;

    @NotBlank(message = "El impacto es obligatorio.")
    private String impacto;

    @NotBlank(message = "El público objetivo es obligatorio.")
    private String publicoObjetivo;

    @NotBlank(message = "La lección aprendida es obligatoria.")
    private String leccionAprendida;

    @NotBlank(message = "La información adicional es obligatoria.")
    private String infoAdicional;

    // --- NUEVOS CAMPOS COMPLETOS (Antes ocultos) ---

    @NotBlank(message = "El aporte relevante es obligatorio.")
    private String aporteRelevante;

    @NotBlank(message = "La situación anterior es obligatoria.")
    private String situacionAnterior;

    @NotBlank(message = "La situación después es obligatoria.")
    private String situacionDespues;

    @NotBlank(message = "El impacto principal es obligatorio.")
    private String impactoPrincipal;

    @NotBlank(message = "La mejora es obligatoria.")
    private String mejora;

    @NotBlank(message = "La posibilidad de réplica es obligatoria.")
    private String posibilidadReplica;

    @NotBlank(message = "Las acciones son obligatorias.")
    private String acciones;

    @NotBlank(message = "El objetivo institucional es obligatorio.")
    private String objInstitucional;

    @NotBlank(message = "La política pública es obligatoria.")
    private String politicaPublica;

    @NotBlank(message = "La importancia es obligatoria.")
    private String importancia;

    @NotBlank(message = "Los aspectos de implementación son obligatorios.")
    private String aspectosImplementacion;

    @NotBlank(message = "El aporte a la sociedad es obligatorio.")
    private String aporteSociedad;

    @NotBlank(message = "Las medidas son obligatorias.")
    private String medidas;

    @NotBlank(message = "La norma interna es obligatoria.")
    private String normaInterna;

    @NotBlank(message = "La dificultad interna es obligatoria.")
    private String dificInterna;

    @NotBlank(message = "La dificultad externa es obligatoria.")
    private String dificExterna;

    @NotBlank(message = "El aliado externo es obligatorio.")
    private String aliadoExt;

    @NotBlank(message = "El aliado interno es obligatorio.")
    private String aliadoInt;
}