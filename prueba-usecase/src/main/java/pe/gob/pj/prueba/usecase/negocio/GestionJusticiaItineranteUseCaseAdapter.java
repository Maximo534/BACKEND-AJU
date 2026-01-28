package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.JusticiaItinerantePersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionArchivosUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionJusticiaItineranteUseCasePort;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestionJusticiaItineranteUseCaseAdapter implements GestionJusticiaItineranteUseCasePort {

    private final JusticiaItinerantePersistencePort persistencePort;
    private final GenerarReportePort reportePort;
    private final GestionArchivosUseCasePort gestorArchivos;
    private final GestionArchivosPersistencePort archivosPersistencePort;

    private static final String MODULO_JI = "evidencias_fji";

    @Override
    @Transactional(readOnly = true)
    public Pagina<JusticiaItinerante> listar(String usuario, JusticiaItinerante filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listar(usuario, filtros, pagina, tamanio);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JusticiaItinerante registrar(JusticiaItinerante dominio, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos, String usuario) throws Exception {
        // Validaciones
        if (dominio.getFechaInicio() == null) throw new Exception("La fecha de inicio es obligatoria.");
        if (dominio.getPublicoObjetivoDetalle() == null) dominio.setPublicoObjetivoDetalle("NINGUNO");

        // Lógica de fechas en tareas
        if (dominio.getTareasRealizadas() != null) {
            for (JusticiaItinerante.DetalleTarea tarea : dominio.getTareasRealizadas()) {
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
        dominio.setId(String.format("%06d-%s-%s-JI", siguiente, corte, anio));

        // Auditoría y Guardado
        dominio.setUsuarioRegistro(usuario);
        dominio.setFechaRegistro(LocalDate.now());
        dominio.setActivo("1");

        JusticiaItinerante registrado = persistencePort.guardar(dominio);

        if (anexo != null && !anexo.isEmpty()) {
            gestorArchivos.subirArchivo(anexo, registrado.getDistritoJudicialId(), "ANEXO", MODULO_JI, registrado.getFechaInicio(), registrado.getId());
        }

        if (fotos != null) {
            for (MultipartFile f : fotos) {
                if (!f.isEmpty())
                    gestorArchivos.subirArchivo(f, registrado.getDistritoJudicialId(), "FOTO", MODULO_JI, registrado.getFechaInicio(), registrado.getId());
            }
        }

        if (videos != null) {
            for (MultipartFile v : videos) {
                if (!v.isEmpty())
                    gestorArchivos.subirArchivo(v, registrado.getDistritoJudicialId(), "VIDEO", MODULO_JI, registrado.getFechaInicio(), registrado.getId());
            }
        }

        return registrado;
    }

    @Override
    @Transactional(readOnly = true)
    public JusticiaItinerante buscarPorId(String id) throws Exception {
        if (id == null || id.isBlank()) throw new Exception("El ID es obligatorio");
        return persistencePort.obtenerPorId(id);
    }

    @Override
    @Transactional
    public JusticiaItinerante actualizar(JusticiaItinerante dominio, String usuarioOperacion) throws Exception {
        if (dominio.getFechaInicio() == null) throw new Exception("La fecha de inicio es obligatoria.");
        if (dominio.getFechaFin() != null && dominio.getFechaFin().isBefore(dominio.getFechaInicio())) {
            throw new Exception("La fecha fin no puede ser anterior a la fecha de inicio.");
        }

        if (dominio.getTareasRealizadas() != null) {
            for (JusticiaItinerante.DetalleTarea tarea : dominio.getTareasRealizadas()) {
                if (tarea.getFechaInicio() == null) {
                    tarea.setFechaInicio(dominio.getFechaInicio());
                }
            }
        }
        dominio.setUsuarioRegistro(usuarioOperacion);
        return persistencePort.actualizar(dominio);
    }

    @Override
    @Transactional
    public void agregarArchivo(String idEvento, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception {
        log.info("Usuario [{}] agregando archivo [{}] al evento ID [{}]", usuarioOperacion, tipoArchivo, idEvento);

        if (archivo == null || archivo.isEmpty()) throw new Exception("El archivo no puede estar vacío.");

        JusticiaItinerante evento = persistencePort.obtenerPorId(idEvento);
        if (evento == null) throw new Exception("No se encontró el evento con ID: " + idEvento);

        gestorArchivos.subirArchivo(archivo, evento.getDistritoJudicialId(), tipoArchivo, MODULO_JI, evento.getFechaInicio(), idEvento);
    }

    @Override
    @Transactional
    public void eliminarArchivo(String nombreArchivo) throws Exception {
        gestorArchivos.eliminarPorNombre(nombreArchivo);
    }

    @Override
    public RecursoArchivo descargarAnexo(String idEvento, String usuario) throws Exception {

        if (idEvento == null || idEvento.isEmpty()) {
            throw new Exception("El ID del evento es obligatorio");
        }

        // 1. Buscar cuál es el archivo PDF en base de datos
        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(idEvento);
        Archivo archivoAnexo = archivos.stream()
                .filter(a -> "ANEXO".equalsIgnoreCase(a.getTipo()))
                .findFirst()
                .orElseThrow(() -> new Exception("No existe anexo para este evento."));

        // 2. Descargar usando el servicio común
        return gestorArchivos.descargarPorNombre(archivoAnexo.getNombre());
    }

    @Override
    public RecursoArchivo descargarArchivoPorNombre(String nombreArchivo) throws Exception {
        return gestorArchivos.descargarPorNombre(nombreArchivo);
    }

    @Override
    public byte[] generarFichaPdf(String idEvento) throws Exception {
        if(persistencePort.obtenerPorId(idEvento) == null) throw new Exception("Evento no existe");
        return reportePort.generarFichaItinerante(idEvento);
    }
}