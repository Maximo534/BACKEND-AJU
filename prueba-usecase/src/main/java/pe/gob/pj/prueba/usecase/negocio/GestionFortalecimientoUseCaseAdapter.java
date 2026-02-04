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
import pe.gob.pj.prueba.domain.model.negocio.FortalecimientoCapacidades;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarFortalecimientoQuery;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.FortalecimientoPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionArchivosUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionFortalecimientoUseCasePort;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GestionFortalecimientoUseCaseAdapter implements GestionFortalecimientoUseCasePort {

    FortalecimientoPersistencePort persistencePort;
    GestionArchivosUseCasePort gestorArchivos;
    GenerarReportePort reportePort;

    static final String MODULO_FFC = "evidencias_ffc";
    static final String TX_MANAGER = "txManagerNegocio";

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public Pagina<FortalecimientoCapacidades> listar(String cuo, ListarFortalecimientoQuery query, int pagina, int tamanio) {
        return persistencePort.listar(cuo, query, pagina, tamanio);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public FortalecimientoCapacidades registrar(String cuo, FortalecimientoCapacidades dominio, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos) throws Exception {

        // 1. Validaciones
        validarReglasNegocio(dominio);

        // 2. Generar Correlativo
        String anio = String.valueOf(dominio.getFechaInicio().getYear());
        Long distritoId = dominio.getDistritoJudicialId();

        String ultimoCodigo = persistencePort.obtenerUltimoCodigo(cuo, distritoId, anio);
        long correlativo = 1;

        if (ultimoCodigo != null && !ultimoCodigo.isBlank()) {
            try {
                // Formato esperado BD: 000005-15-2026-FC
                String numeroStr = ultimoCodigo.split("-")[0];
                correlativo = Long.parseLong(numeroStr) + 1;
            } catch (Exception e) {
                log.warn("[{}] Error parseando ultimo codigo {}. Reiniciando a 1.", cuo, ultimoCodigo);
            }
        }

        String nuevoCodigo = String.format("%06d-%02d-%s-FC", correlativo, distritoId, anio);
        dominio.setCodigo(nuevoCodigo);
        dominio.setActivo("1");

        FortalecimientoCapacidades registrado = persistencePort.guardar(cuo, dominio);
        subirArchivosAdjuntos(cuo, registrado, anexo, videos, fotos);

        return registrado;
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public FortalecimientoCapacidades buscarPorId(String cuo, Long id) {
        if (id == null) throw new IllegalArgumentException("El ID es obligatorio.");

        FortalecimientoCapacidades encontrado = persistencePort.obtenerPorId(cuo, id);
        if (encontrado == null) {
            throw new MovimientoNoEncontradoException("No se encontró el evento con ID: " + id);
        }
        return encontrado;
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public FortalecimientoCapacidades actualizar(String cuo, FortalecimientoCapacidades dominio) throws Exception {
        if (dominio.getId() == null) {
            throw new IllegalArgumentException("El ID es obligatorio para actualizar.");
        }

        validarReglasNegocio(dominio);

        return persistencePort.actualizar(cuo, dominio);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public void agregarArchivo(String cuo, Long idEvento, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception {
        if (archivo == null || archivo.isEmpty()) throw new IllegalArgumentException("El archivo no puede estar vacío.");

        FortalecimientoCapacidades evento = buscarPorId(cuo, idEvento);
        evento.setUsuario(usuarioOperacion); // Para auditoría del archivo

        gestorArchivos.subirArchivo(
                archivo,
                String.valueOf(evento.getDistritoJudicialId()),
                tipoArchivo,
                MODULO_FFC,
                evento.getFechaInicio(),
                evento.getCodigo(), // Carpeta/Nombre lógico
                evento
        );
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public void eliminarArchivo(String cuo, Long idArchivo, String usuarioOperacion) throws Exception {
        FortalecimientoCapacidades audit = new FortalecimientoCapacidades();
        audit.setUsuario(usuarioOperacion);

        gestorArchivos.eliminarPorId(idArchivo, audit);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRED, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public RecursoArchivo descargarAnexo(String cuo, Long idEvento) throws Exception {
        FortalecimientoCapacidades evento = buscarPorId(cuo, idEvento);

        if (evento.getArchivosGuardados() == null || evento.getArchivosGuardados().isEmpty()) {
            throw new MovimientoNoEncontradoException("El evento no tiene archivos adjuntos.");
        }

        Archivo archivoAnexo = evento.getArchivosGuardados().stream()
                .filter(a -> "ANEXO".equalsIgnoreCase(a.getTipo()))
                .findFirst()
                .orElseThrow(() -> new MovimientoNoEncontradoException("No existe anexo principal para este evento."));

        return gestorArchivos.descargarPorId(archivoAnexo.getId());
    }

    @Override
    public RecursoArchivo descargarArchivoPorId(Long idArchivo) throws Exception {
        return gestorArchivos.descargarPorId(idArchivo);
    }

    @Override
    public byte[] generarFichaPdf(String cuo, Long idEvento) throws Exception {
        buscarPorId(cuo, idEvento);
        return reportePort.generarFichaFortalecimiento(String.valueOf(idEvento));
    }

    // --- PRIVADOS ---

    private void validarReglasNegocio(FortalecimientoCapacidades dominio) {
        if (dominio.getFechaInicio() == null || dominio.getFechaFin() == null) {
            throw new IllegalArgumentException("Las fechas son obligatorias.");
        }
        if (dominio.getFechaFin().isBefore(dominio.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha fin no puede ser anterior a la fecha inicio.");
        }

        // Asignar fecha del evento a las tareas si no la tienen
        if (dominio.getTareasRealizadas() != null) {
            for (var tarea : dominio.getTareasRealizadas()) {
                if (tarea.getFechaInicio() == null) {
                    tarea.setFechaInicio(dominio.getFechaInicio());
                }
            }
        }
    }

    private void subirArchivosAdjuntos(String cuo, FortalecimientoCapacidades registrado, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos) throws Exception {
        String distritoStr = String.valueOf(registrado.getDistritoJudicialId());

        if (anexo != null && !anexo.isEmpty()) {
            gestorArchivos.subirArchivo(anexo, distritoStr, "ANEXO", MODULO_FFC, registrado.getFechaInicio(), registrado.getCodigo(), registrado);
        }
        if (fotos != null) {
            for (MultipartFile f : fotos) {
                if (!f.isEmpty()) gestorArchivos.subirArchivo(f, distritoStr, "FOTO", MODULO_FFC, registrado.getFechaInicio(), registrado.getCodigo(), registrado);
            }
        }
        if (videos != null) {
            for (MultipartFile v : videos) {
                if (!v.isEmpty()) gestorArchivos.subirArchivo(v, distritoStr, "VIDEO", MODULO_FFC, registrado.getFechaInicio(), registrado.getCodigo(), registrado);
            }
        }
    }
}