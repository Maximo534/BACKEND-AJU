package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.exceptions.negocio.MovimientoNoEncontradoException;
import pe.gob.pj.prueba.domain.model.Auditoria;
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
    public void subirArchivo(MultipartFile file, String distritoId, String tipo, String modulo,
                             LocalDate fecha, String codigoEnlace, Auditoria datosAuditoria) throws Exception {

        String sessionKey = UUID.randomUUID().toString();
        try {
            ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

            // 1. Construir ruta FTP
            String carpeta = switch (tipo.toUpperCase()) {
                case "ANEXO" -> "fichas";
                case "VIDEO" -> "videos";
                case "FOTO" -> "fotos";
                case "PPT" -> "ppts";
                case "RESOLUCION_JPE" -> "resoluciones";
                default -> "otros";
            };

            String anio = String.valueOf(fecha.getYear());
            String mes = String.format("%02d", fecha.getMonthValue());

            String rutaRelativa = String.format("%s/%s/%s/%s/%s/%s/%s",
                    ftpRutaBase, distritoId, modulo, carpeta, anio, mes, codigoEnlace);

            String ext = (file.getOriginalFilename() != null && file.getOriginalFilename().contains("."))
                    ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")) : "";

            String nombreFinal = UUID.randomUUID().toString().replace("-", "") + ext;

            // 2. Subir Físico (FTP)
            if (!ftpPort.uploadFileFTP(sessionKey, rutaRelativa + "/" + nombreFinal, file.getInputStream(), "Carga " + tipo)) {
                throw new Exception("Fallo FTP al subir " + nombreFinal);
            }

            // 3. Preparar Dominio con Auditoría
            Archivo archivoDomain = Archivo.builder()
                    .nombre(nombreFinal)
                    .tipo(tipo.toLowerCase())
                    .ruta(rutaRelativa)
                    .numeroIdentificacion(codigoEnlace) // Enlace por CÓDIGO (String)
                    .build();

            archivoDomain.setUsuario(datosAuditoria.getUsuario());
            archivoDomain.setNumeroIp(datosAuditoria.getNumeroIp());
            archivoDomain.setNombrePc(datosAuditoria.getNombrePc());
            archivoDomain.setDireccionMac(datosAuditoria.getDireccionMac());
            archivoDomain.setRed(datosAuditoria.getRed());

            // 4. Guardar Metadatos en BD
            archivosPersistencePort.guardarReferenciaArchivo(archivoDomain);

        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
    }

    @Override
    public void eliminarPorId(Long idArchivo, Auditoria datosAuditoria) throws Exception {
        // 1. Buscar metadatos en BD para saber la ruta física
        Archivo archivo = archivosPersistencePort.buscarPorId(idArchivo);
        if (archivo == null) {
            throw new MovimientoNoEncontradoException("El archivo con ID " + idArchivo + " no existe.");
        }

        // 2. Borrar del FTP
        String rutaCompleta = archivo.getRuta() + "/" + archivo.getNombre();
        String sessionKey = UUID.randomUUID().toString();

        try {
            ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
            ftpPort.deleteFileFTP(rutaCompleta);
        } catch (Exception e) {
            log.warn("No se pudo borrar físico del FTP (o no existía): {}", e.getMessage());
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }

        // 3. Borrado Lógico en BD usando el ID
        archivosPersistencePort.eliminarReferenciaPorId(
                idArchivo,
                datosAuditoria.getUsuario(),
                datosAuditoria.getNumeroIp(),
                datosAuditoria.getNombrePc(),
                datosAuditoria.getDireccionMac()
        );
    }

    @Override
    public RecursoArchivo descargarPorId(Long idArchivo) throws Exception {
        // 1. Buscar metadatos
        Archivo archivoBd = archivosPersistencePort.buscarPorId(idArchivo);
        if (archivoBd == null) {
            throw new MovimientoNoEncontradoException("El archivo no existe o fue eliminado.");
        }

        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("down_", ".tmp");
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
                    rutaCompleta += archivo.getNombre();

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