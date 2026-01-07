package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.negocio.Documento;
import pe.gob.pj.prueba.domain.port.files.FtpPort; // ✅ Usamos el Puerto FTP existente
import pe.gob.pj.prueba.domain.port.persistence.negocio.DocumentoPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionDocumentosUseCasePort;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GestionDocumentosUseCaseAdapter implements GestionDocumentosUseCasePort {

    private final DocumentoPersistencePort persistencePort;
    private final FtpPort ftpPort; // ✅ Reemplazamos FileStoragePort por FtpPort

    // ✅ Inyectamos las mismas credenciales que usas en Promoción
    @Value("${ftp.ip}") private String ftpIp;
    @Value("${ftp.puerto}") private Integer ftpPuerto;
    @Value("${ftp.usuario}") private String ftpUsuario;
    @Value("${ftp.clave}") private String ftpClave;
    @Value("${ftp.ruta-base:/evidencias}") private String ftpRutaBase;

    @Override
    @Transactional(readOnly = true)
    public List<Documento> listarDocumentos(String tipo) {
        return persistencePort.buscarPorTipoYActivo(tipo, "1").stream()
                .map(doc -> {
                    // Generamos URL virtual para descargar (apunta al controller)
                    doc.setUrlDescarga("/publico/v1/documentos/descargar/" + doc.getId());
                    return doc;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Documento registrarDocumento(MultipartFile archivo, String tipo, Integer categoriaId) throws Exception {

        // 1. Validar extensión y nombre
        String originalFilename = archivo.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            extension = ".dat"; // Default por seguridad
        }

        int anioActual = LocalDate.now().getYear();

        // 2. Definir ruta en el FTP
        // Estructura: /evidencias/documentos_gestion/2025/
        String rutaCarpetaFtp = String.format("%s/documentos_gestion/%d", ftpRutaBase, anioActual);

        // Generar nombre único UUID para evitar colisiones
        String nombreUnico = UUID.randomUUID().toString() + extension;
        String rutaCompletaArchivo = rutaCarpetaFtp + "/" + nombreUnico;

        // 3. Subir archivo al FTP
        try {
            // Iniciamos sesión (igual que en Promoción)
            ftpPort.iniciarSesion("SISTEMA_DOCUMENTOS", ftpIp, ftpPuerto, ftpUsuario, ftpClave);

            try (InputStream is = archivo.getInputStream()) {
                boolean subido = ftpPort.uploadFileFTP("SISTEMA_DOCUMENTOS", rutaCompletaArchivo, is, "Carga Documento Gestión");
                if (!subido) {
                    throw new Exception("Error al subir el archivo al servidor FTP.");
                }
            }

        } catch (Exception e) {
            log.error("Error conexión FTP Documentos", e);
            throw new Exception("No se pudo conectar al repositorio de archivos FTP.");
        } finally {
            // Cerramos sesión siempre
            ftpPort.finalizarSession("SISTEMA_DOCUMENTOS");
        }

        // 4. Crear Dominio para guardar en BD
        Documento nuevoDoc = Documento.builder()
                .nombre(originalFilename) // Guardamos el nombre original para mostrarlo
                .tipo(tipo)
                .formato(extension.replace(".", "").toUpperCase()) // Ej: PDF
                .rutaArchivo(rutaCompletaArchivo) // Guardamos la ruta del FTP
                .periodo(anioActual)
                .activo("1")
                .categoriaId(categoriaId)
                .build();

        // 5. Guardar en BD
        return persistencePort.guardar(nuevoDoc);
    }
}