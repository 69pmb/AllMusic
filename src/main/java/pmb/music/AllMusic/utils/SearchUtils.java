/**
 * 
 */
package pmb.music.AllMusic.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.model.SearchMethod;

/**
 * Contient les méthodes de recherche dans une liste de {@link Composition} avec
 * des critères de recherche.
 * 
 */
public class SearchUtils {

	private static final Logger LOG = Logger.getLogger(SearchUtils.class);
	public static final String CRITERIA_ARTIST = "artist";
	public static final String CRITERIA_TITRE = "titre";
	public static final String CRITERIA_RECORD_TYPE = "type";
	public static final String CRITERIA_PUBLISH_YEAR = "publish";
	public static final String CRITERIA_FILENAME = "fileName";
	public static final String CRITERIA_AUTHOR = "auteur";
	public static final String CRITERIA_CAT = "cat";
	public static final String CRITERIA_DATE_BEGIN = "dateB";
	public static final String CRITERIA_DATE_END = "dateE";
	public static final String CRITERIA_SORTED = "sorted";
	public static final String CRITERIA_TOP = "top";

	private SearchUtils() {
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
		// Critères compositions
		final String artist = criteria.get(CRITERIA_ARTIST);
		final String titre = criteria.get(CRITERIA_TITRE);
		final String type = criteria.get(CRITERIA_RECORD_TYPE);
		// Critères fichiers
		final String publish = criteria.get(CRITERIA_PUBLISH_YEAR);
		final String fileName = criteria.get(CRITERIA_FILENAME);
		final String auteur = criteria.get(CRITERIA_AUTHOR);
		final String cat = criteria.get(CRITERIA_CAT);
		final String dateB = criteria.get(CRITERIA_DATE_BEGIN);
		final String dateE = criteria.get(CRITERIA_DATE_END);
		final String sorted = criteria.get(CRITERIA_SORTED);
		final String topTen = criteria.get(CRITERIA_TOP);

		// Si on doit chercher dans les compos
		final boolean searchCompo = StringUtils.isNotBlank(artist) || StringUtils.isNotBlank(titre)
				|| StringUtils.isNotBlank(type);
		// Si on doit chercher dans les fichiers
		final boolean searchFile = StringUtils.isNotBlank(publish) || StringUtils.isNotBlank(fileName)
				|| StringUtils.isNotBlank(auteur) || StringUtils.isNotBlank(cat) || StringUtils.isNotBlank(dateB)
				|| StringUtils.isNotBlank(dateE) || StringUtils.isNotBlank(sorted) || StringUtils.isNotBlank(topTen);

		if (searchCompo || searchFile) {
			if (log) {
				LOG.debug("searchInFiles: " + searchInFiles + ", searchMethod: " + searchMethod + ", deleted: "
						+ deleted);
				LOG.debug("Critères de recherche: " + criteria.entrySet().stream()
						.map(entry -> entry.getKey() + " - " + entry.getValue()).collect(Collectors.joining(", ")));
			}
			CollectionUtils.filter(arrayList,
					(Object c) -> filterCompositions(searchMethod, searchInFiles, jaro, artist, titre, type, deleted,
							publish, fileName, auteur, cat, dateB, dateE, sorted, topTen, searchFile, c));
		} else if (!deleted) {
			CollectionUtils.filter(arrayList, (Object c) -> !((Composition) c).isDeleted());
		}
		if (log) {
			LOG.debug("End search");
		}
		return arrayList;
	}

