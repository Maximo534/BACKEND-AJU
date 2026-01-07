package pe.gob.pj.prueba.domain.common.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Envuelve métodos de ayuda para aplicar DRY
 * 
 * @author oruizb
 * @version 1.0,07/02/2022 
 */
@Slf4j
@UtilityClass
public class ProjectUtils {

  Random RANDOM = new Random();

  public boolean isNullOrEmpty(Object valor) {
    boolean flag = false;
    if (valor == null || (String.valueOf(valor)).trim().equalsIgnoreCase("")
        || (String.valueOf(valor)).trim().equalsIgnoreCase("null")) {
      flag = true;
    }
    return flag;
  }

  public String obtenerCodigoUnico() {
    Date fechaActual = new Date();
    SimpleDateFormat formato = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
    String strFechaActual = formato.format(fechaActual);
    int aleatorio = RANDOM.nextInt(999) + 1;
    StringBuilder cuo = new StringBuilder();
    cuo.append(strFechaActual).append(String.valueOf(aleatorio));
    return cuo.toString();
  }

  public String convertDateToString(Date fecha, String pattern) {
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    if (Objects.nonNull(fecha))
      return sdf.format(fecha);
    else
      return "";
  }

  public Date sumarRestarSegundos(Date fecha, int segundos) {
    Calendar c = Calendar.getInstance();
    c.setTime(fecha);
    c.add(Calendar.SECOND, segundos);
    return c.getTime();
  }

  public Date parseStringToDate(String fechaString, String format) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.of("es", "ES"));
    Date fechaDate = null;
    try {
      fechaDate = simpleDateFormat.parse(fechaString);
    } catch (ParseException e) {
      log.error(" Error : {}", e);
    }
    return fechaDate;
  }

  /**
   * Convierte una excepción en una cadena de texto.
   *
   * @param e La excepción a convertir.
   * @return Una cadena que representa la excepción, incluyendo su stack trace. Si la excepción es
   *         null, devuelve un mensaje personalizado.
   */
  public String convertExceptionToString(Exception e) {
    if (e == null) {
      return "Se ha producido una excepcion personalizada.";
    }
    try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
      e.printStackTrace(pw);
      return sw.toString();
    } catch (IOException ioException) {
      // Si ocurre un error al escribir el stack trace, devolvemos un mensaje de error.
      return "Error al convertir la excepción a cadena: " + ioException.getMessage();
    }
  }

}
