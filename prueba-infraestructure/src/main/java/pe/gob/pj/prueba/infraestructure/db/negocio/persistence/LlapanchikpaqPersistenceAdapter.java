package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.gob.pj.prueba.domain.model.negocio.LlapanchikpaqJusticia;
import pe.gob.pj.prueba.domain.port.persistence.negocio.LlapanchikpaqPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovLlapanchikpaqJusticia;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovLlapanchikpaqJusticiaRepository;
import pe.gob.pj.prueba.infraestructure.mappers.LlapanchikpaqMapper;

@Component
@RequiredArgsConstructor
public class LlapanchikpaqPersistenceAdapter implements LlapanchikpaqPersistencePort {

    private final MovLlapanchikpaqJusticiaRepository repository;
    private final LlapanchikpaqMapper mapper;

    @Override
    public LlapanchikpaqJusticia guardar(LlapanchikpaqJusticia dominio) throws Exception {
        // 1. Convertir Dominio -> Entidad (Aquí el 'entity' ya tiene el ID porque viene del dominio)
        MovLlapanchikpaqJusticia entity = mapper.toEntity(dominio);

        // 2. CORRECCIÓN CLAVE: Asignar manualmente el ID padre a todos los hijos
        // Esto garantiza que no sean NULL al momento del INSERT
        String idPadre = entity.getId();

        if (entity.getBeneficiadas() != null) {
            entity.getBeneficiadas().forEach(hijo -> hijo.setLljId(idPadre));
        }
        if (entity.getAtendidas() != null) {
            entity.getAtendidas().forEach(hijo -> hijo.setLljId(idPadre));
        }
        if (entity.getCasos() != null) {
            entity.getCasos().forEach(hijo -> hijo.setLljId(idPadre));
        }
        if (entity.getTareas() != null) {
            entity.getTareas().forEach(hijo -> hijo.setLljId(idPadre));
        }

        // 3. Guardar en cascada
        MovLlapanchikpaqJusticia saved = repository.save(entity);

        // Retornar dominio
        dominio.setId(saved.getId());
        return dominio;
    }

    @Override
    public String obtenerUltimoId() throws Exception {
        return repository.obtenerUltimoId();
    }
}