package pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "mae_aju_tareas", schema = EsquemaConstants.PRUEBA)
public class MaeTareaEntity implements Serializable {

    @Id
    @Column(name = "c_tarea_id")
    private String id;

    @Column(name = "x_descripcion")
    private String descripcion;

    @Column(name = "x_medida")
    private String medida;

    @Column(name = "x_tipo_dato")
    private String tipoDato;

    @Column(name = "l_activo")
    private String activo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "c_indicador_id")
    private MaeIndicadorEntity indicador;
}