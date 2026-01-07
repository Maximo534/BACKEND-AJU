package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "mov_aju_meta_anuales") // Mantenemos el nombre físico de la tabla
public class MovOrientadoraJudicialEntity implements Serializable {

    @Id
    @Column(name = "c_meta_anual_id", length = 17)
    private String id;

    @Column(name = "c_distrito_jud_id", length = 2)
    private String distritoJudicialId;

    @Column(name = "f_registro")
    private LocalDate fechaAtencion;

    // --- DATOS USUARIA ---
    @Column(name = "x_nomb_apell", length = 80)
    private String nombreCompleto;

    @Column(name = "c_tipo_doc", length = 25)
    private String tipoDocumento;

    @Column(name = "c_num_doc", length = 18)
    private String numeroDocumento;

    @Column(name = "x_nacionalidad", length = 25)
    private String nacionalidad;

    @Column(name = "n_edad")
    private Integer edad;

    @Column(name = "c_telefono", length = 9)
    private String telefono;

    @Column(name = "x_direccion", length = 150)
    private String direccion;

    // --- UBIGEO ---
    @Column(name = "c_depa_id", length = 2)
    private String departamentoId;

    @Column(name = "c_prov_id", length = 4)
    private String provinciaId;

    @Column(name = "c_dist_id", length = 6)
    private String distritoId;

    // --- DETALLE DEL CASO ---
    @Column(name = "x_tipo_vulne", length = 150)
    private String tipoVulnerabilidad;

    @Column(name = "x_genero", length = 30)
    private String genero;

    @Column(name = "x_lengua_mat", length = 30)
    private String lenguaMaterna;

    @Column(name = "x_desc_caso_ate", length = 150)
    private String tipoCasoAtendido;

    @Column(name = "c_num_exp", length = 26)
    private String numeroExpediente;

    @Column(name = "x_tipo_vio", length = 150)
    private String tipoViolencia;

    @Column(name = "x_derivacion", length = 150)
    private String derivacionInstitucion;

    @Column(name = "t_resena", columnDefinition = "TEXT")
    private String resenaCaso;

    @Column(name = "c_usuario_reg", length = 25)
    private String usuarioRegistro;
}