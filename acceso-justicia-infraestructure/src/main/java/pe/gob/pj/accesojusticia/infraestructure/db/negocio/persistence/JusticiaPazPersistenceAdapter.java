package pe.gob.pj.accesojusticia.infraestructure.db.negocio.persistence;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.accesojusticia.domain.exceptions.negocio.MovimientoNoEncontradoException;
import pe.gob.pj.accesojusticia.domain.model.common.Pagina;
import pe.gob.pj.accesojusticia.domain.model.negocio.Archivo;
import pe.gob.pj.accesojusticia.domain.model.negocio.JpeCasoAtendido;
import pe.gob.pj.accesojusticia.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.accesojusticia.domain.model.negocio.query.ListarJpeCasosQuery;
import pe.gob.pj.accesojusticia.domain.port.persistence.negocio.JusticiaPazPersistencePort;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MovArchivoEntity;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MovJpeCasoAtendidoEntity;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.MovArchivosRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.MovJpeCasoAtendidoRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.masters.MaeDistritoJudicialRepository;
import pe.gob.pj.accesojusticia.infraestructure.mappers.JusticiaPazMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JusticiaPazPersistenceAdapter implements JusticiaPazPersistencePort {

    MovJpeCasoAtendidoRepository repository;
    MovArchivosRepository repoArchivos;
    MaeDistritoJudicialRepository repoCorte;
    JusticiaPazMapper mapper;

    @Override
    public Pagina<JpeCasoAtendido> listar(String cuo, ListarJpeCasosQuery query, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio);

        var pageResult = repository.listarCompleto(
                query.getSearch(),
                query.getDistritoJudicialId(),
                query.getUgelId(),
                query.getInstitucionEducativaId(),
                query.getFechaRegistro(),
                pageable
        );

        List<JpeCasoAtendido> contenido = pageResult.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        return Pagina.<JpeCasoAtendido>builder()
                .contenido(contenido)
                .totalRegistros(pageResult.getTotalElements())
                .totalPaginas(pageResult.getTotalPages())
                .paginaActual(pagina)
                .tamanioPagina(tamanio)
                .build();
    }

    @Override
    @Transactional
    public JpeCasoAtendido guardar(String cuo, JpeCasoAtendido dominio) {
        log.info("[{}] Guardando Caso JPE: {}", cuo, dominio.getCodigo());
        MovJpeCasoAtendidoEntity entity = mapper.toEntity(dominio);
        MovJpeCasoAtendidoEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public JpeCasoAtendido actualizar(String cuo, JpeCasoAtendido dominio) {
        log.info("[{}] Actualizando Caso JPE ID: {}", cuo, dominio.getId());

        MovJpeCasoAtendidoEntity entityDb = repository.findById(dominio.getId())
                .orElseThrow(() -> new MovimientoNoEncontradoException("No se encontró el caso con ID: " + dominio.getId()));

        mapper.updateEntityFromDomain(dominio, entityDb);

        MovJpeCasoAtendidoEntity saved = repository.save(entityDb);
        return mapper.toDomain(saved);
    }

    @Override
    public JpeCasoAtendido obtenerPorId(String cuo, Long id) {
        JpeCasoAtendido dominio = repository.findById(id)
                .map(mapper::toDomain)
                .orElse(null);

        if (dominio != null) {
            cargarArchivosEnDominio(dominio);
        }
        return dominio;
    }

    @Override
    public String obtenerUltimoCodigo(String cuo, Long distritoId, String anio) {
        return repository.obtenerUltimoCodigo(distritoId, "-" + anio + "-PE");
    }

    // =========================================================================================
    // ESTADÍSTICAS HABILITADAS
    // =========================================================================================

    @Override
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        List<Object[]> data = repository.obtenerEstadisticasPorCorte();
        List<ResumenEstadistico> lista = new ArrayList<>();

        for (Object[] row : data) {
            Long idCorte = (Long) row[0];
            Long cant = (Long) row[1];

            String nombreCorte = repoCorte.findById(idCorte)
                    .map(c -> c.getNombreCorto())
                    .orElse("Corte " + idCorte);

            lista.add(ResumenEstadistico.builder()
                    .etiqueta(nombreCorte)
                    .cantidad(cant)
                    .build());
        }
        return lista;
    }

    // --- PRIVADOS ---

    private void cargarArchivosEnDominio(JpeCasoAtendido dominio) {
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