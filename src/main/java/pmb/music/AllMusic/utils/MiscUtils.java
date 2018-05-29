package pmb.music.AllMusic.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by PBR on 29 mai 2018.
 */
public class MiscUtils {

//	private static final Logger LOG = Logger.getLogger(MiscUtils.class);
	
	private MiscUtils(){}

	/**
	 * Retourne la date à l'instant de l'appel.
	 * @return la date au format dd-MM-yyyy HH-mm
	 */
	public static String dateNow() {
		Calendar greg = new GregorianCalendar();
		Date date = greg.getTime();
		return new Constant().getSdfHistory().format(date);
	}
	
	public static String getCurrentTime() {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
}
