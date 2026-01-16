package pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "mae_aju_actividad_operativas", schema = EsquemaConstants.PRUEBA)
public class MaeActividadOperativaEntity implements Serializable {
    @Id
    @Column(name = "c_actividad_id")
    private String id;
    @Column(name = "x_descripcion")
    private String descripcion;

    @Column(name = "l_activo")
    private String activo;
}