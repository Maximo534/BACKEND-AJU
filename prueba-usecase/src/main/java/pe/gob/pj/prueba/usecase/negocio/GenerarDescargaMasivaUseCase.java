package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionArchivosUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GenerarDescargaMasivaUseCasePort;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerarDescargaMasivaUseCase implements GenerarDescargaMasivaUseCasePort {

    private final GestionArchivosPersistencePort archivosPersistencePort;

    private final GestionArchivosUseCasePort gestorArchivos;

    @Override
    public RecursoArchivo generarZipMasivo(String modulo, String tipoArchivo, Integer anio, Integer mes) throws Exception {

        // 1. Validaciones de Negocio
        if (!"evidencias_fji".equalsIgnoreCase(modulo)) {
            throw new Exception("Módulo no soportado: " + modulo);
        }

        // 2. Obtener la lista de la BD
        List<Archivo> listaArchivos = archivosPersistencePort.listarParaDescargaMasiva(tipoArchivo, anio, mes);

        if (listaArchivos == null || listaArchivos.isEmpty()) {
            throw new Exception("No se encontraron archivos para los filtros seleccionados.");
        }

        String nombreZip = "Reporte_" + modulo + "_" + anio + "_" + mes + ".zip";

        return gestorArchivos.descargarListaComoZip(listaArchivos, nombreZip);
    }
}