package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.LlapanchikpaqJusticia;
import pe.gob.pj.prueba.domain.port.files.FtpPort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.LlapanchikpaqPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.RegistrarLlapanchikpaqUseCasePort;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrarLlapanchikpaqUseCaseAdapter implements RegistrarLlapanchikpaqUseCasePort {

    private final LlapanchikpaqPersistencePort persistencePort;
    private final GestionArchivosPersistencePort archivosPersistencePort;
    private final FtpPort ftpPort;

    @Value("${ftp.ip}") private String ftpIp;
    @Value("${ftp.puerto}") private Integer ftpPuerto;
    @Value("${ftp.usuario}") private String ftpUsuario;
    @Value("${ftp.clave}") private String ftpClave;
    @Value("${ftp.ruta-base:/evidencias}") private String ftpRutaBase;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LlapanchikpaqJusticia registrar(LlapanchikpaqJusticia llj, MultipartFile anexo, List<MultipartFile> fotos, String usuario) throws Exception {
        log.info("Iniciando registro LLJ por: {}", usuario);

        // 1. GENERAR ID CORRELATIVO
        String ultimoId = persistencePort.obtenerUltimoId();
        long siguiente = 1;

        if (ultimoId != null && !ultimoId.isBlank()) {
            try {
                // Asumimos formato estandar: 000001-15-2025-LL
                String parteNumerica = ultimoId.substring(0, 6);
                siguiente = Long.parseLong(parteNumerica) + 1;
            } catch (Exception e) {
                // Si falla, reiniciamos (ej: si el ID antiguo tiene otro formato)
                siguiente = 1;
            }
        }

        String numeroStr = String.format("%06d", siguiente); // 6 dígitos (Ej: 000001)
        String anio = String.valueOf(LocalDate.now().getYear());
        String corte = (llj.getDistritoJudicialId() != null) ? llj.getDistritoJudicialId() : "00";

        // --- CAMBIO AQUI: SUFIJO "LL" ---
        String sufijo = "LL";

        // Formato: 000001-15-2025-LL (Exactamente 17 caracteres)
        String idGenerado = String.format("%s-%s-%s-%s", numeroStr, corte, anio, sufijo);

        // Ya no es necesario cortar, pero lo dejamos por seguridad extrema
        if (idGenerado.length() > 17) idGenerado = idGenerado.substring(0, 17);

        llj.setId(idGenerado);
        llj.setUsuarioRegistro(usuario);
        if(llj.getFechaRegistro() == null) llj.setFechaRegistro(LocalDate.now());

        // 2. GUARDAR EN BD
        // OJO: El adaptador de persistencia ya se encarga de asignar este ID a los hijos.
        LlapanchikpaqJusticia registrado = persistencePort.guardar(llj);

        // 3. SUBIR ARCHIVOS (FTP)
        try {
            if ((anexo != null && !anexo.isEmpty()) || (fotos != null && !fotos.isEmpty())) {
                ftpPort.iniciarSesion(usuario, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

                // Anexo PDF
                if (anexo != null && !anexo.isEmpty()) {
                    String ruta = String.format("%s/llj/anexos/%s/%s.pdf", ftpRutaBase, anio, idGenerado);
                    ftpPort.uploadFileFTP(usuario, ruta, anexo.getInputStream(), "Anexo LLJ");

                    archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                            .nombre(idGenerado + "_anexo.pdf").tipo("ANEXO_LLJ").ruta(ruta).numeroIdentificacion(idGenerado).build());
                }

                // Fotos (Lista)
                if (fotos != null) {
                    int count = 1;
                    for (MultipartFile f : fotos) {
                        if (!f.isEmpty()) {
                            String nombreFoto = idGenerado + "_foto_" + count + ".jpg";
                            String ruta = String.format("%s/llj/fotos/%s/%s", ftpRutaBase, anio, nombreFoto);
                            ftpPort.uploadFileFTP(usuario, ruta, f.getInputStream(), "Foto LLJ " + count);

                            archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                                    .nombre(nombreFoto).tipo("FOTO_LLJ").ruta(ruta).numeroIdentificacion(idGenerado).build());
                            count++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error subiendo archivos LLJ", e);
            throw new Exception("Error al subir evidencias: " + e.getMessage());
        } finally {
            ftpPort.finalizarSession(usuario);
        }

        return registrado;
    }
}