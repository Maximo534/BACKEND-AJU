package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovJiTareasRealizadasId;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.TrimStringConverter;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeTareaEntity;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "mov_aju_ji_tarea_realizadas", schema = "public")
@IdClass(MovJiTareasRealizadasId.class)
public class MovJiTareasRealizadasEntity implements Serializable {

    @Id
    @Column(name = "c_just_itin_id", length = 17)
    @Convert(converter = TrimStringConverter.class)
    private String justiciaItineranteId;

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