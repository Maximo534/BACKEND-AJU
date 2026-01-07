package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.negocio.Documento;
import pe.gob.pj.prueba.domain.port.persistence.negocio.DocumentoPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.DocumentoEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.DocumentoRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentoPersistenceAdapter implements DocumentoPersistencePort {

    private final DocumentoRepository repository;

    @Override
    public List<Documento> buscarPorTipoYActivo(String tipo, String activo) {
        return repository.findByTipoAndActivoOrderByPeriodoDesc(tipo, activo).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Documento guardar(Documento domain) throws Exception {
        try {
            DocumentoEntity entity = new DocumentoEntity();
            // Si el ID viene nulo, generamos uno nuevo. Si viene, es actualización.
            entity.setId(domain.getId() != null ? domain.getId() : UUID.randomUUID().toString());

            entity.setNombre(domain.getNombre());
            entity.setTipo(domain.getTipo());
            entity.setFormato(domain.getFormato());
            entity.setRuta(domain.getRutaArchivo());
            entity.setPeriodo(domain.getPeriodo());
            entity.setActivo(domain.getActivo() != null ? domain.getActivo() : "1");
            entity.setCategoriaId(domain.getCategoriaId()); // Mapeo exacto

            DocumentoEntity saved = repository.save(entity);
            return mapToDomain(saved);
        } catch (Exception e) {
            log.error("Error BD al guardar documento", e);
            throw new Exception("Error al registrar en base de datos.");
        }
    }

    private Documento mapToDomain(DocumentoEntity entity) {
        return Documento.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .tipo(entity.getTipo())
                .formato(entity.getFormato())
                .rutaArchivo(entity.getRuta())
                .periodo(entity.getPeriodo())
                .activo(entity.getActivo())
                .categoriaId(entity.getCategoriaId())
                .build();
    }
}