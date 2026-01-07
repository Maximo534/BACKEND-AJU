package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Data;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovEventoDetalleId;
import java.io.Serializable;

@Data
@Entity
@Table(name = "mov_aju_evento_detalles")
@IdClass(MovEventoDetalleId.class)
public class MovEventoDetalleEntity implements Serializable {
    @Id
    @Column(name = "c_evento_id")
    private String eventoId;

    @Id
    @Column(name = "n_tipo_part_id")
    private Integer tipoParticipanteId;

    @Id
    @Column(name = "c_rango")
    private String rangoEdad;

    @Column(name = "n_cant_fem")
    private Integer cantidadFemenino;
    @Column(name = "n_cant_mas")
    private Integer cantidadMasculino;
    @Column(name = "n_cant_lgtbiq")
    private Integer cantidadLgtbiq;
}