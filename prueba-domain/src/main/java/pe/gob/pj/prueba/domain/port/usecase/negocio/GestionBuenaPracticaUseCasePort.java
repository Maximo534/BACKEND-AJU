package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina; // ✅ Importante para la paginación
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.BuenaPractica;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;

import java.util.List;

public interface GestionBuenaPracticaUseCasePort {
    Pagina<BuenaPractica> listar(String usuario, BuenaPractica filtros, int pagina, int tamanio) throws Exception;

    BuenaPractica registrar(BuenaPractica bp, MultipartFile anexo, MultipartFile ppt, List<MultipartFile> fotos, MultipartFile video, String usuario) throws Exception;
    BuenaPractica buscarPorId(String id) throws Exception;

    BuenaPractica actualizar(BuenaPractica bp, String usuario) throws Exception;

    void eliminarArchivo(String nombreArchivo) throws Exception;

    void agregarArchivo(String idEvento, MultipartFile archivo, String tipoArchivo, String usuario) throws Exception;
    RecursoArchivo descargarArchivoPorTipo(String idEvento, String tipoArchivo) throws Exception;
    byte[] generarFichaPdf(String id) throws Exception;
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception;
    RecursoArchivo descargarArchivoPorNombre(String nombreArchivo) throws Exception;

}