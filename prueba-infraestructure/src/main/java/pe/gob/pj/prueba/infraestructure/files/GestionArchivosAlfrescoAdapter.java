package pe.gob.pj.prueba.infraestructure.files;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.port.files.GestionArchivosAlfrescoPort;


@Slf4j
@Component
public class GestionArchivosAlfrescoAdapter implements GestionArchivosAlfrescoPort {

  private static final String ATOM_PUB_URL_41 = "alfresco/cmisatom";
  private static final String ATOM_PUB_URL_42 =
      "alfresco/api/-default-/public/cmis/versions/1.0/atom";
  private static final String FOLDER_TYPE = "cmis:folder";
  private static final int BUFFER_SIZE = 8192;

  private final Map<String, String> credencialesConexion = new HashMap<>();
  private final ReentrantLock sessionLock = new ReentrantLock();

  private Session sessionBase;
  @SuppressWarnings("unused")
  private String rutaRaiz = "default";

  @Override
  public void inicializarCredenciales(String host, String puerto, String usuario, String clave,
      String rutaRaiz, String version) {
    try {
      String atomPubUrl = determinarUrlAtomPub(version);
      String baseUrl = String.format("http://%s:%s/%s", host, puerto, atomPubUrl);

      credencialesConexion.clear();
      credencialesConexion.put(SessionParameter.ATOMPUB_URL, baseUrl);
      credencialesConexion.put(SessionParameter.USER, usuario);
      credencialesConexion.put(SessionParameter.PASSWORD, clave);
      credencialesConexion.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

      this.rutaRaiz = rutaRaiz != null ? rutaRaiz : "default";

      log.info("Credenciales inicializadas para Alfresco en {}:{}", host, puerto);
    } catch (Exception e) {
      log.error("Error inicializando credenciales: {}", e.getMessage(), e);
      throw new RuntimeException("Error inicializando credenciales de Alfresco", e);
    }
  }

  @Override
  public boolean verificarConexion() {
    try (var sessionWrapper = obtenerSesion()) {
      return sessionWrapper.session() != null
          && sessionWrapper.session().getRepositoryInfo() != null;
    } catch (Exception e) {
      log.error("Error verificando conexión: {}", e.getMessage());
      return false;
    }
  }

  @Override
  public String crearCarpeta(String rutaPadre, String nombreCarpeta) {
    try (var sessionWrapper = obtenerSesion()) {
      var session = sessionWrapper.session();
      var carpetaPadre = obtenerCarpeta(rutaPadre, session);

      var propiedadesCarpeta =
          Map.of(PropertyIds.OBJECT_TYPE_ID, FOLDER_TYPE, PropertyIds.NAME, nombreCarpeta);

      var nuevaCarpeta = carpetaPadre.createFolder(propiedadesCarpeta);
      log.info("Carpeta '{}' creada en '{}'", nombreCarpeta, rutaPadre);
      return nuevaCarpeta.getId();

    } catch (CmisContentAlreadyExistsException e) {
      log.warn("La carpeta '{}' ya existe en '{}'", nombreCarpeta, rutaPadre);
      return null;
    } catch (Exception e) {
      log.error("Error creando carpeta '{}' en '{}': {}", nombreCarpeta, rutaPadre, e.getMessage());
      throw new RuntimeException("Error creando carpeta", e);
    }
  }

  @Override
  public String crearCarpetasRecursivo(String rutaCompleta) {
    if (rutaCompleta == null || rutaCompleta.trim().isEmpty()) {
      throw new IllegalArgumentException("La ruta no puede estar vacía");
    }

    // Normalizar la ruta
    String ruta = rutaCompleta.startsWith("/") ? rutaCompleta : "/" + rutaCompleta;
    String[] segmentos = ruta.split("/");

    String rutaActual = "";
    String ultimoId = null;

    for (String segmento : segmentos) {
      if (segmento.trim().isEmpty())
        continue;

      String rutaPadre = rutaActual.isEmpty() ? "/" : rutaActual;
      rutaActual = rutaActual + "/" + segmento;

      if (!existeCarpeta(rutaActual)) {
        ultimoId = crearCarpeta(rutaPadre, segmento);
      }
    }

    return ultimoId;
  }

