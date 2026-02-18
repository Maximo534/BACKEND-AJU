package pe.gob.pj.accesojusticia.domain.model.negocio;

import lombok.*;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.accesojusticia.domain.model.Auditoria;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JuezPazEscolar extends Auditoria implements Serializable {

    static final long serialVersionUID = 1L;

    // --- Identificadores ---
    Long id;
    String codigo;

    // --- Datos Personales ---
    String dni;
    String apePaterno;
    String apeMaterno;
    String nombres;
    LocalDate fechaNacimiento;
    String genero;

    // --- Datos Escolares ---
    String grado;
    String seccion;

    // --- FK (Long) ---
    Long institucionEducativaId;

    // --- Contacto ---
    String email;
    String celular;

    // --- Datos del Cargo ---
    String cargo;
    LocalDate fechaJuramentacion;
    String resolucionAcreditacion;

    // --- Campos de Salida ---
    String distritoJudicialNombre;
    String ugelNombre;
    String institucionEducativaNombre;
    String activo;
    // --- Archivos ---
    List<Archivo> archivosGuardados;
}