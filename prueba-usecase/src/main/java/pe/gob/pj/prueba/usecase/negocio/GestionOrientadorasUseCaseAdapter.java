package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.OrientadoraJudicial;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.OrientadoraJudicialPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionArchivosUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionOrientadorasUseCasePort;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestionOrientadorasUseCaseAdapter implements GestionOrientadorasUseCasePort {

    private final OrientadoraJudicialPersistencePort persistencePort;
    private final GenerarReportePort reportePort;
    private final GestionArchivosUseCasePort gestorArchivos;
    private final GestionArchivosPersistencePort archivosPersistencePort;

    private static final String MODULO_OJ = "evidencias_oj";

    @Override
    public Pagina<OrientadoraJudicial> listar(String usuario, OrientadoraJudicial filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listar(usuario, filtros, pagina, tamanio);
    }

    @Override
    public OrientadoraJudicial buscarPorId(String id) throws Exception {
        OrientadoraJudicial encontrado = persistencePort.buscarPorId(id);
        if (encontrado == null) throw new Exception("Registro no encontrado");
        return encontrado;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrientadoraJudicial registrarAtencion(OrientadoraJudicial oj, MultipartFile anexo, List<MultipartFile> fotos, String usuario) throws Exception {

        //Generar ID
        String ultimoId = persistencePort.obtenerUltimoId();
        long siguiente = 1;
        if (ultimoId != null && !ultimoId.isBlank()) {
            try { siguiente = Long.parseLong(ultimoId.substring(0, 6)) + 1; } catch (Exception e) { siguiente = 1; }
        }
        String numeroStr = String.format("%06d", siguiente);
        String anio = String.valueOf(LocalDate.now().getYear());
        String corte = oj.getDistritoJudicialId() != null ? oj.getDistritoJudicialId() : "00";
        String idGenerado = String.format("%s-%s-%s-OJ", numeroStr, corte, anio);

        oj.setId(idGenerado);
        oj.setUsuarioRegistro(usuario);
        if(oj.getFechaAtencion() == null) oj.setFechaAtencion(LocalDate.now());

        OrientadoraJudicial registrado = persistencePort.guardar(oj);

        LocalDate fecha = (registrado.getFechaAtencion() != null) ? registrado.getFechaAtencion() : LocalDate.now();

        if (anexo != null && !anexo.isEmpty()) {
            gestorArchivos.subirArchivo(anexo, corte, "ANEXO", MODULO_OJ, fecha, registrado.getId());
        }

        if (fotos != null && !fotos.isEmpty()) {
            for (MultipartFile foto : fotos) {
                if (foto != null && !foto.isEmpty()) {
                    gestorArchivos.subirArchivo(foto, corte, "FOTO", MODULO_OJ, fecha, registrado.getId());
                }
            }
        }

        return registrado;
    }

    @Override
    @Transactional
    public OrientadoraJudicial actualizar(OrientadoraJudicial dominio, String usuario) throws Exception {
        if(dominio.getId() == null) throw new Exception("ID obligatorio");
        dominio.setUsuarioRegistro(usuario);
        return persistencePort.actualizar(dominio);
    }

    @Override
    @Transactional
    public void agregarArchivo(String idCaso, MultipartFile archivo, String tipo, String usuario) throws Exception {
        OrientadoraJudicial oj = persistencePort.buscarPorId(idCaso);
        if(oj == null) throw new Exception("No existe registro");

        String corte = (oj.getDistritoJudicialId() != null) ? oj.getDistritoJudicialId() : "00";
        LocalDate fecha = (oj.getFechaAtencion() != null) ? oj.getFechaAtencion() : LocalDate.now();
        gestorArchivos.subirArchivo(archivo, corte, tipo, MODULO_OJ, fecha, idCaso);
    }

    @Override
    @Transactional
    public void eliminarArchivo(String nombreArchivo) throws Exception {
        gestorArchivos.eliminarPorNombre(nombreArchivo);
    }

    @Override
    public RecursoArchivo descargarAnexo(String id, String tipoArchivo) throws Exception {
        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(id);

        Archivo encontrado = archivos.stream()
                .filter(a -> a.getTipo().equalsIgnoreCase(tipoArchivo) ||
                        (tipoArchivo.toUpperCase().contains("ANEXO") && a.getTipo().equalsIgnoreCase("ANEXO")))
                .findFirst()
                .orElseThrow(() -> new Exception("Archivo no encontrado: " + tipoArchivo));

        return gestorArchivos.descargarPorNombre(encontrado.getNombre());
    }

    @Override
    public RecursoArchivo descargarArchivoPorNombre(String nombreArchivo) throws Exception {
        return gestorArchivos.descargarPorNombre(nombreArchivo);
    }

    @Override
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        return persistencePort.obtenerResumenGrafico();
    }

    @Override
    public byte[] generarFichaPdf(String id) throws Exception {
        if(persistencePort.buscarPorId(id) == null) throw new Exception("Evento no existe");
        return reportePort.generarFichaOJ(id);
    }
}