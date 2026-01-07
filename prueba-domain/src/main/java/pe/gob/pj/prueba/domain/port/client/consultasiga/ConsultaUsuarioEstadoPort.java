package pe.gob.pj.prueba.domain.port.client.consultasiga;

import pe.gob.pj.prueba.domain.model.client.consultasiga.response.ConsultaPersonalEstadoResponse;

public interface ConsultaUsuarioEstadoPort {

  ConsultaPersonalEstadoResponse consultarPorDocumento(String cuo,
      String numeroDocumentoIdentidad);

}
