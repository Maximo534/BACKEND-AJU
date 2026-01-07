package pe.gob.pj.prueba.domain.model.client.google.response;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor
@Data
public class CaptchaValidResponse implements Serializable{/**
	 * 
	 */
	private static final long serialVersionUID = 2449017822669485501L;
	
	private String success;
	private String challenge_ts;
	private String hostname;

}
