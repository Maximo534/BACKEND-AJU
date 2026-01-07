package pe.gob.pj.prueba.infraestructure.common.utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.infraestructure.properties.SeguridadProperty;

/**
 * Utilitario para encriptacion, desencriptacion, hasheo y validacion de hasheo
 * 
 * @author oruizb
 * @version 1.0,07/02/2022
 */
@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EncriptacionUtils {

  SeguridadProperty seguridadProperties;
  
  String ALGORITHM = "AES";
  String TRANSFORMATION = "AES/GCM/NoPadding";
  int GCM_TAG_LENGTH = 128; // Tamaño del tag de autenticación en AES-GCM (128
                            // bits)
  int IV_LENGTH = 12; // Longitud estándar del IV para GCM (12 bytes)
  SecureRandom SECURE_RANDOM = new SecureRandom();

  int KEY_LENGTH = 32 * 8; // 32 bytes (256 bits)
  int ITERATIONS = 10000; // Aumentado para mayor seguridad
  String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";

  /**
   * Cifra una cadena ingresada utilizando AES-GCM con un IV generado aleatoriamente. AES-GCM es un
   * modo de cifrado que proporciona tanto confidencialidad como autenticidad.
   * 
   * @param cuo String código único de operación
   * @param cadena String cadena de caracteres que se quiere cifrar
   * @return Cadena cifrada en formato Base64
   */
  public Optional<String> cifrarCadena(String cuo, @NonNull String cadena) {
    return cryptBase64(cuo, cadena, Cipher.ENCRYPT_MODE);
  }

  /**
   * Decifra una cadena ingresada
   * 
   * @param cuo String código único de operación
   * @param cadenaEncriptada String cadena a desencriptar
   * @return Cadena decifrada
   */
  public Optional<String> descifrarCadena(String cuo, @NonNull String cadenaEncriptada) {
    return cryptBase64(cuo, cadenaEncriptada, Cipher.DECRYPT_MODE);
  }

  Optional<String> cryptBase64(String cuo, String input, int mode) {
    try {
      var keyBytes = deriveKey(seguridadProperties.key());
      var keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
      var cipher = Cipher.getInstance(TRANSFORMATION);

      if (mode == Cipher.ENCRYPT_MODE) {
        var iv = new byte[IV_LENGTH];
        SECURE_RANDOM.nextBytes(iv);

        cipher.init(mode, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        var encrypted = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
        var combined =
            ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();

        return Optional.of(Base64.getUrlEncoder().withoutPadding().encodeToString(combined));
      } else {

        var decodedBytes = Base64.getUrlDecoder().decode(input);
        var iv = Arrays.copyOfRange(decodedBytes, 0, IV_LENGTH);
        var encryptedBytes = Arrays.copyOfRange(decodedBytes, IV_LENGTH, decodedBytes.length);

        cipher.init(mode, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        byte[] decrypted = cipher.doFinal(encryptedBytes);
        return Optional.of(new String(decrypted, StandardCharsets.UTF_8));
      }
    } catch (Exception e) {
      log.error("{} Error al procesar la encriptación/desencriptación: {}", cuo, e);
      return Optional.empty();
    }
  }

  @SneakyThrows
  byte[] deriveKey(String secret) {
    var salt = generateFixedSalt(secret);
    var spec = new PBEKeySpec(secret.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
    var factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
    return factory.generateSecret(spec).getEncoded();
  }

  @SneakyThrows
  byte[] generateFixedSalt(String secret) {
    var digest = MessageDigest.getInstance("SHA-256");
    return Arrays.copyOf(digest.digest(secret.getBytes(StandardCharsets.UTF_8)), 16);
  }

  String bytesToHex(byte[] bytes) {
    var sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02X", b));
    }
    return sb.toString();
  }

}
