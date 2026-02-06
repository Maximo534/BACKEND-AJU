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
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarJusticiaItineranteQuery;
import pe.gob.pj.prueba.domain.port.persistence.negocio.JusticiaItinerantePersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivoEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovJusticiaItineranteEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovArchivosRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovJusticiaItineranteRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovUsuarioRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.MaeDistritoJudicialRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.MaeTareaRepository; // Importar Repo Tarea
import pe.gob.pj.prueba.infraestructure.mappers.JusticiaItineranteMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JusticiaItinerantePersistenceAdapter implements JusticiaItinerantePersistencePort {

    MovJusticiaItineranteRepository repository;
    MaeDistritoJudicialRepository repoDistrito;
    MovArchivosRepository repoArchivos;
    MaeTareaRepository repoTareas;
    MovUsuarioRepository usuarioRepository;
    JusticiaItineranteMapper mapper;

    @Override
    public Pagina<JusticiaItinerante> listar(String cuo, ListarJusticiaItineranteQuery query, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio);

        var pageResult = repository.listarCompleto(
                query.getSearch(),
                query.getDistritoJudicialId(),
                query.getFechaInicio(),
                query.getFechaFin(),
                pageable
        );

        List<JusticiaItinerante> contenido = pageResult.getContent().stream()
                .map(entity -> {
                    JusticiaItinerante dominio = mapper.toDomain(entity);

                    if (dominio.getDistritoJudicialId() != null) {
                        repoDistrito.findById(dominio.getDistritoJudicialId())
                                .ifPresent(dj -> dominio.setDistritoJudicialNombre(dj.getNombre()));
                    }

                    return dominio;
                })
                .collect(Collectors.toList());

        return Pagina.<JusticiaItinerante>builder()
                .contenido(contenido)
                .totalRegistros(pageResult.getTotalElements())
                .totalPaginas(pageResult.getTotalPages())
                .paginaActual(pagina)
                .tamanioPagina(tamanio)
                .build();
    }

    @Override
    @Transactional
    public JusticiaItinerante guardar(String cuo, JusticiaItinerante dominio) {
        log.info("[{}] Guardando Justicia Itinerante: {}", cuo, dominio.getUsuario());

        if (dominio.getUsuario() != null) {
            var usuarioEntity = usuarioRepository.findByActivoAndUsuario("1", dominio.getUsuario())
                    .orElseThrow(() -> new MovimientoNoEncontradoException(
                            "No se encontró el usuario '"+ dominio.getUsuario() +"' en la BD."));

            dominio.setUsuarioRegistroId(usuarioEntity.getId().longValue());
        }

        MovJusticiaItineranteEntity entity = mapper.toEntity(dominio);

        if (entity.getTareasRealizadas() != null) {
            entity.getTareasRealizadas().forEach(child -> {
                if (child.getTareaId() != null) {
                    repoTareas.findById(child.getTareaId()).ifPresent(child::setTareaMaestra);
                }
            });
        }

        MovJusticiaItineranteEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public JusticiaItinerante actualizar(String cuo, JusticiaItinerante dominio) {
        log.info("[{}] Actualizando Justicia Itinerante ID: {}", cuo, dominio.getId());

        MovJusticiaItineranteEntity entityDb = repository.findByIdAndActivo(dominio.getId(), "1")
                .orElseThrow(() -> new MovimientoNoEncontradoException("No se encontró el registro con ID: " + dominio.getId()));

        mapper.updateEntityFromDomain(dominio, entityDb);

        actualizarHijos(entityDb, dominio);

        MovJusticiaItineranteEntity saved = repository.save(entityDb);
        return mapper.toDomain(saved);
    }

    @Override
    public JusticiaItinerante obtenerPorId(String cuo, Long id) {
        JusticiaItinerante dominio = repository.findByIdAndActivo(id, "1")
                .map(mapper::toDomain)
                .orElse(null);

        if (dominio != null) {
            cargarArchivosEnDominio(dominio);

            if (dominio.getDistritoJudicialId() != null) {
                repoDistrito.findById(dominio.getDistritoJudicialId())
                        .ifPresent(dj -> dominio.setDistritoJudicialNombre(dj.getNombre()));
            }

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
        return repository.obtenerUltimoCodigo(distritoId, "-" + anio + "-JI");
    }

    // --- MÉTODOS PRIVADOS ---

    private void cargarArchivosEnDominio(JusticiaItinerante dominio) {
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

    private void actualizarHijos(MovJusticiaItineranteEntity entityDb, JusticiaItinerante dominio) {

        // =================================================================================
        // 1. CASOS ATENDIDOS (El que te dio error)
        // =================================================================================
        var casosBd = entityDb.getCasosAtendidos();
        var casosNuevos = dominio.getCasosAtendidos();

        if (casosNuevos != null) {
            List<Long> idsNuevos = casosNuevos.stream().map(JusticiaItinerante.DetalleCaso::getMateriaId).toList();
            casosBd.removeIf(bd -> !idsNuevos.contains(bd.getMateriaId()));

            // B. Actualizamos o Agregamos
            casosNuevos.forEach(dto -> {
                var existente = casosBd.stream()
                        .filter(bd -> bd.getMateriaId().equals(dto.getMateriaId()))
                        .findFirst();

                if (existente.isPresent()) {
                    var item = existente.get();
                    // Usamos un temporal del mapper para sacar los valores convertidos
                    var temp = mapper.toEntityPCA(dto);

                    item.setCantidadDemandas(temp.getCantidadDemandas());
                    item.setCantidadAudiencias(temp.getCantidadAudiencias());
                    item.setCantidadSentencias(temp.getCantidadSentencias());
                    item.setCantidadProcesos(temp.getCantidadProcesos());
                    item.setCantidadNotificaciones(temp.getCantidadNotificaciones());
                    item.setCantidadOrientaciones(temp.getCantidadOrientaciones());

                    item.setFAud(java.time.LocalDateTime.now());
                    item.setBAud("M");
                    item.setCAudId(entityDb.getCAudId());
                } else {
                    // --- AGREGAR NUEVO ---
                    var nuevo = mapper.toEntityPCA(dto);
                    nuevo.setJusticiaItinerante(entityDb);
                    nuevo.setCAudId(entityDb.getCAudId());
                    casosBd.add(nuevo);
                }
            });
        } else {
            casosBd.clear();
        }

        // =================================================================================
        // 2. PERSONAS ATENDIDAS
        // =================================================================================
        var perBd = entityDb.getPersonasAtendidas();
        var perNuevas = dominio.getPersonasAtendidas();

        if (perNuevas != null) {
            perBd.removeIf(bd -> perNuevas.stream().noneMatch(dto ->
                    dto.getTipoVulnerabilidadId().equals(bd.getTipoVulnerabilidadId()) &&
                            dto.getRangoEdad().trim().equals(bd.getRangoEdad().trim())
            ));

            perNuevas.forEach(dto -> {
                var existente = perBd.stream()
                        .filter(bd -> bd.getTipoVulnerabilidadId().equals(dto.getTipoVulnerabilidadId()) &&
                                bd.getRangoEdad().trim().equals(dto.getRangoEdad().trim()))
                        .findFirst();

                if (existente.isPresent()) {
                    var item = existente.get();
                    var temp = mapper.toEntityPA(dto);

                    item.setCantFemenino(temp.getCantFemenino());
                    item.setCantMasculino(temp.getCantMasculino());
                    item.setCantLgtbiq(temp.getCantLgtbiq());

                    item.setFAud(java.time.LocalDateTime.now());
                    item.setBAud("M");
                    item.setCAudId(entityDb.getCAudId());
                } else {
                    var nuevo = mapper.toEntityPA(dto);
                    nuevo.setJusticiaItinerante(entityDb);
                    nuevo.setCAudId(entityDb.getCAudId());
                    perBd.add(nuevo);
                }
            });
        } else {
            perBd.clear();
        }

        // =================================================================================
        // 3. PERSONAS BENEFICIADAS
        // =================================================================================
        var benBd = entityDb.getPersonasBeneficiadas();
        var benNuevas = dominio.getPersonasBeneficiadas();

        if (benNuevas != null) {
            benBd.removeIf(bd -> benNuevas.stream().noneMatch(dto ->
                    dto.getCodigoRango().trim().equals(bd.getCodigoRango().trim())
            ));

            benNuevas.forEach(dto -> {
                var existente = benBd.stream()
                        .filter(bd -> bd.getCodigoRango().trim().equals(dto.getCodigoRango().trim()))
                        .findFirst();

                if (existente.isPresent()) {
                    var item = existente.get();
                    var temp = mapper.toEntityPB(dto);

                    item.setCantFemenino(temp.getCantFemenino());
                    item.setCantMasculino(temp.getCantMasculino());
                    item.setCantLgtbiq(temp.getCantLgtbiq());
                    // Actualiza descripción por si cambió
                    item.setDescripcionRango(temp.getDescripcionRango());

                    item.setFAud(java.time.LocalDateTime.now());
                    item.setBAud("M");
                    item.setCAudId(entityDb.getCAudId());
                } else {
                    var nuevo = mapper.toEntityPB(dto);
                    nuevo.setJusticiaItinerante(entityDb);
                    nuevo.setCAudId(entityDb.getCAudId());
                    benBd.add(nuevo);
                }
            });
        } else {
            benBd.clear();
        }

        // =================================================================================
        // 4. TAREAS REALIZADAS
        // =================================================================================
        var tarBd = entityDb.getTareasRealizadas();
        var tarNuevas = dominio.getTareasRealizadas();

        if (tarNuevas != null) {
            List<Long> idsTareasNuevos = tarNuevas.stream().map(JusticiaItinerante.DetalleTarea::getTareaId).toList();
            tarBd.removeIf(bd -> !idsTareasNuevos.contains(bd.getTareaId()));

            tarNuevas.forEach(dto -> {
                var existente = tarBd.stream()
                        .filter(bd -> bd.getTareaId().equals(dto.getTareaId()))
                        .findFirst();

                if (existente.isPresent()) {
                    var item = existente.get();
                    var temp = mapper.toEntityTR(dto);

                    item.setFechaInicio(temp.getFechaInicio());
                    if(item.getTareaMaestra() == null) {
                        repoTareas.findById(item.getTareaId()).ifPresent(item::setTareaMaestra);
                    }

                    item.setFAud(java.time.LocalDateTime.now());
                    item.setBAud("M");
                    item.setCAudId(entityDb.getCAudId());
                } else {
                    var nuevo = mapper.toEntityTR(dto);
                    nuevo.setJusticiaItinerante(entityDb);
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