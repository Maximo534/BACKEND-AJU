package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.PromocionCultura;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.PromocionCulturaPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionArchivosUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionPromocionUseCasePort;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestionPromocionUseCaseAdapter implements GestionPromocionUseCasePort {

    private final PromocionCulturaPersistencePort persistencePort;
    private final GenerarReportePort reportePort;
    private final GestionArchivosUseCasePort gestorArchivos;
    private final GestionArchivosPersistencePort archivosPersistencePort;

    @Override
    public Pagina<PromocionCultura> listar(String usuario, PromocionCultura filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listar(usuario, filtros, pagina, tamanio);
    }

    @Override
    public PromocionCultura buscarPorId(String id) throws Exception {
        PromocionCultura encontrado = persistencePort.obtenerPorId(id);
        if (encontrado == null) throw new Exception("Evento no encontrado.");
        return encontrado;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromocionCultura registrar(PromocionCultura dominio, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos, String usuario) throws Exception {

        if (dominio.getTareasRealizadas() != null) {
            for (PromocionCultura.DetalleTarea tarea : dominio.getTareasRealizadas()) {
                if (tarea.getFechaInicio() == null) {
                    tarea.setFechaInicio(dominio.getFechaInicio());
                }
            }
        }

        String ultimoId = persistencePort.obtenerUltimoId();
        long siguiente = 1;
        if (ultimoId != null) {
            try { siguiente = Long.parseLong(ultimoId.split("-")[0]) + 1; } catch (Exception e) { siguiente = 1; }
        }
        String anio = String.valueOf(LocalDate.now().getYear());
        String idGenerado = String.format("%06d-%s-%s-CJ", siguiente, dominio.getDistritoJudicialId(), anio);

        dominio.setId(idGenerado);
        dominio.setUsuarioRegistro(usuario);
        dominio.setFechaRegistro(LocalDate.now());
        dominio.setActivo("1");

        PromocionCultura registrado = persistencePort.guardar(dominio);

        if (anexo != null && !anexo.isEmpty()) {
            gestorArchivos.subirArchivo(anexo, registrado.getDistritoJudicialId(), "ANEXO", registrado.getFechaInicio(), registrado.getId());
        }

        if (fotos != null) {
            for (MultipartFile f : fotos) {
                if(!f.isEmpty())
                    gestorArchivos.subirArchivo(f, registrado.getDistritoJudicialId(), "FOTO", registrado.getFechaInicio(), registrado.getId());
            }
        }

        if (videos != null) {
            for (MultipartFile v : videos) {
                if(!v.isEmpty())
                    gestorArchivos.subirArchivo(v, registrado.getDistritoJudicialId(), "VIDEO", registrado.getFechaInicio(), registrado.getId());
            }
        }

        return registrado;
    }

    @Override
    @Transactional
    public PromocionCultura actualizar(PromocionCultura dominio, String usuario) throws Exception {
        if(dominio.getId() == null) throw new Exception("ID obligatorio");

        if (dominio.getTareasRealizadas() != null) {
            for (PromocionCultura.DetalleTarea tarea : dominio.getTareasRealizadas()) {
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
        PromocionCultura evento = persistencePort.obtenerPorId(idEvento);
        if (evento == null) throw new Exception("Evento no existe");

        gestorArchivos.subirArchivo(archivo, evento.getDistritoJudicialId(), tipo, evento.getFechaInicio(), idEvento);
    }

    @Override
    public void eliminarArchivo(String nombreArchivo) throws Exception {
        gestorArchivos.eliminarPorNombre(nombreArchivo);
    }

    @Override
    public RecursoArchivo descargarAnexo(String idEvento, String usuario) throws Exception {
        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(idEvento);
        Archivo encontrado = archivos.stream()
                .filter(a -> a.getTipo().toUpperCase().contains("ANEXO"))
                .findFirst()
                .orElseThrow(() -> new Exception("Anexo no encontrado para este evento"));

        return gestorArchivos.descargarPorNombre(encontrado.getNombre());
    }

    @Override
    public RecursoArchivo descargarArchivoPorNombre(String nombreArchivo) throws Exception {
        return gestorArchivos.descargarPorNombre(nombreArchivo);
    }

    @Override
    public byte[] generarFichaPdf(String id) throws Exception {
        if(persistencePort.obtenerPorId(id) == null) throw new Exception("Evento no existe");
        return reportePort.generarFichaPromocion(id);
    }
}