  @Override
  public boolean existeCarpeta(String ruta) {
    try (var sessionWrapper = obtenerSesion()) {
      var session = sessionWrapper.session();
      var objeto = session.getObjectByPath(ruta);
      return objeto instanceof Folder && ((Folder) objeto).getPath().equals(ruta);
    } catch (CmisObjectNotFoundException e) {
      return false;
    } catch (Exception e) {
      log.error("Error verificando existencia de carpeta '{}': {}", ruta, e.getMessage());
      throw new RuntimeException("Error verificando carpeta", e);
    }
  }

  @Override
  public String subirArchivo(Map<String, Object> propiedades, InputStream inputStream,
      String nombreArchivo, String rutaDestino, String mimeType) {
    try (var sessionWrapper = obtenerSesion()) {
      var session = sessionWrapper.session();
      var carpetaDestino = obtenerCarpeta(rutaDestino, session);

      var contentStream =
          session.getObjectFactory().createContentStream(nombreArchivo, -1, mimeType, inputStream);

      var documento =
          carpetaDestino.createDocument(propiedades, contentStream, VersioningState.MAJOR);

      log.info("Archivo '{}' subido a '{}'", nombreArchivo, rutaDestino);
      return documento.getId();

    } catch (Exception e) {
      log.error("Error subiendo archivo '{}' a '{}': {}", nombreArchivo, rutaDestino,
          e.getMessage());
      throw new RuntimeException("Error subiendo archivo", e);
    }
  }

  @Override
  public String subirArchivo(Map<String, Object> propiedades, byte[] contenido,
      String nombreArchivo, String rutaDestino, String mimeType) {
    try (var inputStream = new ByteArrayInputStream(contenido)) {
      return subirArchivo(propiedades, inputStream, nombreArchivo, rutaDestino, mimeType);
    } catch (IOException e) {
      log.error("Error creando stream para archivo '{}': {}", nombreArchivo, e.getMessage());
      throw new RuntimeException("Error procesando contenido del archivo", e);
    }
  }

  @Override
  public byte[] descargarArchivoPorUuid(String uuidDocumento) {
    try (var sessionWrapper = obtenerSesion()) {
      var session = sessionWrapper.session();
      var objeto = session.getObject(uuidDocumento);

      if (objeto instanceof Document documento) {
        return extraerContenidoDocumento(documento, uuidDocumento);
      } else {
        log.warn("El objeto con UUID '{}' no es un documento", uuidDocumento);
        return null;
      }

    } catch (CmisObjectNotFoundException e) {
      log.warn("No se encontró documento con UUID '{}'", uuidDocumento);
      return null;
    } catch (Exception e) {
      log.error("Error descargando archivo con UUID '{}': {}", uuidDocumento, e.getMessage());
      throw new RuntimeException("Error descargando archivo", e);
    }
  }

  @Override
  public byte[] descargarArchivoPorRuta(String rutaArchivo) {
    try (var sessionWrapper = obtenerSesion()) {
      var session = sessionWrapper.session();
      var objeto = session.getObjectByPath(rutaArchivo);

      if (objeto instanceof Document documento) {
        return extraerContenidoDocumento(documento, rutaArchivo);
      } else {
        log.warn("El objeto en ruta '{}' no es un documento", rutaArchivo);
        return null;
      }

    } catch (CmisObjectNotFoundException e) {
      log.warn("No se encontró archivo en ruta '{}'", rutaArchivo);
      return null;
    } catch (Exception e) {
      log.error("Error descargando archivo en ruta '{}': {}", rutaArchivo, e.getMessage());
      throw new RuntimeException("Error descargando archivo", e);
    }
  }

  @Override
  public boolean existeArchivo(String rutaArchivo) {
    try (var sessionWrapper = obtenerSesion()) {
      var session = sessionWrapper.session();
      var objeto = session.getObjectByPath(rutaArchivo);
      return objeto instanceof Document;
    } catch (CmisObjectNotFoundException e) {
      return false;
    } catch (Exception e) {
      log.error("Error verificando existencia de archivo '{}': {}", rutaArchivo, e.getMessage());
      return false;
    }
  }

