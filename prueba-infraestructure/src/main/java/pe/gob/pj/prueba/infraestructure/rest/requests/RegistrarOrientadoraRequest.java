package pe.gob.pj.prueba.infraestructure.rest.requests;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class RegistrarOrientadoraRequest implements Serializable {

    private String id; // ✅ Agregado para Update

    private String distritoJudicialId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaAtencion;

    private String nombreCompleto;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nacionalidad;
    private Integer edad;
    private String telefono;
    private String direccion;

    private String departamentoId;
    private String provinciaId;
    private String distritoId;

    private String tipoVulnerabilidad;
    private String genero;
    private String lenguaMaterna;
    private String tipoCasoAtendido; // Materia
    private String numeroExpediente;
    private String tipoViolencia;
    private String derivacionInstitucion;
    private String resenaCaso;
}