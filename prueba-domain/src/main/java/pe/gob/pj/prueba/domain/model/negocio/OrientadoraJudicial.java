package pe.gob.pj.prueba.domain.model.negocio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrientadoraJudicial implements Serializable {
    private String id;
    private String distritoJudicialId;
    private String distritoJudicialNombre;

    private LocalDate fechaAtencion;

    // Datos Persona
    private String nombreCompleto;
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

    // Detalle Caso
    private String tipoVulnerabilidad;
    private String genero;
    private String lenguaMaterna;
    private String tipoCasoAtendido;
    private String numeroExpediente;
    private String tipoViolencia;
    private String derivacionInstitucion;
    private String resenaCaso;

    private String usuarioRegistro;
    private String search;

    private List<Archivo> archivosGuardados;

}