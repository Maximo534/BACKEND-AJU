package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.BuenaPractica;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.BuenaPracticaPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionArchivosUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionBuenaPracticaUseCasePort;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestionBuenaPracticaUseCaseAdapter implements GestionBuenaPracticaUseCasePort {

    private final BuenaPracticaPersistencePort persistencePort;
    private final GenerarReportePort generarReportePort;
    private final GestionArchivosUseCasePort gestorArchivos;
    private final GestionArchivosPersistencePort archivosPersistencePort;
    private static final String MODULO_BP = "evidencias_bp";

    @Override
    public Pagina<BuenaPractica> listar(String usuario, BuenaPractica filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listar(usuario, filtros, pagina, tamanio);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BuenaPractica registrar(BuenaPractica dominio, MultipartFile anexo, MultipartFile ppt, List<MultipartFile> fotos, MultipartFile video, String usuario) throws Exception {

        String ultimoId = persistencePort.obtenerUltimoId();
        long siguiente = 1;
        if (ultimoId != null) {
            try { siguiente = Long.parseLong(ultimoId.split("-")[0]) + 1; } catch (Exception e) { siguiente = 1; }
        }
        String anio = String.valueOf(LocalDate.now().getYear());
        String corte = (dominio.getDistritoJudicialId() != null) ? dominio.getDistritoJudicialId() : "00";
        dominio.setId(String.format("%06d-%s-%s-BP", siguiente, corte, anio));

        dominio.setUsuarioRegistro(usuario);
        BuenaPractica registrado = persistencePort.guardar(dominio);
        if (anexo != null && !anexo.isEmpty()) {
            gestorArchivos.subirArchivo(anexo, registrado.getDistritoJudicialId(), "ANEXO", MODULO_BP, LocalDate.now(), registrado.getId());
        }

        if (ppt != null && !ppt.isEmpty()) {
            gestorArchivos.subirArchivo(ppt, registrado.getDistritoJudicialId(), "PPT", MODULO_BP, LocalDate.now(), registrado.getId());
        }

        if (video != null && !video.isEmpty()) {
            gestorArchivos.subirArchivo(video, registrado.getDistritoJudicialId(), "VIDEO", MODULO_BP, LocalDate.now(), registrado.getId());
        }

        if (fotos != null) {
            for (MultipartFile f : fotos) {
                if (!f.isEmpty()) {
                    gestorArchivos.subirArchivo(f, registrado.getDistritoJudicialId(), "FOTO", MODULO_BP, LocalDate.now(), registrado.getId());
                }
            }
        }

        return registrado;
    }

    @Override
    @Transactional(readOnly = true)
    public BuenaPractica buscarPorId(String id) throws Exception {
        return persistencePort.buscarPorId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BuenaPractica actualizar(BuenaPractica dominio, String usuario) throws Exception {
        if (dominio.getId() == null) throw new Exception("ID obligatorio");
        dominio.setUsuarioRegistro(usuario);
        return persistencePort.actualizar(dominio);
    }

    @Override
    @Transactional
    public void agregarArchivo(String idEvento, MultipartFile archivo, String tipoArchivo, String usuario) throws Exception {
        log.info("Usuario [{}] agregando archivo [{}] a la BP ID [{}]", usuario, tipoArchivo, idEvento);

        if (archivo == null || archivo.isEmpty()) throw new Exception("Archivo vacío");

        BuenaPractica bp = persistencePort.buscarPorId(idEvento);
        if (bp == null) throw new Exception("ID no válido");
        gestorArchivos.subirArchivo(archivo, bp.getDistritoJudicialId(), tipoArchivo, MODULO_BP, LocalDate.now(), idEvento);
    }

    @Override
    @Transactional
    public void eliminarArchivo(String nombreArchivo) throws Exception {
        gestorArchivos.eliminarPorNombre(nombreArchivo);
    }

    @Override
    public RecursoArchivo descargarArchivoPorTipo(String idEvento, String tipoArchivo) throws Exception {
        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(idEvento);

        Archivo encontrado = archivos.stream()
                .filter(a -> a.getTipo().toUpperCase().contains(tipoArchivo.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new Exception("No se encontró archivo de tipo " + tipoArchivo));

        return gestorArchivos.descargarPorNombre(encontrado.getNombre());
    }

    @Override
    public RecursoArchivo descargarArchivoPorNombre(String nombreArchivo) throws Exception {
        return gestorArchivos.descargarPorNombre(nombreArchivo);
    }

    @Override
    public byte[] generarFichaPdf(String id) throws Exception {
        return generarReportePort.generarFichaBuenaPractica(id);
    }

    @Override
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        return persistencePort.obtenerResumenGrafico();
    }
}