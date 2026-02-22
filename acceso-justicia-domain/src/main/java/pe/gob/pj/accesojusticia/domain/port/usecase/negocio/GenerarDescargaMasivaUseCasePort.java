package pe.gob.pj.accesojusticia.domain.port.usecase.negocio;

import pe.gob.pj.accesojusticia.domain.model.common.RecursoArchivo;

public interface GenerarDescargaMasivaUseCasePort {

    RecursoArchivo generarZipMasivo(String modulo, String tipoArchivo, Integer anio, Integer mes) throws Exception;

}