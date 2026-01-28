package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.JuezPazEscolar;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.JuezPazEscolarPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionArchivosUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionJuecesEscolaresUseCasePort;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class GestionJuecesEscolaresUseCaseAdapter implements GestionJuecesEscolaresUseCasePort {

    private final JuezPazEscolarPersistencePort persistencePort;
    private final GestionArchivosPersistencePort archivosPersistencePort;
    private final GestionArchivosUseCasePort gestorArchivos;
    private static final String MODULO_JPE = "evidencias_jpe";

    @Override
    public Pagina<JuezPazEscolar> listar(JuezPazEscolar filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listar(filtros, pagina, tamanio);
    }

    @Override
    public JuezPazEscolar buscarPorId(String id) throws Exception {
        return persistencePort.buscarPorId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JuezPazEscolar registrar(JuezPazEscolar juez, MultipartFile resolucion, String usuario) throws Exception {
        if (persistencePort.existeDniEnColegio(juez.getDni(), juez.getInstitucionEducativaId())) {
            throw new Exception("El alumno con DNI " + juez.getDni() + " ya está registrado en este colegio.");
        }

        String idCorto = generarIdCorto();

        juez.setId(idCorto);
        juez.setFechaRegistro(LocalDate.now());
        juez.setUsuarioRegistro(usuario);
        juez.setActivo("1");

        JuezPazEscolar registrado = persistencePort.guardar(juez);

        if (resolucion != null && !resolucion.isEmpty()) {
            gestorArchivos.subirArchivo(resolucion, "00", "RESOLUCION_JPE", MODULO_JPE, registrado.getFechaRegistro(), registrado.getId());
        }
        return registrado;
    }

    @Override
    @Transactional
    public JuezPazEscolar actualizar(JuezPazEscolar juez, String usuario) throws Exception {
        if (juez.getId() == null) throw new Exception("ID Requerido");
        juez.setUsuarioRegistro(usuario);
        return persistencePort.actualizar(juez);
    }

    @Override
    @Transactional
    public void agregarArchivo(String idJuez, MultipartFile archivo, String tipo, String usuario) throws Exception {
        JuezPazEscolar juez = persistencePort.buscarPorId(idJuez);
        if (juez == null) throw new Exception("Juez no encontrado");

        LocalDate fecha = (juez.getFechaRegistro() != null) ? juez.getFechaRegistro() : LocalDate.now();
        gestorArchivos.subirArchivo(archivo, "00", tipo, MODULO_JPE, fecha, idJuez);
    }

    @Override
    @Transactional
    public void eliminarArchivo(String nombreArchivo) throws Exception {
        gestorArchivos.eliminarPorNombre(nombreArchivo);
    }

    @Override
    public RecursoArchivo descargarResolucion(String id) throws Exception {
        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(id);
        Archivo res = archivos.stream()
                .filter(a -> "RESOLUCION_JPE".equals(a.getTipo()))
                .findFirst()
                .orElseThrow(() -> new Exception("Este registro no tiene resolución adjunta."));

        // 2. Descargamos por nombre físico
        return gestorArchivos.descargarPorNombre(res.getNombre());
    }

    @Override
    public boolean existeDniEnColegio(String dni, String colegioId) {
        return persistencePort.existeDniEnColegio(dni, colegioId);
    }
    @Override
    public RecursoArchivo descargarArchivoPorNombre(String nombreArchivo) throws Exception {
        return gestorArchivos.descargarPorNombre(nombreArchivo);
    }
    private String generarIdCorto() {
        long timestamp = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(100, 999);
        return "J" + timestamp + random;
    }
}