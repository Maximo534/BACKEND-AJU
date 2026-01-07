package pe.gob.pj.prueba.domain.model.seguridad;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Rol implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public Integer id;		
	private String cRol;
	private String xNombre;
	private String lActivo;
	
}
