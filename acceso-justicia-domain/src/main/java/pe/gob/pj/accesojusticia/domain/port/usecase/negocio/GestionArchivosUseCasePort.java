package pe.gob.pj.accesojusticia.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.accesojusticia.domain.model.Auditoria; // Clase padre de tus dominios
import pe.gob.pj.accesojusticia.domain.model.common.RecursoArchivo;
import pe.gob.pj.accesojusticia.domain.model.negocio.Archivo;
import java.time.LocalDate;
import java.util.List;

public interface GestionArchivosUseCasePort {

    /**
     * Sube un archivo al FTP y guarda la referencia en BD con auditoría.
     * @param datosAuditoria Objeto que contiene usuario, IP, MAC, PC (puede ser el objeto Itinerante, Usuario, etc.)
     */
    void subirArchivo(MultipartFile file, String distritoId, String tipo, String modulo,
                      LocalDate fecha, String codigoEnlace, Auditoria datosAuditoria) throws Exception;

    RecursoArchivo descargarPorId(Long idArchivo) throws Exception;

    void eliminarPorId(Long idArchivo, Auditoria datosAuditoria) throws Exception;

    RecursoArchivo descargarListaComoZip(List<Archivo> listaArchivos, String nombreZipSalida) throws Exception;
}