package pe.gob.pj.prueba.usecase.negocio;


import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.common.enums.Flag;
import pe.gob.pj.prueba.domain.common.utils.ProjectUtils;
import pe.gob.pj.prueba.domain.exceptions.general.CaptchaException;
import pe.gob.pj.prueba.domain.exceptions.negocio.CredencialesSinCoincidenciaException;
import pe.gob.pj.prueba.domain.exceptions.negocio.UsuarioSinPerfilAsignadoException;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.domain.model.negocio.Usuario;
import pe.gob.pj.prueba.domain.model.negocio.query.IniciarSesionQuery;
import pe.gob.pj.prueba.domain.port.client.google.GooglePort;
import pe.gob.pj.prueba.domain.port.persistence.negocio.AccesoPersistenceReadPort;
import pe.gob.pj.prueba.domain.port.usecase.negocio.IniciarSesionUseCasePort;
import pe.gob.pj.prueba.usecase.common.utils.CredencialesUtils;

/**
 * 
 * @author oruizb
 * @version 1.0,31/01/2025
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class IniciarSesionUseCaseAdapter implements IniciarSesionUseCasePort {

  AccesoPersistenceReadPort accesoPersistencePort;
  GooglePort googlePort;

  @Override
  @Transactional(transactionManager = "txManagerNegocio", propagation = Propagation.REQUIRES_NEW,
      readOnly = true, rollbackFor = {Exception.class, SQLException.class})
  public Usuario iniciarSesion(String cuo, IniciarSesionQuery iniciarSesionQuery,
      PeticionServicios peticion) {

    if (Flag.SI.getCodigo().equalsIgnoreCase(
        Optional.ofNullable(iniciarSesionQuery.aplicaCaptcha()).map(String::trim).orElse(null))
        && ProjectUtils.isNullOrEmpty(iniciarSesionQuery.tokenCaptcha())) {
      log.error(
          "{} Datos de validación captcha -> indicador de validación: {}, token captcha: {} y la ip de la petición {}",
          peticion.getCuo(), iniciarSesionQuery.aplicaCaptcha(), iniciarSesionQuery.tokenCaptcha(),
          peticion.getIp());
      throw new CaptchaException();
    }

    if (Flag.SI.getCodigo().equalsIgnoreCase(
        Optional.ofNullable(iniciarSesionQuery.aplicaCaptcha()).map(String::trim).orElse(null))
        && !googlePort.validarCaptcha(peticion.getCuo(), iniciarSesionQuery.tokenCaptcha(),
            peticion.getIp())) {
      log.error(
          "{} Datos de validación captcha -> indicador de validación: {}, token captcha: {} y la ip de la petición {}",
          peticion.getCuo(), iniciarSesionQuery.aplicaCaptcha(), iniciarSesionQuery.tokenCaptcha(),
          peticion.getIp());
      throw new CaptchaException();
    }

    var user = accesoPersistencePort.iniciarSesion(cuo, iniciarSesionQuery.usuario());
    if (Objects.isNull(user) || Objects.isNull(user.getClave()) || user.getClave().isBlank()
        || !CredencialesUtils.validarClave(cuo, iniciarSesionQuery.clave(), user.getClave())) {
      log.error("{} Usuario {}", cuo, user);
      throw new CredencialesSinCoincidenciaException();
    }

    if (user.getPerfiles().isEmpty()) {
      throw new UsuarioSinPerfilAsignadoException();
    }

    user.setClave("******");

    return user;

  }

}
