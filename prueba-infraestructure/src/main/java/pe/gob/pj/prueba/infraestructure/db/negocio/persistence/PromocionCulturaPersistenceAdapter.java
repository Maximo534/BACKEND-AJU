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
import pe.gob.pj.prueba.domain.model.negocio.PromocionCultura;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarPromocionQuery;
import pe.gob.pj.prueba.domain.port.persistence.negocio.PromocionCulturaPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivoEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovPromocionCulturaEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovArchivosRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovPromocionCulturaRepository;
import pe.gob.pj.prueba.infraestructure.mappers.PromocionCulturaMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromocionCulturaPersistenceAdapter implements PromocionCulturaPersistencePort {

    MovPromocionCulturaRepository repository;
    MovArchivosRepository repoArchivos;
    PromocionCulturaMapper mapper;

    @Override
    public Pagina<PromocionCultura> listar(String cuo, ListarPromocionQuery query, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio);

        // Filtro por ID
        var pageResult = repository.listarCompleto(
                query.getSearch(),
                query.getDistritoJudicialId(),
                query.getFechaInicio(),
                query.getFechaFin(),
                pageable
        );

        List<PromocionCultura> contenido = pageResult.getContent().stream()
                .map(entity -> {
                    PromocionCultura dominio = mapper.toDomain(entity);
                    cargarArchivosEnDominio(dominio);
                    return dominio;
                })
                .collect(Collectors.toList());

        return Pagina.<PromocionCultura>builder()
                .contenido(contenido)
                .totalRegistros(pageResult.getTotalElements())
                .totalPaginas(pageResult.getTotalPages())
                .paginaActual(pagina)
                .tamanioPagina(tamanio)
                .build();
    }

    @Override
    @Transactional
    public PromocionCultura guardar(String cuo, PromocionCultura dominio) {
        log.info("[{}] Guardando Promocion Cultura: {}", cuo, dominio.getCodigo());
        MovPromocionCulturaEntity entity = mapper.toEntity(dominio);
        MovPromocionCulturaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public PromocionCultura actualizar(String cuo, PromocionCultura dominio) {
        log.info("[{}] Actualizando Promocion Cultura ID: {}", cuo, dominio.getId());

        // Buscamos por ID Long
        MovPromocionCulturaEntity entityDb = repository.findById(dominio.getId())
                .orElseThrow(() -> new MovimientoNoEncontradoException("No se encontró el registro con ID: " + dominio.getId()));

        mapper.updateEntityFromDomain(dominio, entityDb);

        // Actualizar Listas Hijas (Orphan Removal Manual)
        actualizarHijos(entityDb, dominio);

        MovPromocionCulturaEntity saved = repository.save(entityDb);
        return mapper.toDomain(saved);
    }

    @Override
    public PromocionCultura obtenerPorId(String cuo, Long id) {
        PromocionCultura dominio = repository.findById(id)
                .map(mapper::toDomain)
                .orElse(null);

        if (dominio != null) {
            cargarArchivosEnDominio(dominio);
        }
        return dominio;
    }

    @Override
    public String obtenerUltimoCodigo(String cuo, Long distritoId, String anio) {
        return repository.obtenerUltimoCodigo(distritoId, "-" + anio + "-PC");
    }

    // --- PRIVADOS ---

    private void cargarArchivosEnDominio(PromocionCultura dominio) {
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

    private void actualizarHijos(MovPromocionCulturaEntity entityDb, PromocionCultura dominio) {
        // 1. Personas Beneficiadas
        if (entityDb.getPersonasBeneficiadas() != null) {
            entityDb.getPersonasBeneficiadas().clear();
        }
        if (dominio.getPersonasBeneficiadas() != null) {
            dominio.getPersonasBeneficiadas().forEach(d -> {
                var child = mapper.toEntityPB(d);
                child.setPromocionCulturaId(entityDb.getId());
                entityDb.getPersonasBeneficiadas().add(child);
            });
        }

        // 2. Tareas Realizadas
        if (entityDb.getTareas() != null) {
            entityDb.getTareas().clear();
        }
        if (dominio.getTareasRealizadas() != null) {
            dominio.getTareasRealizadas().forEach(d -> {
                var child = mapper.toEntityTarea(d);
                child.setPromocionCulturaId(entityDb.getId());
                entityDb.getTareas().add(child);
            });
        }
    }
}