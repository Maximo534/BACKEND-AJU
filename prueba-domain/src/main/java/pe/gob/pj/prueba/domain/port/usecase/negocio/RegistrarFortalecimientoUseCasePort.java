package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.FortalecimientoCapacidades;
import java.util.List;

public interface RegistrarFortalecimientoUseCasePort {

    Pagina<FortalecimientoCapacidades> listar(String usuario, FortalecimientoCapacidades filtros, int pagina, int tamanio) throws Exception;

    FortalecimientoCapacidades registrar(FortalecimientoCapacidades dominio, String usuarioOperacion) throws Exception;

    FortalecimientoCapacidades registrarConEvidencias(
            FortalecimientoCapacidades dominio,
            MultipartFile anexo,
            List<MultipartFile> videos,
            List<MultipartFile> fotos,
            String usuarioOperacion
    ) throws Exception;

    FortalecimientoCapacidades actualizar(FortalecimientoCapacidades dominio, String usuarioOperacion) throws Exception;
    FortalecimientoCapacidades buscarPorId(String id) throws Exception;

    void eliminarArchivo(String nombreArchivo) throws Exception;
    void subirArchivoAdicional(String idEvento, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception;
    RecursoArchivo descargarAnexo(String idEvento, String usuario) throws Exception;
    byte[] generarFichaPdf(String idEvento) throws Exception;
}