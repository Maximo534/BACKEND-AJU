package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante;
import pe.gob.pj.prueba.domain.port.persistence.negocio.JusticiaItinerantePersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivosEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovJusticiaItineranteEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.*;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.MaeDistritoJudicialRepository;
import pe.gob.pj.prueba.infraestructure.mappers.JusticiaItineranteMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JusticiaItinerantePersistenceAdapter implements JusticiaItinerantePersistencePort {

    private final MovJusticiaItineranteRepository repository;
    private final MovArchivosRepository repoArchivos;
    private final MaeDistritoJudicialRepository repoDistrito; // ✅ Repo Maestro
    private final JusticiaItineranteMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Pagina<JusticiaItinerante> listar(String usuario, JusticiaItinerante filtros, int pagina, int tamanio) throws Exception {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio);
        if (filtros == null) filtros = JusticiaItinerante.builder().build();

        String search = (filtros != null) ? filtros.getSearch() : null;
        String distrito = (filtros != null) ? filtros.getDistritoJudicialId() : null;
        LocalDate fIni = (filtros != null) ? filtros.getFechaInicio() : null;
        LocalDate fFin = (filtros != null) ? filtros.getFechaFin() : null;

        // La query nativa ya trae el nombre del distrito (JOIN), no hace falta buscarlo uno por uno.
        var result = repository.listar(usuario, search, distrito, fIni, fFin, pageable);

        List<JusticiaItinerante> contenido = result.getContent().stream()
                .map(p -> JusticiaItinerante.builder()
                        .id(p.getId())
                        .fechaInicio(p.getFechaInicio())
                        .fechaFin(p.getFechaFin())
                        .fechaRegistro(p.getFechaRegistro())
                        .lugarActividad(p.getLugar())
                        .publicoObjetivo(p.getPublicoObjetivo())
                        .activo(p.getEstado())
                        .distritoJudicialNombre(p.getDistritoJudicialNombre()) // Viene directo de la BD
                        .build())
                .collect(Collectors.toList());

        return Pagina.<JusticiaItinerante>builder()
                .contenido(contenido)
                .totalRegistros(result.getTotalElements())
                .totalPaginas(result.getTotalPages())
                .paginaActual(pagina)
                .tamanioPagina(tamanio)
                .build();
    }

    @Override
    @Transactional
    public JusticiaItinerante guardar(JusticiaItinerante dominio) throws Exception {
        try {
            MovJusticiaItineranteEntity entidad = mapper.toEntity(dominio);

            // Asegurar integridad de hijos (IDs)
            if (entidad.getId() != null) {
                String id = entidad.getId();
                if (entidad.getPersonasAtendidas() != null) entidad.getPersonasAtendidas().forEach(d -> d.setJusticiaItineranteId(id));
                if (entidad.getCasosAtendidos() != null) entidad.getCasosAtendidos().forEach(d -> d.setJusticiaItineranteId(id));
                if (entidad.getPersonasBeneficiadas() != null) entidad.getPersonasBeneficiadas().forEach(d -> d.setJusticiaItineranteId(id));
                if (entidad.getTareasRealizadas() != null) entidad.getTareasRealizadas().forEach(d -> d.setJusticiaItineranteId(id));
            }

            MovJusticiaItineranteEntity entidadGuardada = repository.save(entidad);
            JusticiaItinerante resultado = mapper.toDomain(entidadGuardada);

            // ✅ Lógica Inline: Enriquecer con nombre para devolver al front
            if (resultado.getDistritoJudicialId() != null) {
                repoDistrito.findById(resultado.getDistritoJudicialId())
                        .ifPresent(d -> resultado.setDistritoJudicialNombre(d.getNombre()));
            }

            return resultado;

        } catch (Exception e) {
            log.error("Error al guardar Justicia Itinerante en BD", e);
            throw new Exception("Error al guardar en base de datos: " + e.getMessage());
        }
    }

    @Override
    public String obtenerUltimoId() throws Exception {
        return repository.obtenerUltimoId();
    }

    @Override
    @Transactional(readOnly = true)
    public JusticiaItinerante obtenerPorId(String id) throws Exception {
        MovJusticiaItineranteEntity entidad = repository.findById(id)
                .orElseThrow(() -> new Exception("No se encontró el evento con ID: " + id));

        JusticiaItinerante dominio = mapper.toDomain(entidad);

        // ✅ Lógica Inline: Enriquecer con nombre del distrito
        if (dominio.getDistritoJudicialId() != null) {
            repoDistrito.findById(dominio.getDistritoJudicialId())
                    .ifPresent(d -> dominio.setDistritoJudicialNombre(d.getNombre()));
        }

        // Archivos
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

        return dominio;
    }

    @Override
    @Transactional
    public JusticiaItinerante actualizar(JusticiaItinerante dominio) throws Exception {
        try {
            if (dominio.getId() == null || dominio.getId().isBlank()) {
                throw new Exception("El ID es obligatorio para actualizar.");
            }

            MovJusticiaItineranteEntity entidadDb = repository.findById(dominio.getId())
                    .orElseThrow(() -> new Exception("No existe el evento FJI con ID: " + dominio.getId()));

            // Actualizar campos simples
            mapper.updateEntityFromDomain(dominio, entidadDb);

            // Actualización de listas hijas (Lógica OrphanRemoval manual)
            // A. Personas Atendidas
            if (entidadDb.getPersonasAtendidas() != null) entidadDb.getPersonasAtendidas().clear();
            if (dominio.getPersonasAtendidas() != null) {
                dominio.getPersonasAtendidas().forEach(d -> {
                    var entity = mapper.toEntityPA(d);
                    entity.setJusticiaItineranteId(entidadDb.getId());
                    entidadDb.getPersonasAtendidas().add(entity);
                });
            }
            // B. Casos Atendidos
            if (entidadDb.getCasosAtendidos() != null) entidadDb.getCasosAtendidos().clear();
            if (dominio.getCasosAtendidos() != null) {
                dominio.getCasosAtendidos().forEach(d -> {
                    var entity = mapper.toEntityPCA(d);
                    entity.setJusticiaItineranteId(entidadDb.getId());
                    entidadDb.getCasosAtendidos().add(entity);
                });
            }
            // C. Personas Beneficiadas
            if (entidadDb.getPersonasBeneficiadas() != null) entidadDb.getPersonasBeneficiadas().clear();
            if (dominio.getPersonasBeneficiadas() != null) {
                dominio.getPersonasBeneficiadas().forEach(d -> {
                    var entity = mapper.toEntityPB(d);
                    entity.setJusticiaItineranteId(entidadDb.getId());
                    entidadDb.getPersonasBeneficiadas().add(entity);
                });
            }
            // D. Tareas Realizadas
            if (entidadDb.getTareasRealizadas() != null) entidadDb.getTareasRealizadas().clear();
            if (dominio.getTareasRealizadas() != null) {
                dominio.getTareasRealizadas().forEach(d -> {
                    var entity = mapper.toEntityTR(d);
                    entity.setJusticiaItineranteId(entidadDb.getId());
                    entidadDb.getTareasRealizadas().add(entity);
                });
            }

            MovJusticiaItineranteEntity actualizado = repository.save(entidadDb);
            JusticiaItinerante resultado = mapper.toDomain(actualizado);

            // ✅ Lógica Inline: Enriquecer con nombre para devolver al front
            if (resultado.getDistritoJudicialId() != null) {
                repoDistrito.findById(resultado.getDistritoJudicialId())
                        .ifPresent(d -> resultado.setDistritoJudicialNombre(d.getNombre()));
            }

            return resultado;

        } catch (Exception e) {
            log.error("Error al actualizar JI en BD", e);
            throw new Exception("Error al actualizar: " + e.getMessage());
        }
    }
}