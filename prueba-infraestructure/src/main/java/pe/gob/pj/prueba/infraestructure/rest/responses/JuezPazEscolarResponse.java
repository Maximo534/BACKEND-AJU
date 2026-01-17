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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JuezPazEscolarResponse implements Serializable {

    private String id;

    // --- DATOS PERSONALES (Necesarios para Editar) ---
    private String dni;
    private String apePaterno;
    private String apeMaterno;
    private String nombres;
    private LocalDate fechaNacimiento;
    private String genero;

    // --- CAMPO CALCULADO (Para Listado/Tabla) ---
    private String nombreCompleto;

    // --- DATOS ESCOLARES ---
    private String grado;
    private String seccion;
    private String cargo;

    // --- CONTACTO ---
    private String email;
    private String celular;

    // --- ACREDITACIÓN ---
    private LocalDate fechaJuramentacion;
    private String resolucionAcreditacion;

    // --- JERARQUÍA INSTITUCIONAL (Para Listado y Selects) ---
    private String institucionEducativaId;
    private String nombreColegio;          // Para columna "I.E."

    private String ugelNombre;             // Para columna "UGEL"
    private String distritoJudicialNombre; // Para columna "CORTE"

    // --- ESTADO ---
    private String estado; // "ACTIVO" (o 1)

    // --- ARCHIVOS (Para ver la resolución al editar) ---
    private List<Archivo> archivos;
}