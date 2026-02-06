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
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovUsuarioRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.MaeDistritoJudicialRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.MaeTareaRepository;
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
    MaeDistritoJudicialRepository repoDistrito;
    MaeTareaRepository repoTareas;
    MovUsuarioRepository usuarioRepository;
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

                    // Lógica optimizada: Solo nombre de corte, NO archivos
                    if (dominio.getDistritoJudicialId() != null) {
                        repoDistrito.findById(dominio.getDistritoJudicialId())
                                .ifPresent(dj -> dominio.setDistritoJudicialNombre(dj.getNombre()));
                    }

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

        if (dominio.getUsuario() != null) {
            var usuarioEntity = usuarioRepository.findByActivoAndUsuario("1", dominio.getUsuario())
                    .orElseThrow(() -> new MovimientoNoEncontradoException(
                            "No se encontró el usuario '"+ dominio.getUsuario() +"' en la BD."));

            dominio.setUsuarioRegistroId(usuarioEntity.getId().longValue());
        }

        MovEventoFcEntity entity = mapper.toEntity(dominio);

        if (entity.getTareasRealizadas() != null) {
            entity.getTareasRealizadas().forEach(child -> {
                if (child.getTareaId() != null) {
                    repoTareas.findById(child.getTareaId()).ifPresent(child::setTareaMaestra);
                }
            });
        }

        MovEventoFcEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public FortalecimientoCapacidades actualizar(String cuo, FortalecimientoCapacidades dominio) {
        log.info("[{}] Actualizando Fortalecimiento ID: {}", cuo, dominio.getId());

        MovEventoFcEntity entityDb = repository.findById(dominio.getId())
                .orElseThrow(() -> new MovimientoNoEncontradoException("No se encontró el evento con ID: " + dominio.getId()));

        mapper.updateEntityFromDomain(dominio, entityDb);

        // Actualización inteligente de hijos (Merge)
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

            // 2. Nombre de Corte
            if (dominio.getDistritoJudicialId() != null) {
                repoDistrito.findById(dominio.getDistritoJudicialId())
                        .ifPresent(dj -> dominio.setDistritoJudicialNombre(dj.getNombre()));
            }

            // 3. Descripciones de Tareas
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
        return repository.obtenerUltimoCodigo(distritoId, "-" + anio + "-FC");
    }

    // --- MÉTODOS PRIVADOS ---

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

        var partBd = entityDb.getParticipantes();
        var partNuevos = dominio.getParticipantes();

        if (partNuevos != null) {
            partBd.removeIf(bd -> partNuevos.stream().noneMatch(dto ->
                    dto.getTipoParticipanteId().equals(bd.getTipoParticipanteId()) &&
                            dto.getRangoEdad().trim().equals(bd.getRangoEdad().trim())
            ));

            partNuevos.forEach(dto -> {
                var existente = partBd.stream()
                        .filter(bd -> bd.getTipoParticipanteId().equals(dto.getTipoParticipanteId()) &&
                                bd.getRangoEdad().trim().equals(dto.getRangoEdad().trim()))
                        .findFirst();

                if (existente.isPresent()) {
                    // Actualizar existente
                    var item = existente.get();
                    var temp = mapper.toEntityPart(dto);

                    item.setCantidadFemenino(temp.getCantidadFemenino());
                    item.setCantidadMasculino(temp.getCantidadMasculino());
                    item.setCantidadLgtbiq(temp.getCantidadLgtbiq());

                    // Actualizar auditoría
                    item.setFAud(java.time.LocalDateTime.now());
                    item.setBAud("M");
                    item.setCAudId(entityDb.getCAudId());
                } else {
                    // Agregar nuevo
                    var nuevo = mapper.toEntityPart(dto);
                    nuevo.setEvento(entityDb);
                    nuevo.setCAudId(entityDb.getCAudId());
                    partBd.add(nuevo);
                }
            });
        } else {
            partBd.clear();
        }


        var tarBd = entityDb.getTareasRealizadas();
        var tarNuevas = dominio.getTareasRealizadas();

        if (tarNuevas != null) {
            List<Long> idsTareasNuevos = tarNuevas.stream().map(FortalecimientoCapacidades.DetalleTarea::getTareaId).toList();
            tarBd.removeIf(bd -> !idsTareasNuevos.contains(bd.getTareaId()));

            tarNuevas.forEach(dto -> {
                var existente = tarBd.stream()
                        .filter(bd -> bd.getTareaId().equals(dto.getTareaId()))
                        .findFirst();

                if (existente.isPresent()) {
                    var item = existente.get();
                    var temp = mapper.toEntityTarea(dto);

                    item.setFechaInicio(temp.getFechaInicio());

                    // Cargar Maestra si falta
                    if(item.getTareaMaestra() == null) {
                        repoTareas.findById(item.getTareaId()).ifPresent(item::setTareaMaestra);
                    }

                    item.setFAud(java.time.LocalDateTime.now());
                    item.setBAud("M");
                    item.setCAudId(entityDb.getCAudId());
                } else {
                    var nuevo = mapper.toEntityTarea(dto);
                    nuevo.setEvento(entityDb);
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