package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "mov_aju_justicia_itinerantes", schema = EsquemaConstants.PRUEBA)
public class MovJusticiaItineranteEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "c_just_itin_id", length = 17)
    private String id;

    @Column(name = "c_distrito_jud_id", length = 2, nullable = false)
    private String distritoJudicialId;

    @Column(name = "f_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "f_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "x_res_plan_anual", length = 50)
    private String resolucionPlanAnual;

    @Column(name = "x_res_admin_plan", length = 50)
    private String resolucionAdminPlan;

    @Column(name = "x_doc_autoriza", length = 60)
    private String documentoAutoriza;

    @Column(name = "c_eje_id", length = 5)
    private String ejeId;

    @Column(name = "x_publico_obj", length = 150)
    private String publicoObjetivo;

    @Column(name = "x_publico_obj_det", length = 50)
    private String publicoObjetivoDetalle;

    @Column(name = "x_lugar_activ", length = 150)
    private String lugarActividad;

    // UBIGEO
    @Column(name = "c_depa_id", length = 2)
    private String departamentoId;

    @Column(name = "c_prov_id", length = 4)
    private String provinciaId;

    @Column(name = "c_dist_id", length = 6)
    private String distritoGeograficoId;

    // ESTADISTICAS
    @Column(name = "n_num_mesas_inst")
    private Integer numMesasInstaladas;

    @Column(name = "n_num_ser_bri_ate")
    private Integer numServidores;

    @Column(name = "n_num_juez_ate")
    private Integer numJueces;

    @Column(name = "l_adc_pueb_indg", length = 2)
    private String codigoAdcPueblosIndigenas;

    @Column(name = "x_tambo", length = 100)
    private String tambo;

    @Column(name = "l_sae_leng_nativa", length = 2)
    private String codigoSaeLenguaNativa;

    @Column(name = "x_leng_nativa", length = 25)
    private String lenguaNativa;

    // CAMPOS DE TEXTO LARGO
    @Column(name = "t_des_activ_realz", columnDefinition = "TEXT")
    private String descripcionActividad;

    @Column(name = "t_inst_aliada", columnDefinition = "TEXT")
    private String institucionesAliadas;

    @Column(name = "t_observacion", columnDefinition = "TEXT")
    private String observaciones;

    // AUDITORIA
    @Column(name = "f_reg_activ")
    private LocalDate fechaRegistro;

    @Column(name = "c_usuario_reg", length = 25)
    private String usuarioRegistro;

    @Column(name = "l_activo", length = 1)
    private String activo;


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "c_just_itin_id", referencedColumnName = "c_just_itin_id", nullable = false, insertable = false, updatable = false)
    private List<MovJiPersonasAtendidasEntity> personasAtendidas = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "c_just_itin_id", referencedColumnName = "c_just_itin_id", nullable = false, insertable = false, updatable = false)
    private List<MovJiCasosAtendidosEntity> casosAtendidos = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "c_just_itin_id", referencedColumnName = "c_just_itin_id", nullable = false, insertable = false, updatable = false)
    private List<MovJiPersonasBeneficiadasEntity> personasBeneficiadas = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "c_just_itin_id", referencedColumnName = "c_just_itin_id", nullable = false, insertable = false, updatable = false)
    private List<MovJiTareasRealizadasEntity> tareasRealizadas = new ArrayList<>();

    // SOLO LÓGICA ESTRUCTURAL, NADA DE VALORES POR DEFECTO
    @PrePersist
    public void prePersist() {
        if (this.fechaRegistro == null) this.fechaRegistro = LocalDate.now();
        if (this.activo == null) this.activo = "1";

        // Mantenemos la lógica de IDs para los hijos
        if (this.id != null) {
            if (this.personasAtendidas != null)
                this.personasAtendidas.forEach(h -> h.setJusticiaItineranteId(this.id));

            if (this.casosAtendidos != null)
                this.casosAtendidos.forEach(h -> h.setJusticiaItineranteId(this.id));

            if (this.personasBeneficiadas != null)
                this.personasBeneficiadas.forEach(h -> h.setJusticiaItineranteId(this.id));

            if (this.tareasRealizadas != null)
                this.tareasRealizadas.forEach(h -> h.setJusticiaItineranteId(this.id));
        }
    }
}