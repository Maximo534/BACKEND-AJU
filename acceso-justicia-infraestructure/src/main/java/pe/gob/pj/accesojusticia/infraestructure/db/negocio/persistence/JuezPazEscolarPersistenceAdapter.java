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
import pe.gob.pj.accesojusticia.domain.model.negocio.JuezPazEscolar;
import pe.gob.pj.accesojusticia.domain.model.negocio.query.ListarJuezEscolarQuery;
import pe.gob.pj.accesojusticia.domain.port.persistence.negocio.JuezPazEscolarPersistencePort;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MaeJuezPazEscolarEntity;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MovArchivoEntity;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.MaeJuezPazEscolarRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.MovArchivosRepository;
import pe.gob.pj.accesojusticia.infraestructure.mappers.JuezPazEscolarMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JuezPazEscolarPersistenceAdapter implements JuezPazEscolarPersistencePort {

    MaeJuezPazEscolarRepository repository;
    MovArchivosRepository repoArchivos;
    JuezPazEscolarMapper mapper;

    @Override
    public Pagina<JuezPazEscolar> listar(String cuo, ListarJuezEscolarQuery query, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio);

        var pageResult = repository.listarCompleto(
                query.getSearch(),
                query.getDistritoJudicialId(),
                query.getUgelId(),
                query.getInstitucionEducativaId(),
                pageable
        );

        List<JuezPazEscolar> contenido = pageResult.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        return Pagina.<JuezPazEscolar>builder()
                .contenido(contenido)
                .totalRegistros(pageResult.getTotalElements())
                .totalPaginas(pageResult.getTotalPages())
                .paginaActual(pagina)
                .tamanioPagina(tamanio)
                .build();
    }

    @Override
    @Transactional
    public JuezPazEscolar guardar(String cuo, JuezPazEscolar dominio) {
        log.info("[{}] Guardando Juez Paz Escolar: {}", cuo, dominio.getCodigo());
        MaeJuezPazEscolarEntity entity = mapper.toEntity(dominio);
        MaeJuezPazEscolarEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public JuezPazEscolar actualizar(String cuo, JuezPazEscolar dominio) {
        log.info("[{}] Actualizando Juez Paz Escolar ID: {}", cuo, dominio.getId());

        MaeJuezPazEscolarEntity entityDb = repository.findById(dominio.getId())
                .orElseThrow(() -> new MovimientoNoEncontradoException("No se encontró el registro con ID: " + dominio.getId()));

        mapper.updateEntityFromDomain(dominio, entityDb);

        MaeJuezPazEscolarEntity saved = repository.save(entityDb);
        return mapper.toDomain(saved);
    }

    @Override
    public JuezPazEscolar obtenerPorId(String cuo, Long id) {
        JuezPazEscolar dominio = repository.findById(id)
                .map(mapper::toDomain)
                .orElse(null);

        if (dominio != null) {
            cargarArchivosEnDominio(dominio);
        }
        return dominio;
    }

    @Override
    public boolean existeDniEnColegio(String dni, Long colegioId) {
        return repository.existsByDniAndInstitucionEducativaIdAndActivo(dni, colegioId, "1");
    }

    @Override
    public String obtenerUltimoCodigo(String cuo, String anio) {
        return repository.obtenerUltimoCodigo("-" + anio + "-JE");
    }

    // --- PRIVADOS ---

    private void cargarArchivosEnDominio(JuezPazEscolar dominio) {
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