package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.OrientadoraJudicial;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.files.FtpPort;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.OrientadoraJudicialPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionOrientadorasUseCasePort;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestionOrientadorasUseCaseAdapter implements GestionOrientadorasUseCasePort {

    private final OrientadoraJudicialPersistencePort persistencePort;
    private final GestionArchivosPersistencePort archivosPersistencePort;
    private final FtpPort ftpPort;
    private final GenerarReportePort reportePort;

    @Value("${ftp.ip}") private String ftpIp;
    @Value("${ftp.puerto}") private Integer ftpPuerto;
    @Value("${ftp.usuario}") private String ftpUsuario;
    @Value("${ftp.clave}") private String ftpClave;
    @Value("${ftp.ruta-base:/evidencias}") private String ftpRutaBase;

    @Override
    public Pagina<OrientadoraJudicial> listar(String usuario, OrientadoraJudicial filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listar(usuario, filtros, pagina, tamanio);
    }

    @Override
    public OrientadoraJudicial buscarPorId(String id) throws Exception {
        OrientadoraJudicial encontrado = persistencePort.buscarPorId(id);
        if (encontrado == null) throw new Exception("Registro no encontrado");
        return encontrado;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrientadoraJudicial registrarAtencion(OrientadoraJudicial oj, MultipartFile anexo, List<MultipartFile> fotos, String usuario) throws Exception {

        //Generar ID
        String ultimoId = persistencePort.obtenerUltimoId();
        long siguiente = 1;
        if (ultimoId != null && !ultimoId.isBlank()) {
            try { siguiente = Long.parseLong(ultimoId.substring(0, 6)) + 1; } catch (Exception e) { siguiente = 1; }
        }
        String numeroStr = String.format("%06d", siguiente);
        String anio = String.valueOf(LocalDate.now().getYear());
        String corte = oj.getDistritoJudicialId() != null ? oj.getDistritoJudicialId() : "00";
        String idGenerado = String.format("%s-%s-%s-OJ", numeroStr, corte, anio);

        oj.setId(idGenerado);
        oj.setUsuarioRegistro(usuario);
        if(oj.getFechaAtencion() == null) oj.setFechaAtencion(LocalDate.now());

        // Guardar Datos
        OrientadoraJudicial registrado = persistencePort.guardar(oj);

        // Subir Archivos
        String sessionKey = UUID.randomUUID().toString();
        try {
            ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

            // Subir Anexo
            if (anexo != null && !anexo.isEmpty()) {
                uploadFile(anexo, registrado, "ANEXO_OJ", sessionKey);
            }

            // Subir Fotos
            if (fotos != null && !fotos.isEmpty()) {
                for (MultipartFile foto : fotos) {
                    if (foto != null && !foto.isEmpty()) {
                        // Se reutiliza uploadFile
                        uploadFile(foto, registrado, "FOTO_OJ", sessionKey);
                    }
                }
            }
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }

        return registrado;
    }

    @Override
    @Transactional
    public OrientadoraJudicial actualizar(OrientadoraJudicial dominio, String usuario) throws Exception {
        if(dominio.getId() == null) throw new Exception("ID obligatorio");
        dominio.setUsuarioRegistro(usuario);
        return persistencePort.actualizar(dominio);
    }

    @Override
    @Transactional
    public void agregarArchivo(String idCaso, MultipartFile archivo, String tipo, String usuario) throws Exception {
        OrientadoraJudicial oj = persistencePort.buscarPorId(idCaso);
        if(oj == null) throw new Exception("No existe registro");

        String sessionKey = UUID.randomUUID().toString();
        try {
            ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
            uploadFile(archivo, oj, tipo, sessionKey);
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
    }

    @Override
    @Transactional
    public void eliminarArchivo(String nombreArchivo) throws Exception {
        Archivo archivo = archivosPersistencePort.buscarPorNombre(nombreArchivo);
        if (archivo == null) throw new Exception("Archivo no existe");

        String sessionKey = UUID.randomUUID().toString();
        ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try {
            ftpPort.deleteFileFTP(archivo.getRuta() + "/" + archivo.getNombre());
        } catch (Exception e) {
            log.warn("Error borrado físico: {}", e.getMessage());
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
        archivosPersistencePort.eliminarReferenciaArchivo(nombreArchivo);
    }

    @Override
    public RecursoArchivo descargarAnexo(String id, String tipoArchivo) throws Exception {
        // Buscar metadatos en BD
        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(id);
        Archivo encontrado = archivos.stream()
                .filter(a -> a.getTipo().equalsIgnoreCase(tipoArchivo))
                .findFirst()
                .orElseThrow(() -> new Exception("Archivo no encontrado: " + tipoArchivo));

        // Preparar temporal
        String sessionKey = UUID.randomUUID().toString();
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("oj_" + tipoArchivo, ".tmp");

        //Descarga FTP segura
        ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

        try {
            String rutaCompleta = encontrado.getRuta() + "/" + encontrado.getNombre();
            InputStream is = ftpPort.downloadFileStream(sessionKey, rutaCompleta);

            if (is == null) {
                throw new Exception("El archivo no existe en el servidor FTP.");
            }

            try (java.io.OutputStream os = java.nio.file.Files.newOutputStream(tempFile)) {
                is.transferTo(os);
            }
            is.close();

        } catch (Exception e) {
            java.nio.file.Files.deleteIfExists(tempFile);
            throw e;
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }

        //Retorno con auto-borrado
        return RecursoArchivo.builder()
                .stream(new java.io.FileInputStream(tempFile.toFile()) {
                    @Override public void close() throws java.io.IOException {
                        super.close();
                        java.nio.file.Files.deleteIfExists(tempFile);
                    }
                })
                .nombreFileName(encontrado.getNombre())
                .build();
    }
    @Override
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        return persistencePort.obtenerResumenGrafico();
    }

    private void uploadFile(MultipartFile file, OrientadoraJudicial oj, String tipo, String sessionKey) throws Exception {
        String carpeta = switch (tipo.toUpperCase()) {
            case "ANEXO_OJ" -> "anexos";
            case "FOTO_OJ" -> "fotos";
            default -> "otros";
        };

        String anio = String.valueOf(oj.getFechaAtencion().getYear());
        String rutaBase = String.format("%s/oj/%s/%s", ftpRutaBase, carpeta, anio);

        String ext = obtenerExtension(file.getOriginalFilename());

        // Usamos UUID parcial
        String uniqueId = UUID.randomUUID().toString().substring(0,4);
        String nombreFinal = oj.getId() + "_" + tipo + "_" + System.currentTimeMillis() + "_" + uniqueId + ext;

        String rutaCompleta = rutaBase + "/" + nombreFinal;

        if (!ftpPort.uploadFileFTP(sessionKey, rutaCompleta, file.getInputStream(), tipo)) {
            throw new Exception("Fallo FTP al subir " + nombreFinal);
        }

        archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                .nombre(nombreFinal)
                .tipo(tipo)
                .ruta(rutaBase)
                .numeroIdentificacion(oj.getId())
                .build());
    }

    private String obtenerExtension(String nombre) {
        return (nombre != null && nombre.lastIndexOf(".") != -1) ? nombre.substring(nombre.lastIndexOf(".")) : "";
    }

    @Override
    public byte[] generarFichaPdf(String id) throws Exception {
        if(persistencePort.buscarPorId(id) == null) throw new Exception("Evento no existe");
        return reportePort.generarFichaOJ(id);
    }

}