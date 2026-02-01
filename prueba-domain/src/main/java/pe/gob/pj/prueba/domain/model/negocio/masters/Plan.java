package pe.gob.pj.prueba.domain.model.negocio.masters;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Plan implements Serializable {

    static final long serialVersionUID = 1L;

    Long id;
    String descripcion;
    String periodo;
    String resolucionGerencia;
    String resolucionAprobacion;
    String sigla;           // Nuevo: estaba en la BD
    Long distritoJudicialId; // Nuevo: FK necesaria
    String activo;
}