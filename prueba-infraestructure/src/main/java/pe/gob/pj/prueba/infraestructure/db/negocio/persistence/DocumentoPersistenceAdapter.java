package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.Documento;
import pe.gob.pj.prueba.domain.port.persistence.negocio.DocumentoPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.DocumentoEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.DocumentoRepository;
import pe.gob.pj.prueba.infraestructure.mappers.DocumentoMapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentoPersistenceAdapter implements DocumentoPersistencePort {

    private final DocumentoRepository repository;
    private final DocumentoMapper mapper;


    @Override
    @Transactional(readOnly = true)
    public List<Documento> listarPorTipo(String tipo) {

        List<DocumentoEntity> entities = repository.findByTipoAndActivoOrderByPeriodoDesc(tipo, "1");

        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
//    @Override
//    @Transactional(readOnly = true)
//    public Pagina<Documento> listarConFiltros(Documento filtros, int pagina, int tamanio) {
//        Pageable pageable = PageRequest.of(pagina - 1, tamanio);
//
//        String tipo = (filtros != null) ? filtros.getTipo() : null;
//        Integer periodo = (filtros != null) ? filtros.getPeriodo() : null;
//        Integer catId = (filtros != null) ? filtros.getCategoriaId() : null;
//        String nombre = (filtros != null) ? filtros.getNombre() : null;
//
//        Page<DocumentoEntity> result = repository.listar(tipo, periodo, catId, nombre, pageable);
//
//        List<Documento> contenido = result.getContent().stream()
//                .map(mapper::toDomain)
//                .collect(Collectors.toList());
//
//        return Pagina.<Documento>builder()
//                .contenido(contenido)
//                .totalRegistros(result.getTotalElements())
//                .totalPaginas(result.getTotalPages())
//                .paginaActual(pagina)
//                .tamanioPagina(tamanio)
//                .build();
//    }

    @Override
    @Transactional
    public Documento guardar(Documento domain) throws Exception {
        DocumentoEntity entity;
        if (domain.getId() != null) {
            entity = repository.findById(domain.getId())
                    .orElseThrow(() -> new Exception("ID no encontrado: " + domain.getId()));
            mapper.updateEntityFromDomain(domain, entity);
        } else {
            entity = mapper.toEntity(domain);
            entity.setId(UUID.randomUUID().toString());
        }

        if (entity.getActivo() == null) entity.setActivo("1");

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Documento buscarPorId(String id) throws Exception {
        return repository.findById(id)
                .map(mapper::toDomain)
                .orElse(null);
    }
}