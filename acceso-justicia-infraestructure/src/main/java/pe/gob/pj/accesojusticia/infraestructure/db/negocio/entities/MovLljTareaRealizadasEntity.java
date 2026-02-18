package pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.accesojusticia.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.ids.MovLljTareaRealizadasId;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters.MaeTareaEntity;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "mov_llj_tarea_realizadas", schema = EsquemaConstants.PRUEBA)
@IdClass(MovLljTareaRealizadasId.class)
public class MovLljTareaRealizadasEntity implements Serializable {

    @Id
    @Column(name = "n_llj_id")
    Long lljId;

    @Id
    @Column(name = "n_tarea_id")
    Long tareaId;

    @Column(name = "f_inicio")
    LocalDate fechaInicio;

    @Column(name = "l_activo") String activo = "1";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "n_tarea_id", insertable = false, updatable = false)
    MaeTareaEntity tareaMaestra;
}