package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.exceptions.negocio.MovimientoNoEncontradoException;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.FortalecimientoCapacidades;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarFortalecimientoQuery;
import pe.gob.pj.prueba.domain.port.persistence.negocio.FortalecimientoPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivoEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovEventoFcEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovArchivosRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovEventoFcRepository;
import pe.gob.pj.prueba.infraestructure.mappers.FortalecimientoMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FortalecimientoPersistenceAdapter implements FortalecimientoPersistencePort {

    MovEventoFcRepository repository;
    MovArchivosRepository repoArchivos;
    FortalecimientoMapper mapper;

    @Override
    public Pagina<FortalecimientoCapacidades> listar(String cuo, ListarFortalecimientoQuery query, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio);

        var pageResult = repository.listarCompleto(
                query.getSearch(),
                query.getDistritoJudicialId(),
                query.getTipoEvento(),
                query.getFechaInicio(),
                query.getFechaFin(),
                pageable
        );

        List<FortalecimientoCapacidades> contenido = pageResult.getContent().stream()
                .map(entity -> {
                    FortalecimientoCapacidades dominio = mapper.toDomain(entity);
                    cargarArchivosEnDominio(dominio);
                    return dominio;
                })
                .collect(Collectors.toList());

        return Pagina.<FortalecimientoCapacidades>builder()
                .contenido(contenido)
                .totalRegistros(pageResult.getTotalElements())
                .totalPaginas(pageResult.getTotalPages())
                .paginaActual(pagina)
                .tamanioPagina(tamanio)
                .build();
    }

    @Override
    @Transactional
    public FortalecimientoCapacidades guardar(String cuo, FortalecimientoCapacidades dominio) {
        log.info("[{}] Guardando Fortalecimiento: {}", cuo, dominio.getCodigo());
        MovEventoFcEntity entity = mapper.toEntity(dominio);
        MovEventoFcEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public FortalecimientoCapacidades actualizar(String cuo, FortalecimientoCapacidades dominio) {
        log.info("[{}] Actualizando Fortalecimiento ID: {}", cuo, dominio.getId());

        // Buscamos por ID
        MovEventoFcEntity entityDb = repository.findById(dominio.getId())
                .orElseThrow(() -> new MovimientoNoEncontradoException("No se encontró el evento con ID: " + dominio.getId()));

        mapper.updateEntityFromDomain(dominio, entityDb);

        actualizarHijos(entityDb, dominio);

        MovEventoFcEntity saved = repository.save(entityDb);
        return mapper.toDomain(saved);
    }

    @Override
    public FortalecimientoCapacidades obtenerPorId(String cuo, Long id) {
        FortalecimientoCapacidades dominio = repository.findById(id)
                .map(mapper::toDomain)
                .orElse(null);

        if (dominio != null) {
            cargarArchivosEnDominio(dominio);
        }
        return dominio;
    }

    @Override
    public String obtenerUltimoCodigo(String cuo, Long distritoId, String anio) {
        return repository.obtenerUltimoCodigo(distritoId, "-" + anio + "-FC");
    }

    // --- PRIVADOS ---

    private void cargarArchivosEnDominio(FortalecimientoCapacidades dominio) {
        List<MovArchivoEntity> archivosEntities = repoArchivos.findByNumeroIdentificacionAndActivo(dominio.getCodigo(), "1");
        if (archivosEntities != null && !archivosEntities.isEmpty()) {
            dominio.setArchivosGuardados(archivosEntities.stream()
                    .map(a -> Archivo.builder()
                            .id(a.getId())
                            .nombre(a.getNombre())
                            .tipo(a.getTipo())
                            .ruta(a.getRuta())
                            .numeroIdentificacion(a.getNumeroIdentificacion())
                            .build())
                    .collect(Collectors.toList()));
        }
    }

    private void actualizarHijos(MovEventoFcEntity entityDb, FortalecimientoCapacidades dominio) {
        // 1. Participantes
        if (entityDb.getParticipantes() != null) {
            entityDb.getParticipantes().clear();
        }
        if (dominio.getParticipantes() != null) {
            dominio.getParticipantes().forEach(d -> {
                var child = mapper.toEntityPart(d);
                child.setEventoId(entityDb.getId()); // ID Long del padre
                entityDb.getParticipantes().add(child);
            });
        }

        // 2. Tareas Realizadas
        if (entityDb.getTareasRealizadas() != null) {
            entityDb.getTareasRealizadas().clear();
        }
        if (dominio.getTareasRealizadas() != null) {
            dominio.getTareasRealizadas().forEach(d -> {
                var child = mapper.toEntityTarea(d);
                child.setEventoId(entityDb.getId()); // ID Long del padre
                entityDb.getTareasRealizadas().add(child);
            });
        }
    }
}