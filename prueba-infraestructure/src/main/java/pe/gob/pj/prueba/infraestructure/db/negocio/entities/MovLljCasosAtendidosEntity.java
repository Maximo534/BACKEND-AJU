package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovLljCasosAtendidosId;

import java.io.Serializable;

@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "mov_llj_caso_atendidos", schema = EsquemaConstants.PRUEBA)
@IdClass(MovLljCasosAtendidosId.class)
public class MovLljCasosAtendidosEntity implements Serializable {

    @Id
    @Column(name = "n_llj_id")
    Long lljId;

    @Id
    @Column(name = "n_materia_id")
    Long materiaId;

    @Column(name = "n_cant_demanda") Integer cantidadDemandas;
    @Column(name = "n_cant_audt") Integer cantidadAudiencias;
    @Column(name = "n_cant_sent") Integer cantidadSentencias;
    @Column(name = "n_cant_procesos") Integer cantidadProcesos;
    @Column(name = "n_cant_notifi") Integer cantidadNotificaciones;
    @Column(name = "n_cant_orienta") Integer cantidadOrientaciones;

    @Column(name = "l_activo") String activo = "1";
}