package pe.gob.pj.prueba.infraestructure.common.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utilitario para obtener datos de Network usados para las auditorias
 * 
 * @author oruizb
 * @version 1.0,07/02/2022
 */
@Slf4j
@UtilityClass
public class InformacionRedUtils {

  public String getPc() {
    String pc = null;
    try {
      InetAddress addr;
      addr = InetAddress.getLocalHost();
      pc = addr.getHostName();
    } catch (UnknownHostException e) {
      pc = "host";
    }

    return pc;
  }

  public String getIp() {
    String ip = null;
    try {
      ip = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      ip = "0.0.0.0";
    }

    return ip;
  }

  public String getNombreRed() {
    String nombreRed = "Desconocido";
    try {
      Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
      while (networkInterfaces.hasMoreElements()) {
        NetworkInterface networkInterface = networkInterfaces.nextElement();
        Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
        while (inetAddresses.hasMoreElements()) {
          InetAddress inetAddress = inetAddresses.nextElement();
          if (!inetAddress.isLoopbackAddress()) {
            nombreRed = networkInterface.getDisplayName();
          }
        }
      }
    } catch (SocketException e) {
      e.printStackTrace();
    }
    return nombreRed.length() < 30 ? nombreRed : nombreRed.substring(0, 29);
  }

  public String getMac() {
    Map<String, String> addressByNetwork = new HashMap<>();
    String firstInterface = null;
    try {
      Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
      while (networkInterfaces.hasMoreElements()) {
        NetworkInterface network = networkInterfaces.nextElement();
        byte[] bmac = network.getHardwareAddress();
        if (bmac != null) {
          String macAddress = formatMacAddress(bmac);
          addressByNetwork.put(network.getName(), macAddress);
          firstInterface = network.getName();
        }
      }
    } catch (SocketException e) {
      log.warn("No se pudo obtener la mac.");
    }
    return Optional.ofNullable(firstInterface).map(addressByNetwork::get).orElse(null);
  }

  private static String formatMacAddress(byte[] mac) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < mac.length; i++) {
      sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
    }
    return sb.toString();
  }

}
