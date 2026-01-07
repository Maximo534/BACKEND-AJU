package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivosEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovArchivosRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GestionArchivosPersistenceAdapter implements GestionArchivosPersistencePort {

    private final MovArchivosRepository repository;

    @Override
    @Transactional
    public void guardarReferenciaArchivo(Archivo archivoDomain) throws Exception {
        try {
            MovArchivosEntity entidad = new MovArchivosEntity();
            entidad.setNombre(archivoDomain.getNombre());
            entidad.setTipo(archivoDomain.getTipo());
            entidad.setRuta(archivoDomain.getRuta());
            entidad.setNumeroIdentificacion(archivoDomain.getNumeroIdentificacion());

            repository.save(entidad);
        } catch (Exception e) {
            log.error("Error al guardar referencia de archivo en BD", e);
            throw new Exception("Error al guardar metadatos del archivo: " + e.getMessage());
        }
    }

    @Override
    public Archivo buscarPorNombre(String nombre) throws Exception {
        MovArchivosEntity entidad = repository.findById(nombre)
                .orElseThrow(() -> new Exception("El archivo no existe en la base de datos: " + nombre));

        return Archivo.builder()
                .nombre(entidad.getNombre())
                .tipo(entidad.getTipo())
                .ruta(entidad.getRuta())
                .numeroIdentificacion(entidad.getNumeroIdentificacion())
                .build();
    }

    @Override
    @Transactional
    public void eliminarReferenciaArchivo(String nombre) throws Exception {
        repository.deleteById(nombre);
    }

    // ... imports ...

    @Override
    public List<Archivo> listarArchivosPorEvento(String idEvento) throws Exception {
        List<MovArchivosEntity> entities = repository.findByNumeroIdentificacion(idEvento);

        return entities.stream()
                .map(e -> Archivo.builder()
                        .nombre(e.getNombre())
                        .tipo(e.getTipo())
                        .ruta(e.getRuta())
                        .numeroIdentificacion(e.getNumeroIdentificacion())
                        .build())
                .collect(Collectors.toList());
    }
}