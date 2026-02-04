package pe.gob.pj.prueba.domain.model.negocio;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor; // Agregado para el Builder
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor; // Agregado
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.domain.model.Auditoria;

@Data
@Builder
@NoArgsConstructor // Necesario para frameworks y herencia
@AllArgsConstructor // Necesario para el Builder
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Archivo extends Auditoria implements Serializable {

    static final long serialVersionUID = 1L;

    Long id;

    String nombre;
    String tipo;
    String ruta;

    String numeroIdentificacion;

    String activo;
}