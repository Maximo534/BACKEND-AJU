package pe.gob.pj.prueba.domain.port.usecase.negocio;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;

import java.time.LocalDate;
import java.util.List;

public interface GestionArchivosUseCasePort {

    void subirArchivo(MultipartFile file, String distritoId, String tipoArchivo, LocalDate fechaEvento, String idRegistro) throws Exception;

    RecursoArchivo descargarPorNombre(String nombreArchivo) throws Exception;

    void eliminarPorNombre(String nombreArchivo) throws Exception;

    RecursoArchivo descargarListaComoZip(List<Archivo> listaArchivos, String nombreZipSalida) throws Exception;
}