  @Override
  public boolean existeArchivo(String rutaCarpeta, String nombreArchivo) {
    String rutaCompleta = construirRutaCompleta(rutaCarpeta, nombreArchivo);
    return existeArchivo(rutaCompleta);
  }

  @Override
  public boolean eliminarArchivo(String rutaArchivo) {
    try (var sessionWrapper = obtenerSesion()) {
      var session = sessionWrapper.session();
      var objeto = session.getObjectByPath(rutaArchivo);
      session.delete(objeto);
      log.info("Archivo eliminado: '{}'", rutaArchivo);
      return true;
    } catch (CmisObjectNotFoundException e) {
      log.warn("No se encontró archivo para eliminar: '{}'", rutaArchivo);
      return false;
    } catch (Exception e) {
      log.error("Error eliminando archivo '{}': {}", rutaArchivo, e.getMessage());
      throw new RuntimeException("Error eliminando archivo", e);
    }
  }

  @Override
  public boolean eliminarArchivo(String rutaCarpeta, String nombreArchivo) {
    String rutaCompleta = construirRutaCompleta(rutaCarpeta, nombreArchivo);
    return eliminarArchivo(rutaCompleta);
  }

  @Override
  public List<String> obtenerContenidoCarpeta(String rutaCarpeta) {
    try (var sessionWrapper = obtenerSesion()) {
      var session = sessionWrapper.session();
      var carpeta = obtenerCarpeta(rutaCarpeta, session);
      var hijos = carpeta.getChildren();

      // Convertir ItemIterable a List usando un bucle tradicional
      List<String> contenido = new ArrayList<>();
      for (CmisObject hijo : hijos) {
        contenido.add(hijo.getName());
      }

      return contenido;

    } catch (Exception e) {
      log.error("Error obteniendo contenido de carpeta '{}': {}", rutaCarpeta, e.getMessage());
      throw new RuntimeException("Error obteniendo contenido de carpeta", e);
    }
  }

  @Override
  public String actualizarDocumento(Map<String, Object> propiedades, byte[] contenido,
      String nombreArchivo, String rutaDestino, String mimeType) {
    
    try (var sessionWrapper = obtenerSesion()) {
      var session = sessionWrapper.session();
      String rutaCompleta = construirRutaCompleta(rutaDestino, nombreArchivo);
      var documento = (Document) session.getObjectByPath(rutaCompleta);

      // Verificar si el documento soporta versionado usando las acciones permitidas
      var accionesPermitidas = documento.getAllowableActions().getAllowableActions();
      boolean esVersionable = accionesPermitidas.contains(Action.CAN_CHECK_OUT)
          && accionesPermitidas.contains(Action.CAN_CHECK_IN);

      if (esVersionable) {
        return actualizarDocumentoVersionable(documento, contenido, nombreArchivo, mimeType,
            session);
      } else {
        // Si no es versionable, actualizar el contenido directamente
        try (var inputStream = new ByteArrayInputStream(contenido)) {
          var contentStream = session.getObjectFactory().createContentStream(nombreArchivo,
              contenido.length, mimeType, inputStream);

          documento.setContentStream(contentStream, true);
          log.info("Documento '{}' actualizado (sin versionado)", nombreArchivo);
          return documento.getId();
        }
      }

    } catch (Exception e) {
      log.error("Error actualizando documento '{}': {}", nombreArchivo, e.getMessage());
      throw new RuntimeException("Error actualizando documento", e);
    }
  }

  @Override
  public String obtenerRutaDocumento(String uuidDocumento) {
    try (var sessionWrapper = obtenerSesion()) {
      var session = sessionWrapper.session();
      var objeto = session.getObject(uuidDocumento);

      if (objeto instanceof Document documento) {
        var rutas = documento.getPaths();
        return rutas.isEmpty() ? "" : rutas.get(0);
      }
      return "";

    } catch (CmisObjectNotFoundException e) {
      log.warn("No se encontró documento con UUID '{}'", uuidDocumento);
      return "";
    } catch (Exception e) {
      log.error("Error obteniendo ruta de documento '{}': {}", uuidDocumento, e.getMessage());
      return "";
    }
  }

