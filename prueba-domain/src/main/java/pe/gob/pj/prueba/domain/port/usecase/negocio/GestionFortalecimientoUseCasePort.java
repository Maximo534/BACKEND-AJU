package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.FortalecimientoCapacidades;
import java.util.List;

public interface GestionFortalecimientoUseCasePort {

    Pagina<FortalecimientoCapacidades> listar(String usuario, FortalecimientoCapacidades filtros, int pagina, int tamanio) throws Exception;

    FortalecimientoCapacidades registrar(FortalecimientoCapacidades dominio, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos, String usuario) throws Exception;

    FortalecimientoCapacidades actualizar(FortalecimientoCapacidades dominio, String usuarioOperacion) throws Exception;
    FortalecimientoCapacidades buscarPorId(String id) throws Exception;

    void eliminarArchivo(String nombreArchivo) throws Exception;
    void agregarArchivo(String idEvento, MultipartFile archivo, String tipo, String usuario) throws Exception;
    RecursoArchivo descargarAnexo(String idEvento, String usuario) throws Exception;
    byte[] generarFichaPdf(String idEvento) throws Exception;
    RecursoArchivo descargarArchivoPorNombre(String nombreArchivo) throws Exception;
}