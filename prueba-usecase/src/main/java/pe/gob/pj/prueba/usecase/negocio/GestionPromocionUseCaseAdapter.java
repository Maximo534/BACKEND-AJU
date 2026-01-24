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
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante;
import pe.gob.pj.prueba.domain.model.negocio.PromocionCultura;
import pe.gob.pj.prueba.domain.port.files.FtpPort;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.PromocionCulturaPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionPromocionUseCasePort;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestionPromocionUseCaseAdapter implements GestionPromocionUseCasePort {

    private final PromocionCulturaPersistencePort persistencePort;
    private final GestionArchivosPersistencePort archivosPersistencePort;
    private final FtpPort ftpPort;
    private final GenerarReportePort reportePort;

    @Value("${ftp.ip}") private String ftpIp;
    @Value("${ftp.puerto}") private Integer ftpPuerto;
    @Value("${ftp.usuario}") private String ftpUsuario;
    @Value("${ftp.clave}") private String ftpClave;
    @Value("${ftp.ruta-base:/evidencias}") private String ftpRutaBase;

    @Override
    public Pagina<PromocionCultura> listar(String usuario, PromocionCultura filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listar(usuario, filtros, pagina, tamanio);
    }

    @Override
    public PromocionCultura buscarPorId(String id) throws Exception {
        PromocionCultura encontrado = persistencePort.obtenerPorId(id);
        if (encontrado == null) throw new Exception("Evento no encontrado.");
        return encontrado;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromocionCultura registrar(PromocionCultura dominio, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos, String usuario) throws Exception {

        if (dominio.getTareasRealizadas() != null) {
            for (PromocionCultura.DetalleTarea tarea : dominio.getTareasRealizadas()) {
                if (tarea.getFechaInicio() == null) {
                    tarea.setFechaInicio(dominio.getFechaInicio());
                }
            }
        }
        //Generar ID: 000001-15-2025-CJ
        String ultimoId = persistencePort.obtenerUltimoId();
        long siguiente = 1;
        if (ultimoId != null) {
            try { siguiente = Long.parseLong(ultimoId.split("-")[0]) + 1; } catch (Exception e) { siguiente = 1; }
        }
        String anio = String.valueOf(LocalDate.now().getYear());
        String idGenerado = String.format("%06d-%s-%s-CJ", siguiente, dominio.getDistritoJudicialId(), anio);

        dominio.setId(idGenerado);
        dominio.setUsuarioRegistro(usuario);
        dominio.setFechaRegistro(LocalDate.now());
        dominio.setActivo("1");

        //Guardar BD
        PromocionCultura registrado = persistencePort.guardar(dominio);

        // Subir Archivos
        if ((anexo != null && !anexo.isEmpty()) || (videos != null && !videos.isEmpty()) || (fotos != null && !fotos.isEmpty())) {
            String sessionKey = UUID.randomUUID().toString();
            try {
                ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

                if (anexo != null && !anexo.isEmpty()) {
                    uploadFile(anexo, registrado, "ANEXO_CJ", sessionKey);
                }

                if (videos != null) {
                    for (MultipartFile v : videos) {
                        if(!v.isEmpty()) uploadFile(v, registrado, "VIDEO_CJ", sessionKey);
                    }
                }

                if (fotos != null) {
                    for (MultipartFile f : fotos) {
                        if(!f.isEmpty()) uploadFile(f, registrado, "FOTO_CJ", sessionKey);
                    }
                }

            } catch (Exception e) {
                log.error("Error archivos CJ", e);
                 Opcional: throw new Exception("Error al cargar archivos: " + e.getMessage());
            } finally {
                ftpPort.finalizarSession(sessionKey);
            }
        }
        return registrado;
    }

    @Override
    @Transactional
    public PromocionCultura actualizar(PromocionCultura dominio, String usuario) throws Exception {
        if(dominio.getId() == null) throw new Exception("ID obligatorio");

        if (dominio.getTareasRealizadas() != null) {
            for (PromocionCultura.DetalleTarea tarea : dominio.getTareasRealizadas()) {
                if (tarea.getFechaInicio() == null) {
                    tarea.setFechaInicio(dominio.getFechaInicio());
                }
            }
        }

        dominio.setUsuarioRegistro(usuario);
        return persistencePort.actualizar(dominio);
    }

    @Override
    @Transactional
    public void agregarArchivo(String idEvento, MultipartFile archivo, String tipo, String usuario) throws Exception {
        PromocionCultura evento = persistencePort.obtenerPorId(idEvento);
        if (evento == null) throw new Exception("Evento no existe");

        String sessionKey = UUID.randomUUID().toString();
        try {
            ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
            uploadFile(archivo, evento, tipo, sessionKey);
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
    }

    @Override
    public void eliminarArchivo(String nombreArchivo) throws Exception {
        Archivo archivo = archivosPersistencePort.buscarPorNombre(nombreArchivo);
        if (archivo == null) throw new Exception("Archivo no encontrado");

        String sessionKey = UUID.randomUUID().toString();
        ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try {
            String fullPath = archivo.getRuta() + "/" + archivo.getNombre();
            ftpPort.deleteFileFTP(fullPath);
        } catch (Exception e) {
            log.warn("Error borrado físico FTP: " + e.getMessage());
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
        archivosPersistencePort.eliminarReferenciaArchivo(nombreArchivo);
    }

    @Override
    public RecursoArchivo descargarAnexo(String idEvento, String usuario) throws Exception {
        return descargarArchivoPorTipo(idEvento, "ANEXO_CJ");
    }

    public RecursoArchivo descargarArchivoPorTipo(String id, String tipoArchivo) throws Exception {
        // Buscar metadatos en BD
        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(id);
        Archivo encontrado = archivos.stream()
                .filter(a -> a.getTipo().equalsIgnoreCase(tipoArchivo))
                .findFirst()
                .orElseThrow(() -> new Exception("Archivo " + tipoArchivo + " no encontrado"));

        // Preparar archivo temporal
        String sessionKey = UUID.randomUUID().toString();
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("cj_" + tipoArchivo + "_" + id, ".tmp");

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
        // Stream con auto-borrado
        InputStream autoDeleteStream = new java.io.FileInputStream(tempFile.toFile()) {
            @Override public void close() throws java.io.IOException {
                super.close();
                java.nio.file.Files.deleteIfExists(tempFile);
            }
        };

        return RecursoArchivo.builder()
                .nombreFileName(encontrado.getNombre())
                .stream(autoDeleteStream)
                .build();
    }

    @Override
    public byte[] generarFichaPdf(String id) throws Exception {
        if(persistencePort.obtenerPorId(id) == null) throw new Exception("Evento no existe");
        return reportePort.generarFichaPromocion(id);
    }

    private void uploadFile(MultipartFile file, PromocionCultura evento, String tipo, String sessionKey) throws Exception {

        //Determinar subcarpeta según el tipo
        String carpetaTipo = switch (tipo.toUpperCase()) {
            case "ANEXO_CJ" -> "fichas";
            case "VIDEO_CJ" -> "videos";
            case "FOTO_CJ" -> "fotos";
            default -> "otros";
        };

        // Extraer datos para la ruta
        String distrito = evento.getDistritoJudicialId();
        String anio = String.valueOf(evento.getFechaInicio() != null ? evento.getFechaInicio().getYear() : LocalDate.now().getYear());
        // Mes formateado a 2 dígitos
        String mes = String.format("%02d", evento.getFechaInicio() != null ? evento.getFechaInicio().getMonthValue() : LocalDate.now().getMonthValue());

        // Construir Ruta Base: /evidencias/{distrito}/evidencias_apcj/{CARPETA}/{ANIO}/{MES}/{ID}
        String rutaBase = String.format("%s/%s/evidencias_apcj/%s/%s/%s/%s",
                ftpRutaBase, distrito, carpetaTipo, anio, mes, evento.getId());

        // Nombre físico único
        String ext = obtenerExtension(file.getOriginalFilename());
        String nombreFinal = evento.getId() + "_" + tipo + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0,4) + ext;

        String rutaCompleta = rutaBase + "/" + nombreFinal;

        // Subir FTP
        if (!ftpPort.uploadFileFTP(sessionKey, rutaCompleta, file.getInputStream(), tipo)) {
            throw new Exception("Fallo FTP al subir " + nombreFinal);
        }

        // Guardar Referencia
        archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                .nombre(nombreFinal)
                .tipo(tipo)
                .ruta(rutaBase)
                .numeroIdentificacion(evento.getId())
                .build());
    }

    private String obtenerExtension(String nombre) {
        return (nombre != null && nombre.contains(".")) ? nombre.substring(nombre.lastIndexOf(".")) : "";
    }
}