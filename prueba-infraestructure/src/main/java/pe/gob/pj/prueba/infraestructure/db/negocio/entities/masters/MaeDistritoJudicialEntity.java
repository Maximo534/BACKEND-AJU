package pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "mae_aju_distrito_judiciales", schema = "public")
public class MaeDistritoJudicialEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "c_distrito_jud_id", length = 2)
    private String id;

    @Column(name = "x_nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "x_nom_corto", length = 50, nullable = false)
    private String nombreCorto;

    @Column(name = "c_sigla", length = 5, nullable = false)
    private String sigla;
}