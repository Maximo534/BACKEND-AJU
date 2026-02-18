package pe.gob.pj.accesojusticia.domain.model.negocio;

import lombok.*;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.accesojusticia.domain.model.Auditoria;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Documento extends Auditoria implements Serializable {

    static final long serialVersionUID = 1L;

    // --- Identificadores ---
    Long id;

    // --- Datos del Documento ---
    String nombre;
    String tipo;
    String formato;
    String ruta;
    Integer periodo;
    String activo;
    // --- FK ---
    Long categoriaDocumentoId;

    // --- Campos de Salida ---
    String categoriaNombre;
}