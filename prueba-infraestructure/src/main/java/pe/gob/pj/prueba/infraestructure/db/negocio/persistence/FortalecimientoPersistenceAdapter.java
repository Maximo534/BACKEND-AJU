package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.MaeDistritoJudicialRepository;
import pe.gob.pj.prueba.infraestructure.mappers.FortalecimientoMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class FortalecimientoPersistenceAdapter implements FortalecimientoPersistencePort {

    private final MovEventoFcRepository repository;
    private final MovArchivosRepository repoArchivos;
    private final MaeDistritoJudicialRepository repoDistrito;
    private final FortalecimientoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Pagina<FortalecimientoCapacidades> listar(String usuario, FortalecimientoCapacidades filtros, int pagina, int tamanio) throws Exception {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio);
        if (filtros == null) filtros = FortalecimientoCapacidades.builder().build();

        String search = filtros.getSearch();
        String distrito = filtros.getDistritoJudicialId();
        String tipo = filtros.getTipoEvento();
        LocalDate fIni = filtros.getFechaInicio();
        LocalDate fFin = filtros.getFechaFin();

        // La query nativa ya trae el nombre del distrito (JOIN)
        var result = repository.listar(usuario, search, distrito, tipo, fIni, fFin, pageable);

        List<FortalecimientoCapacidades> contenido = result.getContent().stream()
                .map(p -> FortalecimientoCapacidades.builder()
                        .id(p.getId())
                        .fechaInicio(p.getFechaInicio())
                        .fechaFin(p.getFechaFin())
                        .tipoEvento(p.getTipoEvento())
                        .distritoJudicialNombre(p.getDistritoJudicialNombre())
                        .activo(p.getEstado())
                        .build())
                .collect(Collectors.toList());

        return Pagina.<FortalecimientoCapacidades>builder()
                .contenido(contenido)
                .totalRegistros(result.getTotalElements())
                .totalPaginas(result.getTotalPages())
                .paginaActual(pagina)
                .tamanioPagina(tamanio)
                .build();
    }

    @Override
    @Transactional
    public FortalecimientoCapacidades guardar(FortalecimientoCapacidades dominio) throws Exception {
        try {
            MovEventoFcEntity entidad = mapper.toEntity(dominio);

            // Asegurar integridad de IDs hijos
            if (entidad.getId() != null) {
                String id = entidad.getId();
                if (entidad.getParticipantes() != null) entidad.getParticipantes().forEach(d -> d.setEventoId(id));
                if (entidad.getTareas() != null) entidad.getTareas().forEach(d -> d.setEventoId(id));
            }

            MovEventoFcEntity guardado = repository.save(entidad);
            FortalecimientoCapacidades res = mapper.toDomain(guardado);

            // ✅ Lógica Inline: Enriquecer con nombre para devolver al front
            if (res.getDistritoJudicialId() != null) {
                repoDistrito.findById(res.getDistritoJudicialId())
                        .ifPresent(d -> res.setDistritoJudicialNombre(d.getNombre()));
            }

            return res;

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

            // Mapper actualiza campos simples
            mapper.updateEntityFromDomain(dominio, entidadDb);

            // Actualizar Listas (Limpia e inserta - Orphan Removal manual)
            if (entidadDb.getParticipantes() != null) entidadDb.getParticipantes().clear();
            if (dominio.getParticipantes() != null) {
                dominio.getParticipantes().forEach(p -> {
                    var entityPart = mapper.toEntityPart(p);
                    entityPart.setEventoId(entidadDb.getId());
                    entidadDb.getParticipantes().add(entityPart);
                });
            }

            if (entidadDb.getTareas() != null) entidadDb.getTareas().clear();
            if (dominio.getTareas() != null) {
                dominio.getTareas().forEach(t -> {
                    var entityTarea = mapper.toEntityTarea(t);
                    entityTarea.setEventoId(entidadDb.getId());
                    entidadDb.getTareas().add(entityTarea);
                });
            }

            MovEventoFcEntity guardado = repository.save(entidadDb);
            FortalecimientoCapacidades res = mapper.toDomain(guardado);

            // ✅ Lógica Inline: Enriquecer con nombre
            if (res.getDistritoJudicialId() != null) {
                repoDistrito.findById(res.getDistritoJudicialId())
                        .ifPresent(d -> res.setDistritoJudicialNombre(d.getNombre()));
            }

            return res;

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

        // ✅ Lógica Inline: Enriquecer con nombre
        if (dominio.getDistritoJudicialId() != null) {
            repoDistrito.findById(dominio.getDistritoJudicialId())
                    .ifPresent(d -> dominio.setDistritoJudicialNombre(d.getNombre()));
        }

        // Archivos
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
}