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
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.FortalecimientoPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.RegistrarFortalecimientoUseCasePort;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrarFortalecimientoUseCaseAdapter implements RegistrarFortalecimientoUseCasePort {

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
    @Transactional
    public FortalecimientoCapacidades registrar(FortalecimientoCapacidades dominio, String usuarioOperacion) throws Exception {
        validarDatos(dominio);

        if (dominio.getResolucionAdminPlan() == null) {
            dominio.setResolucionAdminPlan("NINGUNO");
        }
        if (dominio.getInstitucionesAliadas() == null || dominio.getInstitucionesAliadas().isBlank()) {
            dominio.setInstitucionesAliadas("NINGUNA");
        }
        if ("NO".equalsIgnoreCase(dominio.getSeDictoLenguaNativa())) {
            dominio.setLenguaNativaDesc("CASTELLANO");
        } else if (dominio.getLenguaNativaDesc() == null || dominio.getLenguaNativaDesc().isBlank()) {
            throw new Exception("Debe especificar la lengua nativa.");
        }

        String ultimoId = persistencePort.obtenerUltimoId();
        long siguiente = 1;

        if (ultimoId != null && !ultimoId.isBlank()) {
            try {
                // Formato esperado: 000001-11-2025-FC
                String parteNumerica = ultimoId.substring(0, 6);
                siguiente = Long.parseLong(parteNumerica) + 1;
            } catch (Exception e) {
                log.warn("Último ID '{}' no tiene formato estándar. Iniciando en 1.", ultimoId);
                siguiente = 1;
            }
        }

        String numeroStr = String.format("%06d", siguiente);
        String anio = String.valueOf(LocalDate.now().getYear());
        String corte = dominio.getDistritoJudicialId();

        // Armamos ID: 000001-11-2025-FC (Usamos sufijo FC para Fortalecimiento)
        String idGenerado = String.format("%s-%s-%s-FC", numeroStr, corte, anio);

        if (idGenerado.length() > 17) idGenerado = idGenerado.substring(0, 17);

        dominio.setId(idGenerado);

        dominio.setUsuarioRegistro(usuarioOperacion);
        dominio.setFechaRegistro(LocalDate.now());
        dominio.setActivo("1");

        return persistencePort.guardar(dominio);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FortalecimientoCapacidades registrarConEvidencias(FortalecimientoCapacidades dominio, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos, String usuarioOperacion) throws Exception {
        FortalecimientoCapacidades registrado = this.registrar(dominio, usuarioOperacion);

        try {
            ftpPort.iniciarSesion(usuarioOperacion, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

            String id = registrado.getId();
            String distrito = registrado.getDistritoJudicialId();
            LocalDate fecha = registrado.getFechaInicio();

            if (anexo != null && !anexo.isEmpty())
                guardarEvidencia(anexo, id, distrito, fecha, "ANEXO", usuarioOperacion);

            if (videos != null)
                for (MultipartFile v : videos) if (!v.isEmpty()) guardarEvidencia(v, id, distrito, fecha, "VIDEO", usuarioOperacion);

            if (fotos != null)
                for (MultipartFile f : fotos) if (!f.isEmpty()) guardarEvidencia(f, id, distrito, fecha, "FOTO", usuarioOperacion);

        } catch (Exception e) {
            log.error("Error subiendo archivos FFC. Rollback.", e);
            throw new Exception("Error al subir evidencias: " + e.getMessage());
        } finally {
            ftpPort.finalizarSession(usuarioOperacion);
        }
        return registrado;
    }

    @Override
    @Transactional
    public FortalecimientoCapacidades actualizar(FortalecimientoCapacidades dominio, String usuarioOperacion) throws Exception {
        log.info("Iniciando actualización de FFC ID: {} por: {}", dominio.getId(), usuarioOperacion);

        validarDatos(dominio);

        if (dominio.getResolucionAdminPlan() == null || dominio.getResolucionAdminPlan().isBlank()) {
            dominio.setResolucionAdminPlan("NINGUNO");
        }
        if (dominio.getInstitucionesAliadas() == null || dominio.getInstitucionesAliadas().isBlank()) {
            dominio.setInstitucionesAliadas("NINGUNA");
        }

        if ("NO".equalsIgnoreCase(dominio.getSeDictoLenguaNativa())) {
            dominio.setLenguaNativaDesc("CASTELLANO");
        } else if (dominio.getLenguaNativaDesc() == null || dominio.getLenguaNativaDesc().isBlank()) {
            dominio.setLenguaNativaDesc("NO ESPECIFICADO");
        }

        dominio.setUsuarioRegistro(usuarioOperacion);
        dominio.setActivo("1");

        return persistencePort.actualizar(dominio);
    }

    @Override
    @Transactional(readOnly = true)
    public FortalecimientoCapacidades buscarPorId(String id) throws Exception {
        return persistencePort.obtenerPorId(id);
    }

    private void guardarEvidencia(MultipartFile archivo, String idEvento, String idDistrito, LocalDate fecha, String tipo, String usuario) throws Exception {
        String anio = String.valueOf(fecha.getYear());
        String mes = String.format("%02d", fecha.getMonthValue());

        String carpetaTipo = switch (tipo.toUpperCase()) {
            case "ANEXO" -> "fichas"; // O "informes"
            case "VIDEO" -> "videos";
            case "FOTO" -> "fotos";
            default -> "otros";
        };

        String rutaRelativa = String.format("%s/%s/evidencias_ffc/%s/%s/%s/%s",
                ftpRutaBase, idDistrito, carpetaTipo, anio, mes, idEvento);

        String nombreFinal = UUID.randomUUID().toString().replace("-", "") + obtenerExtension(archivo.getOriginalFilename());
        String rutaCompletaFtp = rutaRelativa + "/" + nombreFinal;

        if(!ftpPort.uploadFileFTP(usuario, rutaCompletaFtp, archivo.getInputStream(), "Evidencia FFC " + tipo)) {
            throw new Exception("Fallo subida FTP: " + nombreFinal);
        }

        Archivo archivoDomain = Archivo.builder()
                .nombre(nombreFinal).tipo(tipo.toLowerCase())
                .ruta(rutaRelativa).numeroIdentificacion(idEvento).build();

        archivosPersistencePort.guardarReferenciaArchivo(archivoDomain);
    }

    @Override
    public void eliminarArchivo(String nombreArchivo) throws Exception {
        Archivo archivo = archivosPersistencePort.buscarPorNombre(nombreArchivo);
        String rutaCompletaFtp = archivo.getRuta() + "/" + archivo.getNombre();

        ftpPort.iniciarSesion("ELIMINAR", ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try {
            ftpPort.deleteFileFTP(rutaCompletaFtp);
        } finally {
            ftpPort.finalizarSession("ELIMINAR");
        }
        archivosPersistencePort.eliminarReferenciaArchivo(nombreArchivo);
    }

    @Override
    public void subirArchivoAdicional(String idEvento, MultipartFile archivo, String tipo, String usuario) throws Exception {
        FortalecimientoCapacidades evento = persistencePort.obtenerPorId(idEvento);
        try {
            ftpPort.iniciarSesion(usuario, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
            guardarEvidencia(archivo, evento.getId(), evento.getDistritoJudicialId(), evento.getFechaInicio(), tipo, usuario);
        } finally {
            ftpPort.finalizarSession(usuario);
        }
    }

    @Override
    public RecursoArchivo descargarAnexo(String idEvento, String usuario) throws Exception {
        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(idEvento);
        Archivo anexo = archivos.stream().filter(a -> "ANEXO".equalsIgnoreCase(a.getTipo())).findFirst()
                .orElseThrow(() -> new Exception("Sin anexo PDF."));

        ftpPort.iniciarSesion(usuario, ftpIp, ftpPuerto, ftpUsuario, ftpClave);
        try {
            return RecursoArchivo.builder()
                    .stream(ftpPort.descargarArchivo(anexo.getRuta() + "/" + anexo.getNombre()))
                    .nombreFileName(anexo.getNombre()).build();
        } catch (Exception e) {
            ftpPort.finalizarSession(usuario);
            throw e;
        }
    }

    private void validarDatos(FortalecimientoCapacidades ffc) throws Exception {
        if (ffc.getFechaInicio() == null) throw new Exception("Fecha inicio obligatoria.");
        if (ffc.getNombreEvento() == null || ffc.getNombreEvento().isBlank()) throw new Exception("Nombre evento obligatorio.");
    }

    private String obtenerExtension(String nombre) {
        return (nombre != null && nombre.contains(".")) ? nombre.substring(nombre.lastIndexOf(".")) : "";
    }

    public byte[] generarFichaPdf(String idEvento) throws Exception {
        // Validamos existencia (opcional, el servicio también lo hace)
        if (persistencePort.obtenerPorId(idEvento) == null) {
            throw new Exception("El evento no existe.");
        }
        return reportePort.generarFichaFortalecimiento(idEvento);
    }
}