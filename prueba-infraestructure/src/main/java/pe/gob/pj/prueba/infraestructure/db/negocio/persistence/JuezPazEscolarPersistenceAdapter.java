package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.JuezPazEscolar;
import pe.gob.pj.prueba.domain.port.persistence.negocio.JuezPazEscolarPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MaeJuezPazEscolarEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivosEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MaeJuezPazEscolarRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovArchivosRepository;
import pe.gob.pj.prueba.infraestructure.mappers.JuezPazEscolarMapper;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JuezPazEscolarPersistenceAdapter implements JuezPazEscolarPersistencePort {

    private final MaeJuezPazEscolarRepository repository;
    private final MovArchivosRepository repoArchivos;
    private final JuezPazEscolarMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Pagina<JuezPazEscolar> listar(JuezPazEscolar filtros, int pagina, int tamanio) {
        if (filtros == null) filtros = JuezPazEscolar.builder().build();

        var result = repository.listar(
                filtros.getSearch(),
                filtros.getDistritoJudicialId(),
                filtros.getUgelId(),
                filtros.getInstitucionEducativaId(),
                PageRequest.of(pagina - 1, tamanio));

        List<JuezPazEscolar> content = result.getContent().stream()
                .map(r -> JuezPazEscolar.builder()
                        .id(r.getId())
                        .dni(r.getDni())
                        .nombres(r.getNombres())
                        .apePaterno(r.getApePaterno())
                        .apeMaterno(r.getApeMaterno())
                        .cargo(r.getCargo())
                        .distritoJudicialNombre(r.getCorteNombre())
                        .ugelNombre(r.getUgelNombre())
                        .institucionEducativaNombre(r.getColegioNombre())
                        .build())
                .collect(Collectors.toList());

        return Pagina.<JuezPazEscolar>builder()
                .contenido(content)
                .totalRegistros(result.getTotalElements())
                .totalPaginas(result.getTotalPages())
                .paginaActual(pagina)
                .tamanioPagina(tamanio)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public JuezPazEscolar buscarPorId(String id) throws Exception {
        MaeJuezPazEscolarEntity entity = repository.findById(id).orElse(null);
        if (entity == null) return null;

        JuezPazEscolar domain = mapper.toDomain(entity);

        // Enriquecer Archivos
        List<MovArchivosEntity> archivos = repoArchivos.findByNumeroIdentificacion(id);
        if (!archivos.isEmpty()) {
            domain.setArchivosGuardados(archivos.stream()
                    .map(a -> Archivo.builder()
                            .nombre(a.getNombre()).tipo(a.getTipo())
                            .ruta(a.getRuta()).numeroIdentificacion(a.getNumeroIdentificacion())
                            .build())
                    .collect(Collectors.toList()));
        }
        return domain;
    }

    @Override
    @Transactional
    public JuezPazEscolar guardar(JuezPazEscolar domain) throws Exception {
        MaeJuezPazEscolarEntity entity = mapper.toEntity(domain);
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    @Transactional
    public JuezPazEscolar actualizar(JuezPazEscolar domain) throws Exception {
        MaeJuezPazEscolarEntity db = repository.findById(domain.getId())
                .orElseThrow(() -> new Exception("No existe"));

        mapper.updateEntityFromDomain(domain, db);
        return mapper.toDomain(repository.save(db));
    }

    @Override
    public boolean existeDniEnColegio(String dni, String colegioId) {
        return repository.existsByDniAndInstitucionEducativaIdAndActivo(dni, colegioId, "1");
    }
}