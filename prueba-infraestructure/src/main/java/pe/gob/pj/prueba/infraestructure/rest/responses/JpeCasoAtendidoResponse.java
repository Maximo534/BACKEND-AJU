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

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JpeCasoAtendidoResponse implements Serializable {

    private String id;
    private String distritoJudicialId;
    private String distritoJudicialNombre;

    // Datos Institucionales (Recuperados via Juez)
    private String ugelId;
    private String ugelNombre;
    private String institucionEducativaId;
    private String institucionNombre;

    // Juez
    private String juezEscolarId;
    private String juezEscolarNombre;
    private String juezGradoSeccion;

    private LocalDate fechaRegistro;
    private String lugarActividad;
    private String estado;

    // Detalle Conflicto
    private String nombreEstudiante1;
    private String dniEstudiante1;
    private String gradoEstudiante1;

    private String nombreEstudiante2;
    private String dniEstudiante2;
    private String gradoEstudiante2;

    private String resumenHechos;
    private String acuerdos;

    private List<Archivo> archivos;
}