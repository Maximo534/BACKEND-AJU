package pe.gob.pj.prueba.domain.model.negocio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author oruizb
 * @version 1.0
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Usuario implements Serializable {

	static final long serialVersionUID = 1L;
	
	Integer idUsuario;
	String usuario;
	String clave;
	Persona persona = new Persona();
	List<PerfilUsuario> perfiles = new ArrayList<>();
	
}
