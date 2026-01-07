package pe.gob.pj.prueba.usecase.negocio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.pj.prueba.domain.model.negocio.Archivo;
import pe.gob.pj.prueba.domain.model.negocio.OrientadoraJudicial;
import pe.gob.pj.prueba.domain.port.files.FtpPort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.GestionArchivosPersistencePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.OrientadoraJudicialPersistencePort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.GestionOrientadorasUseCasePort;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestionOrientadorasUseCaseAdapter implements GestionOrientadorasUseCasePort {

    private final OrientadoraJudicialPersistencePort persistencePort;
    private final GestionArchivosPersistencePort archivosPersistencePort;
    private final FtpPort ftpPort;

    @Value("${ftp.ip}") private String ftpIp;
    @Value("${ftp.puerto}") private Integer ftpPuerto;
    @Value("${ftp.usuario}") private String ftpUsuario;
    @Value("${ftp.clave}") private String ftpClave;
    @Value("${ftp.ruta-base:/evidencias}") private String ftpRutaBase;

    @Transactional
    public OrientadoraJudicial registrarDatos(OrientadoraJudicial oj, String usuarioOperacion) throws Exception {
        log.info("Iniciando registro simple (JSON) de OJ por: {}", usuarioOperacion);

        // 1. Obtener último ID para calcular correlativo
        String ultimoId = persistencePort.obtenerUltimoId();
        long siguiente = 1;

        if (ultimoId != null && !ultimoId.isBlank()) {
            try {
                // Formato esperado: 000001-15-2025-OJ (Tomamos los primeros 6 digitos)
                String parteNumerica = ultimoId.substring(0, 6);
                siguiente = Long.parseLong(parteNumerica) + 1;
            } catch (Exception e) {
                log.warn("El último ID '{}' no tiene formato estándar. Se reinicia el contador en 1.", ultimoId);
                siguiente = 1;
            }
        }

        // 2. Formatear nuevo ID: 00000X-Corte-Anio-OJ
        String numeroStr = String.format("%06d", siguiente);
        String anio = String.valueOf(LocalDate.now().getYear());
        String corte = oj.getDistritoJudicialId(); // Debe venir en el Request (ej: "15")

        if (corte == null || corte.isEmpty()) corte = "00"; // Fallback por seguridad

        String idGenerado = String.format("%s-%s-%s-OJ", numeroStr, corte, anio);

        // Validar longitud máxima de 17 (Database Limit)
        if (idGenerado.length() > 17) {
            idGenerado = idGenerado.substring(0, 17);
        }

        oj.setId(idGenerado);
        oj.setUsuarioRegistro(usuarioOperacion);
        if(oj.getFechaAtencion() == null) oj.setFechaAtencion(LocalDate.now());

        return persistencePort.guardar(oj);
    }

    // MÉTODO PÚBLICO: REGISTRO CON EVIDENCIAS (Igual a 'registrarConEvidencias' de JI)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrientadoraJudicial registrarAtencion(OrientadoraJudicial oj, MultipartFile anexo, MultipartFile foto, String usuario) throws Exception {
        log.info("Iniciando registro UNIFICADO (Datos + Archivos) OJ por: {}", usuario);

        // 1. Registrar primero los datos en BD
        OrientadoraJudicial registrado = this.registrarDatos(oj, usuario);
        String idGenerado = registrado.getId();
        String anio = String.valueOf(LocalDate.now().getYear());

        // 2. Subir Archivos al FTP
        try {
            ftpPort.iniciarSesion(usuario, ftpIp, ftpPuerto, ftpUsuario, ftpClave);

            // --- A. ANEXO (PDF) ---
            if (anexo != null && !anexo.isEmpty()) {
                String rutaAnexo = String.format("%s/oj/anexos/%s/%s.pdf", ftpRutaBase, anio, idGenerado);
                ftpPort.uploadFileFTP(usuario, rutaAnexo, anexo.getInputStream(), "Anexo OJ");

                archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                        .nombre(idGenerado + "_anexo.pdf")
                        .tipo("ANEXO_OJ")
                        .ruta(rutaAnexo)
                        .numeroIdentificacion(idGenerado)
                        .build());
            }

            // --- B. FOTO (JPG) ---
            if (foto != null && !foto.isEmpty()) {
                String rutaFoto = String.format("%s/oj/fotos/%s/%s.jpg", ftpRutaBase, anio, idGenerado);
                ftpPort.uploadFileFTP(usuario, rutaFoto, foto.getInputStream(), "Foto OJ");

                archivosPersistencePort.guardarReferenciaArchivo(Archivo.builder()
                        .nombre(idGenerado + "_foto.jpg")
                        .tipo("FOTO_OJ")
                        .ruta(rutaFoto)
                        .numeroIdentificacion(idGenerado)
                        .build());
            }

        } catch (Exception e) {
            log.error("Error subiendo archivos OJ. Iniciando Rollback.", e);
            throw new Exception("Error crítico al subir evidencias: " + e.getMessage());
        } finally {
            ftpPort.finalizarSession(usuario);
        }

        return registrado;
    }
}