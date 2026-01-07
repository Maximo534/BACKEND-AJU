package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovLljPersonasBeneficiadasId;
import java.io.Serializable;

@Getter @Setter
@Entity
@Table(name = "mov_aju_llj_per_beneficiadas")
@IdClass(MovLljPersonasBeneficiadasId.class)
public class MovLljPersonasBeneficiadasEntity implements Serializable {

    @Id
    @Column(name = "c_llj_id", length = 17)
    private String lljId;

    @Id
    @Column(name = "c_rango", length = 5)
    private String codigoRango;

    @Id
    @Column(name = "x_desc_rango", length = 30)
    private String descripcionRango;

    @Column(name = "n_cant_fem", nullable = false)
    private Integer cantFemenino;

    @Column(name = "n_cant_mas", nullable = false)
    private Integer cantMasculino;

    @Column(name = "n_cant_lgtbiq", nullable = false)
    private Integer cantLgtbiq;

    @PrePersist
    public void prePersist() {
        if(this.cantFemenino == null) this.cantFemenino = 0;
        if(this.cantMasculino == null) this.cantMasculino = 0;
        if(this.cantLgtbiq == null) this.cantLgtbiq = 0;
    }
}