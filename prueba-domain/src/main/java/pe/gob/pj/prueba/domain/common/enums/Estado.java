package pe.gob.pj.prueba.domain.common.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * 
 * Representa los estados manejados de un registro
 * 
 * @author oruizb
 * @version 1.0, 07/02/2022
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum Estado {

	ACTIVO_NUMERICO("1", "Flag númerico activo."), 
    ACTIVO_LETRA("S", "Flag letra activo."), 
	INACTIVO_NUMERICO("0", "Flag númerico inactivo"),
	INACTIVO_LETRA("N", "Flag letra inactivo");

	String nombre;
	String descripcion;

}
