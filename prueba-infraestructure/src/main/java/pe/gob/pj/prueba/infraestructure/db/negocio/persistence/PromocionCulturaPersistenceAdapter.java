package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.PromocionCultura;
import pe.gob.pj.prueba.domain.port.persistence.negocio.PromocionCulturaPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivosEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovPromocionCulturaEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovArchivosRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovPromocionCulturaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.MaeDistritoJudicialRepository;
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
    private final MovArchivosRepository repoArchivos;
    private final MaeDistritoJudicialRepository repoCorte; // Solo inyectamos lo necesario para nombres
    private final PromocionCulturaMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Pagina<PromocionCultura> listar(String usuario, PromocionCultura filtros, int pagina, int tamanio) throws Exception {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio);
        if (filtros == null) filtros = PromocionCultura.builder().build();

        // Query nativa ya trae nombre de corte
        var result = repository.listar(usuario, filtros.getSearch(), filtros.getDistritoJudicialId(),
                filtros.getFechaInicio(), filtros.getFechaFin(), pageable);

        List<PromocionCultura> contenido = result.getContent().stream()
                .map(p -> PromocionCultura.builder()
                        .id(p.getId())
                        .fechaInicio(p.getFechaInicio())
                        .fechaFin(p.getFechaFin())
                        .tipoActividad(p.getTipoActividad())
                        .distritoJudicialNombre(p.getDistritoJudicialNombre())
                        .activo(p.getEstado())
                        .build())
                .collect(Collectors.toList());

        return Pagina.<PromocionCultura>builder()
                .contenido(contenido)
                .totalRegistros(result.getTotalElements())
                .totalPaginas(result.getTotalPages())
                .paginaActual(pagina)
                .tamanioPagina(tamanio)
                .build();
    }

    @Override
    @Transactional
    public PromocionCultura guardar(PromocionCultura dominio) throws Exception {
        try {
            MovPromocionCulturaEntity entity = mapper.toEntity(dominio);

            // Asignar ID a hijos antes de guardar (si el ID ya existe, pero en insert es nuevo)
            // Hibernate maneja las FKs si la relación está bien hecha, pero por seguridad:
            if (entity.getId() != null) {
                if (entity.getParticipantes() != null) entity.getParticipantes().forEach(p -> p.setPromocionCulturaId(entity.getId()));
                if (entity.getTareas() != null) entity.getTareas().forEach(t -> t.setPromocionCulturaId(entity.getId()));
            }

            MovPromocionCulturaEntity saved = repository.save(entity);
            PromocionCultura res = mapper.toDomain(saved);

            // ✅ ENRIQUECIMIENTO INLINE
            if (res.getDistritoJudicialId() != null) {
                repoCorte.findById(res.getDistritoJudicialId())
                        .ifPresent(c -> res.setDistritoJudicialNombre(c.getNombreCorto()));
            }

            return res;

        } catch (Exception e) {
            log.error("Error guardando CJ", e);
            throw new Exception("Error BD: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PromocionCultura actualizar(PromocionCultura dominio) throws Exception {
        try {
            MovPromocionCulturaEntity entidadDb = repository.findById(dominio.getId())
                    .orElseThrow(() -> new Exception("Evento no encontrado: " + dominio.getId()));

            // 1. Actualizar campos simples
            mapper.updateEntityFromDomain(dominio, entidadDb);

            // 2. ACTUALIZACIÓN SEGURA DE LISTAS (Flush Strategy)

            // Participantes
            if (entidadDb.getParticipantes() != null) entidadDb.getParticipantes().clear();
            else entidadDb.setParticipantes(new ArrayList<>());

            repository.flush(); // 🔥 Elimina viejos de BD

            if (dominio.getParticipantesPorGenero() != null) {
                dominio.getParticipantesPorGenero().forEach(p -> {
                    var entityPart = mapper.toEntityPart(p);
                    entityPart.setPromocionCulturaId(entidadDb.getId());
                    entidadDb.getParticipantes().add(entityPart);
                });
            }

            // Tareas
            if (entidadDb.getTareas() != null) entidadDb.getTareas().clear();
            else entidadDb.setTareas(new ArrayList<>());

            repository.flush(); // 🔥 Elimina viejos de BD

            if (dominio.getTareasRealizadas() != null) {
                dominio.getTareasRealizadas().forEach(t -> {
                    var entityTarea = mapper.toEntityTarea(t);
                    entityTarea.setPromocionCulturaId(entidadDb.getId());
                    entidadDb.getTareas().add(entityTarea);
                });
            }

            MovPromocionCulturaEntity saved = repository.save(entidadDb);
            PromocionCultura res = mapper.toDomain(saved);

            // ✅ ENRIQUECIMIENTO INLINE
            if (res.getDistritoJudicialId() != null) {
                repoCorte.findById(res.getDistritoJudicialId())
                        .ifPresent(c -> res.setDistritoJudicialNombre(c.getNombreCorto()));
            }

            return res;

        } catch (Exception e) {
            log.error("Error actualizando CJ", e);
            throw new Exception("Error BD: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PromocionCultura obtenerPorId(String id) throws Exception {
        MovPromocionCulturaEntity entidad = repository.findById(id).orElse(null);
        if (entidad == null) return null;

        PromocionCultura dominio = mapper.toDomain(entidad);

        // ✅ ENRIQUECIMIENTO INLINE
        if (dominio.getDistritoJudicialId() != null) {
            repoCorte.findById(dominio.getDistritoJudicialId())
                    .ifPresent(c -> dominio.setDistritoJudicialNombre(c.getNombreCorto()));
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

    @Override
    public String obtenerUltimoId() throws Exception {
        return repository.obtenerUltimoId();
    }
}