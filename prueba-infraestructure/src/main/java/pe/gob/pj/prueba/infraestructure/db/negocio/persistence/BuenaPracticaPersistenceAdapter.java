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
import pe.gob.pj.prueba.domain.model.negocio.BuenaPractica;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarBuenaPracticaQuery;
import pe.gob.pj.prueba.domain.port.persistence.negocio.BuenaPracticaPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivoEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovBuenaPracticaEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovArchivosRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovBuenaPracticaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.MaeDistritoJudicialRepository;
import pe.gob.pj.prueba.infraestructure.mappers.BuenaPracticaMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BuenaPracticaPersistenceAdapter implements BuenaPracticaPersistencePort {

    MovBuenaPracticaRepository repository;
    MaeDistritoJudicialRepository repoDistrito;
    MovArchivosRepository repoArchivos;
    BuenaPracticaMapper mapper;

    @Override
    public Pagina<BuenaPractica> listar(String cuo, ListarBuenaPracticaQuery query, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio);

        var pageResult = repository.listarCompleto(
                query.getSearch(),
                query.getDistritoJudicialId(),
                query.getFechaInicio(),
                query.getFechaFin(),
                pageable
        );

        List<BuenaPractica> contenido = pageResult.getContent().stream()
                .map(entity -> {
                    BuenaPractica dominio = mapper.toDomain(entity);
                    return dominio;
                })
                .collect(Collectors.toList());

        return Pagina.<BuenaPractica>builder()
                .contenido(contenido)
                .totalRegistros(pageResult.getTotalElements())
                .totalPaginas(pageResult.getTotalPages())
                .paginaActual(pagina)
                .tamanioPagina(tamanio)
                .build();
    }

    @Override
    @Transactional
    public BuenaPractica guardar(String cuo, BuenaPractica dominio) {
        log.info("[{}] Guardando Buena Práctica: {}", cuo, dominio.getCodigo());
        MovBuenaPracticaEntity entity = mapper.toEntity(dominio);
        MovBuenaPracticaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public BuenaPractica actualizar(String cuo, BuenaPractica dominio) {
        log.info("[{}] Actualizando Buena Práctica ID: {}", cuo, dominio.getId());

        MovBuenaPracticaEntity entityDb = repository.findById(dominio.getId())
                .orElseThrow(() -> new MovimientoNoEncontradoException("No se encontró el registro con ID: " + dominio.getId()));

        mapper.updateEntityFromDomain(dominio, entityDb);


        MovBuenaPracticaEntity saved = repository.save(entityDb);
        return mapper.toDomain(saved);
    }

    @Override
    public BuenaPractica obtenerPorId(String cuo, Long id) {
        BuenaPractica dominio = repository.findById(id)
                .map(mapper::toDomain)
                .orElse(null);

        if (dominio != null) {
            cargarArchivosEnDominio(dominio);
        }
        return dominio;
    }

    @Override
    public String obtenerUltimoCodigo(String cuo, Long distritoId, String anio) {
        return repository.obtenerUltimoCodigo(distritoId, "-" + anio + "-BP");
    }

    // --- PRIVADOS ---

    private void cargarArchivosEnDominio(BuenaPractica dominio) {
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

    @Override
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        // La consulta JPQL devuelve Object[]: row[0] = distritoJudicialId (Long), row[1] = count (Long)
        List<Object[]> rawData = repository.obtenerEstadisticasPorCorte();
        List<ResumenEstadistico> lista = new ArrayList<>();

        for (Object[] row : rawData) {
            Long distritoId = (Long) row[0];
            Long cantidad = (Long) row[1];

            // Buscamos el nombre de la corte en el maestro
            String nombreCorte = repoDistrito.findById(distritoId)
                    .map(d -> d.getNombreCorto())
                    .orElse("Corte " + distritoId);

            lista.add(ResumenEstadistico.builder()
                    .etiqueta(nombreCorte)
                    .cantidad(cantidad)
                    .build());
        }
        return lista;
    }

}


