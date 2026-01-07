package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante;
import pe.gob.pj.prueba.domain.port.persistence.negocio.JusticiaItinerantePersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivosEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovJusticiaItineranteEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.*;
import pe.gob.pj.prueba.infraestructure.mappers.JusticiaItineranteMapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JusticiaItinerantePersistenceAdapter implements JusticiaItinerantePersistencePort {

    private final MovJusticiaItineranteRepository repository;
    private final MovArchivosRepository repoArchivos;
    private final JusticiaItineranteMapper mapper;

    private final MovJiPersonasBeneficiadasRepository repoBeneficiadas;
    private final MovJiPersonasAtendidasRepository repoAtendidas;
    private final MovJiCasosAtendidosRepository repoCasos;
    private final MovJiTareasRealizadasRepository repoTareas;

    @Override
    @Transactional(readOnly = true)
    public Pagina<JusticiaItinerante> listarItinerante(String usuario, JusticiaItinerante filtros, int pagina, int tamanio) throws Exception {
        try {
            Pageable pageable = PageRequest.of(pagina - 1, tamanio);

            if (filtros == null) filtros = JusticiaItinerante.builder().build();

            Page<MovJusticiaItineranteEntity> pageResult = repository.listarDinamico(
                    usuario,
                    filtros.getId(),
                    filtros.getPublicoObjetivo(),
                    filtros.getDistritoJudicialId(),
                    filtros.getFechaInicio(),
                    filtros.getFechaFin(),
                    pageable
            );

            List<JusticiaItinerante> contenido = pageResult.getContent().stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());

            return Pagina.<JusticiaItinerante>builder()
                    .contenido(contenido)
                    .totalRegistros(pageResult.getTotalElements())
                    .totalPaginas(pageResult.getTotalPages())
                    .paginaActual(pagina)
                    .tamanioPagina(tamanio)
                    .build();

        } catch (Exception e) {
            log.error("Error al listar itinerante", e);
            throw new Exception("Error al listar: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public JusticiaItinerante guardar(JusticiaItinerante dominio) throws Exception {
        try {
            MovJusticiaItineranteEntity entidad = mapper.toEntity(dominio);

            if (entidad.getPersonasAtendidas() != null) {
                entidad.getPersonasAtendidas().forEach(d -> d.setJusticiaItineranteId(entidad.getId()));
            }
            if (entidad.getCasosAtendidos() != null) {
                entidad.getCasosAtendidos().forEach(d -> d.setJusticiaItineranteId(entidad.getId()));
            }
            if (entidad.getPersonasBeneficiadas() != null) {
                entidad.getPersonasBeneficiadas().forEach(d -> d.setJusticiaItineranteId(entidad.getId()));
            }
            if (entidad.getTareasRealizadas() != null) {
                entidad.getTareasRealizadas().forEach(d -> d.setJusticiaItineranteId(entidad.getId()));
            }

            MovJusticiaItineranteEntity entidadGuardada = repository.save(entidad);

            return mapper.toDomain(entidadGuardada);

        } catch (Exception e) {
            log.error("Error al guardar Justicia Itinerante en BD", e);
            throw new Exception("Error al guardar en base de datos: " + e.getMessage());
        }
    }
    @Override
    public String obtenerUltimoId() throws Exception {
        return repository.obtenerUltimoId();
    }
    @Override
    @Transactional
    public JusticiaItinerante actualizar(JusticiaItinerante dominio) throws Exception {
        try {
            if (dominio.getId() == null || dominio.getId().isEmpty()) {
                throw new Exception("El ID es obligatorio para actualizar.");
            }

            MovJusticiaItineranteEntity entidadDb = repository.findById(dominio.getId())
                    .orElseThrow(() -> new Exception("No existe el evento FJI con ID: " + dominio.getId()));



            entidadDb.setDistritoJudicialId(dominio.getDistritoJudicialId());
            entidadDb.setFechaInicio(dominio.getFechaInicio());
            entidadDb.setFechaFin(dominio.getFechaFin());
            entidadDb.setResolucionPlanAnual(dominio.getResolucionPlanAnual());
            entidadDb.setResolucionAdminPlan(dominio.getResolucionAdminPlan());
            entidadDb.setDocumentoAutoriza(dominio.getDocumentoAutoriza());
            entidadDb.setEjeId(dominio.getEjeId());

            entidadDb.setPublicoObjetivo(dominio.getPublicoObjetivo());
            entidadDb.setPublicoObjetivoDetalle(dominio.getPublicoObjetivoDetalle());
            entidadDb.setLugarActividad(dominio.getLugarActividad());

            // Ubigeo
            entidadDb.setDepartamentoId(dominio.getDepartamentoId());
            entidadDb.setProvinciaId(dominio.getProvinciaId());
            entidadDb.setDistritoGeograficoId(dominio.getDistritoGeograficoId());

            // Estadísticas y Datos Específicos
            entidadDb.setNumMujeresIndigenas(dominio.getNumMujeresIndigenas());
            entidadDb.setNumPersonasNoIdiomaNacional(dominio.getNumPersonasNoIdiomaNacional());
            entidadDb.setNumJovenesQuechuaAymara(dominio.getNumJovenesQuechuaAymara());
            entidadDb.setCodigoAdcPueblosIndigenas(dominio.getCodigoAdcPueblosIndigenas());
            entidadDb.setTambo(dominio.getTambo());
            entidadDb.setCodigoSaeLenguaNativa(dominio.getCodigoSaeLenguaNativa());
            entidadDb.setLenguaNativa(dominio.getLenguaNativa());

            // Textos
            entidadDb.setDescripcionActividad(dominio.getDescripcionActividad());
            entidadDb.setInstitucionesAliadas(dominio.getInstitucionesAliadas());
            entidadDb.setObservaciones(dominio.getObservaciones());

            //ACTUALIZAR LISTAS HIJAS

            //Personas Atendidas (PA)
            if (entidadDb.getPersonasAtendidas() != null) {
                entidadDb.getPersonasAtendidas().clear();
            }
            if (dominio.getPersonasAtendidas() != null) {
                dominio.getPersonasAtendidas().forEach(d -> {
                    var entityHija = mapper.toEntityPA(d);
                    entityHija.setJusticiaItineranteId(entidadDb.getId());
                    entidadDb.getPersonasAtendidas().add(entityHija);
                });
            }

            //Casos Atendidos (PCA)
            if (entidadDb.getCasosAtendidos() != null) {
                entidadDb.getCasosAtendidos().clear();
            }
            if (dominio.getCasosAtendidos() != null) {
                dominio.getCasosAtendidos().forEach(d -> {
                    var entityHija = mapper.toEntityPCA(d);
                    entityHija.setJusticiaItineranteId(entidadDb.getId());
                    entidadDb.getCasosAtendidos().add(entityHija);
                });
            }

            // Personas Beneficiadas (PB)
            if (entidadDb.getPersonasBeneficiadas() != null) {
                entidadDb.getPersonasBeneficiadas().clear();
            }
            if (dominio.getPersonasBeneficiadas() != null) {
                dominio.getPersonasBeneficiadas().forEach(d -> {
                    var entityHija = mapper.toEntityPB(d);
                    entityHija.setJusticiaItineranteId(entidadDb.getId());
                    entidadDb.getPersonasBeneficiadas().add(entityHija);
                });
            }

            // Tareas Realizadas (TR)
            if (entidadDb.getTareasRealizadas() != null) {
                entidadDb.getTareasRealizadas().clear();
            }
            if (dominio.getTareasRealizadas() != null) {
                dominio.getTareasRealizadas().forEach(d -> {
                    var entityHija = mapper.toEntityTR(d);
                    entityHija.setJusticiaItineranteId(entidadDb.getId());
                    entidadDb.getTareasRealizadas().add(entityHija);
                });
            }

            MovJusticiaItineranteEntity actualizado = repository.save(entidadDb);

            return mapper.toDomain(actualizado);

        } catch (Exception e) {
            log.error("Error al actualizar Justicia Itinerante en BD", e);
            throw new Exception("Error al actualizar: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public JusticiaItinerante obtenerPorId(String id) throws Exception {
        MovJusticiaItineranteEntity entidad = repository.findById(id)
                .orElseThrow(() -> new Exception("No se encontró el evento con ID: " + id));

        JusticiaItinerante dominio = mapper.toDomain(entidad);

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

}