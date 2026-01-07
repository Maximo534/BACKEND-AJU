package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.PromocionCultura;
import pe.gob.pj.prueba.domain.port.persistence.negocio.PromocionCulturaPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovPromocionCulturaEntity;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivosEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovArchivosRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovPromocionCulturaRepository;
import pe.gob.pj.prueba.infraestructure.mappers.PromocionCulturaMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromocionCulturaPersistenceAdapter implements PromocionCulturaPersistencePort {

    private final MovPromocionCulturaRepository repository;
    private final MovArchivosRepository repoArchivos; // <--- 1. INYECTAR ESTO
    private final PromocionCulturaMapper mapper;

    @Override
    @Transactional
    public PromocionCultura guardar(PromocionCultura dominio) throws Exception {
        try {
            MovPromocionCulturaEntity entity = mapper.toEntity(dominio);

            if (entity.getParticipantes() != null) {
                entity.getParticipantes().forEach(p -> p.setPromocionCulturaId(entity.getId()));
            }

            if (entity.getTareas() != null) {
                entity.getTareas().forEach(t -> t.setPromocionCulturaId(entity.getId()));
            }

            return mapper.toDomain(repository.save(entity));

        } catch (Exception e) {
            log.error("Error guardando PromocionCultura", e);
            throw new Exception("Error BD al guardar: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PromocionCultura actualizar(PromocionCultura dominio) throws Exception {
        try {
            MovPromocionCulturaEntity entidadDb = repository.findById(dominio.getId())
                    .orElseThrow(() -> new Exception("Evento no encontrado con ID: " + dominio.getId()));

            entidadDb.setDistritoJudicialId(dominio.getDistritoJudicialId());
            entidadDb.setNombreActividad(dominio.getNombreActividad());
            entidadDb.setTipoActividad(dominio.getTipoActividad());
            entidadDb.setTipoActividadOtros(dominio.getTipoActividadOtros());
            entidadDb.setZonaIntervencion(dominio.getZonaIntervencion());
            entidadDb.setModalidadProyecto(dominio.getModalidad());
            entidadDb.setPublicoObjetivo(dominio.getPublicoObjetivo());
            entidadDb.setPublicoObjetivoOtros(dominio.getPublicoObjetivoOtros());
            entidadDb.setFechaInicio(dominio.getFechaInicio());
            entidadDb.setFechaFin(dominio.getFechaFin());
            entidadDb.setResolucionPlanAnual(dominio.getResolucionPlanAnual());
            entidadDb.setResolucionAdminPlan(dominio.getResolucionAdminPlan());
            entidadDb.setDocumentoAutoriza(dominio.getDocumentoAutoriza());
            entidadDb.setLugarActividad(dominio.getLugarActividad());
            entidadDb.setDepartamentoId(dominio.getDepartamentoId());
            entidadDb.setProvinciaId(dominio.getProvinciaId());
            entidadDb.setDistritoGeograficoId(dominio.getDistritoGeograficoId());
            entidadDb.setEjeId(dominio.getEjeId());
            entidadDb.setSeDictoLenguaNativa(dominio.getSeDictoLenguaNativa());
            entidadDb.setLenguaNativaDesc(dominio.getLenguaNativaDesc());
            entidadDb.setParticiparonDiscapacitados(dominio.getParticiparonDiscapacitados());
            entidadDb.setNumeroDiscapacitados(dominio.getNumeroDiscapacitados());
            entidadDb.setDescripcionActividad(dominio.getDescripcionActividad());
            entidadDb.setInstitucionesAliadas(dominio.getInstitucionesAliadas());
            entidadDb.setObservacion(dominio.getObservaciones());

            if (entidadDb.getParticipantes() == null) entidadDb.setParticipantes(new ArrayList<>());
            entidadDb.getParticipantes().clear(); // Borramos los hijos actuales de la memoria
            repository.flush(); // 🔥 IMPORTANTE: Forzamos el DELETE en BD antes de insertar los nuevos

            if (dominio.getParticipantesPorGenero() != null) {
                dominio.getParticipantesPorGenero().forEach(p -> {
                    var entityPart = mapper.toEntityPart(p);
                    entityPart.setPromocionCulturaId(entidadDb.getId());
                    entidadDb.getParticipantes().add(entityPart);
                });
            }

            if (entidadDb.getTareas() == null) entidadDb.setTareas(new ArrayList<>());
            entidadDb.getTareas().clear();
            repository.flush();

            if (dominio.getTareasRealizadas() != null) {
                dominio.getTareasRealizadas().forEach(t -> {
                    var entityTarea = mapper.toEntityTarea(t);
                    entityTarea.setPromocionCulturaId(entidadDb.getId());
                    entidadDb.getTareas().add(entityTarea);
                });
            }

            return mapper.toDomain(repository.save(entidadDb));

        } catch (Exception e) {
            log.error("Error actualizando PromocionCultura", e);
            throw new Exception("Error BD al actualizar: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PromocionCultura obtenerPorId(String id) throws Exception {
        MovPromocionCulturaEntity entidad = repository.findById(id).orElse(null);
        if (entidad == null) return null;
        PromocionCultura dominio = mapper.toDomain(entidad);

        try {
            List<MovArchivosEntity> archivosEntities = repoArchivos.findByNumeroIdentificacion(id);

            if (archivosEntities != null && !archivosEntities.isEmpty()) {
                List<Archivo> listaArchivos = archivosEntities.stream()
                        .map(a -> Archivo.builder()
                                .nombre(a.getNombre())
                                .tipo(a.getTipo())
                                .ruta(a.getRuta())
                                .numeroIdentificacion(a.getNumeroIdentificacion())
                                .build())
                        .collect(Collectors.toList());

                dominio.setArchivosGuardados(listaArchivos);
            }
        } catch (Exception e) {
            log.warn("Error recuperando archivos para ID {}: {}", id, e.getMessage());
        }

        return dominio;
    }

    @Override
    public String obtenerUltimoId() throws Exception {
        try {
            return repository.obtenerUltimoId();
        } catch (Exception e) {
            log.error("Error obteniendo ultimo ID", e);
            return null;
        }
    }

    @Override
    public Pagina<PromocionCultura> listarPromocion(String usuario, PromocionCultura filtros, int pagina, int tamanio) throws Exception {
        try {
            Pageable pageable = PageRequest.of(pagina - 1, tamanio);

            String codigo = (filtros != null) ? filtros.getId() : null;
            String descripcion = (filtros != null) ? filtros.getDescripcionActividad() : null;
            String distrito = (filtros != null) ? filtros.getDistritoJudicialId() : null;
            LocalDate fecIni = (filtros != null) ? filtros.getFechaInicio() : null;
            LocalDate fecFin = (filtros != null) ? filtros.getFechaFin() : null;

            Page<MovPromocionCulturaEntity> pageResult = repository.listarDinamico(
                    usuario,
                    codigo,
                    descripcion,
                    distrito,
                    fecIni,
                    fecFin,
                    pageable
            );

            List<PromocionCultura> listaDominio = pageResult.getContent().stream()
                    .map(mapper::toDomainResumen)
                    .collect(Collectors.toList());

            return Pagina.<PromocionCultura>builder()
                    .contenido(listaDominio)
                    .totalRegistros(pageResult.getTotalElements())
                    .totalPaginas(pageResult.getTotalPages())
                    .paginaActual(pagina)
                    .tamanioPagina(tamanio)
                    .build();

        } catch (Exception e) {
            log.error("Error listando PromocionCultura", e);
            throw new Exception("Error BD al listar: " + e.getMessage());
        }
    }
}