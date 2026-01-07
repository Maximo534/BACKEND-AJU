package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovLljCasosAtendidosId;
import java.io.Serializable;

@Getter @Setter
@Entity
@Table(name = "mov_aju_llj_caso_atendidos")
@IdClass(MovLljCasosAtendidosId.class)
public class MovLljCasosAtendidosEntity implements Serializable {

    @Id
    @Column(name = "c_llj_id", length = 17)
    private String lljId;

    @Id
    @Column(name = "n_materia_id")
    private Integer materiaId;

    @Column(name = "n_cant_demanda", nullable = false)
    private Integer cantidadDemandas;

    @Column(name = "n_cant_audt", nullable = false)
    private Integer cantidadAudiencias;

    @Column(name = "n_cant_sent", nullable = false)
    private Integer cantidadSentencias;

    @Column(name = "n_cant_procesos", nullable = false)
    private Integer cantidadProcesos;

    @Column(name = "n_cant_notifi", nullable = false)
    private Integer cantidadNotificaciones;

    @Column(name = "n_cant_orienta", nullable = false)
    private Integer cantidadOrientaciones;

    @PrePersist
    public void prePersist() {
        if(this.cantidadDemandas == null) this.cantidadDemandas = 0;
        if(this.cantidadAudiencias == null) this.cantidadAudiencias = 0;
        if(this.cantidadSentencias == null) this.cantidadSentencias = 0;
        if(this.cantidadProcesos == null) this.cantidadProcesos = 0;
        if(this.cantidadNotificaciones == null) this.cantidadNotificaciones = 0;
        if(this.cantidadOrientaciones == null) this.cantidadOrientaciones = 0;
    }
}