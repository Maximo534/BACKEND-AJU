package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class RegistrarCasoRequest implements Serializable {
    private String id;
    // Datos generales
    private String juezEscolarId; // ID del alumno juez (Obligatorio)
    private String distritoJudicialId;
    private String lugarActividad;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaRegistro; // Fecha del suceso

    // Ubigeo
    private String departamentoId;
    private String provinciaId;
    private String distritoId;

    // Estudiante 1 (Agraviado/Solicitante)
    private String nombreEstudiante1;
    private String dniEstudiante1;
    private String gradoEstudiante1;
    private String seccionEstudiante1;

    // Estudiante 2 (Agresor/Invitado)
    private String nombreEstudiante2;
    private String dniEstudiante2;
    private String gradoEstudiante2;
    private String seccionEstudiante2;

    // Conflicto
    private String resumenHechos;
    private String acuerdos;
}