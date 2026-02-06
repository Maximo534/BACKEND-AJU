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
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.MaeDistritoJudicialRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.MaeTareaRepository;
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
    MaeDistritoJudicialRepository repoDistrito;
    MaeTareaRepository repoTareas;
    PromocionCulturaMapper mapper;

    @Override
    public Pagina<PromocionCultura> listar(String cuo, ListarPromocionQuery query, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio);

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

                    if (dominio.getDistritoJudicialId() != null) {
                        repoDistrito.findById(dominio.getDistritoJudicialId())
                                .ifPresent(dj -> dominio.setDistritoJudicialNombre(dj.getNombre()));
                    }

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
        log.info("[{}] Guardando Promoción Cultura: {}", cuo, dominio.getCodigo());
        MovPromocionCulturaEntity entity = mapper.toEntity(dominio);

        if (entity.getTareas() != null) {
            entity.getTareas().forEach(child -> {
                if (child.getTareaId() != null) {
                    repoTareas.findById(child.getTareaId()).ifPresent(child::setTareaMaestra);
                }
            });
        }

        MovPromocionCulturaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public PromocionCultura actualizar(String cuo, PromocionCultura dominio) {
        log.info("[{}] Actualizando Promoción Cultura ID: {}", cuo, dominio.getId());

        MovPromocionCulturaEntity entityDb = repository.findById(dominio.getId())
                .orElseThrow(() -> new MovimientoNoEncontradoException("No se encontró el registro con ID: " + dominio.getId()));

        mapper.updateEntityFromDomain(dominio, entityDb);

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
            // 1. Cargar Archivos (Solo para detalle)
            cargarArchivosEnDominio(dominio);

            // 2. Nombre Corte
            if (dominio.getDistritoJudicialId() != null) {
                repoDistrito.findById(dominio.getDistritoJudicialId())
                        .ifPresent(dj -> dominio.setDistritoJudicialNombre(dj.getNombre()));
            }

            // 3. Descripción Tareas
            if (dominio.getTareasRealizadas() != null) {
                dominio.getTareasRealizadas().forEach(t -> {
                    if (t.getTareaId() != null) {
                        repoTareas.findById(t.getTareaId())
                                .ifPresent(tm -> t.setDescripcion(tm.getDescripcion()));
                    }
                });
            }
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

        // 1. Personas Beneficiadas (Merge por CodigoRango)
        var benBd = entityDb.getPersonasBeneficiadas();
        var benNuevas = dominio.getPersonasBeneficiadas();

        if (benNuevas != null) {
            // Eliminar
            benBd.removeIf(bd -> benNuevas.stream().noneMatch(dto ->
                    dto.getCodigoRango().trim().equals(bd.getCodigoRango().trim())
            ));

            // Actualizar o Agregar
            benNuevas.forEach(dto -> {
                var existente = benBd.stream()
                        .filter(bd -> bd.getCodigoRango().trim().equals(dto.getCodigoRango().trim()))
                        .findFirst();

                if (existente.isPresent()) {
                    var item = existente.get();
                    var temp = mapper.toEntityPB(dto);

                    item.setCantidadFemenino(temp.getCantidadFemenino());
                    item.setCantidadMasculino(temp.getCantidadMasculino());
                    item.setCantidadLgtbiq(temp.getCantidadLgtbiq());
                    item.setDescripcionRango(temp.getDescripcionRango()); // Por si cambia la descripción

                    item.setFAud(java.time.LocalDateTime.now());
                    item.setBAud("M");
                    item.setCAudId(entityDb.getCAudId());
                } else {
                    var nuevo = mapper.toEntityPB(dto);
                    nuevo.setPromocionCultura(entityDb);
                    nuevo.setCAudId(entityDb.getCAudId());
                    benBd.add(nuevo);
                }
            });
        } else {
            benBd.clear();
        }

        // 2. Tareas Realizadas
        var tarBd = entityDb.getTareas();
        var tarNuevas = dominio.getTareasRealizadas();

        if (tarNuevas != null) {
            List<Long> idsNuevos = tarNuevas.stream().map(PromocionCultura.DetalleTarea::getTareaId).toList();
            tarBd.removeIf(bd -> !idsNuevos.contains(bd.getTareaId()));

            tarNuevas.forEach(dto -> {
                var existente = tarBd.stream()
                        .filter(bd -> bd.getTareaId().equals(dto.getTareaId()))
                        .findFirst();

                if (existente.isPresent()) {
                    var item = existente.get();
                    var temp = mapper.toEntityTarea(dto);

                    item.setFechaInicio(temp.getFechaInicio());

                    if(item.getTareaMaestra() == null) {
                        repoTareas.findById(item.getTareaId()).ifPresent(item::setTareaMaestra);
                    }

                    item.setFAud(java.time.LocalDateTime.now());
                    item.setBAud("M");
                    item.setCAudId(entityDb.getCAudId());
                } else {
                    var nuevo = mapper.toEntityTarea(dto);
                    nuevo.setPromocionCultura(entityDb);
                    nuevo.setCAudId(entityDb.getCAudId());

                    repoTareas.findById(nuevo.getTareaId()).ifPresent(nuevo::setTareaMaestra);

                    tarBd.add(nuevo);
                }
            });
        } else {
            tarBd.clear();
        }
    }
}