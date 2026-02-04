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
import pe.gob.pj.prueba.domain.model.negocio.OrientadoraJudicial;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarOrientadoraQuery;
import pe.gob.pj.prueba.domain.port.persistence.negocio.OrientadoraJudicialPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivoEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovOrientadoraJudicialEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovArchivosRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovOrientadoraJudicialRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.MaeDistritoJudicialRepository;
import pe.gob.pj.prueba.infraestructure.mappers.OrientadoraJudicialMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrientadoraJudicialPersistenceAdapter implements OrientadoraJudicialPersistencePort {

    MovOrientadoraJudicialRepository repository;
    MovArchivosRepository repoArchivos;
    MaeDistritoJudicialRepository repoDistrito;
    OrientadoraJudicialMapper mapper;

    @Override
    public Pagina<OrientadoraJudicial> listar(String cuo, ListarOrientadoraQuery query, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio);

        var pageResult = repository.listarCompleto(
                query.getSearch(),
                query.getDistritoJudicialId(),
                query.getFechaInicio(),
                query.getFechaFin(),
                pageable
        );

        List<OrientadoraJudicial> contenido = pageResult.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        return Pagina.<OrientadoraJudicial>builder()
                .contenido(contenido)
                .totalRegistros(pageResult.getTotalElements())
                .totalPaginas(pageResult.getTotalPages())
                .paginaActual(pagina)
                .tamanioPagina(tamanio)
                .build();
    }

    @Override
    @Transactional
    public OrientadoraJudicial guardar(String cuo, OrientadoraJudicial dominio) {
        log.info("[{}] Guardando Orientadora Judicial: {}", cuo, dominio.getCodigo());
        MovOrientadoraJudicialEntity entity = mapper.toEntity(dominio);
        MovOrientadoraJudicialEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public OrientadoraJudicial actualizar(String cuo, OrientadoraJudicial dominio) {
        log.info("[{}] Actualizando Orientadora Judicial ID: {}", cuo, dominio.getId());

        MovOrientadoraJudicialEntity entityDb = repository.findById(dominio.getId())
                .orElseThrow(() -> new MovimientoNoEncontradoException("No se encontró el registro con ID: " + dominio.getId()));

        mapper.updateEntityFromDomain(dominio, entityDb);

        MovOrientadoraJudicialEntity saved = repository.save(entityDb);
        return mapper.toDomain(saved);
    }

    @Override
    public OrientadoraJudicial obtenerPorId(String cuo, Long id) {
        OrientadoraJudicial dominio = repository.findById(id)
                .map(mapper::toDomain)
                .orElse(null);

        if (dominio != null) {
            cargarArchivosEnDominio(dominio);
        }
        return dominio;
    }

    @Override
    public String obtenerUltimoCodigo(String cuo, Long distritoId, String anio) {
        return repository.obtenerUltimoCodigo(distritoId, "-" + anio + "-OJ");
    }


    @Override
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        // Devuelve row[0] = idCorte (Long), row[1] = count (Long)
        List<Object[]> rawData = repository.obtenerEstadisticasPorCorte();
        List<ResumenEstadistico> lista = new ArrayList<>();

        for (Object[] row : rawData) {
            Long distritoId = (Long) row[0];
            Long cantidad = (Long) row[1];

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


    private void cargarArchivosEnDominio(OrientadoraJudicial dominio) {
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
}