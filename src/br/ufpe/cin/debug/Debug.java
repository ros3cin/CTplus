package br.ufpe.cin.debug;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author RENATO
 *
 */
final public class Debug {
	private static final boolean DEBUG = false;
	private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss.SSS"; 
	public static final Logger logger = LogManager.getLogger(Debug.class);
	
	private Debug() {}
	
	/**
	 * Prints the desired message if the DEBUG flag is on
	 * @param message
	 */
	public static void println(String message) {
		if(DEBUG) {
			logger.info(message);
		}
	}
	
	/**
	 * Gets the current time following the format: MM/dd/yyyy HH:mm:ss.SSS
	 * @return
	 */
	public static String getCurrentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		return sdf.format(new Date());
	}
}
