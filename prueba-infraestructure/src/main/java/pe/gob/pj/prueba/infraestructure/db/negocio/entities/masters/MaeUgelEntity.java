package pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;

@Data
@Entity
@Table(name = "mae_aju_ugeles")
public class MaeUgelEntity implements Serializable {

    @Id
    @Column(name = "c_ugel_id", length = 11)
    private String id;

    @Column(name = "x_nombre", length = 200)
    private String nombre;

    @Column(name = "x_direccion", length = 200)
    private String direccion;

    @Column(name = "c_distrito_jud_id", length = 2)
    private String distritoJudicialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "c_distrito_jud_id", insertable = false, updatable = false)
    private MaeDistritoJudicialEntity distritoJudicial;
}