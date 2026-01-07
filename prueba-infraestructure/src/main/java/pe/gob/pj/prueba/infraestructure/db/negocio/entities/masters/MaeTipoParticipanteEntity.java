package pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;

@Entity
@Table(name = "mae_aju_tipo_participantes")
@Data
public class MaeTipoParticipanteEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "n_tipo_part_id")
    private Integer id;

    @Column(name = "x_descripcion")
    private String descripcion;

    @Column(name = "x_detalle")
    private String detalle;

    @Column(name = "l_activo")
    private String activo;
}