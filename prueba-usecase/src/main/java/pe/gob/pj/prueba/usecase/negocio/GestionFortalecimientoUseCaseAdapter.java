package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.common.Pagina;
import pe.gob.pj.prueba.domain.model.common.RecursoArchivo;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.FortalecimientoCapacidades;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.FortalecimientoPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionArchivosUseCasePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionFortalecimientoUseCasePort;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestionFortalecimientoUseCaseAdapter implements GestionFortalecimientoUseCasePort {

    private final FortalecimientoPersistencePort persistencePort;
    private final GenerarReportePort reportePort;
    private final GestionArchivosUseCasePort gestorArchivos;
    private final GestionArchivosPersistencePort archivosPersistencePort;

    private static final String MODULO_FFC = "evidencias_ffc";

    @Override
    @Transactional(readOnly = true)
    public Pagina<FortalecimientoCapacidades> listar(String usuario, FortalecimientoCapacidades filtros, int pagina, int tamanio) throws Exception {
        return persistencePort.listar(usuario, filtros, pagina, tamanio);
    }

    @Override
    @Transactional(readOnly = true)
    public FortalecimientoCapacidades buscarPorId(String id) throws Exception {
        if (id == null || id.isBlank()) throw new Exception("El ID es obligatorio");
        return persistencePort.obtenerPorId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FortalecimientoCapacidades registrar(FortalecimientoCapacidades dominio, MultipartFile anexo, List<MultipartFile> videos, List<MultipartFile> fotos, String usuario) throws Exception {

        validarDatos(dominio);

        if (dominio.getTareasRealizadas() != null) {
            for (FortalecimientoCapacidades.DetalleTarea tarea : dominio.getTareasRealizadas()) {
                if (tarea.getFechaInicio() == null) {
                    tarea.setFechaInicio(dominio.getFechaInicio());
                }
            }
        }

        if (dominio.getResolucionAdminPlan() == null) dominio.setResolucionAdminPlan("NINGUNO");

        String ultimoId = persistencePort.obtenerUltimoId();
        long siguiente = 1;
        if (ultimoId != null && !ultimoId.isBlank()) {
            try { siguiente = Long.parseLong(ultimoId.split("-")[0]) + 1; } catch (Exception e) { siguiente = 1; }
        }
        String anio = String.valueOf(LocalDate.now().getYear());
        String corte = (dominio.getDistritoJudicialId() != null) ? dominio.getDistritoJudicialId() : "00";
        dominio.setId(String.format("%06d-%s-%s-FC", siguiente, corte, anio));

        dominio.setUsuarioRegistro(usuario);
        dominio.setFechaRegistro(LocalDate.now());
        dominio.setActivo("1");

        FortalecimientoCapacidades registrado = persistencePort.guardar(dominio);

        if (anexo != null && !anexo.isEmpty()) {
            gestorArchivos.subirArchivo(anexo, registrado.getDistritoJudicialId(), "ANEXO", MODULO_FFC, registrado.getFechaInicio(), registrado.getId());
        }

        if (fotos != null) {
            for (MultipartFile f : fotos) {
                if(!f.isEmpty())
                    gestorArchivos.subirArchivo(f, registrado.getDistritoJudicialId(), "FOTO", MODULO_FFC, registrado.getFechaInicio(), registrado.getId());
            }
        }

        if (videos != null) {
            for (MultipartFile v : videos) {
                if(!v.isEmpty())
                    gestorArchivos.subirArchivo(v, registrado.getDistritoJudicialId(), "VIDEO", MODULO_FFC, registrado.getFechaInicio(), registrado.getId());
            }
        }

        return registrado;
    }

    @Override
    @Transactional
    public FortalecimientoCapacidades actualizar(FortalecimientoCapacidades dominio, String usuarioOperacion) throws Exception {
        log.info("Actualizando FFC ID: {} por: {}", dominio.getId(), usuarioOperacion);

        validarDatos(dominio);

        // Lógica de actualización de datos
        if (dominio.getTareasRealizadas() != null) {
            for (FortalecimientoCapacidades.DetalleTarea tarea : dominio.getTareasRealizadas()) {
                if (tarea.getFechaInicio() == null) {
                    tarea.setFechaInicio(dominio.getFechaInicio());
                }
            }
        }
        if (dominio.getResolucionAdminPlan() == null || dominio.getResolucionAdminPlan().isBlank()) dominio.setResolucionAdminPlan("NINGUNO");
        if (dominio.getInstitucionesAliadas() == null || dominio.getInstitucionesAliadas().isBlank()) dominio.setInstitucionesAliadas("NINGUNA");

        if ("NO".equalsIgnoreCase(dominio.getSeDictoLenguaNativa())) {
            dominio.setLenguaNativaDesc("CASTELLANO");
        } else if (dominio.getLenguaNativaDesc() == null || dominio.getLenguaNativaDesc().isBlank()) {
            dominio.setLenguaNativaDesc("NO ESPECIFICADO");
        }

        dominio.setUsuarioRegistro(usuarioOperacion);
        return persistencePort.actualizar(dominio);
    }

    @Override
    @Transactional
    public void agregarArchivo(String idEvento, MultipartFile archivo, String tipo, String usuario) throws Exception {
        log.info("Usuario [{}] agregando archivo [{}] al evento FFC ID [{}]", usuario, tipo, idEvento);

        if (archivo == null || archivo.isEmpty()) throw new Exception("Archivo vacío");

        FortalecimientoCapacidades evento = persistencePort.obtenerPorId(idEvento);
        if (evento == null) throw new Exception("No existe el evento: " + idEvento);

        gestorArchivos.subirArchivo(archivo, evento.getDistritoJudicialId(), tipo, MODULO_FFC, evento.getFechaInicio(), idEvento);
    }

    @Override
    public void eliminarArchivo(String nombreArchivo) throws Exception {
        gestorArchivos.eliminarPorNombre(nombreArchivo);
    }

    @Override
    public RecursoArchivo descargarAnexo(String idEvento, String usuario) throws Exception {
        if (idEvento == null || idEvento.isEmpty()) throw new Exception("ID obligatorio");

        List<Archivo> archivos = archivosPersistencePort.listarArchivosPorEvento(idEvento);

        Archivo anexo = archivos.stream()
                .filter(a -> "ANEXO".equalsIgnoreCase(a.getTipo()))
                .findFirst()
                .orElseThrow(() -> new Exception("Sin anexo PDF para este evento."));

        return gestorArchivos.descargarPorNombre(anexo.getNombre());
    }

    @Override
    public RecursoArchivo descargarArchivoPorNombre(String nombreArchivo) throws Exception {
        return gestorArchivos.descargarPorNombre(nombreArchivo);
    }

    @Override
    public byte[] generarFichaPdf(String idEvento) throws Exception {
        if (persistencePort.obtenerPorId(idEvento) == null) throw new Exception("El evento no existe.");
        return reportePort.generarFichaFortalecimiento(idEvento);
    }

    private void validarDatos(FortalecimientoCapacidades ffc) throws Exception {
        if (ffc.getFechaInicio() == null) throw new Exception("Fecha inicio obligatoria.");
        if (ffc.getNombreEvento() == null || ffc.getNombreEvento().isBlank()) throw new Exception("Nombre evento obligatorio.");
    }
}