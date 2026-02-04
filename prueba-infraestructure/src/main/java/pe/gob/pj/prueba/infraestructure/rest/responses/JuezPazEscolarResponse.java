package pe.gob.pj.prueba.infraestructure.rest.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JuezPazEscolarResponse implements Serializable {

    static final long serialVersionUID = 1L;

    Long id;
    String codigo;

    // --- DATOS PERSONALES ---
    String dni;
    String apePaterno;
    String apeMaterno;
    String nombres;
    LocalDate fechaNacimiento;
    String genero;

    // --- CAMPO CALCULADO ---
    String nombreCompleto;

    // --- DATOS ESCOLARES ---
    String grado;
    String seccion;
    String cargo;

    // --- CONTACTO ---
    String email;
    String celular;

    // --- ACREDITACIÓN ---
    LocalDate fechaJuramentacion;
    String resolucionAcreditacion;

    // --- JERARQUÍA INSTITUCIONAL ---
    Long institucionEducativaId;
    String nombreColegio;

    String ugelNombre;
    String distritoJudicialNombre;

    // --- ESTADO ---
    String estado;

    // --- ARCHIVOS ---
    List<Archivo> archivos;
}