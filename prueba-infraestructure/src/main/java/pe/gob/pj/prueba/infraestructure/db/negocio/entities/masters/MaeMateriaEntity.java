package pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters;

import jakarta.persistence.*;
import lombok.Data;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;

import java.io.Serializable;

@Entity
@Table(name = "mae_aju_materias", schema = EsquemaConstants.PRUEBA)
@Data
public class MaeMateriaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_materia_id")
    private Integer id;

    @Column(name = "x_descripcion", length = 150, nullable = false)
    private String descripcion;

    @Column(name = "x_nom_corto", length = 100, nullable = false)
    private String nombreCorto;

    @Column(name = "l_activo", length = 1, nullable = false)
    private String activo;
}