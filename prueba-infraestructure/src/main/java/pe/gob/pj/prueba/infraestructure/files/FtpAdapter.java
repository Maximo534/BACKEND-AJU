package pe.gob.pj.prueba.infraestructure.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.port.files.FtpPort;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.stereotype.Component;

/**
 * 
 * @author oruizb
 *
 */
@Slf4j
@Component("ftpPort")
public class FtpAdapter implements FtpPort {

  FTPClient ftp = new FTPClient();
  @Override
  public void iniciarSesion(String cuo, String ip, Integer puerto, String usuario, String clave)
          throws Exception {

    // 1. Conectar
    ftp.connect(ip, puerto);
    int reply = ftp.getReplyCode();
    if (!FTPReply.isPositiveCompletion(reply)) {
      ftp.disconnect();
      throw new Exception("No se pudo conectar al servidor FTP. Código respuesta: " + reply);
    }

    // 2. Iniciar Sesión (AQUÍ ESTABA EL HUECO)
    boolean loginExitoso = ftp.login(usuario, clave);
    if (!loginExitoso) {
      ftp.logout(); // Cerrar intento
      ftp.disconnect(); // Desconectar
      throw new Exception("Error de autenticación FTP: Usuario o clave incorrectos.");
    }
  }
//  @Override
//  public void iniciarSesion(String cuo, String ip, Integer puerto, String usuario, String clave)
//          throws Exception {
//    // try {
//    // Conectar al servidor FTP
//    ftp.connect(ip, puerto);
//    // Iniciar sesión en el servidor ftp
//    ftp.login(usuario, clave);
//    // } catch (SocketException ex) {
//    // log.error("{} [{}|{}] Se produjo un error con la conexión al ftp.", cuo, ip, usuario);
//    // } catch (Exception e) {
//    // log.error("{} [{}|{}] Ocurrió un error inesperado al conectarse.", cuo, ip);
//    // }
//  }

  @Override
  public void finalizarSession(String cuo) throws Exception {
    // try {
    if (ftp.isConnected()) {
      ftp.logout();
      ftp.disconnect();
    }
    // } catch (IOException e) {
    // e.printStackTrace();
    // log.error("{} [{}|{}] Ocurrió un error inesperado al desconectarse del ftp.", cuo);
    // }
  }

  @Override
  public boolean uploadFileFTP(String cuo, String srcFtpPDF, InputStream inputStream, String desc)
          throws Exception {
    boolean result = false;

    ftp.setFileType(FTP.BINARY_FILE_TYPE);
    ftp.enterLocalPassiveMode();

    int reply = ftp.getReplyCode();
    if (FTPReply.isPositiveCompletion(reply)) {

      // --- CORRECCIÓN PARA WINDOWS ---
      // 1. Normalizamos la ruta para que siempre use "/" (incluso en Windows)
      String rutaNormalizada = srcFtpPDF.replace("\\", "/");

      // 2. Extraemos la carpeta padre manualmente (sin usar java.io.File)
      String ftpDirPath = "";
      if (rutaNormalizada.contains("/")) {
        ftpDirPath = rutaNormalizada.substring(0, rutaNormalizada.lastIndexOf("/"));
      }

      // 3. Aseguramos iniciar desde la raíz
      ftp.changeWorkingDirectory("/");

      // 4. Recorremos carpeta por carpeta
      for (String dir : ftpDirPath.split("/")) {
        if (!dir.isEmpty()) {
          // Intentamos entrar, si falla, creamos
          if (!ftp.changeWorkingDirectory(dir)) {
            if (!ftp.makeDirectory(dir)) {
              throw new IOException("No se puede crear el directorio '" + dir + "'. error='"
                      + ftp.getReplyString() + "'");
            }
            if (!ftp.changeWorkingDirectory(dir)) {
              throw new IOException("No se puede cambiar al directorio recién creado '" + dir
                      + "'. error='" + ftp.getReplyString() + "'");
            }
          }
        }
      }

      // 5. Guardamos el archivo (usando solo el nombre final, pues ya estamos en la carpeta)
      String nombreArchivo = rutaNormalizada.substring(rutaNormalizada.lastIndexOf("/") + 1);
      OutputStream outputStream = ftp.storeFileStream(nombreArchivo);

      if (outputStream != null) {
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = inputStream.read(bytesIn)) != -1) {
          outputStream.write(bytesIn, 0, read);
        }
        inputStream.close();
        outputStream.close();

        result = ftp.completePendingCommand();
        log.info("{} Se guardo el archivo {} de manera correcta.", cuo, srcFtpPDF);
      } else {
        log.info(
                "{} No se pudo guardar el documento {}. La ubicación no se encontró",
                cuo, srcFtpPDF);
      }

    } else {
      log.info("{} No se pudo validar la conexión al FTP, para subir el documento {}", cuo,
              srcFtpPDF);
      ftp.disconnect();
    }

    return result;
  }

