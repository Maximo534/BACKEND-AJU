package pe.gob.pj.prueba.infraestructure.common.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import pe.gob.pj.prueba.infraestructure.common.utils.InfraestructureConstant;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public enum TipoError {

  OPERACION_EXITOSA("0000","La operación se realizo de manera exitosa.",null),
  PARAMETROS_NO_VALIDOS("E400","Error en la validación de los parámetros enviados.",null),
  
  ERROR_INESPERADO("E000",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"Error no controlado."),
  PARAMETROS_CONSUMO_NO_DESENCRIPTADOS("E001",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"Ocurrió un error en la desencriptación de los parámetros de consumo del servicio."),
  PARAMETROS_CONSUMO_NO_VALIDOS("E002",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"Los parámetros utilizados para el consumo no fueron válidos."),
  AUDITORIA_PETICION_REQUERIDA("E003",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"Error de auditoría: los datos del emisor de la petición son obligatorios (usuario, dirección IP, nombre del equipo y dirección MAC)."),
  TOKEN_NO_VALIDO("E004",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"El token enviado para consumir el servicio es null, vacío o tiene un formato no válido."),
  USUARIO_TOKEN_NO_VALIDO("E005",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"El usuario del token no es válido."),
  USUARIO_ROL_NO_TIENE_PERMISOS("E006",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"El usuario y rol enviados en el token, no tienen permisos para el enpoint consumido."),
  TOKEN_NO_FUE_GENERADO_EN_SERVIDOR_DE_VALIDACION("E007",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"El token proviene de un origen diferente el servidor donde se esta validando."),
  TIEMPO_REFRESH_TOKEN_EXCEDIDO("E008",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"El token ha superado el tiempo de expiración y refresh."),
  TOKEN_EXPIRADO("E009",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"El token ha expirado."),
  EJECUCION_SP_FALLIDO("E010",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"El Stored Procedure enviado no se ejecutó correctamente."),
  ENDPOINT_CONSUMIDO_FALLIDO("E011",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"Ocurrió error al consumir un servicio."),
  OBTENER_CREDENCIALES_CONEXIONES_FALLIDO("E012",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"Ocurrió error al obtener las credenciales de conexión a la corte judicial."),
  CORTE_NO_TIENE_CONEXIONES_CONFIGURADAS("E013",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"La corte de justicia no tiene conexiones configuradas."),
  DESENCRIPTACION_CREDENCIAL_CONEXION_DINAMICA_FALLIDA("E014",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"Ocurrió un error al desencriptar la clave de conexión dinamica a la base de datos del distrito judicial."),
  CREDENCIALES_CONEXION_DINAMICA_ERRADAS("E015",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"No se pudo establecer la conexión dinámica con la base de datos del distrito judicial."),
  CARGA_ARCHIVO_ALFRESCO_FALLIDO("E016",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"Ocurrió un error al cargar archivo(s) en ALFRESCO."),
  DESCARGA_ARCHIVO_ALFRESCO_FALLIDO("E017",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"Ocurrió un error al descargar el archivo de alfresco."),
  CAPTCHA_NO_VALIDADA("E018",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"Ocurrió un error en la validación del captcha."),
  CANTIDAD_RESULTADO_NO_PERMITIDO("E019",InfraestructureConstant.MENSAJE_ERROR_USUARIO,"Se obtuvo una cantidad de registros no permitida para el proceso actual."),

  CREDENCIALES_INCORRECTAS("N001","Las credenciales son incorrectas o el usuario esta inactivo.",null),
  PERFIL_NO_ASIGNADO("N002","El usuario no tiene pefil asignado.",null),
  OPCIONES_NOASIGNADAS("N003","El perfil de usuario no tiene opciones de menú asignadas.",null),
  TIPO_DOCUMENTO_NO_EXISTE("N004","El tipo de documento de identidad no existe.",null),
  PERSONA_YA_REGISTRADA("N005","La persona con los datos proporcionados ya está registrada en el sistema.",null),
  USUARIO_NO_ES_DE_PODER_JUDICIAL("N006","El usuario %s no pertenece al Poder Judicial.",null),
  NUEVO_TOKEN_NO_VALIDO("N007","Ocurrió un error al generar nuevo token de seguridad.",null)
  ;
  
  String codigo;
  String descripcionUsuario;
  String descripcionTecnica;

}
