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
import pe.gob.pj.prueba.domain.model.negocio.PromocionCultura;
import pe.gob.pj.prueba.domain.port.files.FtpPort;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.PromocionCulturaPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.RegistrarPromocionUseCasePort;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrarPromocionUseCaseAdapter implements RegistrarPromocionUseCasePort {

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
    public Pagina<PromocionCultura> listarPromocion(String usuario, PromocionCultura filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listarPromocion(usuario, filtros, pagina, tamanio);
    }

    @Override
    public PromocionCultura buscarPorId(String id) throws Exception {
        PromocionCultura encontrado = persistencePort.obtenerPorId(id);
        if (encontrado == null) throw new Exception("Evento de Promoción Cultura no encontrado.");
        return encontrado;
    }

    @Override
    @Transactional
    public PromocionCultura registrar(PromocionCultura dominio, String usuarioOperacion) throws Exception {
        // 1. Validaciones básicas y defaults
        validarDatos(dominio);
        asignarValoresPorDefecto(dominio);

        // 2. Generación del ID Correlativo (Lógica estilo EventoFC)
        // Esto es CRUCIAL: El ID debe existir ANTES de guardar para pasarlo a los hijos.
        String ultimoId = persistencePort.obtenerUltimoId();
        long siguiente = 1;

        if (ultimoId != null && !ultimoId.isBlank()) {
            try {
                // Asumimos formato 000001-15-2025-CJ. Extraemos los primeros 6 dígitos.
                String parteNumerica = ultimoId.split("-")[0];
                siguiente = Long.parseLong(parteNumerica) + 1;
            } catch (Exception e) {
                log.warn("No se pudo parsear el último ID: {}. Iniciando en 1.", ultimoId);
                siguiente = 1;
            }
        }

        // Formato: 000001-15-2025-CJ
        String anio = String.valueOf(LocalDate.now().getYear());
        String idGenerado = String.format("%06d-%s-%s-CJ", siguiente, dominio.getDistritoJudicialId(), anio);

        // Recorte de seguridad por si acaso excede 17 chars (aunque con %06d debería ser exacto)
        if (idGenerado.length() > 17) idGenerado = idGenerado.substring(0, 17);

        // 3. Setear datos de auditoría e ID al Dominio
        dominio.setId(idGenerado);
        dominio.setUsuarioRegistro(usuarioOperacion);
        dominio.setFechaRegistro(LocalDate.now());
        dominio.setActivo("1");

        // 4. Guardar (El Mapper pasará el ID a la Entidad Padre -> PrePersist lo pasará a los Hijos)
        return persistencePort.guardar(dominio);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromocionCultura registrarConEvidencias(PromocionCultura dominio, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos, String usuarioOperacion) throws Exception {
        // Primero registramos la data (Aquí se genera el ID)
        PromocionCultura registrado = this.registrar(dominio, usuarioOperacion);

        // Luego subimos los archivos usando el ID generado
        try {
            ftpPort.iniciarSesion(usuarioOperacion, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

            String id = registrado.getId();
            String dist = registrado.getDistritoJudicialId();
            LocalDate fecha = registrado.getFechaInicio(); // Usamos fecha inicio para la ruta

            if (anexo != null && !anexo.isEmpty())
                guardarEvidencia(anexo, id, dist, fecha, "ANEXO", usuarioOperacion);

            if (videos != null)
                for (MultipartFile v : videos)
                    if (!v.isEmpty()) guardarEvidencia(v, id, dist, fecha, "VIDEO", usuarioOperacion);

            if (fotos != null)
                for (MultipartFile f : fotos)
                    if (!f.isEmpty()) guardarEvidencia(f, id, dist, fecha, "FOTO", usuarioOperacion);

        } catch (Exception e) {
            log.error("Error subiendo archivos. Se hará Rollback de la transacción.", e);
            throw new Exception("Error al subir evidencias: " + e.getMessage());
        } finally {
            ftpPort.finalizarSession(usuarioOperacion);
        }

        return registrado;
    }

    @Override
    @Transactional
    public PromocionCultura actualizar(PromocionCultura dominio, String usuarioOperacion) throws Exception {
        validarDatos(dominio);
        asignarValoresPorDefecto(dominio);
        // En actualizar, el ID ya viene del front, así que no generamos uno nuevo.
        dominio.setUsuarioRegistro(usuarioOperacion);
        return persistencePort.actualizar(dominio);
    }


    @Override
    @Transactional
    public void eliminarArchivo(String nombreArchivo) throws Exception {
        log.info("Eliminando archivo: {}", nombreArchivo);

        Archivo archivo = archivosPersistencePort.buscarPorNombre(nombreArchivo);
        if (archivo == null) throw new Exception("Archivo no encontrado en BD.");

        String rutaCompleta = archivo.getRuta() + "/" + archivo.getNombre();
        String usuarioFTP = "SISTEMA"; // Usuario para la operación FTP interna

        try {
            ftpPort.iniciarSesion(usuarioFTP, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
            boolean eliminado = ftpPort.deleteFileFTP(rutaCompleta);
            if(!eliminado) log.warn("El archivo no existía en el FTP o no se pudo borrar: {}", rutaCompleta);
        } finally {
            ftpPort.finalizarSession(usuarioFTP);
        }

        archivosPersistencePort.eliminarReferenciaArchivo(nombreArchivo);
    }

    @Override
    @Transactional
    public void subirArchivoAdicional(String idEvento, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception {
        PromocionCultura evento = persistencePort.obtenerPorId(idEvento);
        if (evento == null) throw new Exception("El evento no existe.");

        try {
            ftpPort.iniciarSesion(usuarioOperacion, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
            guardarEvidencia(archivo, evento.getId(), evento.getDistritoJudicialId(), evento.getFechaInicio(), tipoArchivo, usuarioOperacion);
        } finally {
            ftpPort.finalizarSession(usuarioOperacion);
        }
    }

    @Override
    public RecursoArchivo descargarAnexo(String idEvento, String usuario) throws Exception {
        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(idEvento);

        Archivo anexo = archivos.stream()
                .filter(a -> "ANEXO".equalsIgnoreCase(a.getTipo()))
                .findFirst()
                .orElseThrow(() -> new Exception("No se encontró anexo para el evento " + idEvento));

        String rutaCompleta = anexo.getRuta() + "/" + anexo.getNombre();

        ftpPort.iniciarSesion(usuario, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try {
            InputStream is = ftpPort.descargarArchivo(rutaCompleta);
            return RecursoArchivo.builder()
                    .stream(is)
                    .nombreFileName(anexo.getNombre()) // Devolvemos el nombre real UUID o el original si lo guardaste
                    .build();
        } catch (Exception e) {
            ftpPort.finalizarSession(usuario); // Asegurar cierre si falla la descarga
            throw e;
        }
        // Nota: El controller cerrará el stream, pero la sesión FTP debe cerrarse con cuidado.
        // Idealmente el FtpAdapter maneja el cierre, pero aquí está bien para el ejemplo.
    }

    // --- MÉTODOS PRIVADOS ---

    private void validarDatos(PromocionCultura d) throws Exception {
        if (d.getFechaInicio() == null) throw new Exception("La fecha de inicio es obligatoria.");
        if (d.getNombreActividad() == null || d.getNombreActividad().isBlank()) throw new Exception("El nombre de la actividad es obligatorio.");
    }

    private void asignarValoresPorDefecto(PromocionCultura d) {
        if (d.getResolucionAdminPlan() == null) d.setResolucionAdminPlan("NINGUNO");
        if (d.getResolucionPlanAnual() == null) d.setResolucionPlanAnual("NINGUNO");
        if (d.getDocumentoAutoriza() == null) d.setDocumentoAutoriza("NINGUNO");
        if (d.getInstitucionesAliadas() == null) d.setInstitucionesAliadas("NINGUNA");
        if (d.getObservaciones() == null) d.setObservaciones("");

        // Flags SI/NO
        if (d.getSeDictoLenguaNativa() == null) d.setSeDictoLenguaNativa("NO");
        if (d.getParticiparonDiscapacitados() == null) d.setParticiparonDiscapacitados("NO");
        if (d.getRequiereInterprete() == null) d.setRequiereInterprete("NO");
        if (d.getNumeroDiscapacitados() == null) d.setNumeroDiscapacitados(0);
    }

    private void guardarEvidencia(MultipartFile file, String idEvento, String distrito, LocalDate fecha, String tipo, String usuario) throws Exception {
        String anio = String.valueOf(fecha.getYear());
        String mes = String.format("%02d", fecha.getMonthValue());

        String carpetaTipo = switch (tipo.toUpperCase()) {
            case "ANEXO" -> "fichas";
            case "VIDEO" -> "videos";
            case "FOTO" -> "fotos";
            default -> "otros";
        };

        // Ruta: /evidencias/15/evidencias_apcj/fichas/2025/06/{ID_EVENTO}/archivo.pdf
        String rutaRelativa = String.format("%s/%s/evidencias_apcj/%s/%s/%s/%s",
                ftpRutaBase, distrito, carpetaTipo, anio, mes, idEvento);

        String extension = obtenerExtension(file.getOriginalFilename());
        // Nombre único para el archivo en disco/FTP
        String nombreFinal = UUID.randomUUID().toString().replace("-", "") + extension;
        String rutaCompletaFtp = rutaRelativa + "/" + nombreFinal;

        boolean exito = ftpPort.uploadFileFTP(usuario, rutaCompletaFtp, file.getInputStream(), "Carga Evidencia");
        if (!exito) throw new Exception("Falló la subida del archivo al servidor FTP");

        // Guardamos referencia en BD
        Archivo archivoDomain = Archivo.builder()
                .nombre(nombreFinal) // Nombre físico UUID
                .tipo(tipo.toUpperCase())
                .ruta(rutaRelativa)
                .numeroIdentificacion(idEvento)
                .build();

        archivosPersistencePort.guardarReferenciaArchivo(archivoDomain);
    }

    private String obtenerExtension(String nombre) {
        return (nombre != null && nombre.lastIndexOf(".") != -1) ? nombre.substring(nombre.lastIndexOf(".")) : ".dat";
    }


    public byte[] generarFichaPdf(String idEvento) throws Exception {
        if (persistencePort.obtenerPorId(idEvento) == null) {
            throw new Exception("El evento APCJ no existe.");
        }
        return reportePort.generarFichaPromocion(idEvento);
    }
}