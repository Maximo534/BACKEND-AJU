package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.FortalecimientoCapacidades;
import pe.gob.pj.prueba.domain.port.persistence.negocio.FortalecimientoPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivosEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovEventoFcEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.*;
import pe.gob.pj.prueba.infraestructure.mappers.FortalecimientoMapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class FortalecimientoPersistenceAdapter implements FortalecimientoPersistencePort {

    private final MovEventoFcRepository repository;
    private final MovEventoDetalleRepository repoDetalles;
    private final MovEventoTareaRepository repoTareas;
    private final MovArchivosRepository repoArchivos;
    private final FortalecimientoMapper mapper;

    @Override
    @Transactional
    public FortalecimientoCapacidades guardar(FortalecimientoCapacidades dominio) throws Exception {
        try {
            MovEventoFcEntity entidad = mapper.toEntity(dominio);

            if (entidad.getParticipantes() != null) {
                entidad.getParticipantes().forEach(d -> d.setEventoId(entidad.getId()));
            }
            if (entidad.getTareas() != null) {
                entidad.getTareas().forEach(d -> d.setEventoId(entidad.getId()));
            }

            MovEventoFcEntity guardado = repository.save(entidad);
            return mapper.toDomain(guardado);

        } catch (Exception e) {
            log.error("Error al guardar FFC", e);
            throw new Exception("Error BD: " + e.getMessage());
        }
    }
    @Override
    public String obtenerUltimoId() throws Exception {
        return repository.obtenerUltimoId();
    }
    @Override
    @Transactional
    public FortalecimientoCapacidades actualizar(FortalecimientoCapacidades dominio) throws Exception {
        try {
            MovEventoFcEntity entidadDb = repository.findById(dominio.getId())
                    .orElseThrow(() -> new Exception("Evento no encontrado: " + dominio.getId()));

            entidadDb.setDistritoJudicialId(dominio.getDistritoJudicialId());
            entidadDb.setTipoEvento(dominio.getTipoEvento());
            entidadDb.setNombreEvento(dominio.getNombreEvento());
            entidadDb.setFechaInicio(dominio.getFechaInicio());
            entidadDb.setFechaFin(dominio.getFechaFin());
            entidadDb.setResolucionPlanAnual(dominio.getResolucionPlanAnual());
            entidadDb.setResolucionAdminPlan(dominio.getResolucionAdminPlan());
            entidadDb.setDocumentoAutoriza(dominio.getDocumentoAutoriza());
            entidadDb.setEjeId(dominio.getEjeId());
            entidadDb.setModalidad(dominio.getModalidad());
            entidadDb.setDuracionHoras(dominio.getDuracionHoras());
            entidadDb.setNumeroSesiones(dominio.getNumeroSesiones());
            entidadDb.setDocenteExpositor(dominio.getDocenteExpositor());
            entidadDb.setInterpreteSenias(dominio.getInterpreteSenias());
            entidadDb.setNumeroDiscapacitados(dominio.getNumeroDiscapacitados());
            entidadDb.setSeDictoLenguaNativa(dominio.getSeDictoLenguaNativa());
            entidadDb.setLenguaNativaDesc(dominio.getLenguaNativaDesc());
            entidadDb.setPublicoObjetivo(dominio.getPublicoObjetivo());
            entidadDb.setNombreInstitucion(dominio.getNombreInstitucion());
            entidadDb.setDepartamentoId(dominio.getDepartamentoId());
            entidadDb.setProvinciaId(dominio.getProvinciaId());
            entidadDb.setDistritoGeograficoId(dominio.getDistritoGeograficoId());
            entidadDb.setDescripcionActividad(dominio.getDescripcionActividad());
            entidadDb.setObservaciones(dominio.getObservaciones());
            entidadDb.setInstitucionesAliadas(dominio.getInstitucionesAliadas());


            entidadDb.getParticipantes().clear();
            repository.flush();

            if (dominio.getParticipantes() != null) {
                dominio.getParticipantes().forEach(p -> {
                    var entityPart = mapper.toEntityPart(p);
                    entityPart.setEventoId(entidadDb.getId());
                    entidadDb.getParticipantes().add(entityPart);
                });
            }

            entidadDb.getTareas().clear();
            repository.flush();

            if (dominio.getTareas() != null) {
                dominio.getTareas().forEach(t -> {
                    var entityTarea = mapper.toEntityTarea(t);
                    entityTarea.setEventoId(entidadDb.getId());
                    entidadDb.getTareas().add(entityTarea);
                });
            }

            MovEventoFcEntity guardado = repository.save(entidadDb);

            return mapper.toDomain(guardado);

        } catch (Exception e) {
            log.error("Error al actualizar FFC", e);
            throw new Exception("Error BD al actualizar: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FortalecimientoCapacidades obtenerPorId(String id) throws Exception {
        MovEventoFcEntity entidad = repository.findById(id)
                .orElseThrow(() -> new Exception("Evento no encontrado: " + id));

        FortalecimientoCapacidades dominio = mapper.toDomain(entidad);

        List<MovArchivosEntity> archivos = repoArchivos.findByNumeroIdentificacion(id);
        if (archivos != null && !archivos.isEmpty()) {
            dominio.setArchivosGuardados(archivos.stream()
                    .map(a -> Archivo.builder()
                            .nombre(a.getNombre()).tipo(a.getTipo())
                            .ruta(a.getRuta()).numeroIdentificacion(a.getNumeroIdentificacion())
                            .build())
                    .collect(Collectors.toList()));
        }
        return dominio;
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<FortalecimientoCapacidades> listar(String usuario, FortalecimientoCapacidades filtros, int pagina, int tamanio) throws Exception {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio);
        if (filtros == null) filtros = FortalecimientoCapacidades.builder().build();

        Page<MovEventoFcEntity> pageResult = repository.listarDinamico(
                usuario,
                filtros.getId(),
                filtros.getNombreEvento(),
                filtros.getDistritoJudicialId(),
                filtros.getFechaInicio(),
                filtros.getFechaFin(),
                pageable
        );

        List<FortalecimientoCapacidades> contenido = pageResult.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        return Pagina.<FortalecimientoCapacidades>builder()
                .contenido(contenido)
                .totalRegistros(pageResult.getTotalElements())
                .totalPaginas(pageResult.getTotalPages())
                .paginaActual(pagina)
                .tamanioPagina(tamanio)
                .build();
    }

}