package pe.gob.pj.prueba.usecase.negocio;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.exceptions.negocio.MovimientoNoEncontradoException;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.BuenaPractica;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarBuenaPracticaQuery;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.BuenaPracticaPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionArchivosUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionBuenaPracticaUseCasePort;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GestionBuenaPracticaUseCaseAdapter implements GestionBuenaPracticaUseCasePort {

    BuenaPracticaPersistencePort persistencePort;
    GenerarReportePort reportePort;
    GestionArchivosUseCasePort gestorArchivos;

    static final String MODULO_BP = "evidencias_bp";
    static final String TX_MANAGER = "txManagerNegocio";

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public Pagina<BuenaPractica> listar(String cuo, ListarBuenaPracticaQuery query, int pagina, int tamanio) {
        return persistencePort.listar(cuo, query, pagina, tamanio);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public BuenaPractica registrar(String cuo, BuenaPractica dominio, MultipartFile anexo, MultipartFile ppt, List<MultipartFile> fotos, MultipartFile video) throws Exception {

        if (dominio.getFechaInicio() == null) throw new IllegalArgumentException("La fecha de inicio es obligatoria.");

        // Generar Código Correlativo (Ej: 000001-15-2026-BP)
        String anio = String.valueOf(dominio.getFechaInicio().getYear());
        Long distritoId = dominio.getDistritoJudicialId();

        String ultimoCodigo = persistencePort.obtenerUltimoCodigo(cuo, distritoId, anio);
        long correlativo = 1;

        if (ultimoCodigo != null && !ultimoCodigo.isBlank()) {
            try {
                String numeroStr = ultimoCodigo.split("-")[0];
                correlativo = Long.parseLong(numeroStr) + 1;
            } catch (Exception e) {
                log.warn("[{}] Error parseando ultimo codigo {}. Reiniciando a 1.", cuo, ultimoCodigo);
            }
        }

        String nuevoCodigo = String.format("%06d-%02d-%s-BP", correlativo, distritoId, anio);
        dominio.setCodigo(nuevoCodigo);
        dominio.setActivo("1");

        // Guardar
        BuenaPractica registrado = persistencePort.guardar(cuo, dominio);

        // Subir Archivos (Usa Código Visible)
        subirArchivosAdjuntos(cuo, registrado, anexo, ppt, fotos, video);

        return registrado;
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public BuenaPractica buscarPorId(String cuo, Long id) {
        if (id == null) throw new IllegalArgumentException("El ID es obligatorio.");

        BuenaPractica encontrado = persistencePort.obtenerPorId(cuo, id);
        if (encontrado == null) {
            throw new MovimientoNoEncontradoException("No se encontró el registro con ID: " + id);
        }
        return encontrado;
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public BuenaPractica actualizar(String cuo, BuenaPractica dominio) throws Exception {
        if (dominio.getId() == null) throw new IllegalArgumentException("El ID es obligatorio para actualizar.");

        return persistencePort.actualizar(cuo, dominio);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public void agregarArchivo(String cuo, Long idEvento, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception {
        if (archivo == null || archivo.isEmpty()) throw new IllegalArgumentException("El archivo no puede estar vacío.");

        BuenaPractica evento = buscarPorId(cuo, idEvento);
        evento.setUsuario(usuarioOperacion); // Auditoría archivo

        gestorArchivos.subirArchivo(
                archivo,
                String.valueOf(evento.getDistritoJudicialId()),
                tipoArchivo,
                MODULO_BP,
                evento.getFechaInicio(),
                evento.getCodigo(),
                evento
        );
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public void eliminarArchivo(String cuo, Long idArchivo, String usuarioOperacion) throws Exception {
        BuenaPractica audit = new BuenaPractica();
        audit.setUsuario(usuarioOperacion);

        gestorArchivos.eliminarPorId(idArchivo, audit);
    }

    @Override
    public RecursoArchivo descargarArchivoPorTipo(String cuo, Long idEvento, String tipoArchivo) throws Exception {
        BuenaPractica evento = buscarPorId(cuo, idEvento);

        if (evento.getArchivosGuardados() == null || evento.getArchivosGuardados().isEmpty()) {
            throw new MovimientoNoEncontradoException("El evento no tiene archivos adjuntos.");
        }

        Archivo archivoEncontrado = evento.getArchivosGuardados().stream()
                .filter(a -> tipoArchivo.equalsIgnoreCase(a.getTipo()))
                .findFirst()
                .orElseThrow(() -> new MovimientoNoEncontradoException("No existe un archivo de tipo " + tipoArchivo + " para este evento."));

        return gestorArchivos.descargarPorId(archivoEncontrado.getId());
    }

    @Override
    public RecursoArchivo descargarArchivoPorId(Long idArchivo) throws Exception {
        return gestorArchivos.descargarPorId(idArchivo);
    }

    @Override
    public byte[] generarFichaPdf(String cuo, Long idEvento) throws Exception {
        buscarPorId(cuo, idEvento);
        return reportePort.generarFichaBuenaPractica(String.valueOf(idEvento));
    }

    // --- PRIVADOS ---

    private void subirArchivosAdjuntos(String cuo, BuenaPractica registrado, MultipartFile anexo, MultipartFile ppt, List<MultipartFile> fotos, MultipartFile video) throws Exception {
        String distritoStr = String.valueOf(registrado.getDistritoJudicialId());

        if (anexo != null && !anexo.isEmpty()) {
            gestorArchivos.subirArchivo(anexo, distritoStr, "ANEXO", MODULO_BP, registrado.getFechaInicio(), registrado.getCodigo(), registrado);
        }
        if (ppt != null && !ppt.isEmpty()) {
            gestorArchivos.subirArchivo(ppt, distritoStr, "PPT", MODULO_BP, registrado.getFechaInicio(), registrado.getCodigo(), registrado);
        }
        if (video != null && !video.isEmpty()) {
            gestorArchivos.subirArchivo(video, distritoStr, "VIDEO", MODULO_BP, registrado.getFechaInicio(), registrado.getCodigo(), registrado);
        }
        if (fotos != null) {
            for (MultipartFile f : fotos) {
                if (!f.isEmpty()) gestorArchivos.subirArchivo(f, distritoStr, "FOTO", MODULO_BP, registrado.getFechaInicio(), registrado.getCodigo(), registrado);
            }
        }
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        return persistencePort.obtenerResumenGrafico();
    }

}
