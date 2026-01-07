package pe.gob.pj.prueba.domain.model.seguridad;

import java.io.Serializable;

import lombok.Data;

@Data
public class Cliente implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String cCodCliente;
	private String xNomCliente;

}
