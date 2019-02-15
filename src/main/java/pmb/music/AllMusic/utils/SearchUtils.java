/**
 * 
 */
package pmb.music.AllMusic.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.model.SearchMethod;
import pmb.music.AllMusic.model.SearchRange;

/**
 * Contient les méthodes de recherche dans une liste de {@link Composition} avec
 * des critères de recherche.
 * 
 * @author pmbroca
 */
public final class SearchUtils {

	private static final Logger LOG = Logger.getLogger(SearchUtils.class);
	public static final String CRITERIA_ARTIST = "artist";
	public static final String CRITERIA_TITRE = "titre";
	public static final String CRITERIA_RECORD_TYPE = "type";
	public static final String CRITERIA_PUBLISH_YEAR = "publish";
	public static final String CRITERIA_PUBLISH_YEAR_RANGE = "publishRange";
	public static final String CRITERIA_FILENAME = "fileName";
	public static final String CRITERIA_AUTHOR = "auteur";
	public static final String CRITERIA_CAT = "cat";
	public static final String CRITERIA_DATE_BEGIN = "dateB";
	public static final String CRITERIA_DATE_END = "dateE";
	public static final String CRITERIA_SORTED = "sorted";
	public static final String CRITERIA_TOP = "top";

	private SearchUtils() {
		throw new AssertionError("Must not be used");
	}

	/**
	 * Searchs in a list on compositions with the given criteria.
	 * 
	 * @param compoList la liste de compo dans laquelle rechercher
	 * @param criteria les critères
	 * @param searchInFiles si on doit filtrer ou non les fichiers des compos
	 * @param searchMethod {@link SearchMethod} la façon de rechercher dans les
	 *            chaines de caractères
	 * @param deleted if false return only compositions not deleted, if true all
	 *            compositions
	 * @param log if true the method is logged
	 * @return la liste de compo filtrée selon les critères
	 */
	public static List<Composition> search(List<Composition> compoList, Map<String, String> criteria,
			final boolean searchInFiles, SearchMethod searchMethod, boolean deleted, boolean log) {
		if (log) {
			LOG.debug("Start search");
		}
		final JaroWinklerDistance jaro = new JaroWinklerDistance();
		List<Composition> arrayList = new ArrayList<>(compoList);

		// Si on doit chercher dans les compos
		final boolean searchCompo = StringUtils.isNotBlank(criteria.get(CRITERIA_ARTIST))
				|| StringUtils.isNotBlank(criteria.get(CRITERIA_TITRE))
				|| StringUtils.isNotBlank(criteria.get(CRITERIA_RECORD_TYPE));
		// Si on doit chercher dans les fichiers
		final boolean searchFile = StringUtils.isNotBlank(criteria.get(CRITERIA_PUBLISH_YEAR))
				|| StringUtils.isNotBlank(criteria.get(CRITERIA_PUBLISH_YEAR_RANGE))
				|| StringUtils.isNotBlank(criteria.get(CRITERIA_FILENAME))
				|| StringUtils.isNotBlank(criteria.get(CRITERIA_AUTHOR))
				|| StringUtils.isNotBlank(criteria.get(CRITERIA_CAT))
				|| StringUtils.isNotBlank(criteria.get(CRITERIA_DATE_BEGIN))
				|| StringUtils.isNotBlank(criteria.get(CRITERIA_DATE_END))
				|| StringUtils.isNotBlank(criteria.get(CRITERIA_SORTED))
				|| StringUtils.isNotBlank(criteria.get(CRITERIA_TOP));

		if (searchCompo || searchFile) {
			if (log) {
				LOG.debug("searchInFiles: " + searchInFiles + ", searchMethod: " + searchMethod + ", deleted: "
						+ deleted);
				LOG.debug("Critères de recherche: " + criteria.entrySet().stream()
						.map(entry -> entry.getKey() + " - " + entry.getValue()).collect(Collectors.joining(", ")));
			}
			arrayList = arrayList.parallelStream()
					.map(c -> filterCompositions(searchMethod, searchInFiles, jaro, criteria, deleted, searchFile, c))
					.filter(Objects::nonNull).collect(Collectors.toList());
		} else if (!deleted) {
			arrayList = arrayList.parallelStream().filter(c -> !c.isDeleted()).collect(Collectors.toList());
		}
		if (log) {
			LOG.debug("End search");
		}
		return arrayList;
	}

	private static Composition filterCompositions(final SearchMethod searchMethod, final boolean searchInFiles,
			final JaroWinklerDistance jaro, final Map<String, String> criteria, final boolean deleted,
			final boolean searchFile, Composition c) {
		Composition co = new Composition(c);

		final String artist = criteria.get(CRITERIA_ARTIST);
		final String titre = criteria.get(CRITERIA_TITRE);
		final String type = criteria.get(CRITERIA_RECORD_TYPE);

		boolean result = true;
		if (StringUtils.isNotBlank(artist)) {
			result = compareString(artist, co.getArtist(), searchMethod, jaro);
		}
		if (result && StringUtils.isNotBlank(titre)) {
			result = compareString(titre, co.getTitre(), searchMethod, jaro);
		}
		if (result && StringUtils.isNotBlank(type)) {
			result = Arrays.asList(StringUtils.split(type, ";")).stream()
					.anyMatch((t -> co.getRecordType() == RecordType.getByValue(t)));
		}
		if (result && !deleted) {
			result = !co.isDeleted();
		}

		List<Fichier> files = new ArrayList<>(co.getFiles());
		if (result && searchFile && !files.isEmpty()) {
			files = files.parallelStream().filter(f -> filterFichier(searchMethod, jaro, criteria, f))
					.collect(Collectors.toList());
		}
		if (searchInFiles) {
			co.setFiles(files);
		}
		return result && !files.isEmpty() ? co : null;
	}

