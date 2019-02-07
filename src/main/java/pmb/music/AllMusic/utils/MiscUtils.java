package pmb.music.AllMusic.utils;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import pmb.music.AllMusic.model.Composition;

/**
 * Created by PBR on 29 mai 2018.
 */
public final class MiscUtils {

	public static final Comparator<String> comparePercentage = (String s1, String s2) -> Double
			.valueOf(Double.parseDouble(RegExUtils.replaceAll(StringUtils.substringBefore(s1, "%"), ",", ".")))
			.compareTo(Double.valueOf(
					Double.parseDouble(RegExUtils.replaceAll(StringUtils.substringBefore(s2, "%"), ",", "."))));

	public static final Comparator<String> compareDouble = (String s1, String s2) -> Double
			.valueOf(Double.parseDouble(RegExUtils.replaceAll(s1, ",", ".")))
			.compareTo(Double.valueOf(Double.parseDouble(RegExUtils.replaceAll(s2, ",", "."))));

	public static final Comparator<String> compareInteger = (String s1, String s2) -> {
		s1 = StringUtils.isBlank(s1) ? "0" : s1;
		s2 = StringUtils.isBlank(s2) ? "0" : s2;
		return Integer.valueOf(s1).compareTo(Integer.valueOf(s2));
	};

	private static ObjectMapper objectMapper;

	private MiscUtils() {
		throw new AssertionError("Must not be used");
	}

	public static synchronized ObjectMapper getObjectMapper() {
		if (null == objectMapper) {
			objectMapper = new ObjectMapper().registerModule(new ParameterNamesModule())
					.registerModule(new Jdk8Module()).registerModule(new JavaTimeModule());
		}
		return objectMapper;
	}

	public static String writeValueAsString(Object o) throws JsonProcessingException {
		return getObjectMapper().writeValueAsString(o);
	}

	/**
	 * Parse a string representing a {@code Map<String, T>}.
	 * 
	 * @param <T> the type of map values
	 * @param content the string to parse
	 * @return the map parsed
	 * @throws IOException
	 */
	public static <T> Map<String, T> readValueAsMap(String content) throws IOException {
		return getObjectMapper().readValue(content, new TypeReference<Map<String, T>>() {
		});
	}

	/**
	 * Parse a string representing a {@code Map<String, List<Composition>}.
	 * 
	 * @param content the string to parse
	 * @return the map parsed
	 * @throws IOException
	 */
	public static Map<String, List<Composition>> readValueAsMapOfList(String content) throws IOException {
		return getObjectMapper().readValue(content, new TypeReference<Map<String, Collection<Composition>>>() {
		});
	}

	/**
	 * Copy in the clipboard the given text.
	 * 
	 * @param text the text to put in the cipboard
	 */
	public static void clipBoardAction(String text) {
		StringSelection selection = new StringSelection(text);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, selection);
	}

	/**
	 * Retourne la date à l'instant de l'appel.
	 * 
	 * @return la date au format dd-MM-yyyy HH-mm-ss
	 */
	public static String dateNow() {
		Calendar greg = new GregorianCalendar();
		Date date = greg.getTime();
		return new Constant().getSdfDt().format(date);
	}

	/**
	 * Returns the current time, only.
	 * 
	 * @return format: {@code hour:minute:second}
	 */
	public static String getCurrentTime() {
		return DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalTime.now());
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
				} else if (obj instanceof Boolean) {
					row.add(String.valueOf(obj));
				} else if (obj instanceof Integer || obj instanceof Long || obj instanceof Double) {
					row.add(String.valueOf(obj));
				} else if (obj instanceof LocalDateTime) {
					row.add(new Constant().getDateDTF().format((LocalDateTime) obj));
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
	 * @param count
	 * @return
	 */
	public static double calculateSD(List<Integer> numArray, double average, long count) {
		double standardDeviation = 0.0;

		for (double num : numArray) {
			standardDeviation += Math.pow(num - average, 2);
		}

		return Math.sqrt(standardDeviation / count);
	}
}
