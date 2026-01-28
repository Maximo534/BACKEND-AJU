package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.LlapanchikpaqJusticia;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.LlapanchikpaqPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionArchivosUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionLlapanchikpaqUseCasePort;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestionLlapanchikpaqUseCaseAdapter implements GestionLlapanchikpaqUseCasePort {

    private final LlapanchikpaqPersistencePort persistencePort;
    private final GestionArchivosPersistencePort archivosPersistencePort;
    private final GenerarReportePort reportePort;
    private final GestionArchivosUseCasePort gestorArchivos;

    private static final String MODULO_LLJ = "evidencias_llj";

    @Override
    public Pagina<LlapanchikpaqJusticia> listar(String usuario, LlapanchikpaqJusticia filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listar(usuario, filtros, pagina, tamanio);
    }

    @Override
    public LlapanchikpaqJusticia buscarPorId(String id) throws Exception {
        if (id == null || id.isBlank()) throw new Exception("ID obligatorio");
        return persistencePort.buscarPorId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LlapanchikpaqJusticia registrar(LlapanchikpaqJusticia dominio, MultipartFile anexo, List<MultipartFile> fotos, String usuario) throws Exception {
        if (dominio.getTareas() != null) {
            for (var tarea : dominio.getTareas()) {
                if (tarea.getFechaInicio() == null) {
                    tarea.setFechaInicio(dominio.getFechaInicio());
                }
            }
        }
        // Generar ID
        String ultimoId = persistencePort.obtenerUltimoId();
        long siguiente = 1;
        if (ultimoId != null && !ultimoId.isBlank()) {
            try { siguiente = Long.parseLong(ultimoId.split("-")[0]) + 1; } catch (Exception e) { siguiente = 1; }
        }
        String anio = String.valueOf(LocalDate.now().getYear());
        String corte = (dominio.getDistritoJudicialId() != null) ? dominio.getDistritoJudicialId() : "00";
        dominio.setId(String.format("%06d-%s-%s-LL", siguiente, corte, anio));

        // BD
        dominio.setUsuarioRegistro(usuario);
        dominio.setFechaRegistro(LocalDate.now());
        dominio.setActivo("1");

        LlapanchikpaqJusticia registrado = persistencePort.guardar(dominio);

        LocalDate fecha = (registrado.getFechaInicio() != null) ? registrado.getFechaInicio() : LocalDate.now();

        if (anexo != null && !anexo.isEmpty()) {
            gestorArchivos.subirArchivo(anexo, corte, "ANEXO", MODULO_LLJ, fecha, registrado.getId());
        }

        if (fotos != null) {
            for (MultipartFile f : fotos) {
                if (!f.isEmpty()) {
                    gestorArchivos.subirArchivo(f, corte, "FOTO", MODULO_LLJ, fecha, registrado.getId());
                }
            }
        }
        return registrado;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LlapanchikpaqJusticia actualizar(LlapanchikpaqJusticia dominio, String usuario) throws Exception {
        log.info("Actualizando LLJ ID: {} por: {}", dominio.getId(), usuario);
        if (dominio.getTareas() != null) {
            for (var tarea : dominio.getTareas()) {
                if (tarea.getFechaInicio() == null) {
                    tarea.setFechaInicio(dominio.getFechaInicio());
                }
            }
        }
        dominio.setUsuarioRegistro(usuario);
        return persistencePort.actualizar(dominio);
    }

    @Override
    @Transactional
    public void agregarArchivo(String idEvento, MultipartFile archivo, String tipo, String usuario) throws Exception {
        log.info("Usuario [{}] agregando archivo [{}] al evento LLJ ID [{}]", usuario, tipo, idEvento);

        if (archivo == null || archivo.isEmpty()) throw new Exception("Archivo vacío");

        LlapanchikpaqJusticia evento = persistencePort.buscarPorId(idEvento);
        if (evento == null) throw new Exception("ID no válido");

        String corte = (evento.getDistritoJudicialId() != null) ? evento.getDistritoJudicialId() : "00";
        LocalDate fecha = (evento.getFechaInicio() != null) ? evento.getFechaInicio() : LocalDate.now();

        gestorArchivos.subirArchivo(archivo, corte, tipo, MODULO_LLJ, fecha, idEvento);
    }

    @Override
    public RecursoArchivo descargarArchivoPorTipo(String id, String tipoArchivo) throws Exception {
        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(id);

        Archivo encontrado = archivos.stream()
                .filter(a -> a.getTipo().toUpperCase().contains(tipoArchivo.toUpperCase()) ||
                        (tipoArchivo.toUpperCase().contains("ANEXO") && a.getTipo().equalsIgnoreCase("ANEXO")))
                .findFirst()
                .orElseThrow(() -> new Exception("No se encontró archivo " + tipoArchivo));

        return gestorArchivos.descargarPorNombre(encontrado.getNombre());
    }

    @Override
    public RecursoArchivo descargarArchivoPorNombre(String nombreArchivo) throws Exception {
        return gestorArchivos.descargarPorNombre(nombreArchivo);
    }

    @Override
    @Transactional
    public void eliminarArchivo(String nombreArchivo) throws Exception {
        gestorArchivos.eliminarPorNombre(nombreArchivo);
    }

    @Override
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        return persistencePort.obtenerResumenGrafico();
    }

    @Override
    public byte[] generarFichaPdf(String id) throws Exception {
        if(persistencePort.buscarPorId(id) == null) throw new Exception("Evento no existe");
        return reportePort.generarFichaLlj(id);
    }
}