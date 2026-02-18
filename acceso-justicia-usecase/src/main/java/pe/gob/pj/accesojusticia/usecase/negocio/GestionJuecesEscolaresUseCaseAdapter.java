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
import pe.gob.pj.accesojusticia.domain.model.negocio.JuezPazEscolar;
import pe.gob.pj.accesojusticia.domain.model.negocio.query.ListarJuezEscolarQuery;
import pe.gob.pj.accesojusticia.domain.port.persistence.negocio.JuezPazEscolarPersistencePort;
import pe.gob.pj.accesojusticia.domain.port.usecase.negocio.GestionArchivosUseCasePort;
import pe.gob.pj.accesojusticia.domain.port.usecase.negocio.GestionJuecesEscolaresUseCasePort;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GestionJuecesEscolaresUseCaseAdapter implements GestionJuecesEscolaresUseCasePort {

    JuezPazEscolarPersistencePort persistencePort;
    GestionArchivosUseCasePort gestorArchivos;

    static final String MODULO_JPE = "evidencias_jpe";
    static final String TX_MANAGER = "txManagerNegocio";

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public Pagina<JuezPazEscolar> listar(String cuo, ListarJuezEscolarQuery query, int pagina, int tamanio) {
        return persistencePort.listar(cuo, query, pagina, tamanio);
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public JuezPazEscolar registrar(String cuo, JuezPazEscolar dominio, MultipartFile anexo, List<MultipartFile> fotos) throws Exception {

        if (persistencePort.existeDniEnColegio(dominio.getDni(), dominio.getInstitucionEducativaId())) {
            throw new IllegalArgumentException("El alumno con DNI " + dominio.getDni() + " ya está registrado en este colegio.");
        }

        // 2. Generar Correlativo (Ej: 000001-2026-JE)
        String anio = String.valueOf(LocalDate.now().getYear());

        String ultimoCodigo = persistencePort.obtenerUltimoCodigo(cuo, anio);
        long correlativo = 1;

        if (ultimoCodigo != null && !ultimoCodigo.isBlank()) {
            try {
                String numeroStr = ultimoCodigo.split("-")[0];
                correlativo = Long.parseLong(numeroStr) + 1;
            } catch (Exception e) {
                log.warn("[{}] Error parseando ultimo codigo {}. Reiniciando a 1.", cuo, ultimoCodigo);
            }
        }

        String nuevoCodigo = String.format("%06d-%s-JE", correlativo, anio);
        dominio.setCodigo(nuevoCodigo);
        dominio.setActivo("1");

        JuezPazEscolar registrado = persistencePort.guardar(cuo, dominio);

        subirArchivosAdjuntos(cuo, registrado, anexo, fotos);

        return registrado;
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = {Exception.class, SQLException.class})
    public JuezPazEscolar buscarPorId(String cuo, Long id) {
        if (id == null) throw new IllegalArgumentException("El ID es obligatorio.");

        JuezPazEscolar encontrado = persistencePort.obtenerPorId(cuo, id);
        if (encontrado == null) {
            throw new MovimientoNoEncontradoException("No se encontró el juez escolar con ID: " + id);
        }
        return encontrado;
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public JuezPazEscolar actualizar(String cuo, JuezPazEscolar dominio) throws Exception {
        if (dominio.getId() == null) throw new IllegalArgumentException("El ID es obligatorio para actualizar.");

        JuezPazEscolar actual = buscarPorId(cuo, dominio.getId());
        if (!actual.getDni().equals(dominio.getDni())) {
            if (persistencePort.existeDniEnColegio(dominio.getDni(), dominio.getInstitucionEducativaId())) {
                throw new IllegalArgumentException("El nuevo DNI " + dominio.getDni() + " ya está registrado en este colegio.");
            }
        }

        return persistencePort.actualizar(cuo, dominio);
    }

    // =========================================================================================
    // ARCHIVOS
    // =========================================================================================

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public void agregarArchivo(String cuo, Long idJuez, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception {
        if (archivo == null || archivo.isEmpty()) throw new IllegalArgumentException("El archivo no puede estar vacío.");

        JuezPazEscolar evento = buscarPorId(cuo, idJuez);
        evento.setUsuario(usuarioOperacion);
        gestorArchivos.subirArchivo(
                archivo,
                String.valueOf(evento.getInstitucionEducativaId()),
                tipoArchivo,
                MODULO_JPE,
                LocalDate.now(),
                evento.getCodigo(),
                evento
        );
    }

    @Override
    @Transactional(transactionManager = TX_MANAGER, propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, SQLException.class})
    public void eliminarArchivo(String cuo, Long idArchivo, String usuarioOperacion) throws Exception {
        JuezPazEscolar audit = new JuezPazEscolar();
        audit.setUsuario(usuarioOperacion);

        gestorArchivos.eliminarPorId(idArchivo, audit);
    }

    @Override
    public RecursoArchivo descargarArchivoPorTipo(String cuo, Long idJuez, String tipoArchivo) throws Exception {
        JuezPazEscolar juez = buscarPorId(cuo, idJuez);

        if (juez.getArchivosGuardados() == null || juez.getArchivosGuardados().isEmpty()) {
            throw new MovimientoNoEncontradoException("El registro no tiene archivos adjuntos.");
        }

        Archivo archivoEncontrado = juez.getArchivosGuardados().stream()
                .filter(a -> tipoArchivo.equalsIgnoreCase(a.getTipo()))
                .findFirst()
                .orElseThrow(() -> new MovimientoNoEncontradoException("No existe un archivo de tipo " + tipoArchivo + " para este registro."));

        return gestorArchivos.descargarPorId(archivoEncontrado.getId());
    }

    @Override
    public RecursoArchivo descargarArchivoPorId(Long idArchivo) throws Exception {
        return gestorArchivos.descargarPorId(idArchivo);
    }

    // --- PRIVADOS ---

    private void subirArchivosAdjuntos(String cuo, JuezPazEscolar registrado, MultipartFile anexo, List<MultipartFile> fotos) throws Exception {
        String colegioIdStr = String.valueOf(registrado.getInstitucionEducativaId());

        if (anexo != null && !anexo.isEmpty()) {
            gestorArchivos.subirArchivo(anexo, colegioIdStr, "RESOLUCION", MODULO_JPE, LocalDate.now(), registrado.getCodigo(), registrado);
        }
        if (fotos != null) {
            for (MultipartFile f : fotos) {
                if (!f.isEmpty()) gestorArchivos.subirArchivo(f, colegioIdStr, "FOTO", MODULO_JPE, LocalDate.now(), registrado.getCodigo(), registrado);
            }
        }
    }
}