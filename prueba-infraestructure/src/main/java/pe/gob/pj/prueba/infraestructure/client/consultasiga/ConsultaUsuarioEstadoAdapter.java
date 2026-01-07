package pe.gob.pj.prueba.infraestructure.client.consultasiga;

import org.springframework.stereotype.Service;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.domain.exceptions.general.EndpointNoSatisfactorioException;
import pe.gob.pj.prueba.domain.model.client.consultasiga.response.ConsultaPersonalEstadoResponse;
import pe.gob.pj.prueba.domain.port.client.consultasiga.ConsultaUsuarioEstadoPort;
import pe.gob.pj.prueba.infraestructure.properties.ConsultaSigaProperty;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ConsultaUsuarioEstadoAdapter implements ConsultaUsuarioEstadoPort {

  ConsultaSigaClient consultaSigaClient;
  ConsultaSigaProperty consultaSigaProperties;

  @Override
  public ConsultaPersonalEstadoResponse consultarPorDocumento(String cuo,
      String numeroDocumentoIdentidad) {
    try {
      return consultaSigaClient
          .consultarUsuarioEstado("Bearer " + obtenerToken(cuo), numeroDocumentoIdentidad)
          .getBody();
    } catch (Exception e) {
      log.error("{} Error al consultar Siga consulta/usuario-estado : ", cuo, e);
      throw new EndpointNoSatisfactorioException();
    }
  }

  private String obtenerToken(String cuo) {
    try {
      return consultaSigaClient
          .obtenerToken(consultaSigaProperties.getUsuario(), consultaSigaProperties.clave(),
              consultaSigaProperties.rol(), consultaSigaProperties.cliente())
          .getBody().getToken();
    } catch (Exception e) {
      log.error("{} Error al consultar Siga api/authenticate : ", cuo, e);
      throw new EndpointNoSatisfactorioException();
    }
  }

}
