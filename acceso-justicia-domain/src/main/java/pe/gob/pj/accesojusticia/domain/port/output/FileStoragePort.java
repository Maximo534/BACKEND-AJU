package pe.gob.pj.accesojusticia.domain.port.output;

import org.springframework.web.multipart.MultipartFile;

public interface FileStoragePort {
    String guardarArchivo(MultipartFile archivo, String subDirectorio) throws Exception;
}