package pe.gob.pj.prueba.infraestructure.db.negocio.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.gob.pj.prueba.infraestructure.common.utils.EsquemaConstants;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeInstitucionEducativaEntity;
import java.io.Serializable;
import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "mae_aju_juez_paz_escolares", schema = EsquemaConstants.PRUEBA)
public class MaeJuezPazEscolarEntity implements Serializable {

    @Id
    @Column(name = "c_cod_reg", length = 36)
    private String id;

    @Column(name = "c_dni", length = 8)
    private String dni;
    @Column(name = "x_ape_paterno", length = 40)
    private String apePaterno;
    @Column(name = "x_ape_materno", length = 40)
    private String apeMaterno;
    @Column(name = "x_nombre", length = 80)
    private String nombres;
    @Column(name = "f_nacimiento")
    private LocalDate fechaNacimiento;
    @Column(name = "x_genero", length = 15)
    private String genero;

    @Column(name = "c_grado", length = 1)
    private String grado;
    @Column(name = "c_seccion", length = 3)
    private String seccion;

    @Column(name = "x_email", length = 100)
    private String email;
    @Column(name = "c_celular", length = 9)
    private String celular;

    @Column(name = "x_cargo", length = 50)
    private String cargo;
    @Column(name = "f_juramentacion")
    private LocalDate fechaJuramentacion;
    @Column(name = "x_res_acreditacion", length = 25)
    private String resolucionAcreditacion;

    @Column(name = "c_institucion_id", length = 7)
    private String institucionEducativaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "c_institucion_id", insertable = false, updatable = false)
    private MaeInstitucionEducativaEntity institucionEducativa;

    @Column(name = "l_activo", length = 1)
    private String activo;
    @Column(name = "f_registro")
    private LocalDate fechaRegistro;
    @Column(name = "c_usuario_reg", length = 25)
    private String usuarioRegistro;
}