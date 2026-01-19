package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.OrientadoraJudicial;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.persistence.negocio.OrientadoraJudicialPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivosEntity;
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
public class OrientadoraJudicialPersistenceAdapter implements OrientadoraJudicialPersistencePort {

    private final MovOrientadoraJudicialRepository repository;
    private final MovArchivosRepository repoArchivos;
    private final MaeDistritoJudicialRepository repoDistrito;
    private final OrientadoraJudicialMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Pagina<OrientadoraJudicial> listar(String usuario, OrientadoraJudicial filtros, int pagina, int tamanio) throws Exception {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio);
        if (filtros == null) filtros = OrientadoraJudicial.builder().build();


        var result = repository.listar(usuario, filtros.getSearch(), filtros.getDistritoJudicialId(), filtros.getFechaAtencion(), null, pageable);

        List<OrientadoraJudicial> contenido = result.getContent().stream()
                .map(p -> OrientadoraJudicial.builder()
                        .id(p.getId())
                        .fechaAtencion(p.getFechaAtencion())
                        .nombreCompleto(p.getNombrePersona())
                        .numeroExpediente(p.getNumeroExpediente())
                        .distritoJudicialId(p.getDistritoJudicialId())
                        .distritoJudicialNombre(p.getDistritoJudicialNombre()) // Viene del LEFT JOIN
                        .build())
                .collect(Collectors.toList());

        return Pagina.<OrientadoraJudicial>builder()
                .contenido(contenido)
                .totalRegistros(result.getTotalElements())
                .totalPaginas(result.getTotalPages())
                .paginaActual(pagina)
                .tamanioPagina(tamanio)
                .build();
    }

    @Override
    @Transactional
    public OrientadoraJudicial guardar(OrientadoraJudicial dominio) throws Exception {
        MovOrientadoraJudicialEntity entity = mapper.toEntity(dominio);
        MovOrientadoraJudicialEntity saved = repository.save(entity);
        OrientadoraJudicial res = mapper.toDomain(saved);

        // ✅ ENRIQUECIMIENTO INLINE
        if (res.getDistritoJudicialId() != null) {
            repoDistrito.findById(res.getDistritoJudicialId())
                    .ifPresent(c -> res.setDistritoJudicialNombre(c.getNombreCorto()));
        }
        return res;
    }

    @Override
    @Transactional
    public OrientadoraJudicial actualizar(OrientadoraJudicial dominio) throws Exception {
        MovOrientadoraJudicialEntity dbEntity = repository.findById(dominio.getId())
                .orElseThrow(() -> new Exception("No existe registro OJ con ID: " + dominio.getId()));

        mapper.updateEntityFromDomain(dominio, dbEntity);

        MovOrientadoraJudicialEntity saved = repository.save(dbEntity);
        OrientadoraJudicial res = mapper.toDomain(saved);

        if (res.getDistritoJudicialId() != null) {
            repoDistrito.findById(res.getDistritoJudicialId())
                    .ifPresent(c -> res.setDistritoJudicialNombre(c.getNombreCorto()));
        }
        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public OrientadoraJudicial buscarPorId(String id) throws Exception {
        MovOrientadoraJudicialEntity entity = repository.findById(id).orElse(null);
        if (entity == null) return null;

        OrientadoraJudicial dominio = mapper.toDomain(entity);
        if (dominio.getDistritoJudicialId() != null) {
            repoDistrito.findById(dominio.getDistritoJudicialId())
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

    @Override
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        List<Object[]> rawData = repository.obtenerEstadisticasHistoricas();
        List<ResumenEstadistico> lista = new ArrayList<>();

        for (Object[] row : rawData) {
            String distritoId = (String) row[0];
            Long cantidad = (Long) row[1];

            String nombreCorte = repoDistrito.findById(distritoId)
                    .map(d -> d.getNombre()).orElse("Corte " + distritoId);

            lista.add(ResumenEstadistico.builder()
                    .etiqueta(nombreCorte).cantidad(cantidad).build());
        }
        return lista;
    }
}