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
import pe.gob.pj.prueba.domain.model.negocio.PromocionCultura;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarPromocionQuery;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.PromocionCulturaPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionArchivosUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionPromocionUseCasePort;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GestionPromocionUseCaseAdapter implements GestionPromocionUseCasePort {

    PromocionCulturaPersistencePort persistencePort;
    GestionArchivosUseCasePort gestorArchivos;
    GenerarReportePort reportePort;

    static final String MODULO_PC = "evidencias_pc";
    static final String TX_MANAGER = "txManagerNegocio";

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public Pagina<PromocionCultura> listar(String cuo, ListarPromocionQuery query, int pagina, int tamanio, String rolUsuario, String loginUsuario) {

        if (rolUsuario != null && rolUsuario.toUpperCase().contains("JUEZ")) {
            query.setUsuarioRegistroLogin(loginUsuario);
        } else {
            query.setUsuarioRegistroLogin(null);
        }

        return persistencePort.listar(cuo, query, pagina, tamanio);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRED, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public byte[] exportarExcel(String cuo, ListarPromocionQuery query, String rolUsuario, String loginUsuario) throws Exception {
        log.info("[{}] Iniciando exportación Excel de Promoción Cultura...", cuo);

        if (rolUsuario != null && rolUsuario.toUpperCase().contains("JUEZ")) {
            query.setUsuarioRegistroLogin(loginUsuario);
        } else {
            query.setUsuarioRegistroLogin(null);
        }

        List<PromocionCultura> lista = persistencePort.listarParaExcel(cuo, query);

        if (lista.isEmpty()) {
            log.warn("[{}] No se encontraron registros de Promoción Cultura para exportar.", cuo);
        }

        return reportePort.generarExcelPromocion(lista);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public PromocionCultura registrar(String cuo, PromocionCultura dominio, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos) throws Exception {

        // 1. Validaciones
        validarReglasNegocio(dominio);

        // 2. Generar Correlativo (Formato: 000001-01-2025-PC)
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

        String nuevoCodigo = String.format("%06d-%02d-%s-PC", correlativo, distritoId, anio);
        dominio.setCodigo(nuevoCodigo);
        dominio.setActivo("1");

        // 3. Guardar
        PromocionCultura registrado = persistencePort.guardar(cuo, dominio);

        // 4. Archivos
        subirArchivosAdjuntos(cuo, registrado, anexo, videos, fotos);

        return registrado;
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public PromocionCultura buscarPorId(String cuo, Long id) {
        if (id == null) throw new IllegalArgumentException("El ID es obligatorio.");

        PromocionCultura encontrado = persistencePort.obtenerPorId(cuo, id);
        if (encontrado == null) {
            throw new MovimientoNoEncontradoException("No se encontró el registro con ID: " + id);
        }
        return encontrado;
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public PromocionCultura actualizar(String cuo, PromocionCultura dominio) throws Exception {
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

        PromocionCultura evento = buscarPorId(cuo, idEvento);
        evento.setUsuario(usuarioOperacion);

        gestorArchivos.subirArchivo(
                archivo,
                String.valueOf(evento.getDistritoJudicialId()),
                tipoArchivo,
                MODULO_PC,
                evento.getFechaInicio(),
                evento.getCodigo(),
                evento
        );
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public void eliminarArchivo(String cuo, Long idArchivo, String usuarioOperacion) throws Exception {
        PromocionCultura audit = new PromocionCultura();
        audit.setUsuario(usuarioOperacion);
        gestorArchivos.eliminarPorId(idArchivo, audit);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRED, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public RecursoArchivo descargarAnexo(String cuo, Long idEvento) throws Exception {
        PromocionCultura evento = buscarPorId(cuo, idEvento);

        if (evento.getArchivosGuardados() == null || evento.getArchivosGuardados().isEmpty()) {
            throw new MovimientoNoEncontradoException("El registro no tiene archivos adjuntos.");
        }

        Archivo archivoAnexo = evento.getArchivosGuardados().stream()
                .filter(a -> "ANEXO".equalsIgnoreCase(a.getTipo()))
                .findFirst()
                .orElseThrow(() -> new MovimientoNoEncontradoException("No existe anexo principal para este registro."));

        return gestorArchivos.descargarPorId(archivoAnexo.getId());
    }

    @Override
    public RecursoArchivo descargarArchivoPorId(Long idArchivo) throws Exception {
        return gestorArchivos.descargarPorId(idArchivo);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRED, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public byte[] generarFichaPdf(String cuo, Long idEvento) throws Exception {
        if (persistencePort.obtenerPorId(cuo, idEvento) == null) {
            throw new MovimientoNoEncontradoException("Registro no existe");
        }
        return reportePort.generarFichaPromocion(idEvento);
    }

    // --- PRIVADOS ---

    private void validarReglasNegocio(PromocionCultura dominio) {
        if (dominio.getFechaInicio() == null || dominio.getFechaFin() == null) {
            throw new IllegalArgumentException("Las fechas son obligatorias.");
        }
        if (dominio.getFechaFin().isBefore(dominio.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha fin no puede ser anterior a la fecha inicio.");
        }

        // Consistencia de datos para tareas
        if (dominio.getTareasRealizadas() != null) {
            for (var tarea : dominio.getTareasRealizadas()) {
                if (tarea.getFechaInicio() == null) {
                    tarea.setFechaInicio(dominio.getFechaInicio());
                }
            }
        }
    }

    private void subirArchivosAdjuntos(String cuo, PromocionCultura registrado, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos) throws Exception {
        String distritoStr = String.valueOf(registrado.getDistritoJudicialId());

        if (anexo != null && !anexo.isEmpty()) {
            gestorArchivos.subirArchivo(anexo, distritoStr, "ANEXO", MODULO_PC, registrado.getFechaInicio(), registrado.getCodigo(), registrado);
        }
        if (fotos != null) {
            for (MultipartFile f : fotos) {
                if (!f.isEmpty()) gestorArchivos.subirArchivo(f, distritoStr, "FOTO", MODULO_PC, registrado.getFechaInicio(), registrado.getCodigo(), registrado);
            }
        }
        if (videos != null) {
            for (MultipartFile v : videos) {
                if (!v.isEmpty()) gestorArchivos.subirArchivo(v, distritoStr, "VIDEO", MODULO_PC, registrado.getFechaInicio(), registrado.getCodigo(), registrado);
            }
        }
    }

}