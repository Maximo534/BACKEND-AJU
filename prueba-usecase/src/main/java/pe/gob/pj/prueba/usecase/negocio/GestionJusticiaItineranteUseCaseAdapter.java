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
import pe.gob.pj.prueba.domain.port.files.FtpPort;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.JusticiaItinerantePersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionJusticiaItineranteUseCasePort;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestionJusticiaItineranteUseCaseAdapter implements GestionJusticiaItineranteUseCasePort {

    private final JusticiaItinerantePersistencePort persistencePort;
    private final FtpPort ftpPort;
    private final GestionArchivosPersistencePort archivosPersistencePort;
    private final GenerarReportePort reportePort;

    @Value("${ftp.ip}") private String ftpIp;
    @Value("${ftp.puerto}") private Integer ftpPuerto;
    @Value("${ftp.usuario}") private String ftpUsuario;
    @Value("${ftp.clave}") private String ftpClave;
    @Value("${ftp.ruta-base:/evidencias}") private String ftpRutaBase;

    @Override
    @Transactional(readOnly = true)
    public Pagina<JusticiaItinerante> listar(String usuario, JusticiaItinerante filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listar(usuario, filtros, pagina, tamanio);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JusticiaItinerante registrar(JusticiaItinerante dominio, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos, String usuario) throws Exception {
        // Validaciones
        if (dominio.getFechaInicio() == null) throw new Exception("La fecha de inicio es obligatoria.");
        if (dominio.getPublicoObjetivoDetalle() == null) dominio.setPublicoObjetivoDetalle("NINGUNO");

        // Generar ID
        String ultimoId = persistencePort.obtenerUltimoId();
        long siguiente = 1;
        if (ultimoId != null && !ultimoId.isBlank()) {
            try { siguiente = Long.parseLong(ultimoId.split("-")[0]) + 1; } catch (Exception e) { siguiente = 1; }
        }
        String anio = String.valueOf(LocalDate.now().getYear());
        String corte = (dominio.getDistritoJudicialId() != null) ? dominio.getDistritoJudicialId() : "00";
        dominio.setId(String.format("%06d-%s-%s-JI", siguiente, corte, anio));

        //Auditoría y Guardado
        dominio.setUsuarioRegistro(usuario);
        dominio.setFechaRegistro(LocalDate.now());
        dominio.setActivo("1");

        JusticiaItinerante registrado = persistencePort.guardar(dominio);

        //Subir Archivos (Con UUID)
        boolean hayArchivos = (anexo != null && !anexo.isEmpty()) ||
                (videos != null && !videos.isEmpty()) ||
                (fotos != null && !fotos.isEmpty());

        if (hayArchivos) {
            String sessionKey = UUID.randomUUID().toString();
            try {
                ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

                if (anexo != null && !anexo.isEmpty())
                    uploadFile(anexo, registrado, "ANEXO", sessionKey);

                if (videos != null)
                    for (MultipartFile v : videos)
                        if (!v.isEmpty()) uploadFile(v, registrado, "VIDEO", sessionKey);

                if (fotos != null)
                    for (MultipartFile f : fotos)
                        if (!f.isEmpty()) uploadFile(f, registrado, "FOTO", sessionKey);

            } catch (Exception e) {
                log.error("Error subiendo evidencias JI", e);
                Opcional: throw new Exception("Error carga archivos: " + e.getMessage());
            } finally {
                ftpPort.finalizarSession(sessionKey);
            }
        }

        return registrado;
    }

    @Override
    @Transactional(readOnly = true)
    public JusticiaItinerante buscarPorId(String id) throws Exception {
        if (id == null || id.isBlank()) throw new Exception("El ID es obligatorio");
        return persistencePort.obtenerPorId(id);
    }

    @Override
    @Transactional
    public JusticiaItinerante actualizar(JusticiaItinerante dominio, String usuarioOperacion) throws Exception {
        if (dominio.getFechaInicio() == null) throw new Exception("La fecha de inicio es obligatoria.");
        if (dominio.getFechaFin() != null && dominio.getFechaFin().isBefore(dominio.getFechaInicio())) {
            throw new Exception("La fecha fin no puede ser anterior a la fecha de inicio.");
        }
        dominio.setUsuarioRegistro(usuarioOperacion);
        return persistencePort.actualizar(dominio);
    }

    // Pedimos usuarioOperacion para tener trazabilidad en los logs
    @Override
    @Transactional
    public void agregarArchivo(String idEvento, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception {
        log.info("Usuario [{}] agregando archivo [{}] al evento ID [{}]", usuarioOperacion, tipoArchivo, idEvento);

        if (archivo == null || archivo.isEmpty()) throw new Exception("El archivo no puede estar vacío.");

        JusticiaItinerante evento = persistencePort.obtenerPorId(idEvento);
        if (evento == null) throw new Exception("No se encontró el evento con ID: " + idEvento);

        String sessionKey = UUID.randomUUID().toString();
        try {
            ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
            uploadFile(archivo, evento, tipoArchivo, sessionKey);

        } catch (Exception e) {
            log.error("Error al agregar archivo: {}", e.getMessage());
            throw new Exception("Error al agregar archivo: " + e.getMessage());
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
    }

    private void uploadFile(MultipartFile file, JusticiaItinerante evento, String tipo, String sessionKey) throws Exception {

        String carpeta = switch (tipo.toUpperCase()) {
            case "ANEXO" -> "fichas";
            case "VIDEO" -> "videos";
            case "FOTO" -> "fotos";
            default -> "otros";
        };

        String dist = evento.getDistritoJudicialId();
        String anio = String.valueOf(evento.getFechaInicio().getYear());
        String mes = String.format("%02d", evento.getFechaInicio().getMonthValue());

        // Ruta: /evidencias/DISTRITO/evidencias_fji/CARPETA/ANIO/MES/ID_EVENTO
        String rutaRelativa = String.format("%s/%s/evidencias_fji/%s/%s/%s/%s", ftpRutaBase, dist, carpeta, anio, mes, evento.getId());

        // Nombre del Archivo (Regla de Negocio JI: Siempre UUID)
        String ext = obtenerExtension(file.getOriginalFilename());
        String nombreFinal = UUID.randomUUID().toString().replace("-", "") + ext;

        // Subida FTP
        if (!ftpPort.uploadFileFTP(sessionKey, rutaRelativa + "/" + nombreFinal, file.getInputStream(), "Carga " + tipo)) {
            throw new Exception("Error FTP al subir " + nombreFinal);
        }

        // Guardar en BD
        archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                .nombre(nombreFinal)
                .tipo(tipo.toUpperCase())
                .ruta(rutaRelativa)
                .numeroIdentificacion(evento.getId())
                .build());
    }

    private String obtenerExtension(String nombre) {
        return (nombre != null && nombre.contains(".")) ? nombre.substring(nombre.lastIndexOf(".")) : "";
    }

    @Override
    @Transactional
    public void eliminarArchivo(String nombreArchivo) throws Exception {
        Archivo archivo = archivosPersistencePort.buscarPorNombre(nombreArchivo);
        if (archivo == null) throw new Exception("Archivo no encontrado en BD.");

        String rutaCompletaFtp = archivo.getRuta() + "/" + archivo.getNombre();
        String sessionKey = UUID.randomUUID().toString();

        ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try {
            ftpPort.deleteFileFTP(rutaCompletaFtp);
        } catch (Exception e) {
            log.warn("Fallo borrado FTP, se borrará de BD.");
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
        archivosPersistencePort.eliminarReferenciaArchivo(nombreArchivo);
    }

    @Override
    public RecursoArchivo descargarAnexo(String idEvento, String usuario) throws Exception {
        if (idEvento == null || idEvento.isEmpty()) throw new Exception("El ID del evento es obligatorio");

        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(idEvento);
        Archivo archivoAnexo = archivos.stream()
                .filter(a -> "ANEXO".equalsIgnoreCase(a.getTipo()))
                .findFirst()
                .orElseThrow(() -> new Exception("No existe anexo para este evento."));

        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("ji_anexo_" + idEvento, ".tmp");
        String sessionKey = UUID.randomUUID().toString();

        ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try (InputStream ftpStream = ftpPort.descargarArchivo(archivoAnexo.getRuta() + "/" + archivoAnexo.getNombre());
             java.io.OutputStream tempFileStream = java.nio.file.Files.newOutputStream(tempFile)) {
            ftpStream.transferTo(tempFileStream);
        } catch (Exception e) {
            java.nio.file.Files.deleteIfExists(tempFile);
            throw new Exception("Error descargando del FTP: " + e.getMessage());
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
                .nombreFileName(archivoAnexo.getNombre())
                .build();
    }

    @Override
    public byte[] generarFichaPdf(String idEvento) throws Exception {
        if(persistencePort.obtenerPorId(idEvento) == null) throw new Exception("Evento no existe");
        return reportePort.generarFichaItinerante(idEvento);
    }
}