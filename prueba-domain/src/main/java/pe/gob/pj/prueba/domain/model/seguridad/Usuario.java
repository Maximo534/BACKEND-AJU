package pe.gob.pj.prueba.domain.model.seguridad;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(fluent = true)
@Data
public class Usuario implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Integer id;		
	private String cUsuario;
	private String cClave;	
	private String lActivo;
	
}
