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
	private static final Logger logger = LogManager.getLogger(Debug.class);
	private static boolean nonBlockingErrorAlreadyOccured = false;
	
	private Debug() {}
	
	/**
	 * Prints the desired message if the DEBUG flag is on
	 * @param message
	 */
	public static void debug(String message) {
		if(DEBUG) logger.info(message);
	}
	
	/**
	 * Prints the message to console
	 * @param message
	 */
	public static void info(String message) {
		logger.info(message);
	}
	
	/**
	 * Prints error on log file
	 * @param message
	 * @param e
	 */
	public static void error(String message, Exception e) {
		logger.error(message,e);
	}
	
	public static void error(Exception e) {
		logger.error(e);
	}
	
	public static void nonBlockingError(Exception e) {
		if (!nonBlockingErrorAlreadyOccured) {
			logger.info("A non-blocking error has occurred and has been logged. "
					+ "This shouldn't compromise the analysis. "
					+ "Open an issue on GitHub with the log file (in folder logs) so the "
					+ "developers can take a look.");
			nonBlockingErrorAlreadyOccured=true;
		}
		logger.error("Non blocking error", e);
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
