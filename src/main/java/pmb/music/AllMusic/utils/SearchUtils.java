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

/**
 * Contient les méthodes de recherche dans une liste de {@link Composition} avec
 * des critères de recherche.
 * 
 * @author pmbroca
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
	 * Les critères de recherche sont utilisés comme des {@code '%like'}.
	 * 
	 * @param compoList la liste de compo dans laquelle rechercher
	 * @param criteria les critères
	 * @param searchInFiles si on doit filtrer ou non les fichiers des compos
	 * @return la liste de compo filtrée selon les critères
	 */
	public static List<Composition> searchJaro(List<Composition> compoList, Map<String, String> criteria, final boolean searchInFiles) {
		LOG.debug("Start searchJaro");
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

		final boolean searchCompo = StringUtils.isNotBlank(artist) || StringUtils.isNotBlank(titre) || StringUtils.isNotBlank(type);

		final boolean searchFile = StringUtils.isNotBlank(publish) || StringUtils.isNotBlank(fileName) || StringUtils.isNotBlank(auteur)
				|| StringUtils.isNotBlank(cat) || StringUtils.isNotBlank(dateB) || StringUtils.isNotBlank(dateE) || StringUtils.isNotBlank(sorted) || StringUtils.isNotBlank(topTen);

		if (searchCompo || searchFile) {
			LOG.debug("Critères de recherche: " + criteria.entrySet().stream().map(entry -> entry.getKey() + " - " + entry.getValue()).collect(Collectors.joining(", ")));
			CollectionUtils.filter(arrayList,
					(Object c) -> filterJaro(searchInFiles, jaro, artist, titre, type, publish, fileName, auteur, cat, dateB, dateE, sorted, topTen, searchFile, c));
		}
		LOG.debug("End searchJaro");
		return arrayList;
	}

	private static boolean filterJaro(final boolean searchInFiles, final JaroWinklerDistance jaro, final String artist, final String titre, final String type,
			final String publish, final String fileName, final String auteur, final String cat, final String dateB, final String dateE,
			final String sorted, final String topTen, final boolean searchFile, Object c) {
		Composition co = (Composition) c;

		boolean result = true;
		if (StringUtils.isNotBlank(artist)) {
			result = result && isEqualsByJaroCriteria(jaro, artist, co.getArtist());
		}
		if (StringUtils.isNotBlank(titre)) {
			result = result && isEqualsByJaroCriteria(jaro, titre, co.getTitre());
		}
		if (type != null) {
			result = result && co.getRecordType() == RecordType.valueOf(type);
		}

		List<Fichier> files = new ArrayList<>(co.getFiles());
		if (searchFile && CollectionUtils.isNotEmpty(files) && result) {
			CollectionUtils.filter(files,
					(Object f) -> evaluateFichierStrictly(publish, fileName, auteur, cat, dateB, dateE, sorted, topTen, f));
		}
		if (searchInFiles) {
			co.setFiles(files);
		}
		return result && CollectionUtils.isNotEmpty(files);
	}

	private static boolean isEqualsByJaroCriteria(final JaroWinklerDistance jaro, final String text, String newText) {
		String noPunctuation = removePunctuation(newText);
		return isEqualsJaro(jaro, noPunctuation, text, Constant.SCORE_LIMIT_SEARCH)
				|| StringUtils.containsIgnoreCase(newText, text);
	}

	/**
	 * Return if the two given text are equals if their {@link JaroWinklerDistance}
	 * score is greater than the given limit.
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
	 * Remove all punctuation and lower the case of the given text.
	 * @param text The String to compress
	 * @return a string with no punctuation
	 */
	public static String removePunctuation(String text) {
		if (StringUtils.isBlank(text)) {
			return "";
		}
		return Constant.PATTERN_PUNCTUATION.matcher(text).replaceAll("").toLowerCase();
	}
	
	public static String removePunctuation2(String text) {
		if (StringUtils.isBlank(text)) {
			return "";
		}
		String res = Constant.PATTERN_PUNCTUATION.matcher(text).replaceAll("").toLowerCase();
		return StringUtils.isBlank(res) ? text : res;
	}
	
	public static String removeParentheses(String text) {
		if (StringUtils.isBlank(text)) {
			return "";
		}
		String res = Constant.PATTERN_PARENTHESES.matcher(text).replaceAll("").toLowerCase();
		return StringUtils.isBlank(res) ? text : res;
	}

	/**
	 * Les critères de recherche sont strict, le champ doit être exactement égal
	 * au critère.
	 * 
	 * @param compoList la liste de compo dans laquelle rechercher
	 * @param criteria les critères
	 * @return la liste de compo filtrée selon les critères
	 */
	public static List<Composition> searchStrictly(List<Composition> compoList, Map<String, String> criteria) {
		LOG.debug("Start searchStrictly");
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

		// Si on doit chercher dans les compos
		final boolean searchCompo = StringUtils.isNotBlank(artist) || StringUtils.isNotBlank(titre) || StringUtils.isNotBlank(type);
		// Si on doit chercher dans les fichiers
		final boolean searchFile = StringUtils.isNotBlank(publish) || StringUtils.isNotBlank(fileName) || StringUtils.isNotBlank(auteur)
				|| StringUtils.isNotBlank(cat) || StringUtils.isNotBlank(dateB) || StringUtils.isNotBlank(dateE);

		if (searchCompo || searchFile) {
			LOG.debug("Il y a des critères de recherche");
			CollectionUtils.filter(arrayList, (Object c) -> filterStrictly(artist, titre, type, publish, fileName, auteur, cat, dateB, dateE, searchFile, c));
		}
		LOG.debug("End searchStrictly");
		return arrayList;
	}

	private static boolean filterStrictly(final String artist, final String titre, final String type, final String publish, final String fileName,
			final String auteur, final String cat, final String dateB, final String dateE, final boolean searchFile, Object c) {
		Composition co = (Composition) c;

		boolean result = true;
		if (StringUtils.isNotBlank(artist)) {
			boolean equalsIgnoreCase = StringUtils.equalsIgnoreCase(co.getArtist(), artist);
			result = result && equalsIgnoreCase;
		}
		if (StringUtils.isNotBlank(titre)) {
			result = result && StringUtils.equalsIgnoreCase(co.getTitre(), titre);
		}
		if (type != null) {
			result = result && co.getRecordType() == RecordType.valueOf(type);
		}

		List<Fichier> files = new ArrayList<>(co.getFiles());
		if (searchFile && CollectionUtils.isNotEmpty(files)) {
			CollectionUtils.filter(files, (Object f) -> evaluateFichierContains(publish, fileName, auteur, cat, dateB, dateE, f));
		}
		result = result && CollectionUtils.isNotEmpty(files);
		return result;
	}

	private static boolean evaluateFichierContains(final String publish, final String fileName, final String auteur, final String cat, final String dateB,
			final String dateE, Object f) {
		Fichier fi = (Fichier) f;
		boolean result = true;

		if (StringUtils.isNotBlank(publish)) {
			result = result && fi.getPublishYear() == Integer.parseInt(publish);
		}
		if (StringUtils.isNotBlank(fileName)) {
			result = result && StringUtils.equalsIgnoreCase(fi.getFileName(), fileName);
		}
		if (StringUtils.isNotBlank(auteur)) {
			result = result && StringUtils.equalsIgnoreCase(fi.getAuthor(), auteur);
		}
		if (StringUtils.isNotBlank(cat)) {
			result = result && fi.getCategorie() == Cat.valueOf(cat);
		}
		if (StringUtils.isNotBlank(dateB)) {
			result = result && fi.getRangeDateBegin() == Integer.parseInt(dateB);
		}
		if (StringUtils.isNotBlank(dateE)) {
			result = result && fi.getRangeDateEnd() == Integer.parseInt(dateE);
		}
		return result;
	}

	public static boolean evaluateFichierStrictly(final String publish, final String fileName, final String auteur, final String cat, final String dateB,
			final String dateE, final String sorted, final String topTen, Object f) {
		Fichier fi = (Fichier) f;
		boolean result = true;

		if (StringUtils.isNotBlank(publish)) {
			result = result && fi.getPublishYear() == Integer.parseInt(publish);
		}
		if (StringUtils.isNotBlank(fileName)) {
			result = result && StringUtils.containsIgnoreCase(fi.getFileName(), fileName);
		}
		if (StringUtils.isNotBlank(auteur)) {
			result = result && StringUtils.containsIgnoreCase(fi.getAuthor(), auteur);
		}
		if (StringUtils.isNotBlank(cat)) {
			result = result && fi.getCategorie() == Cat.valueOf(cat);
		}
		if (StringUtils.isNotBlank(dateB)) {
			result = result && fi.getRangeDateBegin() >= Integer.parseInt(dateB);
		}
		if (StringUtils.isNotBlank(dateE)) {
			result = result && fi.getRangeDateEnd() <= Integer.parseInt(dateE);
		}
		if (StringUtils.isNotBlank(sorted)) {
			result = result && BooleanUtils.toBoolean(sorted) == fi.getSorted();
		}
		if (StringUtils.isNotBlank(topTen) && BooleanUtils.toBoolean(topTen)) {
			result = result && fi.getClassement() <= 10 && fi.getSorted();
		}
		return result;
	}

	/**
	 * Returne l'index de la 1ère occurence de l'élement donné dans la liste ou
	 * -1 si la liste ne le contient pas.
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
				if (StringUtils.equals(composition.getArtist(), o.getArtist()) && StringUtils.equals(composition.getTitre(), o.getTitre())
						&& StringUtils.equals(composition.getRecordType().toString(), o.getRecordType().toString())
						&& composition.getFiles().size() == o.getFiles().size()) {
					LOG.warn("indexOf: " + o);
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
