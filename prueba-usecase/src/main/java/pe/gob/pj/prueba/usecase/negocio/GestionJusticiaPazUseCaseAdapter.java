package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.JpeCasoAtendido;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.JusticiaPazPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionArchivosUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionJusticiaPazUseCasePort;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GestionJusticiaPazUseCaseAdapter implements GestionJusticiaPazUseCasePort {

    private final JusticiaPazPersistencePort persistencePort;
    private final GenerarReportePort reportePort;
    private final GestionArchivosUseCasePort gestorArchivos;
    private final GestionArchivosPersistencePort archivosPersistencePort;
    private static final String MODULO_JPE_CASOS = "evidencias_jpe_casos";

    @Override
    public Pagina<JpeCasoAtendido> listar(String usuario, JpeCasoAtendido filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listar(usuario, filtros, pagina, tamanio);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JpeCasoAtendido registrar(JpeCasoAtendido dominio, MultipartFile acta, List<MultipartFile> fotos, String usuario) throws Exception {

        String ultimoId = persistencePort.obtenerUltimoId();
        long siguiente = 1;
        if (ultimoId != null) {
            try { siguiente = Long.parseLong(ultimoId.split("-")[0]) + 1; } catch (Exception e) { siguiente = 1; }
        }
        String anio = String.valueOf(LocalDate.now().getYear());
        String corte = (dominio.getDistritoJudicialId() != null) ? dominio.getDistritoJudicialId() : "00";

        dominio.setId(String.format("%06d-%s-%s-PE", siguiente, corte, anio));
        dominio.setUsuarioRegistro(usuario);

        JpeCasoAtendido registrado = persistencePort.guardar(dominio);

        LocalDate fecha = LocalDate.now();

        if (acta != null && !acta.isEmpty()) {
            gestorArchivos.subirArchivo(acta, corte, "ANEXO", MODULO_JPE_CASOS, fecha, registrado.getId());
        }

        if (fotos != null) {
            for (MultipartFile f : fotos) {
                if (!f.isEmpty()) {
                    gestorArchivos.subirArchivo(f, corte, "FOTO", MODULO_JPE_CASOS, fecha, registrado.getId());
                }
            }
        }

        return registrado;
    }

    @Override
    @Transactional
    public JpeCasoAtendido actualizar(JpeCasoAtendido dominio, String usuario) throws Exception {
        if (dominio.getId() == null) throw new Exception("ID obligatorio");
        dominio.setUsuarioRegistro(usuario);
        return persistencePort.actualizar(dominio);
    }

    @Override
    @Transactional
    public void agregarArchivo(String idCaso, MultipartFile archivo, String tipo, String usuario) throws Exception {
        JpeCasoAtendido caso = persistencePort.buscarPorId(idCaso);
        if (caso == null) throw new Exception("Caso no encontrado");

        String corte = (caso.getDistritoJudicialId() != null) ? caso.getDistritoJudicialId() : "00";
        LocalDate fecha = (caso.getFechaRegistro() != null) ? caso.getFechaRegistro() : LocalDate.now();
        gestorArchivos.subirArchivo(archivo, corte, tipo, MODULO_JPE_CASOS, fecha, idCaso);
    }

    @Override
    public RecursoArchivo descargarArchivoPorTipo(String idCaso, String tipoArchivo) throws Exception {
        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(idCaso);

        Archivo encontrado = archivos.stream()
                .filter(a -> a.getTipo().equalsIgnoreCase(tipoArchivo) ||
                        (tipoArchivo.equalsIgnoreCase("ACTA") && a.getTipo().equalsIgnoreCase("ANEXO")))
                .findFirst()
                .orElseThrow(() -> new Exception("Archivo no encontrado: " + tipoArchivo));

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
    public JpeCasoAtendido buscarPorId(String id) throws Exception {
        return persistencePort.buscarPorId(id);
    }

    @Override
    public byte[] generarFichaPdf(String id) throws Exception {
        return reportePort.generarFichaJpe(id);
    }

    @Override
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        return persistencePort.obtenerResumenGrafico();
    }
}