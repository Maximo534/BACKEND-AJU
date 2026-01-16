package pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "mae_aju_tipo_usuarios", schema = EsquemaConstants.PRUEBA)
public class MaeTipoUsuarioEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "c_tipo_usu_id", length = 5, nullable = false)
    private String id;

    @Column(name = "x_descripcion", length = 100, nullable = false)
    private String descripcion;

    @Column(name = "x_detalle", length = 100, nullable = false)
    private String detalle;

    @Column(name = "b_alcance", length = 1)
    private String alcance;

    @Column(name = "l_activo", length = 1)
    private String activo;
}