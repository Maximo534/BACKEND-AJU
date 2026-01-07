package pe.gob.pj.prueba.infraestructure.common.utils;

import java.util.Optional;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.infraestructure.common.enums.ErrorDefaultInfo;
import pe.gob.pj.prueba.infraestructure.common.enums.TipoError;

@Slf4j
@UtilityClass
public class ManejoExcepcionUtils {

  public void handleException(String cuo, Exception e, TipoError tipoError) {
    var mensaje = Optional.ofNullable(e.getMessage())
        .orElse(ErrorDefaultInfo.MENSAJE_NO_IDENTIFICADO.getNombre());
    var causa = obtenerCausaException(e);
    var claseMetodoLinea = obtenerClaseMetodoLineaException(e);
    log.error(String.format("%s %s | %s | %s | %s | %s | %s", cuo, ErrorDefaultInfo.TRAZA.getNombre(),
        tipoError.getCodigo(), tipoError.getDescripcionUsuario(), claseMetodoLinea, mensaje, causa));
  }

  String obtenerCausaException(Exception e) {
    StringBuilder causaString = new StringBuilder();
    Throwable causa = e.getCause();
    while (causa != null && causaString.length() < 2000) {
      causaString.append(causa).append("; ");
      causa = causa.getCause();
    }
    return causaString.length() == 0 ? ErrorDefaultInfo.CAUSA_NO_IDENTIFICADA.getNombre()
        : causaString.toString().trim();
  }

  String obtenerClaseMetodoLineaException(Exception e) {
    if (e.getStackTrace().length > 0) {
      StackTraceElement firstElement = e.getStackTrace()[0];
      return firstElement.getClassName() + "::" + firstElement.getMethodName() + "::"
          + firstElement.getLineNumber();
    }
    return ErrorDefaultInfo.CLASE_METODO_LINEA_NO_IDENTIFICADO.getNombre();
  }

}
