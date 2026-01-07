package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.BuenaPractica;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.persistence.negocio.BuenaPracticaPersistencePort; // Crear esta interfaz similar a las anteriores
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
public class BuenaPracticaPersistenceAdapter implements BuenaPracticaPersistencePort {

    private final MovBuenaPracticaRepository repository;
    private final BuenaPracticaMapper mapper;
    private final MovArchivosRepository repoArchivos;
    private final MaeDistritoJudicialRepository repoDistrito; // Repo de Maestras (Distritos)
    // Agrega esto a tu clase Adapter existente
    @Override
    public Pagina<BuenaPractica> listar(String usuario, BuenaPractica filtros, int pagina, int tamanio) throws Exception {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio);

        // 1. Llamamos al repositorio con los filtros
        Page<MovBuenaPracticaEntity> pageResult = repository.listarDinamico(
                usuario,
                filtros.getId(),              // Filtro Código
                filtros.getTitulo(),          // Filtro Título
                filtros.getDistritoJudicialId(),
                filtros.getFechaInicio(),     // Rango Inicio
                filtros.getFechaFin(),        // Rango Fin
                pageable
        );

        // 2. Convertimos a Dominio y enriquecemos con el nombre de la Corte
        List<BuenaPractica> lista = pageResult.getContent().stream()
                .map(entity -> {
                    BuenaPractica domain = mapper.toDomain(entity);

                    // ✅ BUSCAMOS EL NOMBRE DE LA CORTE
                    if (domain.getDistritoJudicialId() != null) {
                        // Verifica si en tu entidad Maestra el método es getNombre() o getNomCorto()
                        // Basado en tu PHP parece ser 'nomcorto'
                        String nombreCorte = repoDistrito.findById(domain.getDistritoJudicialId())
                                .map(d -> d.getNombreCorto())
                                .orElse("DESCONOCIDO");
                        domain.setDistritoJudicialNombre(nombreCorte);
                    }

                    return domain;
                })
                .collect(Collectors.toList()); // <--- Corregido (sin el .colle extra)

        // 3. Retornamos la página construida
        return Pagina.<BuenaPractica>builder()
                .contenido(lista)
                .totalRegistros(pageResult.getTotalElements())
                .totalPaginas(pageResult.getTotalPages())
                .paginaActual(pageResult.getNumber() + 1)
                .tamanioPagina(pageResult.getSize())
                .build();
    }

    @Override
    public BuenaPractica buscarPorId(String id) throws Exception {
        // 1. Buscar la entidad principal
        MovBuenaPracticaEntity entity = repository.findById(id).orElse(null);
        if (entity == null) return null;

        // 2. Mapear a Dominio
        BuenaPractica dominio = mapper.toDomain(entity);

        // 3. ✅ BUSCAR ARCHIVOS ASOCIADOS (La parte que faltaba)
        List<MovArchivosEntity> archivosEntities = repoArchivos.findByNumeroIdentificacion(id);

        if (archivosEntities != null && !archivosEntities.isEmpty()) {
            List<Archivo> listaArchivos = archivosEntities.stream()
                    .map(a -> Archivo.builder()
                            .nombre(a.getNombre())
                            .tipo(a.getTipo()) // ej: ANEXO_BP, FOTO_BP
                            .ruta(a.getRuta())
                            .numeroIdentificacion(a.getNumeroIdentificacion())
                            .build())
                    .collect(Collectors.toList());

            dominio.setArchivosGuardados(listaArchivos);
        }

        return dominio;
    }

    @Override
    public BuenaPractica guardar(BuenaPractica dominio) throws Exception {
        MovBuenaPracticaEntity entity = mapper.toEntity(dominio);
        MovBuenaPracticaEntity saved = repository.save(entity);
        return mapper.toDomain(saved); // Mapeo inverso simple o manual
    }

    @Override
    public String obtenerUltimoId() throws Exception {
        return repository.obtenerUltimoId();
    }

    @Override
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        // 1. Llamamos al Repo para obtener la data cruda (ID Corte, Cantidad)
        List<Object[]> rawData = repository.obtenerEstadisticasHistoricas();

        List<ResumenEstadistico> lista = new ArrayList<>();

        // 2. Procesamos y convertimos a Dominio aquí mismo
        for (Object[] row : rawData) {
            String distritoId = (String) row[0];
            Long cantidad = (Long) row[1];

            // Buscamos el nombre de la corte (Lógica de infraestructura)
            String nombreCorte = repoDistrito.findById(distritoId)
                    .map(d -> d.getNombre()) // Asumiendo que la entidad tiene getNombre()
                    .orElse("Corte " + distritoId);

            // Creamos el objeto de Dominio
            lista.add(ResumenEstadistico.builder()
                    .etiqueta(nombreCorte)
                    .cantidad(cantidad)
                    .build());
        }

        return lista;
    }
}