	/**
	 * Returns if the two given text are equals if their {@link JaroWinklerDistance}
	 * score is greater than the {@link Constant#SCORE_LIMIT_SEARCH}. Removes
	 * punctuation before testing, and tests also with a basic containsIgnoreCase.
	 * 
	 * @param jaro a {@link JaroWinklerDistance} instance
	 * @param s1 a string
	 * @param s2 another string
	 * @return true if the score is equal or greater than the limit
	 */
	private static boolean isEqualsJaroForSearch(final JaroWinklerDistance jaro, String s1, String s2) {
		String removePunctuation2 = removePunctuation(s2);
		String removePunctuation1 = removePunctuation(s1);
		return isEqualsJaro(jaro, removePunctuation2, removePunctuation1, Constant.SCORE_LIMIT_SEARCH)
				|| StringUtils.containsIgnoreCase(removePunctuation2, removePunctuation1);
	}

	/**
	 * Return if the two given text are equals if their {@link JaroWinklerDistance}
	 * score is greater than the given limit.
	 * 
	 * @param jaro a {@link JaroWinklerDistance} instance
	 * @param text1 a string
	 * @param text2 another string
	 * @param limit the jaro score limit
	 * @return true if the score is equal or greater than the limit
	 */
	public static boolean isEqualsJaro(JaroWinklerDistance jaro, String text1, String text2, BigDecimal limit) {
		return BigDecimal.valueOf(jaro.apply(text1, text2)).compareTo(limit) > 0;
	}

	/**
	 * Remove all punctuation and lower the case of the given text. The string is
	 * return if it's only made of punctuation.
	 * 
	 * @param text The String to compress
	 * @return a string with no punctuation
	 */
	public static String removePunctuation(String text) {
		if (StringUtils.isBlank(text)) {
			return "";
		}
		String trim = StringUtils.trim(text);
		String res = Constant.PATTERN_PUNCTUATION.matcher(trim).replaceAll("").toLowerCase();
		return StringUtils.isBlank(res) ? trim : res;
	}

	public static String removeParentheses(String text) {
		if (StringUtils.isBlank(text)) {
			return "";
		}
		String res = Constant.PATTERN_PARENTHESES.matcher(text).replaceAll("").toLowerCase();
		return StringUtils.isBlank(res) ? text : res;
	}

	public static boolean filterFichier(final SearchMethod searchMethod, JaroWinklerDistance jaro,
			final Map<String, String> criteria, Fichier fi) {
		boolean result = true;

		final String publish = criteria.get(CRITERIA_PUBLISH_YEAR);
		final String publishRange = criteria.get(CRITERIA_PUBLISH_YEAR_RANGE);
		final String fileName = criteria.get(CRITERIA_FILENAME);
		final String auteur = criteria.get(CRITERIA_AUTHOR);
		final String cat = criteria.get(CRITERIA_CAT);
		final String dateB = criteria.get(CRITERIA_DATE_BEGIN);
		final String dateE = criteria.get(CRITERIA_DATE_END);
		final String sorted = criteria.get(CRITERIA_SORTED);
		final String topTen = criteria.get(CRITERIA_TOP);

		if (StringUtils.isNotBlank(publish)) {
			switch (SearchRange.getByValue(publishRange)) {
			case EQUAL:
				result = result && fi.getPublishYear() == Integer.parseInt(publish);
				break;
			case GREATER:
				result = result && fi.getPublishYear() >= Integer.parseInt(publish);
				break;
			case LESS:
				result = result && fi.getPublishYear() <= Integer.parseInt(publish);
				break;
			default:
				break;
			}
		}
		if (result && StringUtils.isNotBlank(fileName)) {
			if (SearchMethod.CONTAINS == searchMethod) {
				result = result && Arrays.asList(fileName.split(" ")).stream()
						.allMatch(name -> compareString(name, fi.getFileName(), searchMethod, jaro));
			} else {
				result = result && compareString(fileName, fi.getFileName(), searchMethod, jaro);
			}
		}
		if (result && StringUtils.isNotBlank(auteur)) {
			result = result && compareString(fi.getAuthor(), auteur, searchMethod, jaro);
		}
		if (result && StringUtils.isNotBlank(cat)) {
			result = result && Arrays.asList(StringUtils.split(cat, ";")).stream()
					.anyMatch((c -> fi.getCategorie() == Cat.getByValue(c)));
		}
		if (result && StringUtils.isNotBlank(dateB)) {
			result = result && fi.getRangeDateBegin() >= Integer.parseInt(dateB);
		}
		if (result && StringUtils.isNotBlank(dateE)) {
			result = result && fi.getRangeDateEnd() <= Integer.parseInt(dateE);
		}
		if (result && StringUtils.isNotBlank(sorted)) {
			result = result && BooleanUtils.toBoolean(sorted) == fi.getSorted();
		}
		if (result && StringUtils.isNotBlank(topTen) && BooleanUtils.toBoolean(topTen)) {
			result = result && fi.getClassement() <= 10 && fi.getSorted();
		}
		return result;
	}

