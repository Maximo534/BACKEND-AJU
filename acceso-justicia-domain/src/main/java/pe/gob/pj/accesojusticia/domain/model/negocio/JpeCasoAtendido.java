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
public class JpeCasoAtendido extends Auditoria implements Serializable {

    static final long serialVersionUID = 1L;

    // --- Identificadores ---
    Long id;
    String codigo;

    // --- Ubicación y Registro ---
    Long distritoJudicialId;
    LocalDate fechaRegistroCaso;
    String lugarActividad;

    Long departamentoId;
    Long provinciaId;
    Long distritoId;

    // --- Juez de Paz Escolar  ---
    Long juezEscolarId;

    // --- Estudiante 1 ---
    String nombreEstudiante1;
    String dniEstudiante1;
    String gradoEstudiante1;
    String seccionEstudiante1;

    // --- Estudiante 2 ---
    String nombreEstudiante2;
    String dniEstudiante2;
    String gradoEstudiante2;
    String seccionEstudiante2;

    // --- Detalle del Conflicto ---
    String resumenHechos;
    String acuerdos;

    // --- Campos de Salida / ---
    String distritoJudicialNombre;
    String ugelNombre;
    String institucionNombre;
    String juezEscolarNombre;
    String juezGradoSeccion;
    String activo;
    // --- Archivos ---
    List<Archivo> archivosGuardados;
}