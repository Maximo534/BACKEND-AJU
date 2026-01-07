package pe.gob.pj.prueba.infraestructure.rest.responses;

import lombok.Builder;
import lombok.Data;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class JpeCasoAtendidoResponse implements Serializable {

    private String id;

    // ✅ CAMPOS NUEVOS (Para el Listado en la Tabla)
    private String distritoJudicialNombre; // CORTE
    private String ugelNombre;             // UGEL
    private String institucionNombre;      // I.E.

    private String resumenHechos;          // INCIDENTE
    private LocalDate fechaRegistro;       // FECHA

    private String estado;                 // "REGISTRADO"

    // ✅ CAMPO NUEVO (Para mostrar fotos/actas al editar)
    private List<RecursoArchivo> archivosGuardados;
}