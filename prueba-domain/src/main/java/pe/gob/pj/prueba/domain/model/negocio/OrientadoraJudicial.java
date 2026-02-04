package pe.gob.pj.prueba.domain.model.negocio;

import lombok.*;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.model.Auditoria;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrientadoraJudicial extends Auditoria implements Serializable {

    static final long serialVersionUID = 1L;

    // --- Identificadores ---
    Long id;
    String codigo;

    Long distritoJudicialId;
    LocalDate fechaAtencion;

    // --- Datos Usuaria ---
    String nombreCompleto;
    String tipoDocumento;
    String numeroDocumento;
    String nacionalidad;
    Integer edad;
    String telefono;
    String direccion;

    // --- Ubigeo ---
    Long departamentoId;
    Long provinciaId;
    Long distritoId;

    // --- Detalle del Caso ---
    String tipoVulnerabilidad;
    String genero;
    String lenguaMaterna;
    String tipoCasoAtendido;
    String numeroExpediente;
    String tipoViolencia;
    String derivacionInstitucion;
    String resenaCaso;

    // --- Campos de Salida  ---
    String distritoJudicialNombre;
    String activo;
    // --- Archivos ---
    List<Archivo> archivosGuardados;
}