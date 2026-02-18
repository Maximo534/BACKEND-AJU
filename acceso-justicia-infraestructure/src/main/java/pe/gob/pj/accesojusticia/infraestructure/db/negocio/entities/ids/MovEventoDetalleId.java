package pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.ids;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovEventoDetalleId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long eventoId;

    private Long tipoParticipanteId;

    private String rangoEdad;
}