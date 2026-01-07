package pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;

@Entity
@Table(name = "mae_aju_plan_anuales")
@Data
public class MaePlanAnualEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "c_plan_id", length = 4)
    private String id;

    @Column(name = "x_descripcion", length = 100, nullable = false)
    private String descripcion;

    @Column(name = "c_periodo", length = 4, nullable = false)
    private String periodo;

    @Column(name = "x_res_gap", length = 50, nullable = false)
    private String resolucionGerencia;

    @Column(name = "x_res_ap", length = 40, nullable = false)
    private String resolucionAprobacion;

    @Column(name = "x_sigla", length = 80, nullable = false)
    private String sigla;

    @Column(name = "c_distrito_jud_id", length = 2, nullable = false)
    private String distritoJudicialId;
}