	private static boolean filterCompositions(final SearchMethod searchMethod, final boolean searchInFiles,
			final JaroWinklerDistance jaro, final String artist, final String titre, final String type,
			final boolean deleted, final String publish, final String fileName, final String auteur, final String cat,
			final String dateB, final String dateE, final String sorted, final String topTen, final boolean searchFile,
			Object c) {
		Composition co = (Composition) c;

		boolean result = true;
		if (StringUtils.isNotBlank(artist)) {
			result = result && compareString(artist, co.getArtist(), searchMethod, jaro);
		}
		if (result && StringUtils.isNotBlank(titre)) {
			result = result && compareString(titre, co.getTitre(), searchMethod, jaro);
		}
		if (result && type != null) {
			result = result && co.getRecordType() == RecordType.valueOf(type);
		}
		if (result && !deleted) {
			result = result && !co.isDeleted();
		}

		List<Fichier> files = new ArrayList<>(co.getFiles());
		if (result && searchFile && CollectionUtils.isNotEmpty(files)) {
			CollectionUtils.filter(files, (Object f) -> filterFichier(searchMethod, jaro, publish, fileName, auteur,
					cat, dateB, dateE, sorted, topTen, f));
		}
		if (searchInFiles) {
			co.setFiles(files);
		}
		return result && CollectionUtils.isNotEmpty(files);
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
		return isEqualsJaro(jaro, removePunctuation(s2), removePunctuation(s1), Constant.SCORE_LIMIT_SEARCH)
				|| StringUtils.containsIgnoreCase(s2, s1);
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
		return new BigDecimal(jaro.apply(text1, text2)).compareTo(limit) > 0;
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

	public static boolean filterFichier(final SearchMethod searchMethod, JaroWinklerDistance jaro, final String publish,
			final String fileName, final String auteur, final String cat, final String dateB, final String dateE,
			final String sorted, final String topTen, Object f) {
		Fichier fi = (Fichier) f;
		boolean result = true;

		if (StringUtils.isNotBlank(publish)) {
			result = result && fi.getPublishYear() == Integer.parseInt(publish);
		}
		if (result && StringUtils.isNotBlank(fileName)) {
			result = result && compareString(fileName, fi.getFileName(), searchMethod, jaro);
		}
		if (result && StringUtils.isNotBlank(auteur)) {
			result = result && compareString(fi.getAuthor(), auteur, searchMethod, jaro);
		}
		if (result && StringUtils.isNotBlank(cat)) {
			result = result && fi.getCategorie() == Cat.valueOf(cat);
		}
		if (result && StringUtils.isNotBlank(dateB)) {
			result = result && ((searchMethod.equals(SearchMethod.CONTAINS)
					&& fi.getRangeDateBegin() >= Integer.parseInt(dateB))
					|| (!searchMethod.equals(SearchMethod.CONTAINS)
							&& fi.getRangeDateBegin() == Integer.parseInt(dateB)));
		}
		if (result && StringUtils.isNotBlank(dateE)) {
			result = result
					&& ((searchMethod.equals(SearchMethod.CONTAINS) && fi.getRangeDateEnd() <= Integer.parseInt(dateE))
							|| (!searchMethod.equals(SearchMethod.CONTAINS)
									&& fi.getRangeDateEnd() == Integer.parseInt(dateE)));
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
	 * <p>
	 * {@link SearchMethod#CONTAINS}:
	 * {@link SearchUtils#isEqualsJaroForSearch(JaroWinklerDistance, String, String)}
	 * {@link SearchMethod#BEGINS_WITH}:
	 * {@link StringUtils#startsWithIgnoreCase(CharSequence, CharSequence)}
	 * {@link SearchMethod#JOKER}: {@link String#matches(String)}
	 * {@link SearchMethod#WHOLE_WORD}:
	 * {@link StringUtils#equalsIgnoreCase(CharSequence, CharSequence)}
	 * </p>
	 * 
	 * @param s1 a string
	 * @param s2 another string
	 * @param searchMethod {@link SearchMethod} the way of comparing the strings
	 * @param jaro a jaro wrinkler instance if needed
	 * @return true if the strings are equals according to the search method, false
	 *         otherwise
	 */
	private static boolean compareString(String s1, String s2, SearchMethod searchMethod, JaroWinklerDistance jaro) {
		switch (searchMethod) {
		case CONTAINS:
			return isEqualsJaroForSearch(jaro, s1, s2);
		case BEGINS_WITH:
			return StringUtils.startsWithIgnoreCase(s1, s2) || StringUtils.startsWithIgnoreCase(s2, s1);
		case JOKER:
			return s1.toLowerCase().matches(stripRegexCharacters(s2))
					|| s2.toLowerCase().matches(stripRegexCharacters(s1));
		case WHOLE_WORD:
			return StringUtils.equalsIgnoreCase(s1, s2);
		default:
			return false;
		}
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
		if (list == null || list.isEmpty() || o == null) {
			LOG.error("indexOf: " + o);
			return -1;
		}
		int indexOf = list.indexOf(new Composition(o.getArtist(), o.getFiles(), o.getTitre(), o.getRecordType()));
		if (indexOf == -1) {
			int i = 0;
			for (Composition composition : list) {
				if (StringUtils.equals(composition.getArtist(), o.getArtist())
						&& StringUtils.equals(composition.getTitre(), o.getTitre())
						&& StringUtils.equals(composition.getRecordType().toString(), o.getRecordType().toString())
						&& composition.getFiles().size() == o.getFiles().size()) {
					LOG.debug("indexOf: " + o);
					return i;
				}
				i++;
			}
			LOG.error("indexOf: " + o);
			return -1;
		}
		return indexOf;
	}

}
