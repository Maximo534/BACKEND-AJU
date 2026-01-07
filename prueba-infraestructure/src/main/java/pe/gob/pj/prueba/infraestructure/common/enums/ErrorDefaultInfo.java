package pe.gob.pj.prueba.infraestructure.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorDefaultInfo {
	
	TRAZA("TRAZA_LOG"),
	MENSAJE_NO_IDENTIFICADO("RuntimeException: error personalizado."),
	CAUSA_NO_IDENTIFICADA("Causa: Se lanzo excepción estándar o personalizada."),
	CLASE_METODO_LINEA_NO_IDENTIFICADO("Clase-Método-Línea: No se pudo identificar el origen del error."),
	FORMATO_RESPUESTA_NO_IDENTIFICADO("Formato Respuesta: Error al convertir en formato xml.");
	
	private final String nombre;
	
}
