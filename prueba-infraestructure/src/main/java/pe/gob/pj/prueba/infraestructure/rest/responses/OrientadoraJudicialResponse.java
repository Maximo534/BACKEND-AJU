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
public class OrientadoraJudicialResponse implements Serializable {

    // --- Cabecera (Grilla) ---
    private String id;
    private String distritoJudicialId;
    private String distritoJudicialNombre;
    private LocalDate fechaAtencion;
    private String nombrePersona; // Mapeado de nombreCompleto
    private String numeroExpediente;
    private String estado;

    // --- Detalle (Formulario) ---
    private String tipoDocumento;
    private String numeroDocumento;
    private String nacionalidad;
    private Integer edad;
    private String telefono;
    private String direccion;

    // Ubigeo
    private String departamentoId;
    private String provinciaId;
    private String distritoId;

    // Caso
    private String tipoVulnerabilidad;
    private String genero;
    private String lenguaMaterna;
    private String tipoCasoAtendido;
    private String tipoViolencia;
    private String derivacionInstitucion;
    private String resenaCaso;

    // Archivos
    private List<Archivo> archivos;
}