package pe.gob.pj.prueba.infraestructure.rest.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // ✅ CLAVE: Oculta los nulos en el listado
public class BuenaPracticaResponse implements Serializable {

    // --- CAMPOS CABECERA (Siempre visibles o comunes) ---
    private String id;
    private String distritoJudicialId;
    private String distritoJudicialNombre;
    private String titulo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String categoria;

    // --- CAMPOS DE CONTACTO ---
    private String responsable;
    private String email;
    private String telefono;
    private String integrantes;

    // --- CAMPOS DE DETALLE (Textos largos) ---
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

    // --- ARCHIVOS ---
    // En el dominio se llama 'archivosGuardados', aquí lo exponemos como 'archivos'
    private List<Archivo> archivos;
}