//  @Override
//  public boolean uploadFileFTP(String cuo, String srcFtpPDF, InputStream inputStream, String desc)
//      throws Exception {
//    // Long tiempoInicial = System.currentTimeMillis();
//    boolean result = false;
//    // try {
//    ftp.setFileType(FTP.BINARY_FILE_TYPE);
//    ftp.enterLocalPassiveMode();
//    int reply = ftp.getReplyCode();
//    if (FTPReply.isPositiveCompletion(reply)) {
//      // Crear directorio, en caso es necesario
//      File fileFtp = new File(srcFtpPDF);
//      String ftpDirPath = fileFtp.getParent();
//      for (String dir : ftpDirPath.split("/")) {
//        if (!dir.isEmpty()) {
//          if (!ftp.changeWorkingDirectory(dir)) {
//            if (!ftp.makeDirectory(dir)) {
//              throw new IOException("No se puede crear el directorio '" + dir + "'.  error='"
//                  + ftp.getReplyString() + "'");
//            }
//            if (!ftp.changeWorkingDirectory(dir)) {
//              throw new IOException("No se puede cambiar al directorio recién creado '" + dir
//                  + "'.  error='" + ftp.getReplyString() + "'");
//            }
//          }
//        }
//      }
//      OutputStream outputStream = ftp.storeFileStream(srcFtpPDF);
//      if (outputStream != null) {
//        byte[] bytesIn = new byte[4096];
//        int read = 0;
//        while ((read = inputStream.read(bytesIn)) != -1) {
//          outputStream.write(bytesIn, 0, read);
//        }
//        inputStream.close();
//        outputStream.close();
//
//        result = ftp.completePendingCommand();
//        log.info("{} Se guardo el archivo {} de manera correcta.", cuo, srcFtpPDF);
//      } else {
//        log.info(
//            "{} No se pudo guardar el documento {}. La ubiación en donde se quiere guardar no se encontró",
//            cuo, srcFtpPDF);
//      }
//
//    } else {
//      log.info("{}  No se pudo validar la conexión al FTP, para subir el documento {}", cuo,
//          srcFtpPDF);
//      ftp.disconnect();
//    }
//
//    // } catch (IOException e) {
//    // log.error("{} No se pudo subir el archivo {}", cuo, srcFtpPDF);
//    // e.printStackTrace();
//    // } catch (Exception ex) {
//    // log.error("{} Ocurrió un error inesperado al subir el archivo {}", cuo, srcFtpPDF);
//    // }
//    return result;
//  }

  @Override
  public byte[] downloadFileBytes(String cuo, String ruta) throws Exception {
    byte[] bytes = null;
    // try {
    ftp.enterLocalPassiveMode();
    ftp.setFileType(FTP.BINARY_FILE_TYPE);
    int reply = ftp.getReplyCode();
    if (FTPReply.isPositiveCompletion(reply)) {

      InputStream inputStream = ftp.retrieveFileStream(ruta);
      if (inputStream != null) {
        bytes = IOUtils.toByteArray(inputStream);
        inputStream.close();
        log.info("{} Se descargo el archivo {} de manera correcta.", cuo, ruta);
      } else {
        log.warn("{} El archivo {} no se encontró en la ruta indicada.", cuo, ruta);
      }
    } else {
      log.error("{}  No se pudo validar la conexión al FTP, para descargar el documento {}", cuo,
          ruta);
      ftp.disconnect();
    }
    // } catch (IOException e) {
    // log.error("{} No se pudo descargar el archivo {}. (IOException : {})", cuo, ruta, e);
    // } catch (Exception e) {
    // log.error("{} Ocurrió un error inesperado al descargar el archivo {}. (Exception : {})", cuo,
    // ruta, e);
    // }
    return bytes;
  }

  @Override
  public InputStream downloadFileStream(String cuo, String ruta) throws Exception {
    InputStream inputStream = null;
    // try {
    ftp.enterLocalPassiveMode();
    ftp.setFileType(FTP.BINARY_FILE_TYPE);
    int reply = ftp.getReplyCode();
    if (FTPReply.isPositiveCompletion(reply)) {
      inputStream = ftp.retrieveFileStream(ruta);
      if (inputStream != null) {
        log.info("{} Se obtuvo el flujo de datos del archivo {} de manera correcta.", cuo, ruta);
      } else {
        log.warn("{} El archivo {} no se encontró en la ruta indicada.", cuo, ruta);
      }
    } else {
      log.error("{}  No se pudo validar la conexión al FTP, para descargar el documento {}", cuo,
          ruta);
      ftp.disconnect();
    }
    // } catch (IOException e) {
    // log.error("{} No se pudo descargar el archivo {}. (IOException : {})", cuo, ruta, e);
    // } catch (Exception e) {
    // log.error("{} Ocurrió un error inesperado al descargar el archivo {}. (Exception : {})", cuo,
    // ruta, e);
    // }
    return inputStream;
  }


  @Override
  public boolean deleteFileFTP(String rutaCompleta) throws Exception {
    boolean deleted = false;
    try {
      deleted = ftp.deleteFile(rutaCompleta);

      if (deleted) {
        log.info("Archivo eliminado del FTP correctamente: {}", rutaCompleta);
      } else {
        log.warn("No se pudo eliminar el archivo del FTP (¿Quizás no existe?): {}", rutaCompleta);
      }

    } catch (Exception e) {
      log.error("Error al eliminar archivo del FTP: {}", e.getMessage());
      throw new Exception("Error FTP al eliminar: " + e.getMessage());
    }
    return deleted;
  }

  @Override
  public InputStream descargarArchivo(String rutaCompleta) throws Exception {
    try {
      InputStream inputStream = ftp.retrieveFileStream(rutaCompleta);
      if (inputStream == null) {
        throw new Exception("No se pudo obtener el stream del archivo (¿Existe?): " + rutaCompleta);
      }
      return inputStream;
    } catch (Exception e) {
      log.error("Error descargando archivo FTP", e);
      throw e;
    }
  }

}
