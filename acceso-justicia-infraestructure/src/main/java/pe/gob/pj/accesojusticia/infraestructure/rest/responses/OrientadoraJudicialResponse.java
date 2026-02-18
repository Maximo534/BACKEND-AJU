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
public class OrientadoraJudicialResponse implements Serializable {

    static final long serialVersionUID = 1L;

    Long id;
    String codigo;

    Long distritoJudicialId;
    String distritoJudicialNombre;

    LocalDate fechaAtencion;
    String estado;

    // --- DATOS USUARIO ---
    String nombreCompleto;
    String tipoDocumento;
    String numeroDocumento;
    String nacionalidad;
    Integer edad;
    String telefono;
    String direccion;

    // --- UBIGEO ---
    Long departamentoId;
    Long provinciaId;
    Long distritoId;

    // --- DETALLE CASO ---
    String tipoVulnerabilidad;
    String genero;
    String lenguaMaterna;
    String tipoCasoAtendido;
    String numeroExpediente;
    String tipoViolencia;
    String derivacionInstitucion;
    String resenaCaso;

    // --- ARCHIVOS ---
    List<Archivo> archivos;
}