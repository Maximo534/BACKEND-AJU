package pe.gob.pj.prueba.infraestructure.client.consultasiga;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import pe.gob.pj.prueba.domain.model.client.consultasiga.response.ConsultaPersonalEstadoResponse;
import pe.gob.pj.prueba.domain.model.client.consultasiga.response.GeneraTokenResponse;
import pe.gob.pj.prueba.infraestructure.client.FeignConfig;

@FeignClient(name = "consultaSigaClient", url = "${consultasigawsrest.url}",
    configuration = FeignConfig.class)
public interface ConsultaSigaClient {

  @GetMapping("${consultasiga.wsrest.endpoints.consultaestadousuario}")
  ResponseEntity<ConsultaPersonalEstadoResponse> consultarUsuarioEstado(
      @RequestHeader("Authorization") String token,
      @RequestParam("numeroDocumentoIdentidad") String documento);

  @PostMapping("${consultasiga.wsrest.endpoints.authenticate}")
  ResponseEntity<GeneraTokenResponse> obtenerToken(@RequestHeader("username") String usuario,
      @RequestHeader("password") String clave, @RequestHeader("codigoRol") String rol,
      @RequestHeader("codigoCliente") String cliente);

}