	/**
	 * Compares two given strings depending on the search method.
	 * <ul>
	 * <li>{@link SearchMethod#CONTAINS}:
	 * <p>
	 * {@link SearchUtils#isEqualsJaroForSearch(JaroWinklerDistance, String, String)}
	 * </p>
	 * </li>
	 * <li>{@link SearchMethod#BEGINS_WITH}:
	 * <p>
	 * {@link StringUtils#startsWithIgnoreCase(CharSequence, CharSequence)}
	 * </p>
	 * </li>
	 * <li>{@link SearchMethod#JOKER}:
	 * <p>
	 * {@link String#matches(String)}
	 * </p>
	 * </li>
	 * <li>{@link SearchMethod#WHOLE_WORD}:
	 * <p>
	 * {@link StringUtils#equalsIgnoreCase(CharSequence, CharSequence)}
	 * </p>
	 * </li>
	 * </ul>
	 * 
	 * @param s1 a string
	 * @param s2 another string
	 * @param searchMethod {@link SearchMethod} the way of comparing the strings
	 * @param jaro a jaro wrinkler instance if needed
	 * @return true if the strings are equals according to the search method, false
	 *         otherwise
	 */
	public static boolean compareString(String s1, String s2, SearchMethod searchMethod, JaroWinklerDistance jaro) {
		boolean result;
		switch (searchMethod) {
		case CONTAINS:
			result = isEqualsJaroForSearch(jaro, s1, s2);
			break;
		case BEGINS_WITH:
			result = StringUtils.startsWithIgnoreCase(s1, s2) || StringUtils.startsWithIgnoreCase(s2, s1);
			break;
		case JOKER:
			try {
				result = s1.toLowerCase().matches(stripRegexCharacters(s2))
						|| s2.toLowerCase().matches(stripRegexCharacters(s1));
			} catch (PatternSyntaxException e) {
				LOG.info("Regex not valid", e);
				result = false;
			}
			break;
		case WHOLE_WORD:
			result = StringUtils.equalsIgnoreCase(s1, s2);
			break;
		default:
			result = false;
			break;
		}
		return result;
	}

	/**
	 * Remove all regex characters from the given string.
	 * 
	 * <pre>
	 * stripRegexCharacters(null)	= ""
	 * stripRegexCharacters("")		= ""
	 * stripRegexCharacters("aa")	= "aa"
	 * stripRegexCharacters("a*")	= "a"
	 * stripRegexCharacters("a(b")	= "ab"
	 * stripRegexCharacters("ab]c") = "abc"
	 * </pre>
	 * 
	 * @param text a string
	 * @return empty string if given string blank, the string stripped of regex
	 *         characters
	 */
	private static String stripRegexCharacters(String text) {
		if (StringUtils.isBlank(text)) {
			return "";
		}
		return text.replaceAll("\\*", ".*").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\\?", "")
				.replaceAll("\\{", "").replaceAll("\\}", "").replaceAll("\\[", "").replaceAll("\\]", "")
				.replaceAll("\\+", "").toLowerCase();
	}

	/**
	 * Returne l'index de la 1ère occurence de l'élement donné dans la liste ou -1
	 * si la liste ne le contient pas.
	 * 
	 * @param list {@code List<Composition>} la liste
	 * @param o {@link Composition} l'élement à rechercher
	 * @return le numéro d'index dans la liste ou -1 si non trouvé
	 */
	public static int indexOf(List<Composition> list, Composition o) {
		int result;
		if (list == null || list.isEmpty() || o == null) {
			LOG.error("List or Composition empty: " + o);
			result = -1;
		} else {
			result = list.indexOf(new Composition(o.getArtist(), o.getFiles(), o.getTitre(), o.getRecordType(),
					o.isDeleted(), o.isCanBeMerged()));
			if (result == -1) {
				int i = -1;
				Composition composition = list.get(0);
				while (i < list.size() && !(StringUtils.equals(composition.getArtist(), o.getArtist())
							&& StringUtils.equals(composition.getTitre(), o.getTitre())
							&& StringUtils.equals(composition.getRecordType().toString(), o.getRecordType().toString())
						&& composition.getFiles().size() == o.getFiles().size())) {
					i++;
					composition = list.get(i);
				}
				if (i == list.size()) {
					LOG.error("Error in indexOf: " + o);
				} else {
					LOG.debug("indexOf found: " + o.getArtist() + " - " + o.getTitre() + " - " + i);
					result = i;
				}
			}
		}
		return result;
	}

}
