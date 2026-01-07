package pe.gob.pj.prueba.domain.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Sexo permitido para registros persona
 * 
 * @author oruizb
 * @version 1.0,07/02/2022
 */
@Getter
@RequiredArgsConstructor
public enum Sexo {
	
	MASCULIO("M","Masculino"), FEMENINO("F","Femenino");
	
	private final String nombre;
	private final String descripcion;
}
