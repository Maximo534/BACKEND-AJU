package pe.gob.pj.accesojusticia.infraestructure.rest.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.accesojusticia.domain.model.negocio.Archivo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JpeCasoAtendidoResponse implements Serializable {

    static final long serialVersionUID = 1L;

    Long id;
    String codigo;

    Long distritoJudicialId;
    String distritoJudicialNombre;

    // --- Datos Institucionales  ---
    Long ugelId;
    String ugelNombre;
    Long institucionEducativaId;
    String institucionNombre;

    // --- Juez ---
    Long juezEscolarId;
    String juezEscolarNombre;
    String juezGradoSeccion;

    // --- Actividad ---
    LocalDate fechaRegistro;
    String lugarActividad;
    String estado;

    // --- Detalle Conflicto ---
    String nombreEstudiante1;
    String dniEstudiante1;
    String gradoEstudiante1;
    String seccionEstudiante1;

    String nombreEstudiante2;
    String dniEstudiante2;
    String gradoEstudiante2;
    String seccionEstudiante2;

    String resumenHechos;
    String acuerdos;

    List<Archivo> archivos;
}