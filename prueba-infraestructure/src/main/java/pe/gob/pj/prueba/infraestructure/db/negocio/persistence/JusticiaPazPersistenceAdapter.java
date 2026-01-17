package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.JpeCasoAtendido;
import pe.gob.pj.prueba.domain.model.negocio.ResumenEstadistico;
import pe.gob.pj.prueba.domain.port.persistence.negocio.JusticiaPazPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MaeJuezPazEscolarEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivosEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovJpeCasoAtendidoEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MaeJuezPazEscolarRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovArchivosRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovJpeCasoAtendidoRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.MaeDistritoJudicialRepository;
import pe.gob.pj.prueba.infraestructure.mappers.JusticiaPazMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JusticiaPazPersistenceAdapter implements JusticiaPazPersistencePort {

    private final MovJpeCasoAtendidoRepository casoRepository;
    private final MaeDistritoJudicialRepository repoCorte;
    private final MaeJuezPazEscolarRepository repoJuez;
    private final MovArchivosRepository repoArchivos;
    private final JusticiaPazMapper  mapper;

    @Override
    @Transactional(readOnly = true)
    public Pagina<JpeCasoAtendido> listar(String usuario, JpeCasoAtendido filtros, int pagina, int tamanio) throws Exception {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio);
        if (filtros == null) filtros = JpeCasoAtendido.builder().build();

        var result = casoRepository.listar(usuario, filtros.getSearch(), filtros.getDistritoJudicialId(),
                filtros.getUgelId(), filtros.getInstitucionEducativaId(),
                filtros.getFechaRegistro(), pageable);

        List<JpeCasoAtendido> contenido = result.getContent().stream()
                .map(p -> JpeCasoAtendido.builder()
                        .id(p.getId())
                        .distritoJudicialNombre(p.getDistritoJudicialNombre())
                        .ugelNombre(p.getUgelNombre())
                        .institucionNombre(p.getInstitucionNombre())
                        .resumenHechos(p.getResumenHechos())
                        .fechaRegistro(p.getFechaRegistro())
                        .estado(p.getEstado())
                        .build())
                .collect(Collectors.toList());

        return Pagina.<JpeCasoAtendido>builder()
                .contenido(contenido)
                .totalRegistros(result.getTotalElements())
                .totalPaginas(result.getTotalPages())
                .paginaActual(pagina)
                .tamanioPagina(tamanio)
                .build();
    }

    @Override
    @Transactional
    public JpeCasoAtendido guardar(JpeCasoAtendido dominio) throws Exception {
        MovJpeCasoAtendidoEntity entity = mapper.toEntity(dominio);

        if (dominio.getJuezEscolarId() != null) {
            MaeJuezPazEscolarEntity juez = repoJuez.findById(dominio.getJuezEscolarId())
                    .orElseThrow(() -> new Exception("Juez escolar no encontrado"));
            entity.setJuezEscolar(juez);
        }

        MovJpeCasoAtendidoEntity saved = casoRepository.save(entity);
        JpeCasoAtendido res = mapper.toDomain(saved);

        // ✅ LÓGICA INLINE: Enriquecer nombres
        if (res.getDistritoJudicialId() != null) {
            repoCorte.findById(res.getDistritoJudicialId())
                    .ifPresent(c -> res.setDistritoJudicialNombre(c.getNombre()));
        }
        if (saved.getJuezEscolar() != null) {
            MaeJuezPazEscolarEntity juez = saved.getJuezEscolar();
            res.setJuezEscolarNombre(juez.getNombres() + " " + juez.getApePaterno() + " " + juez.getApeMaterno());
            res.setJuezGradoSeccion(juez.getGrado() + " " + juez.getSeccion());

            if (juez.getInstitucionEducativa() != null) {
                res.setInstitucionNombre(juez.getInstitucionEducativa().getNombre());
                res.setInstitucionEducativaId(juez.getInstitucionEducativa().getId());
                if (juez.getInstitucionEducativa().getUgel() != null) {
                    res.setUgelNombre(juez.getInstitucionEducativa().getUgel().getNombre());
                    res.setUgelId(juez.getInstitucionEducativa().getUgel().getId());
                }
            }
        }

        return res;
    }

    @Override
    @Transactional
    public JpeCasoAtendido actualizar(JpeCasoAtendido dominio) throws Exception {
        MovJpeCasoAtendidoEntity entityDb = casoRepository.findById(dominio.getId())
                .orElseThrow(() -> new Exception("Caso no encontrado: " + dominio.getId()));

        mapper.updateEntityFromDomain(dominio, entityDb);

        if (dominio.getJuezEscolarId() != null && !dominio.getJuezEscolarId().equals(entityDb.getJuezEscolar().getId())) {
            MaeJuezPazEscolarEntity juez = repoJuez.findById(dominio.getJuezEscolarId())
                    .orElseThrow(() -> new Exception("Nuevo Juez no encontrado"));
            entityDb.setJuezEscolar(juez);
        }

        MovJpeCasoAtendidoEntity saved = casoRepository.save(entityDb);
        JpeCasoAtendido res = mapper.toDomain(saved);

        // ✅ LÓGICA INLINE: Enriquecer nombres
        if (res.getDistritoJudicialId() != null) {
            repoCorte.findById(res.getDistritoJudicialId())
                    .ifPresent(c -> res.setDistritoJudicialNombre(c.getNombre()));
        }
        if (saved.getJuezEscolar() != null) {
            MaeJuezPazEscolarEntity juez = saved.getJuezEscolar();
            res.setJuezEscolarNombre(juez.getNombres() + " " + juez.getApePaterno() + " " + juez.getApeMaterno());
            res.setJuezGradoSeccion(juez.getGrado() + " " + juez.getSeccion());

            if (juez.getInstitucionEducativa() != null) {
                res.setInstitucionNombre(juez.getInstitucionEducativa().getNombre());
                res.setInstitucionEducativaId(juez.getInstitucionEducativa().getId());
                if (juez.getInstitucionEducativa().getUgel() != null) {
                    res.setUgelNombre(juez.getInstitucionEducativa().getUgel().getNombre());
                    res.setUgelId(juez.getInstitucionEducativa().getUgel().getId());
                }
            }
        }

        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public JpeCasoAtendido buscarPorId(String id) throws Exception {
        MovJpeCasoAtendidoEntity entity = casoRepository.findById(id).orElse(null);
        if (entity == null) return null;

        JpeCasoAtendido dominio = mapper.toDomain(entity);

        // ✅ LÓGICA INLINE: Enriquecer nombres
        if (dominio.getDistritoJudicialId() != null) {
            repoCorte.findById(dominio.getDistritoJudicialId())
                    .ifPresent(c -> dominio.setDistritoJudicialNombre(c.getNombre()));
        }
        if (entity.getJuezEscolar() != null) {
            MaeJuezPazEscolarEntity juez = entity.getJuezEscolar();
            dominio.setJuezEscolarNombre(juez.getNombres() + " " + juez.getApePaterno() + " " + juez.getApeMaterno());
            dominio.setJuezGradoSeccion(juez.getGrado() + " " + juez.getSeccion());

            if (juez.getInstitucionEducativa() != null) {
                dominio.setInstitucionNombre(juez.getInstitucionEducativa().getNombre());
                dominio.setInstitucionEducativaId(juez.getInstitucionEducativa().getId());
                if (juez.getInstitucionEducativa().getUgel() != null) {
                    dominio.setUgelNombre(juez.getInstitucionEducativa().getUgel().getNombre());
                    dominio.setUgelId(juez.getInstitucionEducativa().getUgel().getId());
                }
            }
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
        return casoRepository.obtenerUltimoId();
    }

    @Override
    public List<ResumenEstadistico> obtenerResumenGrafico() throws Exception {
        List<Object[]> data = casoRepository.obtenerEstadisticasPorCorte();
        List<ResumenEstadistico> lista = new ArrayList<>();
        for (Object[] row : data) {
            String idCorte = (String) row[0];
            Long cant = (Long) row[1];
            String nombre = repoCorte.findById(idCorte).map(c -> c.getNombre()).orElse(idCorte);
            lista.add(ResumenEstadistico.builder().etiqueta(nombre).cantidad(cant).build());
        }
        return lista;
    }
}
