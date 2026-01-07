package pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;

@Entity
@Table(name = "mae_aju_ejes")
@Data
public class MaeEjeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "c_eje_id", length = 5)
    private String id;

    @Column(name = "x_descripcion", length = 100, nullable = false)
    private String descripcion;

    @Column(name = "l_activo", length = 1, nullable = false)
    private String activo;
}