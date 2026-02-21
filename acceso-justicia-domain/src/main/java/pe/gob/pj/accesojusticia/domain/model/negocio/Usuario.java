package pe.gob.pj.accesojusticia.domain.model.negocio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.accesojusticia.domain.model.Auditoria;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Usuario extends Auditoria implements Serializable {

	static final long serialVersionUID = 1L;

	Integer id;

	String nombreUsuario;
	String clave;
	String nombrePerfil;
	String cargo;
	String sigla;
	String email;
	Integer idDistritoJudicial;
	Integer idInstancia;

	String nombreDistritoJudicial;
	String nombreInstancia;
	String nombreCompleto;
	String rutaFoto;
	String nomFoto;
	String activo;
	Integer idEje;
	Integer idUsuarioReg;
	String nombreSede;

	List<PerfilUsuario> perfiles = new ArrayList<>();
}