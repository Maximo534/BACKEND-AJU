package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.port.files.FtpPort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GenerarDescargaMasivaUseCasePort;
@Slf4j
@Service
@RequiredArgsConstructor
public class GenerarDescargaMasivaUseCase implements GenerarDescargaMasivaUseCasePort {

    private final GestionArchivosPersistencePort archivosPersistencePort;
    private final FtpPort ftpPort;

    @Value("${ftp.ip}") private String ftpIp;
    @Value("${ftp.puerto}") private Integer ftpPuerto;
    @Value("${ftp.usuario}") private String ftpUsuario;
    @Value("${ftp.clave}") private String ftpClave;

    @Override
    public RecursoArchivo generarZipMasivo(String modulo, String tipoArchivo, Integer anio, Integer mes) throws Exception {

        // Validaciones básicas
        if (!"evidencias_fji".equalsIgnoreCase(modulo)) {

            throw new Exception("Módulo no soportado: " + modulo);
        }

        List<Archivo> listaArchivos = archivosPersistencePort.listarParaDescargaMasiva(tipoArchivo, anio, mes);

        if (listaArchivos == null || listaArchivos.isEmpty()) {
            throw new Exception("No se encontraron archivos para los filtros seleccionados.");
        }

        log.info("Iniciando generación de ZIP masivo. Cantidad archivos: {}", listaArchivos.size());

        // Crear Archivo Temporal en disco para el ZIP (Evita llenar la RAM del servidor)
        Path tempZip = Files.createTempFile("masivo_" + anio + "_" + mes + "_", ".zip");
        String sessionKey = UUID.randomUUID().toString();

        try (FileOutputStream fos = new FileOutputStream(tempZip.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            // Iniciar sesión FTP una sola vez para toda la operación
            ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

            int contador = 0;
            for (Archivo archivo : listaArchivos) {
                try {
                    // Validar datos mínimos
                    if (archivo.getRuta() == null || archivo.getNombre() == null) {
                        continue;
                    }

                    // Armamos la ruta completa: Carpeta + Separador + NombreArchivo
                    String rutaCompleta = archivo.getRuta();

                    // Si la ruta no termina en '/', se lo agregamos
                    if (!rutaCompleta.endsWith("/")) {
                        rutaCompleta += "/";
                    }

                    // 2. Le pegamos el nombre del archivo (ej: foto1.jpg)
                    if (!rutaCompleta.endsWith(archivo.getNombre())) {
                        rutaCompleta += archivo.getNombre();
                    }

                    log.info("Descargando archivo real: {}", rutaCompleta);

                    // pedimos el ARCHIVO (con extensión), no la carpeta
                    InputStream ftpStream = ftpPort.downloadFileStream(sessionKey, rutaCompleta);

                    if (ftpStream != null) {
                        // 1. Agregar al ZIP
                        String nombreEnZip = String.format("%03d_%s", ++contador, archivo.getNombre());
                        ZipEntry zipEntry = new ZipEntry(nombreEnZip);
                        zos.putNextEntry(zipEntry);

                        ftpStream.transferTo(zos);

                        zos.closeEntry();

                        // Cerrar el flujo (Esto cierra tu lectura local)
                        ftpStream.close();

                        // Avisar al FTP que terminamos para liberar el canal
                        // Sin esto, el siguiente archivo fallará.
                        ftpPort.completarTransferencia(sessionKey);

                    } else {
                        log.warn("El archivo no existe en el FTP: {}", rutaCompleta);
                    }
                } catch (Exception e) {
                    log.error("Error al procesar archivo: " + archivo.getNombre(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error crítico generando ZIP", e);
            Files.deleteIfExists(tempZip); // Limpieza si falló el proceso global
            throw new Exception("Error al generar el archivo comprimido: " + e.getMessage());
        } finally {
            // Asegurar cierre de sesión FTP
            try {
                ftpPort.finalizarSession(sessionKey);
            } catch (Exception ex) {
                log.warn("Error cerrando sesión FTP", ex);
            }
        }

        // Preparar el Stream de retorno (Autodestrucción del temporal al terminar la descarga http)
        InputStream autoDeleteStream = new FileInputStream(tempZip.toFile()) {
            @Override
            public void close() throws IOException {
                super.close();
                Files.deleteIfExists(tempZip);
                log.info("Archivo temporal ZIP eliminado correctamente: {}", tempZip);
            }
        };

        return RecursoArchivo.builder()
                .nombreFileName("Reporte_" + modulo + "_" + anio + "_" + mes + ".zip")
                .stream(autoDeleteStream)
                .build();
    }
}