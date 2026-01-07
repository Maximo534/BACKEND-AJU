package pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovEventoTareaId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventoId;
    private String tareaId;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovEventoTareaId that = (MovEventoTareaId) o;

        return Objects.equals(trim(eventoId), trim(that.eventoId)) &&
                Objects.equals(trim(tareaId), trim(that.tareaId));
    }

    @Override
    public int hashCode() {
        return Objects.hash(trim(eventoId), trim(tareaId));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}