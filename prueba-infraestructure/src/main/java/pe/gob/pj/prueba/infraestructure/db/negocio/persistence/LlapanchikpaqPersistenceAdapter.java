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
import pe.gob.pj.prueba.domain.model.negocio.LlapanchikpaqJusticia;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarLlapanchikpaqQuery;
import pe.gob.pj.prueba.domain.port.persistence.negocio.LlapanchikpaqPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivoEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovLlapanchikpaqJusticiaEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovArchivosRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovLlapanchikpaqJusticiaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.MaeDistritoJudicialRepository;
import pe.gob.pj.prueba.infraestructure.mappers.LlapanchikpaqMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LlapanchikpaqPersistenceAdapter implements LlapanchikpaqPersistencePort {

    MovLlapanchikpaqJusticiaRepository repository;
    MaeDistritoJudicialRepository repoDistrito;
    MovArchivosRepository repoArchivos;
    LlapanchikpaqMapper mapper;

    @Override
    public Pagina<LlapanchikpaqJusticia> listar(String cuo, ListarLlapanchikpaqQuery query, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio);

        var pageResult = repository.listar(
                query.getSearch(),
                query.getDistritoJudicialId(),
                query.getFechaInicio(),
                query.getFechaFin(),
                pageable
        );

        List<LlapanchikpaqJusticia> contenido = pageResult.getContent().stream()
                .map(entity -> {
                    LlapanchikpaqJusticia dominio = mapper.toDomain(entity);
                    return dominio;
                })
                .collect(Collectors.toList());

        return Pagina.<LlapanchikpaqJusticia>builder()
                .contenido(contenido)
                .totalRegistros(pageResult.getTotalElements())
                .totalPaginas(pageResult.getTotalPages())
                .paginaActual(pagina)
                .tamanioPagina(tamanio)
                .build();
    }

    @Override
    @Transactional
    public LlapanchikpaqJusticia guardar(String cuo, LlapanchikpaqJusticia dominio) {
        log.info("[{}] Guardando Llapanchikpaq: {}", cuo, dominio.getCodigo());
        MovLlapanchikpaqJusticiaEntity entity = mapper.toEntity(dominio);
        MovLlapanchikpaqJusticiaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public LlapanchikpaqJusticia actualizar(String cuo, LlapanchikpaqJusticia dominio) {
        log.info("[{}] Actualizando Llapanchikpaq ID: {}", cuo, dominio.getId());

        MovLlapanchikpaqJusticiaEntity entityDb = repository.findById(dominio.getId())
                .orElseThrow(() -> new MovimientoNoEncontradoException("No se encontró el registro con ID: " + dominio.getId()));

        mapper.updateEntityFromDomain(dominio, entityDb);

        actualizarHijos(entityDb, dominio);

        MovLlapanchikpaqJusticiaEntity saved = repository.save(entityDb);
        return mapper.toDomain(saved);
    }

    @Override
    public LlapanchikpaqJusticia obtenerPorId(String cuo, Long id) {
        LlapanchikpaqJusticia dominio = repository.findById(id)
                .map(mapper::toDomain)
                .orElse(null);

        if (dominio != null) {
            cargarArchivosEnDominio(dominio);
        }
        return dominio;
    }

    @Override
    public String obtenerUltimoCodigo(String cuo, Long distritoId, String anio) {
        return repository.obtenerUltimoCodigo(distritoId, "-" + anio + "-LL");
    }

    // --- PRIVADOS ---

    private void cargarArchivosEnDominio(LlapanchikpaqJusticia dominio) {
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

    private void actualizarHijos(MovLlapanchikpaqJusticiaEntity entityDb, LlapanchikpaqJusticia dominio) {
        // 1. Beneficiadas
        if (entityDb.getBeneficiadas() != null) entityDb.getBeneficiadas().clear();
        if (dominio.getPersonasBeneficiadas() != null) {
            dominio.getPersonasBeneficiadas().forEach(d -> {
                var child = mapper.toEntityPB(d);
                child.setLljId(entityDb.getId());
                entityDb.getBeneficiadas().add(child);
            });
        }
        // 2. Atendidas
        if (entityDb.getAtendidas() != null) entityDb.getAtendidas().clear();
        if (dominio.getPersonasAtendidas() != null) {
            dominio.getPersonasAtendidas().forEach(d -> {
                var child = mapper.toEntityPA(d);
                child.setLljId(entityDb.getId());
                entityDb.getAtendidas().add(child);
            });
        }
        // 3. Casos
        if (entityDb.getCasos() != null) entityDb.getCasos().clear();
        if (dominio.getCasosAtendidos() != null) {
            dominio.getCasosAtendidos().forEach(d -> {
                var child = mapper.toEntityCA(d);
                child.setLljId(entityDb.getId());
                entityDb.getCasos().add(child);
            });
        }
        // 4. Tareas
        if (entityDb.getTareas() != null) entityDb.getTareas().clear();
        if (dominio.getTareasRealizadas() != null) {
            dominio.getTareasRealizadas().forEach(d -> {
                var child = mapper.toEntityTR(d);
                child.setLljId(entityDb.getId());
                entityDb.getTareas().add(child);
            });
        }
    }

    @Override
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        List<Object[]> data = repository.obtenerEstadisticasPorCorte();
        List<ResumenEstadistico> lista = new ArrayList<>();

        for(Object[] row : data) {
            Long idCorte = (Long) row[0];
            Long cant = (Long) row[1];

            String nombreCorte = repoDistrito.findById(idCorte)
                    .map(c -> c.getNombreCorto())
                    .orElse("Corte " + idCorte);

            lista.add(ResumenEstadistico.builder()
                    .etiqueta(nombreCorte)
                    .cantidad(cant)
                    .build());
        }
        return lista;
    }

}