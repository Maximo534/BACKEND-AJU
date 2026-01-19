package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.LlapanchikpaqJusticia;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.persistence.negocio.LlapanchikpaqPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivosEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovLlapanchikpaqJusticia;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovArchivosRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovLlapanchikpaqJusticiaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.MaeDistritoJudicialRepository;
import pe.gob.pj.prueba.infraestructure.mappers.LlapanchikpaqMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LlapanchikpaqPersistenceAdapter implements LlapanchikpaqPersistencePort {

    private final MovLlapanchikpaqJusticiaRepository repository;
    private final LlapanchikpaqMapper mapper;
    private final MovArchivosRepository repoArchivos;
    private final MaeDistritoJudicialRepository repoCorte; //Inyectado para buscar nombres

    @Override
    @Transactional(readOnly = true)
    public Pagina<LlapanchikpaqJusticia> listar(String usuario, LlapanchikpaqJusticia filtros, int pagina, int tamanio) throws Exception {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio);
        if (filtros == null) filtros = LlapanchikpaqJusticia.builder().build();

        String search = filtros.getSearch();
        String distrito = filtros.getDistritoJudicialId();
        LocalDate fIni = filtros.getFechaInicio();
        LocalDate fFin = filtros.getFechaFin();

        // La Query Nativa ya trae el nombre del distrito (JOIN), no hace falta buscarlo manual.
        var result = repository.listar(usuario, search, distrito, fIni, fFin, pageable);

        List<LlapanchikpaqJusticia> contenido = result.getContent().stream()
                .map(p -> LlapanchikpaqJusticia.builder()
                        .id(p.getId())
                        .fechaInicio(p.getFechaInicio())
                        .activo(p.getEstado())
                        .distritoJudicialNombre(p.getDistritoJudicialNombre()) // Viene de la BD
                        .build())
                .collect(Collectors.toList());

        return Pagina.<LlapanchikpaqJusticia>builder()
                .contenido(contenido)
                .totalRegistros(result.getTotalElements())
                .totalPaginas(result.getTotalPages())
                .paginaActual(pagina)
                .tamanioPagina(tamanio)
                .build();
    }

    @Override
    @Transactional
    public LlapanchikpaqJusticia guardar(LlapanchikpaqJusticia dominio) throws Exception {
        MovLlapanchikpaqJusticia entity = mapper.toEntity(dominio);

        // Asignar ID padre a hijos
        if (entity.getId() != null) {
            String idPadre = entity.getId();
            if (entity.getBeneficiadas() != null) entity.getBeneficiadas().forEach(h -> h.setLljId(idPadre));
            if (entity.getAtendidas() != null) entity.getAtendidas().forEach(h -> h.setLljId(idPadre));
            if (entity.getCasos() != null) entity.getCasos().forEach(h -> h.setLljId(idPadre));
            if (entity.getTareas() != null) entity.getTareas().forEach(h -> h.setLljId(idPadre));
        }

        MovLlapanchikpaqJusticia saved = repository.save(entity);
        LlapanchikpaqJusticia res = mapper.toDomain(saved);

        // ✅ Enriquecer nombre inline
        if (res.getDistritoJudicialId() != null) {
            repoCorte.findById(res.getDistritoJudicialId())
                    .ifPresent(d -> res.setDistritoJudicialNombre(d.getNombre()));
        }

        return res;
    }

    @Override
    public String obtenerUltimoId() throws Exception {
        return repository.obtenerUltimoId();
    }

    @Override
    @Transactional
    public LlapanchikpaqJusticia actualizar(LlapanchikpaqJusticia dominio) throws Exception {
        // Buscar
        MovLlapanchikpaqJusticia entityDb = repository.findById(dominio.getId())
                .orElseThrow(() -> new Exception("No encontrado ID: " + dominio.getId()));

        // Actualizar campos simples
        mapper.updateEntityFromDomain(dominio, entityDb);

        // Actualizar Listas (Orphan Removal manual: limpiar y agregar)
        if (entityDb.getBeneficiadas() != null) entityDb.getBeneficiadas().clear();
        if (dominio.getBeneficiadas() != null) {
            dominio.getBeneficiadas().forEach(d -> {
                var e = mapper.mapBeneficiada(d);
                e.setLljId(entityDb.getId());
                entityDb.getBeneficiadas().add(e);
            });
        }

        if (entityDb.getAtendidas() != null) entityDb.getAtendidas().clear();
        if (dominio.getAtendidas() != null) {
            dominio.getAtendidas().forEach(d -> {
                var e = mapper.mapAtendida(d);
                e.setLljId(entityDb.getId());
                entityDb.getAtendidas().add(e);
            });
        }

        if (entityDb.getCasos() != null) entityDb.getCasos().clear();
        if (dominio.getCasos() != null) {
            dominio.getCasos().forEach(d -> {
                var e = mapper.mapCaso(d);
                e.setLljId(entityDb.getId());
                entityDb.getCasos().add(e);
            });
        }

        if (entityDb.getTareas() != null) entityDb.getTareas().clear();
        if (dominio.getTareas() != null) {
            dominio.getTareas().forEach(d -> {
                var e = mapper.mapTarea(d);
                e.setLljId(entityDb.getId());
                entityDb.getTareas().add(e);
            });
        }

        MovLlapanchikpaqJusticia saved = repository.save(entityDb);
        LlapanchikpaqJusticia res = mapper.toDomain(saved);

        // ✅ Enriquecer nombre inline
        if (res.getDistritoJudicialId() != null) {
            repoCorte.findById(res.getDistritoJudicialId())
                    .ifPresent(d -> res.setDistritoJudicialNombre(d.getNombre()));
        }

        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public LlapanchikpaqJusticia buscarPorId(String id) throws Exception {
        MovLlapanchikpaqJusticia entity = repository.findById(id).orElse(null);
        if (entity == null) return null;

        LlapanchikpaqJusticia dominio = mapper.toDomain(entity);

        // ✅ Enriquecer nombre inline
        if (dominio.getDistritoJudicialId() != null) {
            repoCorte.findById(dominio.getDistritoJudicialId())
                    .ifPresent(d -> dominio.setDistritoJudicialNombre(d.getNombre()));
        }

        // Archivos
        List<MovArchivosEntity> archivos = repoArchivos.findByNumeroIdentificacion(id);
        if(archivos != null && !archivos.isEmpty()) {
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
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        List<Object[]> data = repository.obtenerEstadisticasPorCorte();
        List<ResumenEstadistico> lista = new ArrayList<>();

        for(Object[] row : data) {
            String idCorte = (String) row[0];
            Long cant = (Long) row[1];
            String nombre = repoCorte.findById(idCorte).map(c -> c.getNombre()).orElse(idCorte);
            lista.add(ResumenEstadistico.builder().etiqueta(nombre).cantidad(cant).build());
        }
        return lista;
    }
}