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

    private String id;
    private String distritoJudicialId;
    private String distritoJudicialNombre; // Se llena en el UseCase/Controller
    private LocalDate fechaAtencion;
    private String estado; // Calculado o quemado en mapper (Ej: "REGISTRADO")

    // --- DATOS USUARIA ---
    private String nombreCompleto;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nacionalidad;
    private Integer edad;
    private String telefono;
    private String direccion;

    // --- UBIGEO ---
    private String departamentoId;
    private String provinciaId;
    private String distritoId;

    // --- DETALLE CASO ---
    private String tipoVulnerabilidad;
    private String genero;
    private String lenguaMaterna;
    private String tipoCasoAtendido;
    private String numeroExpediente;
    private String tipoViolencia;
    private String derivacionInstitucion;
    private String resenaCaso;

    // --- ARCHIVOS ---
    private List<Archivo> archivos;
}