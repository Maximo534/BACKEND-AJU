package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.exceptions.negocio.MovimientoNoEncontradoException;
import pe.gob.pj.prueba.domain.model.negocio.Documento;
import pe.gob.pj.prueba.domain.port.persistence.negocio.DocumentoPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.DocumentoEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.DocumentoRepository;
import pe.gob.pj.prueba.infraestructure.mappers.DocumentoMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DocumentoPersistenceAdapter implements DocumentoPersistencePort {

    DocumentoRepository repository;
    DocumentoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<Documento> listarPorTipo(String cuo, String tipo) {
        List<DocumentoEntity> entities = repository.listarActivosPorTipoConCategoria(tipo);

        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Documento guardar(String cuo, Documento dominio) {
        log.info("[{}] Guardando Documento. Nombre: {}", cuo, dominio.getNombre());

        DocumentoEntity entity = mapper.toEntity(dominio);
        DocumentoEntity saved = repository.save(entity);

        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public Documento actualizar(String cuo, Documento dominio) {
        log.info("[{}] Actualizando Documento ID: {}", cuo, dominio.getId());

        DocumentoEntity entityDb = repository.findById(dominio.getId())
                .orElseThrow(() -> new MovimientoNoEncontradoException("No se encontró el documento con ID: " + dominio.getId()));

        mapper.updateEntityFromDomain(dominio, entityDb);

        DocumentoEntity saved = repository.save(entityDb);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Documento buscarPorId(String cuo, Long id) {
        return repository.findById(id)
                .map(mapper::toDomain)
                .orElse(null);
    }
}