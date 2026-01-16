package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovLljTareaRealizadasId;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeTareaEntity;
import java.io.Serializable;
import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "mov_aju_llj_tarea_realizadas", schema = EsquemaConstants.PRUEBA)
@IdClass(MovLljTareaRealizadasId.class)
public class MovLljTareaRealizadasEntity implements Serializable {

    @Id
    @Column(name = "c_llj_id", length = 17)
    private String lljId;

    @Id
    @Column(name = "c_tarea_id", length = 15)
    private String tareaId;

    @Column(name = "f_inicio")
    private LocalDate fechaInicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "c_tarea_id", insertable = false, updatable = false)
    private MaeTareaEntity tareaMaestra;
}