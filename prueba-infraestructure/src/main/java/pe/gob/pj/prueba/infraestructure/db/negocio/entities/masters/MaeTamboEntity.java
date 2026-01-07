package pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;

@Entity
@Table(name = "mae_aju_tambos")
@Data
public class MaeTamboEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "c_tambo_id", length = 6)
    private String id;

    @Column(name = "x_nombre", length = 80, nullable = false)
    private String nombre;

    @Column(name = "l_activo", length = 1, nullable = false)
    private String activo;

    @Column(name = "c_distrito_jud_id", length = 2, nullable = false)
    private String distritoJudicialId;
}