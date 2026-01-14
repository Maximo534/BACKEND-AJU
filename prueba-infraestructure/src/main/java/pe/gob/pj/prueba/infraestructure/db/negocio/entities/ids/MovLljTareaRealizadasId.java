package pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovLljTareaRealizadasId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "c_llj_id")
    private String lljId;

    @Convert(converter = TrimStringConverter.class)
    @Column(name = "c_tarea_id")
    private String tareaId;
}