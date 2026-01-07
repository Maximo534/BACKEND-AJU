package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.JpeCasoAtendido;
import pe.gob.pj.prueba.domain.model.negocio.JuezPazEscolar;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.persistence.negocio.JuezPazEscolarPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MaeJuezPazEscolarEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivosEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovJpeCasoAtendidoEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MaeJuezPazEscolarRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovArchivosRepository; // ✅ Importamos Repositorio Archivos
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovJpeCasoAtendidoRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.*;
import pe.gob.pj.prueba.infraestructure.mappers.JuezPazEscolarMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JuezPazEscolarPersistenceAdapter implements JuezPazEscolarPersistencePort {

    private final MaeJuezPazEscolarRepository repository;
    private final JuezPazEscolarMapper mapper;
    private final MovJpeCasoAtendidoRepository casoRepository;
    private final MovArchivosRepository repoArchivos; // ✅ Inyección nueva (igual que en BP)
    // REPOSITORIOS MAESTROS (Necesarios para llenar los nombres)
    private final MaeJuezPazEscolarRepository repoJuez;
    private final MaeInstitucionEducativaRepository repoColegio;
    private final MaeUgelRepository repoUgel;
    private final MaeDistritoJudicialRepository repoCorte;
    // Repos de Ubigeo
    private final MaeDepartamentoRepository repoDepa;
    private final MaeProvinciaRepository repoProv;
    private final MaeDistritoRepository repoDist;
    // ==========================================
    // SECCIÓN JUECES
    // ==========================================
    @Override
    @Transactional
    public JuezPazEscolar guardar(JuezPazEscolar domain) throws Exception {
        MaeJuezPazEscolarEntity entity = mapper.toEntity(domain);
        MaeJuezPazEscolarEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existeDniEnColegio(String dni, String colegioId) {
        return repository.existsByDniAndInstitucionEducativaIdAndActivo(dni, colegioId, "1");
    }

    @Override
    public List<JuezPazEscolar> listarPorColegio(String colegioId) {
        return repository.findByInstitucionEducativaIdAndActivo(colegioId, "1").stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    // ==========================================
    // SECCIÓN CASOS (INCIDENTES)
    // ==========================================

    @Override
    public String obtenerUltimoIdCaso() {
        return casoRepository.obtenerUltimoId();
    }

    @Override
    @Transactional
    public JpeCasoAtendido guardarCaso(JpeCasoAtendido dominio) throws Exception {
        // Mapeo usando Mapper (recomendado) o manual si prefieres mantenerlo así
        MovJpeCasoAtendidoEntity entity = mapper.toEntity(dominio);
        // Si usas manual, asegúrate de setear todos los campos aquí...

        MovJpeCasoAtendidoEntity saved = casoRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public JpeCasoAtendido buscarCasoPorId(String id) throws Exception {
        // 1. Buscar entidad
        MovJpeCasoAtendidoEntity entity = casoRepository.findById(id).orElse(null);
        if (entity == null) return null;

        // 2. Mapear a dominio
        JpeCasoAtendido dominio = mapper.toDomain(entity);

        // 3. Buscar y adjuntar archivos
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
    public Pagina<JpeCasoAtendido> listarCasos(JpeCasoAtendido filtros, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio);
        Integer anio = (filtros.getFechaRegistro() != null) ? filtros.getFechaRegistro().getYear() : LocalDate.now().getYear();

        Page<Object[]> pageResult = casoRepository.listarDinamico(filtros.getId(), anio, pageable);

        List<JpeCasoAtendido> lista = pageResult.getContent().stream().map(row -> {
            return JpeCasoAtendido.builder()
                    .id((String) row[0])
                    .distritoJudicialNombre((String) row[1])
                    .ugelNombre((String) row[2])
                    .institucionNombre((String) row[3])
                    .resumenHechos((String) row[4])
                    .fechaRegistro(((java.sql.Date) row[5]).toLocalDate())
                    .build();
        }).collect(Collectors.toList());

        return Pagina.<JpeCasoAtendido>builder()
                .contenido(lista)
                .totalRegistros(pageResult.getTotalElements())
                .totalPaginas(pageResult.getTotalPages())
                .paginaActual(pageResult.getNumber() + 1)
                .tamanioPagina(pageResult.getSize())
                .build();
    }

    @Override
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        // 1. Obtener data cruda [ID_CORTE, CANTIDAD]
        List<Object[]> rawData = casoRepository.obtenerEstadisticasPorCorte();

        List<ResumenEstadistico> lista = new ArrayList<>();

        // 2. Transformar a Dominio buscando el nombre de la Corte
        for (Object[] row : rawData) {
            String idCorte = (String) row[0];
            Long cantidad = (Long) row[1];

            // Buscamos el nombre (usamos repoCorte que ya tienes inyectado)
            String nombreCorte = "DESCONOCIDO";
            if (idCorte != null) {
                nombreCorte = repoCorte.findById(idCorte)
                        .map(c -> c.getNombre()) // o getNomCorto() según tu entidad maestra
                        .orElse(idCorte);
            }

            lista.add(ResumenEstadistico.builder()
                    .etiqueta(nombreCorte)
                    .cantidad(cantidad)
                    .build());
        }

        return lista;
    }
}