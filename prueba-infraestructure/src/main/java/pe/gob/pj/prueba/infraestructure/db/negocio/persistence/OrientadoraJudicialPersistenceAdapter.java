package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.negocio.OrientadoraJudicial;
import pe.gob.pj.prueba.domain.port.persistence.negocio.OrientadoraJudicialPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovOrientadoraJudicialEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovOrientadoraJudicialRepository;
import pe.gob.pj.prueba.infraestructure.mappers.OrientadoraJudicialMapper;

@Component
@RequiredArgsConstructor
public class OrientadoraJudicialPersistenceAdapter implements OrientadoraJudicialPersistencePort {

    private final MovOrientadoraJudicialRepository repository;
    private final OrientadoraJudicialMapper mapper;

    @Override
    @Transactional
    public OrientadoraJudicial guardar(OrientadoraJudicial dominio) throws Exception {
        MovOrientadoraJudicialEntity entity = mapper.toEntity(dominio);
        MovOrientadoraJudicialEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public String obtenerUltimoId() throws Exception {
        return repository.obtenerUltimoId();
    }

}