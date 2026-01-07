package pe.gob.pj.prueba.infraestructure.common.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.springframework.stereotype.Component;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.prueba.infraestructure.properties.SeguridadProperty;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EncryptUtils {
  
  SeguridadProperty seguridadProperties;

  byte[] SALT = {(byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
      (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12};

  String MD5 = "MD5";
  String SHA512 = "SHA-512";

  /**
   * Convert array of byte to hexadecimal
   * 
   * @param digest Array of Byte
   * @return The String hexadecimal
   */
  String toHexadecimal(byte[] digest) {
    StringBuilder hash = new StringBuilder();
    for (byte b : digest) {
      int v = b & 0xFF;
      if (Integer.toHexString(v).length() == 1) {
        hash.append('0');
      }
      hash.append(Integer.toHexString(v));
    }
    return hash.toString();
  }

  /**
   * Convert array of byte to hexadecimal 5012
   * 
   * @param digest Array of byte
   * @return The string hexadecimal 512
   */
  private String toHexadecimal512(byte[] digest) {
    StringBuilder hash = new StringBuilder();
    for (byte b : digest) {
      int v = b & 0xFF;
      if (Integer.toHexString(v).length() == 1) {
        hash.append("00");
      }
      hash.append(Integer.toHexString(v));
    }
    return hash.toString();
  }

  /**
   * Encryption MD5
   * 
   * @param message The message to encrypt
   * @return The encrypted message
   */
  public String encryptMd5Hash(String message) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance(MD5);
      messageDigest.update(message.getBytes());
      return toHexadecimal(messageDigest.digest());
    } catch (NoSuchAlgorithmException ex) {
      log.error("Error creating digest MD5 {}", ex);
      return null;
    }
  }

  /**
   * Encryption SHA512
   * 
   * @param message The message to encrypt
   * @return The encrypted message
   */
  public String encryptSHA512Hash(String message) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance(SHA512);
      messageDigest.update(message.getBytes());
      return toHexadecimal512(messageDigest.digest());
    } catch (NoSuchAlgorithmException ex) {
      log.error("Error creating digest SHA512 {}", ex);
      return null;
    }
  }

  /**
   * Custom encryption for two messages
   * 
   * @param firstMessage The first message
   * @param secondMessage The second message
   * @return The encrypted message
   */
  public String encrypt(String firstMessage, String secondMessage) {
    return encryptSHA512Hash(encryptMd5Hash(firstMessage.toLowerCase()) + secondMessage);
  }

  public String encryptPastFrass(String property)
      throws GeneralSecurityException, UnsupportedEncodingException {
    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
    SecretKey key = keyFactory
        .generateSecret(new PBEKeySpec(seguridadProperties.key().toCharArray()));
    Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
    pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
    return base64Encode(pbeCipher.doFinal(property.getBytes("UTF-8")));
  }

  public String decryptPastFrass(String property)
      throws GeneralSecurityException, IOException {
    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
    SecretKey key = keyFactory
        .generateSecret(new PBEKeySpec(seguridadProperties.key().toCharArray()));
    Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
    pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
    return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");
  }

  public String encryptPastFrass(String property, char[] KEY)
      throws GeneralSecurityException, UnsupportedEncodingException {
    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
    SecretKey key = keyFactory.generateSecret(new PBEKeySpec(KEY));
    Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
    pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
    return base64Encode(pbeCipher.doFinal(property.getBytes("UTF-8")));
  }

  public String decryptPastFrass(String property, char[] KEY)
      throws GeneralSecurityException, IOException {
    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
    SecretKey key = keyFactory.generateSecret(new PBEKeySpec(KEY));
    Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
    pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
    return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");
  }

  String base64Encode(byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes);
  }

  byte[] base64Decode(String property) {
    return Base64.getDecoder().decode(property);
  }

}
