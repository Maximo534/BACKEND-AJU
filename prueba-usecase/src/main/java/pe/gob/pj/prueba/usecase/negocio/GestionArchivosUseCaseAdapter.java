package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.port.files.FtpPort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionArchivosUseCasePort;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class GestionArchivosUseCaseAdapter implements GestionArchivosUseCasePort {

    private final FtpPort ftpPort;
    private final GestionArchivosPersistencePort archivosPersistencePort;

    @Value("${ftp.ip}") private String ftpIp;
    @Value("${ftp.puerto}") private Integer ftpPuerto;
    @Value("${ftp.usuario}") private String ftpUsuario;
    @Value("${ftp.clave}") private String ftpClave;
    @Value("${ftp.ruta-base:/evidencias}") private String ftpRutaBase;

    @Override
    public void subirArchivo(MultipartFile file, String distritoId, String tipo, LocalDate fecha, String idRegistro) throws Exception {
        String sessionKey = UUID.randomUUID().toString();
        try {
            ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

            String carpeta = switch (tipo.toUpperCase()) {
                case "ANEXO" -> "fichas";
                case "VIDEO" -> "videos";
                case "FOTO" -> "fotos";
                default -> "otros";
            };

            String anio = String.valueOf(fecha.getYear());
            String mes = String.format("%02d", fecha.getMonthValue());

            // Ruta estandarizada: /evidencias/DISTRITO/evidencias_gen/CARPETA/ANIO/MES/ID
            String rutaRelativa = String.format("%s/%s/evidencias_gen/%s/%s/%s/%s", ftpRutaBase, distritoId, carpeta, anio, mes, idRegistro);

            String ext = (file.getOriginalFilename() != null && file.getOriginalFilename().contains("."))
                    ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")) : "";

            String nombreFinal = UUID.randomUUID().toString().replace("-", "") + ext;

            if (!ftpPort.uploadFileFTP(sessionKey, rutaRelativa + "/" + nombreFinal, file.getInputStream(), "Carga " + tipo)) {
                throw new Exception("Fallo FTP al subir " + nombreFinal);
            }

            archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                    .nombre(nombreFinal)
                    .tipo(tipo.toLowerCase())
                    .ruta(rutaRelativa)
                    .numeroIdentificacion(idRegistro)
                    .build());

        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
    }

    @Override
    public RecursoArchivo descargarPorNombre(String nombreArchivo) throws Exception {
        Archivo archivoBd = archivosPersistencePort.buscarPorNombre(nombreArchivo);
        if (archivoBd == null) throw new Exception("El archivo no existe en la base de datos.");

        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("temp_download_", ".tmp");
        String sessionKey = UUID.randomUUID().toString();

        ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

        try {
            String rutaCompletaFtp = archivoBd.getRuta() + "/" + archivoBd.getNombre();

            InputStream ftpStream = ftpPort.downloadFileStream(sessionKey, rutaCompletaFtp);
            if (ftpStream == null) throw new Exception("Archivo físico no encontrado en FTP.");

            try (java.io.OutputStream tempStream = java.nio.file.Files.newOutputStream(tempFile)) {
                ftpStream.transferTo(tempStream);
            }
            ftpStream.close();

        } catch (Exception e) {
            java.nio.file.Files.deleteIfExists(tempFile);
            throw e;
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }

        InputStream autoDeleteStream = new java.io.FileInputStream(tempFile.toFile()) {
            @Override public void close() throws java.io.IOException {
                super.close();
                java.nio.file.Files.deleteIfExists(tempFile);
            }
        };

        return RecursoArchivo.builder()
                .stream(autoDeleteStream)
                .nombreFileName(archivoBd.getNombre())
                .build();
    }

    @Override
    public void eliminarPorNombre(String nombreArchivo) throws Exception {
        // 1. Buscamos en BD
        Archivo archivo = archivosPersistencePort.buscarPorNombre(nombreArchivo);
        if (archivo == null) throw new Exception("Archivo no encontrado en BD.");

        String rutaCompleta = archivo.getRuta() + "/" + archivo.getNombre();
        String sessionKey = UUID.randomUUID().toString();

        ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try {
            boolean borrado = ftpPort.deleteFileFTP(rutaCompleta);
            if (!borrado) log.warn("El archivo no existía en el FTP o no se pudo borrar: {}", rutaCompleta);
        } catch (Exception e) {
            log.warn("Error de conexión FTP al intentar borrar. Se procederá a borrar de BD. Error: {}", e.getMessage());
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }

        archivosPersistencePort.eliminarReferenciaArchivo(nombreArchivo);
    }

    @Override
    public RecursoArchivo descargarListaComoZip(List<Archivo> listaArchivos, String nombreZipSalida) throws Exception {
        log.info("Generando ZIP masivo para {} archivos...", listaArchivos.size());

        java.nio.file.Path tempZip = java.nio.file.Files.createTempFile("masivo_temp_", ".zip");
        String sessionKey = UUID.randomUUID().toString();

        try (FileOutputStream fos = new FileOutputStream(tempZip.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

            int contador = 0;
            for (Archivo archivo : listaArchivos) {
                try {
                    if (archivo.getRuta() == null || archivo.getNombre() == null) continue;

                    String rutaCompleta = archivo.getRuta();
                    if (!rutaCompleta.endsWith("/")) rutaCompleta += "/";
                    if (!rutaCompleta.endsWith(archivo.getNombre())) rutaCompleta += archivo.getNombre();

                    InputStream ftpStream = ftpPort.downloadFileStream(sessionKey, rutaCompleta);

                    if (ftpStream != null) {
                        String nombreEnZip = String.format("%03d_%s", ++contador, archivo.getNombre());
                        zos.putNextEntry(new ZipEntry(nombreEnZip));
                        ftpStream.transferTo(zos);
                        zos.closeEntry();
                        ftpStream.close();

                        ftpPort.completarTransferencia(sessionKey);
                    }
                } catch (Exception e) {
                    log.error("Error al procesar archivo en ZIP: " + archivo.getNombre(), e);
                }
            }
        } catch (Exception e) {
            java.nio.file.Files.deleteIfExists(tempZip);
            throw new Exception("Error al generar ZIP: " + e.getMessage());
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }

        InputStream autoDeleteStream = new java.io.FileInputStream(tempZip.toFile()) {
            @Override public void close() throws IOException {
                super.close();
                java.nio.file.Files.deleteIfExists(tempZip);
            }
        };

        return RecursoArchivo.builder()
                .nombreFileName(nombreZipSalida)
                .stream(autoDeleteStream)
                .build();
    }

}