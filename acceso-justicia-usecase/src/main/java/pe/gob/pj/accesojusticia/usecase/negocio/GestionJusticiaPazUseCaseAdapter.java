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
import pe.gob.pj.accesojusticia.domain.model.negocio.JpeCasoAtendido;
import pe.gob.pj.accesojusticia.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.accesojusticia.domain.model.negocio.query.ListarJpeCasosQuery;
import pe.gob.pj.accesojusticia.domain.port.output.GenerarReportePort;
import pe.gob.pj.accesojusticia.domain.port.persistence.negocio.JusticiaPazPersistencePort;
import pe.gob.pj.accesojusticia.domain.port.usecase.negocio.GestionArchivosUseCasePort;
import pe.gob.pj.accesojusticia.domain.port.usecase.negocio.GestionJusticiaPazUseCasePort;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GestionJusticiaPazUseCaseAdapter implements GestionJusticiaPazUseCasePort {

    JusticiaPazPersistencePort persistencePort;
    GestionArchivosUseCasePort gestorArchivos;
    GenerarReportePort reportePort;

    static final String MODULO_JPE_CASOS = "evidencias_jpe_casos";
    static final String TX_MANAGER = "txManagerNegocio";

    // =========================================================================================
    // CONSULTAS
    // =========================================================================================

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public Pagina<JpeCasoAtendido> listar(String cuo, ListarJpeCasosQuery query, int pagina, int tamanio) {
        return persistencePort.listar(cuo, query, pagina, tamanio);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public JpeCasoAtendido buscarPorId(String cuo, Long id) {
        if (id == null) throw new IllegalArgumentException("El ID es obligatorio.");

        JpeCasoAtendido encontrado = persistencePort.obtenerPorId(cuo, id);
        if (encontrado == null) {
            throw new MovimientoNoEncontradoException("No se encontró el caso con ID: " + id);
        }
        return encontrado;
    }

    // =========================================================================================
    // TRANSACCIONES
    // =========================================================================================

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public JpeCasoAtendido registrar(String cuo, JpeCasoAtendido dominio, MultipartFile acta, List<MultipartFile> fotos) throws Exception {

        String anio = String.valueOf(dominio.getFechaRegistroCaso().getYear());
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

        String nuevoCodigo = String.format("%06d-%02d-%s-PE", correlativo, distritoId, anio);
        dominio.setCodigo(nuevoCodigo);
        dominio.setActivo("1");

        // 2. Guardar en Base de Datos
        JpeCasoAtendido registrado = persistencePort.guardar(cuo, dominio);

        // 3. Subir Archivos Adjuntos
        subirArchivosAdjuntos(cuo, registrado, acta, fotos);

        return registrado;
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public JpeCasoAtendido actualizar(String cuo, JpeCasoAtendido dominio) throws Exception {
        if (dominio.getId() == null) throw new IllegalArgumentException("El ID es obligatorio para actualizar.");

        return persistencePort.actualizar(cuo, dominio);
    }

    // =========================================================================================
    // ARCHIVOS
    // =========================================================================================

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public void agregarArchivo(String cuo, Long idCaso, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception {
        if (archivo == null || archivo.isEmpty()) throw new IllegalArgumentException("El archivo no puede estar vacío.");

        JpeCasoAtendido caso = buscarPorId(cuo, idCaso);
        caso.setUsuario(usuarioOperacion);

        gestorArchivos.subirArchivo(
                archivo,
                String.valueOf(caso.getDistritoJudicialId()),
                tipoArchivo,
                MODULO_JPE_CASOS,
                caso.getFechaRegistroCaso(),
                caso.getCodigo(),
                caso
        );
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public void eliminarArchivo(String cuo, Long idArchivo, String usuarioOperacion) throws Exception {
        JpeCasoAtendido audit = new JpeCasoAtendido();
        audit.setUsuario(usuarioOperacion);

        gestorArchivos.eliminarPorId(idArchivo, audit);
    }

    @Override
    public RecursoArchivo descargarArchivoPorTipo(String cuo, Long idCaso, String tipoArchivo) throws Exception {
        JpeCasoAtendido caso = buscarPorId(cuo, idCaso);

        if (caso.getArchivosGuardados() == null || caso.getArchivosGuardados().isEmpty()) {
            throw new MovimientoNoEncontradoException("El caso no tiene archivos adjuntos.");
        }

        Archivo archivoEncontrado = caso.getArchivosGuardados().stream()
                .filter(a -> tipoArchivo.equalsIgnoreCase(a.getTipo()))
                .findFirst()
                .orElseThrow(() -> new MovimientoNoEncontradoException("No existe un archivo de tipo " + tipoArchivo + " para este caso."));

        return gestorArchivos.descargarPorId(archivoEncontrado.getId());
    }

    @Override
    public RecursoArchivo descargarArchivoPorId(Long idArchivo) throws Exception {
        return gestorArchivos.descargarPorId(idArchivo);
    }

    // =========================================================================================
    // REPORTES Y GRÁFICOS
    // =========================================================================================

    @Override
    public byte[] generarFichaPdf(String cuo, Long idCaso) throws Exception {
        buscarPorId(cuo, idCaso);
        return reportePort.generarFichaJpe(String.valueOf(idCaso));
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        return persistencePort.obtenerResumenGrafico();
    }

    private void subirArchivosAdjuntos(String cuo, JpeCasoAtendido registrado, MultipartFile acta, List<MultipartFile> fotos) throws Exception {
        String distritoStr = String.valueOf(registrado.getDistritoJudicialId());

        if (acta != null && !acta.isEmpty()) {
            gestorArchivos.subirArchivo(acta, distritoStr, "ACTA", MODULO_JPE_CASOS, registrado.getFechaRegistroCaso(), registrado.getCodigo(), registrado);
        }
        if (fotos != null) {
            for (MultipartFile f : fotos) {
                if (!f.isEmpty()) gestorArchivos.subirArchivo(f, distritoStr, "FOTO", MODULO_JPE_CASOS, registrado.getFechaRegistroCaso(), registrado.getCodigo(), registrado);
            }
        }
    }
}