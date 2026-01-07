package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante;
import java.util.List;

public interface RegistrarJusticiaItineranteUseCasePort {

    Pagina<JusticiaItinerante> listarItinerante(String usuario, JusticiaItinerante filtros, int pagina, int tamanio) throws Exception;
    /**
     * Orquesta el proceso de registro SOLO de datos (JSON).
     * @param justiciaItinerante Datos del formulario.
     * @param usuarioOperacion Usuario que realiza la acción.
     * @return El objeto registrado con su ID generado.
     */
    JusticiaItinerante registrar(JusticiaItinerante justiciaItinerante, String usuarioOperacion) throws Exception;

    /**
     * Orquesta el proceso UNIFICADO: Datos + Archivos (Transactional).
     * Si falla la subida de archivos o el registro de datos, se hace rollback de todo.
     * * @param dominio Datos del negocio mapeados desde el Request.
     * @param anexo Archivo PDF del anexo (puede ser null).
     * @param videos Lista de archivos de video (puede ser null).
     * @param fotos Lista de archivos de fotos (puede ser null).
     * @param usuarioOperacion Usuario de auditoría.
     * @return El objeto registrado completo.
     */
    JusticiaItinerante registrarConEvidencias(
            JusticiaItinerante dominio,
            MultipartFile anexo,
            List<MultipartFile> videos,
            List<MultipartFile> fotos,
            String usuarioOperacion
    ) throws Exception;

    JusticiaItinerante actualizar(JusticiaItinerante fji, String usuarioOperacion) throws Exception;
    JusticiaItinerante buscarPorId(String id) throws Exception;
    void eliminarArchivo(String nombreArchivo) throws Exception;
    void subirArchivoAdicional(String idEvento, MultipartFile archivo, String tipoArchivo, String usuarioOperacion) throws Exception;
    RecursoArchivo descargarAnexo(String idEvento, String usuario) throws Exception;
    byte[] generarFichaPdf(String idEvento) throws Exception;
}