package pe.gob.pj.accesojusticia.usecase.negocio;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.accesojusticia.domain.exceptions.negocio.MovimientoNoEncontradoException;
import pe.gob.pj.accesojusticia.domain.model.common.Pagina;
import pe.gob.pj.accesojusticia.domain.model.common.RecursoArchivo;
import pe.gob.pj.accesojusticia.domain.model.negocio.Archivo;
import pe.gob.pj.accesojusticia.domain.model.negocio.JusticiaItinerante;
import pe.gob.pj.accesojusticia.domain.model.negocio.query.ListarJusticiaItineranteQuery;
import pe.gob.pj.accesojusticia.domain.port.output.GenerarReportePort;
import pe.gob.pj.accesojusticia.domain.port.persistence.negocio.JusticiaItinerantePersistencePort;
import pe.gob.pj.accesojusticia.domain.port.usecase.negocio.GestionArchivosUseCasePort;
import pe.gob.pj.accesojusticia.domain.port.usecase.negocio.GestionJusticiaItineranteUseCasePort;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GestionJusticiaItineranteUseCaseAdapter implements GestionJusticiaItineranteUseCasePort {

    JusticiaItinerantePersistencePort persistencePort;
    GenerarReportePort reportePort;
    GestionArchivosUseCasePort gestorArchivos;

    static final String MODULO_JI = "evidencias_fji";
    static final String TX_MANAGER = "txManagerNegocio";

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Pagina<JusticiaItinerante> listar(String cuo, ListarJusticiaItineranteQuery query, int pagina, int tamanio, String rolUsuario, String loginUsuario) {

        if (rolUsuario != null && rolUsuario.toUpperCase().contains("JUEZ")) {
            query.setUsuarioRegistroLogin(loginUsuario);
        } else {
            query.setUsuarioRegistroLogin(null);
        }

        return persistencePort.listar(cuo, query, pagina, tamanio);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRED, readOnly = true)
    public byte[] exportarExcel(String cuo, ListarJusticiaItineranteQuery query, String rolUsuario, String loginUsuario) throws Exception {
        log.info("[{}] Iniciando exportación Excel...", cuo);

        if (rolUsuario != null && rolUsuario.toUpperCase().contains("JUEZ")) {
            query.setUsuarioRegistroLogin(loginUsuario);
        } else {
            query.setUsuarioRegistroLogin(null);
        }

        List<JusticiaItinerante> lista = persistencePort.listarParaExcel(cuo, query);

        if (lista.isEmpty()) {
            log.warn("[{}] No se encontraron registros.", cuo);
        }

        return reportePort.generarExcelListado(lista);
    }
    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public JusticiaItinerante registrar(String cuo, JusticiaItinerante dominio, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos) throws Exception {

        // 1. Validaciones
        if (dominio.getFechaInicio() == null) throw new IllegalArgumentException("La fecha de inicio es obligatoria.");
        validarFechasTareas(dominio);

        // 2. Generar Código Correlativo (Ej: 00001-15-2026-JI)
        String anio = String.valueOf(dominio.getFechaInicio().getYear());

        Long distritoId = dominio.getDistritoJudicialId();

        String ultimoCodigo = persistencePort.obtenerUltimoCodigo(cuo, distritoId, anio);
        long correlativo = 1;

        if (ultimoCodigo != null && !ultimoCodigo.isBlank()) {
            try {
                // Formato: 000005-15-2026-JI -> Toma "000005"
                String numeroStr = ultimoCodigo.split("-")[0];
                correlativo = Long.parseLong(numeroStr) + 1;
            } catch (Exception e) {
                log.warn("[{}] Error parseando ultimo codigo {}. Reiniciando a 1.", cuo, ultimoCodigo);
            }
        }

        String nuevoCodigo = String.format("%06d-%02d-%s-JI", correlativo, distritoId, anio);
        dominio.setCodigo(nuevoCodigo);
        dominio.setActivo("1");

        // 3. Guardar Entidad (Genera ID Long)
        JusticiaItinerante registrado = persistencePort.guardar(cuo, dominio);

        // 4. Subir Archivos (Usando Código Visible)
        if (anexo != null && !anexo.isEmpty()) {
            gestorArchivos.subirArchivo(anexo, String.valueOf(distritoId), "ANEXO", MODULO_JI, registrado.getFechaInicio(), registrado.getCodigo(), dominio);
        }
        if (fotos != null) {
            for (MultipartFile f : fotos) {
                if (!f.isEmpty()) gestorArchivos.subirArchivo(f, String.valueOf(distritoId), "FOTO", MODULO_JI, registrado.getFechaInicio(), registrado.getCodigo(), dominio);
            }
        }
        if (videos != null) {
            for (MultipartFile v : videos) {
                if (!v.isEmpty()) gestorArchivos.subirArchivo(v, String.valueOf(distritoId), "VIDEO", MODULO_JI, registrado.getFechaInicio(), registrado.getCodigo(), dominio);
            }
        }

        return registrado;
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public JusticiaItinerante buscarPorId(String cuo, Long id) {
        if (id == null) throw new IllegalArgumentException("El ID es obligatorio.");

        JusticiaItinerante encontrado = persistencePort.obtenerPorId(cuo, id);
        if (encontrado == null) {
            throw new MovimientoNoEncontradoException("No se encontró el registro con ID: " + id);
        }
        return encontrado;
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public JusticiaItinerante actualizar(String cuo, JusticiaItinerante dominio) throws Exception {
        if (dominio.getId() == null) throw new IllegalArgumentException("El ID es obligatorio para actualizar.");
        if (dominio.getFechaInicio() == null) throw new IllegalArgumentException("La fecha de inicio es obligatoria.");

        validarFechasTareas(dominio);

        return persistencePort.actualizar(cuo, dominio);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public void agregarArchivo(String cuo, Long idEvento, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception {
        if (archivo == null || archivo.isEmpty()) throw new IllegalArgumentException("El archivo no puede estar vacío.");

        JusticiaItinerante evento = buscarPorId(cuo, idEvento);
        evento.setUsuario(usuarioOperacion);

        gestorArchivos.subirArchivo(archivo, String.valueOf(evento.getDistritoJudicialId()), tipoArchivo, MODULO_JI, evento.getFechaInicio(), evento.getCodigo(), evento);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public void eliminarArchivo(String cuo, Long idArchivo, String usuarioOperacion) throws Exception {
        JusticiaItinerante audit = new JusticiaItinerante();
        audit.setUsuario(usuarioOperacion);

        gestorArchivos.eliminarPorId(idArchivo, audit);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRED, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public RecursoArchivo descargarAnexo(String cuo, Long idEvento) throws Exception {
        JusticiaItinerante evento = buscarPorId(cuo, idEvento);

        if (evento.getArchivosGuardados() == null) throw new MovimientoNoEncontradoException("El evento no tiene archivos adjuntos.");

        Archivo archivoAnexo = evento.getArchivosGuardados().stream()
                .filter(a -> "ANEXO".equalsIgnoreCase(a.getTipo()))
                .findFirst()
                .orElseThrow(() -> new MovimientoNoEncontradoException("No existe anexo para este evento."));

        return gestorArchivos.descargarPorId(archivoAnexo.getId());
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRED, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public byte[] generarFichaPdf(String cuo, Long idEvento) throws Exception {
        if (persistencePort.obtenerPorId(cuo, idEvento) == null) {
            throw new MovimientoNoEncontradoException("Evento no existe");
        }
        return reportePort.generarFichaItinerante(idEvento);
    }

    private void validarFechasTareas(JusticiaItinerante dominio) {
        if (dominio.getTareasRealizadas() != null) {
            for (JusticiaItinerante.DetalleTarea tarea : dominio.getTareasRealizadas()) {
                if (tarea.getFechaInicio() == null) {
                    tarea.setFechaInicio(dominio.getFechaInicio());
                }
            }
        }
    }

    @Override
    public RecursoArchivo descargarArchivoPorId(Long idArchivo) throws Exception {
        return gestorArchivos.descargarPorId(idArchivo);
    }

}