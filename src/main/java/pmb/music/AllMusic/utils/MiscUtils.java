package pmb.music.AllMusic.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.OptionalDouble;

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

	/**
	 * Calculates the median.
	 * @param numArray
	 * @return
	 */
	public static double median(List<Integer> numArray) {
		numArray.sort(Integer::compareTo);
		double median;
		if (numArray.size() % 2 == 0) {
			median = ((double) numArray.get(numArray.size() / 2) + (double) numArray.get(numArray.size() / 2 - 1)) / 2;
		} else {
			median = (double) numArray.get(numArray.size() / 2);
		}
		return median;
	}

	/**
	 * Calculates the standard deviation.
	 * @param numArray
	 * @param average
	 * @param sum
	 * @param count
	 * @return
	 */
	public static double calculateSD(List<Integer> numArray, OptionalDouble average, int sum, long count) {
		double standardDeviation = 0.0;

		for (double num : numArray) {
			standardDeviation += Math.pow(num - average.getAsDouble(), 2);
		}

		return Math.sqrt(standardDeviation / count);
	}
}
