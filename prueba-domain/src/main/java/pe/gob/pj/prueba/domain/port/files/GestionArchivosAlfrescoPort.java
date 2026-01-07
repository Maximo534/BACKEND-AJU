package pe.gob.pj.prueba.domain.port.files;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author oruizb
 * @version 2.0, 26/05/2025
 */
public interface GestionArchivosAlfrescoPort {

  /**
   * Inicializa las credenciales de conexión a Alfresco
   * 
   * @param host Host de Alfresco
   * @param puerto Puerto de Alfresco
   * @param usuario Usuario para la conexión
   * @param clave Contraseña del usuario
   * @param rutaRaiz Carpeta raíz para las operaciones
   * @param version Versión de Alfresco (4.1 o 4.2+)
   */
  void inicializarCredenciales(String host, String puerto, String usuario, String clave,
      String rutaRaiz, String version);

  /**
   * Verifica la conexión con el repositorio Alfresco
   * 
   * @return true si la conexión es exitosa
   */
  boolean verificarConexion();

  /**
   * Crea una carpeta en la ruta especificada
   * 
   * @param rutaPadre Ruta donde se creará la carpeta
   * @param nombreCarpeta Nombre de la nueva carpeta
   * @return ID de la carpeta creada o null si ya existe
   */
  String crearCarpeta(String rutaPadre, String nombreCarpeta);

  /**
   * Crea carpetas de manera recursiva según la ruta proporcionada
   * 
   * @param rutaCompleta Ruta completa a crear (ej: /folder1/folder2/folder3)
   * @return ID de la última carpeta creada
   */
  String crearCarpetasRecursivo(String rutaCompleta);

  /**
   * Verifica si existe una carpeta en la ruta especificada
   * 
   * @param ruta Ruta de la carpeta a verificar
   * @return true si la carpeta existe
   */
  boolean existeCarpeta(String ruta);

  /**
   * Sube un archivo al repositorio Alfresco
   * 
   * @param propiedades Propiedades del archivo
   * @param inputStream Stream del contenido del archivo
   * @param nombreArchivo Nombre que tendrá el archivo en Alfresco
   * @param rutaDestino Ruta donde se guardará el archivo
   * @param mimeType Tipo MIME del archivo
   * @return UUID del documento almacenado
   */
  String subirArchivo(Map<String, Object> propiedades, InputStream inputStream,
      String nombreArchivo, String rutaDestino, String mimeType);

  /**
   * Sube un archivo desde un array de bytes
   * 
   * @param propiedades Propiedades del archivo
   * @param contenido Contenido del archivo como array de bytes
   * @param nombreArchivo Nombre que tendrá el archivo en Alfresco
   * @param rutaDestino Ruta donde se guardará el archivo
   * @param mimeType Tipo MIME del archivo
   * @return UUID del documento almacenado
   */
  String subirArchivo(Map<String, Object> propiedades, byte[] contenido, String nombreArchivo,
      String rutaDestino, String mimeType);

  /**
   * Descarga un archivo por su UUID
   * 
   * @param uuidDocumento UUID del documento a descargar
   * @return Contenido del archivo como array de bytes
   */
  byte[] descargarArchivoPorUuid(String uuidDocumento);

  /**
   * Descarga un archivo por su ruta y nombre
   * 
   * @param rutaArchivo Ruta completa del archivo
   * @return Contenido del archivo como array de bytes
   */
  byte[] descargarArchivoPorRuta(String rutaArchivo);

  /**
   * Verifica si existe un archivo en la ruta especificada
   * 
   * @param rutaArchivo Ruta completa del archivo
   * @return true si el archivo existe
   */
  boolean existeArchivo(String rutaArchivo);

  /**
   * Verifica si existe un archivo en una carpeta específica
   * 
   * @param rutaCarpeta Ruta de la carpeta
   * @param nombreArchivo Nombre del archivo
   * @return true si el archivo existe
   */
  boolean existeArchivo(String rutaCarpeta, String nombreArchivo);

  /**
   * Elimina un archivo por su ruta completa
   * 
   * @param rutaArchivo Ruta completa del archivo a eliminar
   * @return true si se eliminó correctamente
   */
  boolean eliminarArchivo(String rutaArchivo);

  /**
   * Elimina un archivo de una carpeta específica
   * 
   * @param rutaCarpeta Ruta de la carpeta
   * @param nombreArchivo Nombre del archivo a eliminar
   * @return true si se eliminó correctamente
   */
  boolean eliminarArchivo(String rutaCarpeta, String nombreArchivo);

  /**
   * Obtiene la lista de contenido de una carpeta
   * 
   * @param rutaCarpeta Ruta de la carpeta
   * @return Lista con los nombres de los elementos en la carpeta
   */
  List<String> obtenerContenidoCarpeta(String rutaCarpeta);

  /**
   * Actualiza un documento existente
   * 
   * @param propiedades Nuevas propiedades del documento
   * @param contenido Nuevo contenido del archivo
   * @param nombreArchivo Nombre del archivo
   * @param rutaDestino Ruta donde se encuentra el archivo
   * @param mimeType Tipo MIME del archivo
   * @return UUID del documento actualizado
   */
  String actualizarDocumento(Map<String, Object> propiedades, byte[] contenido,
      String nombreArchivo, String rutaDestino, String mimeType);

  /**
   * Obtiene la ruta de un documento por su UUID
   * 
   * @param uuidDocumento UUID del documento
   * @return Ruta del documento
   */
  String obtenerRutaDocumento(String uuidDocumento);

  /**
   * Ejecuta una consulta CMIS
   * 
   * @param consulta Consulta CMIS a ejecutar
   * @return Mapa con los resultados de la consulta
   */
  Map<String, Object> ejecutarConsulta(String consulta);

  /**
   * Cierra la sesión actual con Alfresco
   */
  void cerrarSesion();
}
