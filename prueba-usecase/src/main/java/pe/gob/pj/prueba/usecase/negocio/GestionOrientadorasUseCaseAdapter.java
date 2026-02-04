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
import pe.gob.pj.prueba.domain.model.negocio.OrientadoraJudicial;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarOrientadoraQuery;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.OrientadoraJudicialPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionArchivosUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionOrientadorasUseCasePort;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GestionOrientadorasUseCaseAdapter implements GestionOrientadorasUseCasePort {

    OrientadoraJudicialPersistencePort persistencePort;
    GestionArchivosUseCasePort gestorArchivos;
    GenerarReportePort reportePort;

    static final String MODULO_OJ = "evidencias_oj";
    static final String TX_MANAGER = "txManagerNegocio";


    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public Pagina<OrientadoraJudicial> listar(String cuo, ListarOrientadoraQuery query, int pagina, int tamanio) {
        return persistencePort.listar(cuo, query, pagina, tamanio);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public OrientadoraJudicial buscarPorId(String cuo, Long id) {
        if (id == null) throw new IllegalArgumentException("El ID es obligatorio.");

        OrientadoraJudicial encontrado = persistencePort.obtenerPorId(cuo, id);
        if (encontrado == null) {
            throw new MovimientoNoEncontradoException("No se encontró el registro con ID: " + id);
        }
        return encontrado;
    }


    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public OrientadoraJudicial registrarAtencion(String cuo, OrientadoraJudicial dominio, MultipartFile anexo, List<MultipartFile> fotos) throws Exception {

        String anio = String.valueOf(dominio.getFechaAtencion().getYear());
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

        // Formato: 6 dígitos (Correlativo) - 2 dígios (Distrito) - 4 dígitos (Año) - OJ
        String nuevoCodigo = String.format("%06d-%02d-%s-OJ", correlativo, distritoId, anio);
        dominio.setCodigo(nuevoCodigo);
        dominio.setActivo("1");

        // 2. Guardar en Base de Datos
        OrientadoraJudicial registrado = persistencePort.guardar(cuo, dominio);

        // 3. Subir Archivos Adjuntos
        subirArchivosAdjuntos(cuo, registrado, anexo, fotos);

        return registrado;
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public OrientadoraJudicial actualizar(String cuo, OrientadoraJudicial dominio) throws Exception {
        if (dominio.getId() == null) throw new IllegalArgumentException("El ID es obligatorio para actualizar.");

        return persistencePort.actualizar(cuo, dominio);
    }


    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public void agregarArchivo(String cuo, Long idCaso, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception {
        if (archivo == null || archivo.isEmpty()) throw new IllegalArgumentException("El archivo no puede estar vacío.");

        OrientadoraJudicial caso = buscarPorId(cuo, idCaso);
        caso.setUsuario(usuarioOperacion);

        gestorArchivos.subirArchivo(
                archivo,
                String.valueOf(caso.getDistritoJudicialId()),
                tipoArchivo,
                MODULO_OJ,
                caso.getFechaAtencion(),
                caso.getCodigo(),
                caso
        );
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public void eliminarArchivo(String cuo, Long idArchivo, String usuarioOperacion) throws Exception {
        OrientadoraJudicial audit = new OrientadoraJudicial();
        audit.setUsuario(usuarioOperacion);

        gestorArchivos.eliminarPorId(idArchivo, audit);
    }

    @Override
    public RecursoArchivo descargarArchivoPorTipo(String cuo, Long idCaso, String tipoArchivo) throws Exception {
        OrientadoraJudicial caso = buscarPorId(cuo, idCaso);

        if (caso.getArchivosGuardados() == null || caso.getArchivosGuardados().isEmpty()) {
            throw new MovimientoNoEncontradoException("El registro no tiene archivos adjuntos.");
        }

        Archivo archivoEncontrado = caso.getArchivosGuardados().stream()
                .filter(a -> tipoArchivo.equalsIgnoreCase(a.getTipo()))
                .findFirst()
                .orElseThrow(() -> new MovimientoNoEncontradoException("No existe un archivo de tipo " + tipoArchivo + " para este registro."));

        return gestorArchivos.descargarPorId(archivoEncontrado.getId());
    }

    @Override
    public RecursoArchivo descargarArchivoPorId(Long idArchivo) throws Exception {
        return gestorArchivos.descargarPorId(idArchivo);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        return persistencePort.obtenerResumenGrafico();
    }

    @Override
    public byte[] generarFichaPdf(String cuo, Long id) throws Exception {
        buscarPorId(cuo, id);
        return reportePort.generarFichaOJ(String.valueOf(id));
    }

    private void subirArchivosAdjuntos(String cuo, OrientadoraJudicial registrado, MultipartFile anexo, List<MultipartFile> fotos) throws Exception {
        String distritoStr = String.valueOf(registrado.getDistritoJudicialId());

        if (anexo != null && !anexo.isEmpty()) {
            gestorArchivos.subirArchivo(anexo, distritoStr, "ANEXO", MODULO_OJ, registrado.getFechaAtencion(), registrado.getCodigo(), registrado);
        }
        if (fotos != null) {
            for (MultipartFile f : fotos) {
                if (!f.isEmpty()) gestorArchivos.subirArchivo(f, distritoStr, "FOTO", MODULO_OJ, registrado.getFechaAtencion(), registrado.getCodigo(), registrado);
            }
        }
    }
}