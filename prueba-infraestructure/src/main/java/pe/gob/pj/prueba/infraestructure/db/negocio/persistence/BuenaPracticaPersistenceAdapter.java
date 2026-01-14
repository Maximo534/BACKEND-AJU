package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.BuenaPractica;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.persistence.negocio.BuenaPracticaPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivosEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovBuenaPracticaEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovArchivosRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovBuenaPracticaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.MaeDistritoJudicialRepository;
import pe.gob.pj.prueba.infraestructure.mappers.BuenaPracticaMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BuenaPracticaPersistenceAdapter implements BuenaPracticaPersistencePort {

    private final MovBuenaPracticaRepository repository;
    private final BuenaPracticaMapper mapper;
    private final MovArchivosRepository repoArchivos;
    private final MaeDistritoJudicialRepository repoDistrito;

    @Override
    @Transactional(readOnly = true)
    public Pagina<BuenaPractica> listar(String usuario, BuenaPractica filtros, int pagina, int tamanio) throws Exception {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio);
        if (filtros == null) filtros = BuenaPractica.builder().build();

        // El repositorio ya trae el nombre con un JOIN en la Query Nativa, así que aquí no hace falta buscarlo uno por uno.
        var result = repository.listar(usuario, filtros.getSearch(), filtros.getDistritoJudicialId(),
                filtros.getFechaInicio(), filtros.getFechaFin(), pageable);

        List<BuenaPractica> contenido = result.getContent().stream()
                .map(p -> BuenaPractica.builder()
                        .id(p.getId())
                        .distritoJudicialId(p.getDistritoJudicialId())
                        .distritoJudicialNombre(p.getDistritoJudicialNombre())
                        .titulo(p.getTitulo())
                        .fechaInicio(p.getFechaInicio())
                        .build())
                .collect(Collectors.toList());

        return Pagina.<BuenaPractica>builder()
                .contenido(contenido)
                .totalRegistros(result.getTotalElements())
                .totalPaginas(result.getTotalPages())
                .paginaActual(pagina)
                .tamanioPagina(tamanio)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BuenaPractica buscarPorId(String id) throws Exception {
        MovBuenaPracticaEntity entity = repository.findById(id)
                .orElseThrow(() -> new Exception("No se encontró Buena Práctica con ID: " + id));

        BuenaPractica dominio = mapper.toDomain(entity);

        // ✅ Lógica Inline: Enriquecer con Nombre de Distrito Judicial
        if (dominio.getDistritoJudicialId() != null) {
            repoDistrito.findById(dominio.getDistritoJudicialId())
                    .ifPresent(d -> dominio.setDistritoJudicialNombre(d.getNombre()));
        }

        // Enriquecer con archivos
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
    public BuenaPractica guardar(BuenaPractica dominio) throws Exception {
        MovBuenaPracticaEntity entity = mapper.toEntity(dominio);
        MovBuenaPracticaEntity saved = repository.save(entity);

        BuenaPractica result = mapper.toDomain(saved);

        // ✅ Lógica Inline: Enriquecer con Nombre para devolverlo al Front
        if (result.getDistritoJudicialId() != null) {
            repoDistrito.findById(result.getDistritoJudicialId())
                    .ifPresent(d -> result.setDistritoJudicialNombre(d.getNombre()));
        }

        return result;
    }

    @Override
    @Transactional
    public BuenaPractica actualizar(BuenaPractica dominio) throws Exception {
        if (dominio.getId() == null) throw new Exception("ID obligatorio para actualizar");

        // 1. Obtener
        MovBuenaPracticaEntity entity = repository.findById(dominio.getId())
                .orElseThrow(() -> new Exception("No existe BP con ID: " + dominio.getId()));

        // 2. Actualizar campos
        mapper.updateEntityFromDomain(dominio, entity);

        // 3. Guardar
        MovBuenaPracticaEntity saved = repository.save(entity);

        BuenaPractica result = mapper.toDomain(saved);

        // ✅ Lógica Inline: Enriquecer con Nombre para devolverlo al Front
        if (result.getDistritoJudicialId() != null) {
            repoDistrito.findById(result.getDistritoJudicialId())
                    .ifPresent(d -> result.setDistritoJudicialNombre(d.getNombre()));
        }

        return result;
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