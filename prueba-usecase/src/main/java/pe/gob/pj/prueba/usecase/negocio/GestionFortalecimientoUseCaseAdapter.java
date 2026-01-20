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
import pe.gob.pj.prueba.domain.model.negocio.FortalecimientoCapacidades;
import pe.gob.pj.prueba.domain.port.files.FtpPort;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.FortalecimientoPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionFortalecimientoUseCasePort;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestionFortalecimientoUseCaseAdapter implements GestionFortalecimientoUseCasePort {

    private final FortalecimientoPersistencePort persistencePort;
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
    public Pagina<FortalecimientoCapacidades> listar(String usuario, FortalecimientoCapacidades filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listar(usuario, filtros, pagina, tamanio);
    }

    @Override
    @Transactional(readOnly = true)
    public FortalecimientoCapacidades buscarPorId(String id) throws Exception {
        if (id == null || id.isBlank()) throw new Exception("El ID es obligatorio");
        return persistencePort.obtenerPorId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FortalecimientoCapacidades registrar(FortalecimientoCapacidades dominio, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos, String usuario) throws Exception {

        validarDatos(dominio);
        if (dominio.getResolucionAdminPlan() == null) dominio.setResolucionAdminPlan("NINGUNO");

        // Generar ID
        String ultimoId = persistencePort.obtenerUltimoId();
        long siguiente = 1;
        if (ultimoId != null && !ultimoId.isBlank()) {
            try { siguiente = Long.parseLong(ultimoId.split("-")[0]) + 1; } catch (Exception e) { siguiente = 1; }
        }
        String anio = String.valueOf(LocalDate.now().getYear());
        String corte = (dominio.getDistritoJudicialId() != null) ? dominio.getDistritoJudicialId() : "00";
        dominio.setId(String.format("%06d-%s-%s-FC", siguiente, corte, anio));

        //Auditoría y BD
        dominio.setUsuarioRegistro(usuario);
        dominio.setFechaRegistro(LocalDate.now());
        dominio.setActivo("1");

        FortalecimientoCapacidades registrado = persistencePort.guardar(dominio);

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
                log.error("Error subiendo evidencias FFC", e);
            } finally {
                ftpPort.finalizarSession(sessionKey);
            }
        }
        return registrado;
    }

    @Override
    @Transactional
    public FortalecimientoCapacidades actualizar(FortalecimientoCapacidades dominio, String usuarioOperacion) throws Exception {
        log.info("Actualizando FFC ID: {} por: {}", dominio.getId(), usuarioOperacion);

        validarDatos(dominio);

        if (dominio.getResolucionAdminPlan() == null || dominio.getResolucionAdminPlan().isBlank()) dominio.setResolucionAdminPlan("NINGUNO");
        if (dominio.getInstitucionesAliadas() == null || dominio.getInstitucionesAliadas().isBlank()) dominio.setInstitucionesAliadas("NINGUNA");

        if ("NO".equalsIgnoreCase(dominio.getSeDictoLenguaNativa())) {
            dominio.setLenguaNativaDesc("CASTELLANO");
        } else if (dominio.getLenguaNativaDesc() == null || dominio.getLenguaNativaDesc().isBlank()) {
            dominio.setLenguaNativaDesc("NO ESPECIFICADO");
        }

        dominio.setUsuarioRegistro(usuarioOperacion);
        return persistencePort.actualizar(dominio);
    }

    @Override
    @Transactional
    public void agregarArchivo(String idEvento, MultipartFile archivo, String tipo, String usuario) throws Exception {
        log.info("Usuario [{}] agregando archivo [{}] al evento FFC ID [{}]", usuario, tipo, idEvento);

        if (archivo == null || archivo.isEmpty()) throw new Exception("Archivo vacío");

        FortalecimientoCapacidades evento = persistencePort.obtenerPorId(idEvento);
        if (evento == null) throw new Exception("No existe el evento: " + idEvento);

        String sessionKey = UUID.randomUUID().toString();
        try {
            ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
            uploadFile(archivo, evento, tipo, sessionKey);
        } catch (Exception e) {
            log.error("Error agregando archivo FFC", e);
            throw new Exception("Error al subir archivo: " + e.getMessage());
        } finally {
            ftpPort.finalizarSession(sessionKey);
        }
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
        if (idEvento == null || idEvento.isEmpty()) throw new Exception("ID obligatorio");

        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(idEvento);
        Archivo anexo = archivos.stream()
                .filter(a -> "ANEXO".equalsIgnoreCase(a.getTipo()))
                .findFirst()
                .orElseThrow(() -> new Exception("Sin anexo PDF para este evento."));

        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("ffc_anexo_" + idEvento, ".tmp");
        String sessionKey = UUID.randomUUID().toString();

        ftpPort.iniciarSesion(sessionKey, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

        try {
            String rutaCompleta = anexo.getRuta() + "/" + anexo.getNombre();
            InputStream ftpStream = ftpPort.downloadFileStream(sessionKey, rutaCompleta);

            if (ftpStream == null) {
                throw new Exception("El archivo no se pudo leer del FTP (Posiblemente no existe).");
            }

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
                .nombreFileName(anexo.getNombre())
                .build();
    }

    @Override
    public byte[] generarFichaPdf(String idEvento) throws Exception {
        if (persistencePort.obtenerPorId(idEvento) == null) throw new Exception("El evento no existe.");
        return reportePort.generarFichaFortalecimiento(idEvento);
    }

    private void uploadFile(MultipartFile file, FortalecimientoCapacidades evento, String tipo, String sessionKey) throws Exception {

        String carpeta = switch (tipo.toUpperCase()) {
            case "ANEXO" -> "fichas";
            case "VIDEO" -> "videos";
            case "FOTO" -> "fotos";
            default -> "otros";
        };

        String dist = evento.getDistritoJudicialId();
        String anio = String.valueOf(evento.getFechaInicio().getYear());
        String mes = String.format("%02d", evento.getFechaInicio().getMonthValue());

        // Ruta: /evidencias/DISTRITO/evidencias_ffc/CARPETA/ANIO/MES/ID
        String rutaRelativa = String.format("%s/%s/evidencias_ffc/%s/%s/%s/%s", ftpRutaBase, dist, carpeta, anio, mes, evento.getId());

        String ext = obtenerExtension(file.getOriginalFilename());
        // En FFC usamos UUID aleatorio para evitar colisiones
        String nombreFinal = UUID.randomUUID().toString().replace("-", "") + ext;

        if (!ftpPort.uploadFileFTP(sessionKey, rutaRelativa + "/" + nombreFinal, file.getInputStream(), "Carga " + tipo)) {
            throw new Exception("Fallo FTP al subir " + nombreFinal);
        }

        archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                .nombre(nombreFinal).tipo(tipo.toLowerCase())
                .ruta(rutaRelativa).numeroIdentificacion(evento.getId())
                .build());
    }

    private void validarDatos(FortalecimientoCapacidades ffc) throws Exception {
        if (ffc.getFechaInicio() == null) throw new Exception("Fecha inicio obligatoria.");
        if (ffc.getNombreEvento() == null || ffc.getNombreEvento().isBlank()) throw new Exception("Nombre evento obligatorio.");
    }

    private String obtenerExtension(String nombre) {
        return (nombre != null && nombre.contains(".")) ? nombre.substring(nombre.lastIndexOf(".")) : "";
    }
}