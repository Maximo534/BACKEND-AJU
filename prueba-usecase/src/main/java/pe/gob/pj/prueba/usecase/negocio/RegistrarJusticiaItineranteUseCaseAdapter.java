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
import pe.gob.pj.prueba.domain.port.usecase.negocio.RegistrarJusticiaItineranteUseCasePort;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrarJusticiaItineranteUseCaseAdapter implements RegistrarJusticiaItineranteUseCasePort {

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
    public Pagina<JusticiaItinerante> listarItinerante(String usuario, JusticiaItinerante filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listarItinerante(usuario, filtros, pagina, tamanio);
    }

    @Override
    @Transactional
    public JusticiaItinerante registrar(JusticiaItinerante fji, String usuarioOperacion) throws Exception {
        log.info("Iniciando registro simple (JSON) por: {}", usuarioOperacion);
        validarYCompletarDatos(fji);

        String ultimoId = persistencePort.obtenerUltimoId();

        long siguiente = 1;

        if (ultimoId != null && !ultimoId.isBlank()) {
            try {

                String parteNumerica = ultimoId.substring(0, 6);
                siguiente = Long.parseLong(parteNumerica) + 1;
            } catch (Exception e) {
                log.warn("El último ID '{}' no tiene formato estándar. Se reinicia el contador en 1.", ultimoId);
                siguiente = 1;
            }
        }

        String numeroStr = String.format("%06d", siguiente);
        String anio = String.valueOf(LocalDate.now().getYear());
        String corte = fji.getDistritoJudicialId();

        String idGenerado = String.format("%s-%s-%s-JI", numeroStr, corte, anio);

        if (idGenerado.length() > 17) {
            idGenerado = idGenerado.substring(0, 17);
        }

        fji.setId(idGenerado);

        fji.setUsuarioRegistro(usuarioOperacion);
        fji.setFechaRegistro(LocalDate.now());
        fji.setActivo("1");

        return persistencePort.guardar(fji);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JusticiaItinerante registrarConEvidencias(JusticiaItinerante fji, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos, String usuarioOperacion) throws Exception {
        log.info("Iniciando registro UNIFICADO (Datos + Archivos) por: {}", usuarioOperacion);

        JusticiaItinerante registrado = this.registrar(fji, usuarioOperacion);
        String idEvento = registrado.getId();
        String idDistrito = registrado.getDistritoJudicialId();
        LocalDate fechaInicio = registrado.getFechaInicio();

        try {
            ftpPort.iniciarSesion(usuarioOperacion, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

            if (anexo != null && !anexo.isEmpty()) {
                guardarEvidencia(anexo, idEvento, idDistrito, fechaInicio, "ANEXO", usuarioOperacion);
            }
            if (videos != null) {
                for (MultipartFile v : videos) {
                    if (!v.isEmpty()) guardarEvidencia(v, idEvento, idDistrito, fechaInicio, "VIDEO", usuarioOperacion);
                }
            }
            if (fotos != null) {
                for (MultipartFile f : fotos) {
                    if (!f.isEmpty()) guardarEvidencia(f, idEvento, idDistrito, fechaInicio, "FOTO", usuarioOperacion);
                }
            }

        } catch (Exception e) {
            log.error("Error subiendo archivos. Iniciando Rollback de BD.", e);
            throw new Exception("Error crítico al subir evidencias: " + e.getMessage());
        } finally {
            ftpPort.finalizarSession(usuarioOperacion);
        }

        return registrado;
    }

    @Override
    @Transactional
    public JusticiaItinerante actualizar(JusticiaItinerante fji, String usuarioOperacion) throws Exception {
        log.info("Iniciando actualización de evento ID: {} por: {}", fji.getId(), usuarioOperacion);

        validarYCompletarDatos(fji);
        fji.setUsuarioRegistro(usuarioOperacion);
        fji.setActivo("1");

        return persistencePort.actualizar(fji);
    }

    @Override
    @Transactional(readOnly = true)
    public JusticiaItinerante buscarPorId(String id) throws Exception {
        return persistencePort.obtenerPorId(id);
    }

    @Override
    @Transactional
    public void eliminarArchivo(String nombreArchivo) throws Exception {
        log.info("Iniciando eliminación de archivo: {}", nombreArchivo);

        //  Obtener la metadata del archivo desde BD para saber su ruta
        Archivo archivo = archivosPersistencePort.buscarPorNombre(nombreArchivo);

        // Construir la ruta física completa
        // En BD se guarda: /evidencias/x/y
        // El nombre es: uuid.jpg
        String rutaCompletaFtp = archivo.getRuta() + "/" + archivo.getNombre();

        // Conectar al FTP para borrar el físico
        // CORREGIDO: Se usan las variables de clase ftpUsuario y ftpClave
        ftpPort.iniciarSesion("ELIMINAR", ftpIp, ftpPuerto, ftpUsuario, ftpClave);

        try {
            boolean eliminado = ftpPort.deleteFileFTP(rutaCompletaFtp);
            if (!eliminado) {
                log.warn("El archivo no se encontró en FTP o falló el borrado, pero se procederá a eliminar de BD: {}", rutaCompletaFtp);
            }
        } catch (Exception e) {
            log.error("Error al intentar borrar en FTP: {}", e.getMessage());
            // Dependiendo de tu lógica, podrías lanzar excepción o dejar pasar
            // para que al menos se borre de la BD y no quede un "enlace roto".
        } finally {
            ftpPort.finalizarSession("ELIMINAR");
        }

        // Borrar el registro de la Base de Datos
        archivosPersistencePort.eliminarReferenciaArchivo(nombreArchivo);
    }


    private void guardarEvidencia(MultipartFile archivo, String idEvento, String idDistrito, LocalDate fecha, String tipo, String usuario) throws Exception {
        String anio = String.valueOf(fecha.getYear());
        String mes = String.format("%02d", fecha.getMonthValue());

        String carpetaTipo = switch (tipo.toUpperCase()) {
            case "ANEXO" -> "fichas";
            case "VIDEO" -> "videos";
            case "FOTO" -> "fotos";
            default -> "otros";
        };

        String rutaRelativa = String.format("%s/%s/evidencias_fji/%s/%s/%s/%s",
                ftpRutaBase, idDistrito, carpetaTipo, anio, mes, idEvento);

        String extension = obtenerExtension(archivo.getOriginalFilename());
        String nombreFinal = UUID.randomUUID().toString().replace("-", "") + extension;
        String rutaCompletaFtp = rutaRelativa + "/" + nombreFinal;

        boolean exito = ftpPort.uploadFileFTP(usuario, rutaCompletaFtp, archivo.getInputStream(), "Carga Evidencia " + tipo);
        if (!exito) throw new Exception("Fallo en uploadFileFTP para: " + nombreFinal);

        Archivo archivoDomain = Archivo.builder()
                .nombre(nombreFinal)
                .tipo(tipo.toLowerCase())
                .ruta(rutaRelativa)
                .numeroIdentificacion(idEvento)
                .build();

        archivosPersistencePort.guardarReferenciaArchivo(archivoDomain);
    }

    private void validarYCompletarDatos(JusticiaItinerante fji) throws Exception {
        if (fji.getFechaInicio() == null) throw new Exception("La fecha de inicio es obligatoria.");
        if (fji.getFechaFin() != null && fji.getFechaFin().isBefore(fji.getFechaInicio())) throw new Exception("La fecha fin no puede ser anterior a la fecha de inicio.");

        if (fji.getPublicoObjetivoDetalle() == null) fji.setPublicoObjetivoDetalle("NINGUNO");
        if (fji.getTambo() == null) fji.setTambo("NINGUNO");
        if (fji.getLenguaNativa() == null) fji.setLenguaNativa("NINGUNO");
        if (fji.getCodigoAdcPueblosIndigenas() == null) fji.setCodigoAdcPueblosIndigenas("00");
        if (fji.getCodigoSaeLenguaNativa() == null) fji.setCodigoSaeLenguaNativa("00");

        if (fji.getNumMujeresIndigenas() == null) fji.setNumMujeresIndigenas(0);
        if (fji.getNumPersonasNoIdiomaNacional() == null) fji.setNumPersonasNoIdiomaNacional(0);
        if (fji.getNumJovenesQuechuaAymara() == null) fji.setNumJovenesQuechuaAymara(0);

        if (fji.getDescripcionActividad() == null) fji.setDescripcionActividad("");
        if (fji.getInstitucionesAliadas() == null) fji.setInstitucionesAliadas("");
        if (fji.getObservaciones() == null) fji.setObservaciones("");
    }

    @Override
    @Transactional
    public void subirArchivoAdicional(String idEvento, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception {
        log.info("Subiendo archivo adicional ({}) al evento {}", tipoArchivo, idEvento);

        if (archivo == null || archivo.isEmpty()) {
            throw new Exception("El archivo no puede estar vacío.");
        }

        JusticiaItinerante evento = persistencePort.obtenerPorId(idEvento);
        if (evento == null) {
            throw new Exception("No se encontró el evento con ID: " + idEvento);
        }

        try {
            ftpPort.iniciarSesion(usuarioOperacion, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

            guardarEvidencia(archivo, evento.getId(), evento.getDistritoJudicialId(), evento.getFechaInicio(), tipoArchivo, usuarioOperacion);

        } catch (Exception e) {
            log.error("Error al subir archivo adicional: {}", e.getMessage());
            throw new Exception("Error al subir archivo: " + e.getMessage());
        } finally {
            ftpPort.finalizarSession(usuarioOperacion);
        }
    }
    private String obtenerExtension(String nombre) {
        return (nombre != null && nombre.contains(".")) ? nombre.substring(nombre.lastIndexOf(".")) : "";
    }

    @Override
    public RecursoArchivo descargarAnexo(String idEvento, String usuario) throws Exception {
        if (idEvento == null || idEvento.isEmpty()) {
            throw new Exception("El ID del evento es obligatorio");
        }

        List<Archivo> todosLosArchivos = archivosPersistencePort.listarArchivosPorEvento(idEvento);

        Archivo archivoAnexo = todosLosArchivos.stream()
                .filter(a -> "ANEXO".equalsIgnoreCase(a.getTipo()))
                .findFirst()
                .orElseThrow(() -> new Exception("Este evento no tiene un Anexo PDF registrado."));

        ftpPort.iniciarSesion(usuario, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

        String rutaCompleta = archivoAnexo.getRuta() + "/" + archivoAnexo.getNombre();

        try {
            InputStream stream = ftpPort.descargarArchivo(rutaCompleta);

            return RecursoArchivo.builder()
                    .stream(stream)
                    .nombreFileName(archivoAnexo.getNombre())
                    .build();

        } catch (Exception e) {
            ftpPort.finalizarSession(usuario);
            throw new Exception("Error al descargar del FTP: " + e.getMessage());
        }
    }


    @Override
    public byte[] generarFichaPdf(String idEvento) throws Exception {
        return reportePort.generarFichaItinerante(idEvento);
    }

}