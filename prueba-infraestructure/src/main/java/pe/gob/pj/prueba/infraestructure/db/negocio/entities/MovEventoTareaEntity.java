package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Data;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovEventoTareaId;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.TrimStringConverter;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeTareaEntity;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "mov_aju_tarea_realizadas", schema = "public")
@IdClass(MovEventoTareaId.class)
public class MovEventoTareaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "c_evento_id", length = 17)
    @Convert(converter = TrimStringConverter.class)
    private String eventoId;

    @Id
    @Column(name = "c_tarea_id", length = 15)
    @Convert(converter = TrimStringConverter.class)
    private String tareaId;

    @Column(name = "f_inicio")
    private LocalDate fechaInicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "c_tarea_id", insertable = false, updatable = false)
    private MaeTareaEntity tareaMaestra;
}