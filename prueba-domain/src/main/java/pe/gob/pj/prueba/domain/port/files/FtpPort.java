package pe.gob.pj.prueba.domain.port.files;

import java.io.InputStream;

/**
 * Puerto que define los métodos que se usaran para trabajar con ftp
 * 
 * @author oruizb
 * @version 1.0,07/02/2022
 */
public interface FtpPort {
	
	/**
	 * Método que sirve para conectarse e iniciar sesión al ftp
	 * @param cuo 		Código único de operación
	 * @param ip		ip del servidor ftp
	 * @param puerto	puerto del servidor ftp
	 * @param usuario	usuario para conectarse al ftp
	 * @param clave		password para conectarse al ftp
	 * @throws Exception
	 */
	public void iniciarSesion (String cuo, String ip, Integer puerto, String usuario, String clave) throws Exception;
	
	/**
	 * Método que sirve para cerrar sesión y desconectarse del ftp
	 * @param cuo Código único de operación
	 * @throws Exception
	 */
	public void finalizarSession (String cuo) throws Exception;

    /**
     * Subir el archivo al servidor FTP
     *
     * @param cuo         Código Único de Operación
     * @param srcFtpPDF   es la ruta completa del archivo en el ftp donde seguardara
     * @param inputStream es la entrada de streams del archivo a guardar
     * @param desc        es la descripción general del documento
     * @throws Exception
     */
    public default boolean uploadFileFTP(String cuo, String srcFtpPDF, InputStream inputStream, String desc) throws Exception {
        return false;
    }

    /**
	 * Método que sirve para descargar archivo
	 * @param ruta		ruta completa de donde se encuentra el archivo
	 * @return
	 * @throws Exception
	 */
	public byte[] downloadFileBytes(String cuo, String ruta)  throws Exception;
	
	/**
	 * Método que sirve para descargar archivo
	 * @param ruta		Ruta completa incluido el nombre de archivo
	 * @return			Flujo de stream del archivo a descargar
	 * @throws Exception
	 */
	public InputStream downloadFileStream(String cuo, String ruta)  throws Exception;

	boolean deleteFileFTP(String rutaCompleta) throws Exception;

	InputStream descargarArchivo(String rutaCompleta) throws Exception;

	boolean completarTransferencia(String cuo) throws Exception;
}