  @Override
  public Map<String, Object> ejecutarConsulta(String consulta) {
    try (var sessionWrapper = obtenerSesion()) {
      var session = sessionWrapper.session();
      var resultados = session.query(consulta, false);
      var propiedades = new HashMap<String, Object>();

      for (QueryResult resultado : resultados) {
        for (PropertyData<?> propiedad : resultado.getProperties()) {
          String nombre = propiedad.getQueryName();
          Object valor = propiedad.getFirstValue();
          if (valor != null) {
            propiedades.put(nombre, valor);
          }
        }
      }

      return propiedades.isEmpty() ? null : propiedades;

    } catch (Exception e) {
      log.error("Error ejecutando consulta: {}", e.getMessage());
      throw new RuntimeException("Error ejecutando consulta", e);
    }
  }

  @Override
  public void cerrarSesion() {
    sessionLock.lock();
    try {
      if (sessionBase != null) {
        sessionBase.clear();
        sessionBase = null;
        log.info("Sesión de Alfresco cerrada");
      }
    } finally {
      sessionLock.unlock();
    }
  }

  // Métodos privados auxiliares

  private String determinarUrlAtomPub(String version) {
    return "4.2".equals(version) ? ATOM_PUB_URL_42 : ATOM_PUB_URL_41;
  }

  private SessionWrapper obtenerSesion() {
    sessionLock.lock();
    try {
      if (sessionBase == null) {
        var sessionFactory = SessionFactoryImpl.newInstance();
        var repositorios = sessionFactory.getRepositories(credencialesConexion);
        sessionBase = repositorios.get(0).createSession();
        log.debug("Nueva sesión CMIS creada");
      }
      return new SessionWrapper(sessionBase);
    } finally {
      sessionLock.unlock();
    }
  }

  private Folder obtenerCarpeta(String ruta, Session session) {
    try {
      var objeto = session.getObjectByPath(ruta);
      return (Folder) objeto;
    } catch (ClassCastException e) {
      throw new RuntimeException("El objeto en la ruta '" + ruta + "' no es una carpeta", e);
    }
  }

  private String construirRutaCompleta(String rutaCarpeta, String nombreArchivo) {
    if (rutaCarpeta.endsWith("/")) {
      return rutaCarpeta + nombreArchivo;
    } else {
      return rutaCarpeta + "/" + nombreArchivo;
    }
  }

  private byte[] extraerContenidoDocumento(Document documento, String identificador)
      throws IOException {
    var contentStream = documento.getContentStream();
    if (contentStream != null) {
      byte[] contenido = convertirInputStreamABytes(contentStream.getStream());
      log.info("Archivo descargado correctamente: {}", documento.getName());
      return contenido;
    } else {
      log.warn("El documento '{}' no tiene contenido", identificador);
      return null;
    }
  }

  private byte[] convertirInputStreamABytes(InputStream inputStream) throws IOException {
    try (var outputStream = new ByteArrayOutputStream(); inputStream) {
      byte[] buffer = new byte[BUFFER_SIZE];
      int bytesLeidos;

      while ((bytesLeidos = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesLeidos);
      }

      return outputStream.toByteArray();
    }
  }

  private String actualizarDocumentoVersionable(Document documento, byte[] contenido,
      String nombreArchivo, String mimeType, Session session) {
    try {
      documento.refresh();
      var idCheckOut = documento.checkOut();
      var documentoTrabajo = (Document) session.getObject(idCheckOut);

      try (var inputStream = new ByteArrayInputStream(contenido)) {
        var contentStream = session.getObjectFactory().createContentStream(nombreArchivo,
            contenido.length, mimeType, inputStream);

        var idActualizado =
            documentoTrabajo.checkIn(false, null, contentStream, "Versión actualizada");
        log.info("Documento '{}' actualizado correctamente", nombreArchivo);
        return idActualizado.getId();

      } catch (Exception e) {
        documentoTrabajo.cancelCheckOut();
        throw e;
      }
    } catch (Exception e) {
      log.error("Error en actualización versionable de '{}': {}", nombreArchivo, e.getMessage());
      throw new RuntimeException("Error actualizando documento versionable", e);
    }
  }

  // Record para manejo seguro de sesiones
  private record SessionWrapper(Session session) implements AutoCloseable {
    @Override
    public void close() {
      // Las sesiones se manejan a nivel de instancia, no por operación individual
    }
  }
}
