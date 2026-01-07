package pe.gob.pj.prueba.infraestructure.common.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PatronValidacionConstants {

  public static final String SEXO = "[mfMF]";
  public static final String CELULAR = "\\+?([0-9])+";
  public static final String NUMBER = "\\d+";
  public static final String ALPHANUMBER = "[a-zA-Z0-9]+";
  public static final String ALPHA = "^[\\p{L}\\p{M}]+$";
  public static final String ALPHA_SPACIO = "^[\\p{L}\\p{M}]+( [\\p{L}\\p{M}]+)*$";
  public static final String ALPHANUMBER_COMAS = "[a-zA-Z0-9]+(,[a-zA-Z0-9]+)*";
  public static final String S_N = "[SN]";
  public static final String FLAG_10 = "[10]";
  public static final String FORMATO_EXPEDIENTE = "(\\d{5})[-](\\d{4})[-](\\d{1,4})[-](\\d{4})[-]([A-Za-z]{2})[-]([A-Za-z]{2})[-](\\d{2})";
  public static final String FECHA = "([0][1-9]|[1-2][0-9]|[3][0-1])/([0][1-9]|[1][0-2])/(\\d{4})";
  public static final String FECHA_DD_MM_YYYY_HH_MM_SS_SSS = "(\\d{2})/(\\d{2})/(\\d{4}) (\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3})";
  public static final String FECHA_YYYY_MM_DD_HH_MM_SS_SSS = "(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3})";
  public static final String EMAIL = "([A-Za-z0-9]+[._-]?[A-Za-z0-9]+)+@([A-Za-z0-9]+[-]?[A-Za-z0-9]+\\.)*[A-Za-z0-9]{2,6}";
  public static final String IP =  "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
  public static final String MAC = "([0-9A-F]{2}[:-]){5}([0-9A-F]{2})";

}
