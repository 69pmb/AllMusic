package pmb.music.AllMusic.utils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by PBR on 29 mai 2018.
 */
public class MiscUtils {

	// private static final Logger LOG = Logger.getLogger(MiscUtils.class);

	public static Comparator<String> comparePercentage = (String s1, String s2) -> {
		return new Double(Double.parseDouble(StringUtils.replaceAll(StringUtils.substringBefore(s1, "%"), ",", ".")))
				.compareTo(new Double(
						Double.parseDouble(StringUtils.replaceAll(StringUtils.substringBefore(s2, "%"), ",", "."))));
	};

	private MiscUtils() {
	}

	private static ObjectMapper objectMapper;

	public static synchronized ObjectMapper getObjectMapper() {
		if (null == objectMapper) {
			objectMapper = new ObjectMapper();
		}
		return objectMapper;
	}

	public static String writeValueAsString(Object o) throws JsonProcessingException {
		return getObjectMapper().writeValueAsString(o);
	}

	/**
	 * Parse a string representing a {@code Map<String, String}.
	 * 
	 * @param s the string to parse
	 * @return the map parsed
	 * @throws IOException
	 */
	public static Map<String, String> readValueAsMap(String s) throws IOException {
		TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {
		};
		return getObjectMapper().readValue(s, typeRef);
	}

	/**
	 * Retourne la date à l'instant de l'appel.
	 * 
	 * @return la date au format dd-MM-yyyy HH-mm
	 */
	public static String dateNow() {
		Calendar greg = new GregorianCalendar();
		Date date = greg.getTime();
		return new Constant().getSdfHistory().format(date);
	}

	/**
	 * Returns the current time, only.
	 * 
	 * @return format: {@code hour:minute:second}
	 */
	public static String getCurrentTime() {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	/**
	 * Convert Vector<Vector<Object>> to List<String[]>.
	 * 
	 * @param vector the vector to convert
	 * @return the list converted
	 */
	public static List<List<String>> convertVectorToList(Vector<Vector<Object>> vector) {
		List<List<String>> result = new ArrayList<>();
		for (int i = 0; i < vector.size(); i++) {
			Vector<Object> rowVector = vector.get(i);
			List<String> row = new ArrayList<>();
			for (int j = 0; j < rowVector.size(); j++) {
				Object obj = rowVector.get(j);
				if (obj instanceof String) {
					row.add((String) obj);
				} else if (obj instanceof Integer || obj instanceof Long || obj instanceof Double) {
					row.add(String.valueOf(obj));
				} else if (obj instanceof Date) {
					row.add(new Constant().getSdfDate().format((Date) obj));
				}
			}
			result.add(row);
		}
		return result;
	}

	/**
	 * Calculates the median.
	 * 
	 * @param numArray
	 * @return
	 */
	public static double median(List<Integer> numArray) {
		if (numArray.isEmpty()) {
			return 0;
		}
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
	 * 
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
