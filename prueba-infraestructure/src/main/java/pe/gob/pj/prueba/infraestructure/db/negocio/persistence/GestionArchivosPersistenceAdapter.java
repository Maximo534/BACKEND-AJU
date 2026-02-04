package pe.gob.pj.prueba.infraestructure.db.negocio.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovArchivoEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovArchivosRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GestionArchivosPersistenceAdapter implements GestionArchivosPersistencePort {

    private final MovArchivosRepository repository;

    @Override
    @Transactional
    public void guardarReferenciaArchivo(Archivo dominio) throws Exception {
        try {
            MovArchivoEntity entidad = new MovArchivoEntity();

            // Datos
            entidad.setNombre(dominio.getNombre());
            entidad.setTipo(dominio.getTipo());
            entidad.setRuta(dominio.getRuta());
            entidad.setNumeroIdentificacion(dominio.getNumeroIdentificacion());

            // Auditoría
            entidad.setActivo("1");
            entidad.setCAudId(dominio.getUsuario());
            entidad.setCAudIp(dominio.getNumeroIp());
            entidad.setCAudPc(dominio.getNombrePc());
            entidad.setCAudMcAddr(dominio.getDireccionMac());
            entidad.setCAudIdRed(dominio.getRed());

            repository.save(entidad);
        } catch (Exception e) {
            log.error("Error al guardar referencia de archivo en BD", e);
            throw new Exception("Error al guardar metadatos del archivo: " + e.getMessage());
        }
    }

    @Override
    public Archivo buscarPorId(Long id) throws Exception {
        return repository.findById(id)
                .filter(e -> "1".equals(e.getActivo()))
                .map(this::mapearADominio)
                .orElse(null);
    }

    @Override
    public Archivo buscarPorNombre(String nombre) throws Exception {
        return repository.findByNombreAndActivo(nombre, "1")
                .map(this::mapearADominio).orElse(null);
    }

    @Override
    @Transactional
    public void eliminarReferenciaPorId(Long id, String usuario, String ip, String pc, String mac) throws Exception {
        MovArchivoEntity entidad = repository.findById(id)
                .orElseThrow(() -> new Exception("Archivo no encontrado con ID: " + id));

        entidad.setActivo("0");
        entidad.setBAud("E");
        entidad.setFAud(LocalDateTime.now());
        entidad.setCAudId(usuario);
        entidad.setCAudIp(ip);
        entidad.setCAudPc(pc);
        entidad.setCAudMcAddr(mac);

        repository.save(entidad);
    }

    @Override
    public List<Archivo> listarArchivosPorEvento(String codigoIdentificacion) throws Exception {
        // Buscamos por CÓDIGO y solo activos
        return repository.findByNumeroIdentificacionAndActivo(codigoIdentificacion, "1").stream()
                .map(this::mapearADominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Archivo> listarParaDescargaMasiva(String tipoArchivo, Integer anio, Integer mes) throws Exception {
        try {
            var proyecciones = repository.listarParaDescargaMasiva(tipoArchivo, anio, mes);

            return proyecciones.stream()
                    .map(p -> Archivo.builder()
                            .nombre(p.getNombre())
                            .ruta(p.getRuta())
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error al listar archivos para descarga masiva", e);
            throw new Exception("Error al consultar archivos para reporte: " + e.getMessage());
        }
    }

    private Archivo mapearADominio(MovArchivoEntity e) {
        Archivo a = Archivo.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .tipo(e.getTipo())
                .ruta(e.getRuta())
                .numeroIdentificacion(e.getNumeroIdentificacion())
                .activo(e.getActivo())
                .build();

        // --- Mapear Auditoría de Vuelta (Entity -> Domain) ---
        // Estos métodos vienen de la clase padre 'Auditoria'
        a.setUsuario(e.getCAudId());
        a.setNumeroIp(e.getCAudIp());
        a.setNombrePc(e.getCAudPc());
        a.setDireccionMac(e.getCAudMcAddr());
        a.setRed(e.getCAudIdRed());

        return a;
    }



}