package pe.gob.pj.prueba.domain.model.negocio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo; // Importa esto

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class JpeCasoAtendido implements Serializable {

    private String id;
    private String distritoJudicialId;

    // ✅ CAMPOS NUEVOS (Necesarios para el Listado/Tabla)
    private String distritoJudicialNombre; // Para columna "CORTE"
    private String ugelNombre;             // Para columna "UGEL"
    private String institucionNombre;      // Para columna "I.E."
    private String juezEscolarNombre;     // Nombre completo del alumno juez
    private String juezGradoSeccion;

    private LocalDate fechaRegistro;
    private String lugarActividad;

    // Ubigeo
    private String departamentoId;
    private String provinciaId;
    private String distritoId;

    private String juezEscolarId; // ID del alumno Juez

    // Estudiante 1
    private String nombreEstudiante1;
    private String dniEstudiante1;
    private String gradoEstudiante1;
    private String seccionEstudiante1;

    // Estudiante 2
    private String nombreEstudiante2;
    private String dniEstudiante2;
    private String gradoEstudiante2;
    private String seccionEstudiante2;

    private String resumenHechos; // Columna "INCIDENTE"
    private String acuerdos;

    private String usuarioRegistro;

    // ✅ CAMPO NUEVO (Para ver las fotos/actas al editar)
    private List<Archivo> archivosGuardados;
}