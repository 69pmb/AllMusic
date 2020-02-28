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
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
 */
public final class SearchUtils {

    private static final Logger LOG = LogManager.getLogger(SearchUtils.class);
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
     * @param compoList     la liste de compo dans laquelle rechercher
     * @param criteria      les critères
     * @param searchInFiles si on doit filtrer ou non les fichiers des compos
     * @param searchMethod  {@link SearchMethod} la façon de rechercher dans les
     *                      chaines de caractères
     * @param deleted       if false return only compositions not deleted, if true
     *                      all compositions
     * @param log           if true the method is logged
     * @return la liste de compo filtrée selon les critères
     */
    public static List<Composition> search(List<Composition> compoList, Map<String, String> criteria,
            final boolean searchInFiles, SearchMethod searchMethod, boolean deleted, boolean log) {
        if (log) {
            LOG.debug("Start search");
        }
        final JaroWinklerDistance jaro = new JaroWinklerDistance();
        List<Composition> result = new ArrayList<>(compoList);
        Map<String, String> cleanedCriteria = cleanCriteria(criteria);

        // Si on doit chercher dans les compos
        final boolean searchCompo = cleanedCriteria.keySet().stream()
                .anyMatch(value -> List.of(CRITERIA_ARTIST, CRITERIA_TITRE, CRITERIA_RECORD_TYPE).contains(value));
        // Si on doit chercher dans les fichiers
        final boolean searchFile = cleanedCriteria.keySet().stream()
                .anyMatch(value -> List
                        .of(CRITERIA_PUBLISH_YEAR, CRITERIA_PUBLISH_YEAR_RANGE, CRITERIA_FILENAME, CRITERIA_AUTHOR,
                                CRITERIA_CAT, CRITERIA_DATE_BEGIN, CRITERIA_DATE_END, CRITERIA_SORTED, CRITERIA_TOP)
                        .contains(value));

        if (searchCompo || searchFile) {
            if (log) {
                LOG.debug("searchInFiles: {}, searchMethod: {}, deleted: {}", searchInFiles, searchMethod, deleted);
                Optional.of(cleanedCriteria.entrySet().stream().map(entry -> entry.getKey() + " - " + entry.getValue())
                        .collect(Collectors.joining(", ")))
                .ifPresent(crit -> LOG.debug("Critères de recherche: {}", crit));
            }
            result = result.parallelStream().map(
                    c -> filterCompositions(searchMethod, searchInFiles, jaro, cleanedCriteria, deleted, searchFile, c))
                    .filter(Objects::nonNull).collect(Collectors.toList());
        } else if (!deleted) {
            result = result.parallelStream().filter(c -> !c.isDeleted()).collect(Collectors.toList());
        }
        if (log) {
            LOG.debug("End search");
        }
        return result;
    }

    /**
     * Removes from given map entry with empty values.
     *
     * @param map map to clean
     * @return map cleaned
     */
    public static Map<String, String> cleanCriteria(Map<String, String> map) {
        return map.entrySet().stream().filter(entry -> StringUtils.isNotBlank(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Composition filterCompositions(final SearchMethod searchMethod, final boolean searchInFiles,
            final JaroWinklerDistance jaro, final Map<String, String> criteria, final boolean deleted,
            final boolean searchFile, Composition c) {
        Composition co = new Composition(c);

        boolean result = true;
        if (criteria.containsKey(CRITERIA_ARTIST)) {
            result = compareString(criteria.get(CRITERIA_ARTIST), co.getArtist(), searchMethod, jaro);
        }
        if (result && criteria.containsKey(CRITERIA_TITRE)) {
            result = compareString(criteria.get(CRITERIA_TITRE), co.getTitre(), searchMethod, jaro);
        }
        if (result && criteria.containsKey(CRITERIA_RECORD_TYPE)) {
            result = Arrays.stream(StringUtils.split(criteria.get(CRITERIA_RECORD_TYPE), ";"))
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
     * @param s1   a string
     * @param s2   another string
     * @return true if the score is equal or greater than the limit
     */
    private static boolean isEqualsJaroForSearch(final JaroWinklerDistance jaro, String s1, String s2) {
        String removePunctuation2 = MiscUtils.removePunctuation(s2);
        String removePunctuation1 = MiscUtils.removePunctuation(s1);
        return isEqualsJaro(jaro, removePunctuation2, removePunctuation1, Constant.SCORE_LIMIT_SEARCH)
                || StringUtils.containsIgnoreCase(removePunctuation2, removePunctuation1);
    }

    /**
     * Return if the two given text are equals if their {@link JaroWinklerDistance}
     * score is greater than the given limit.
     *
     * @param jaro  a {@link JaroWinklerDistance} instance
     * @param text1 a string
     * @param text2 another string
     * @param limit the jaro score limit
     * @return true if the score is equal or greater than the limit
     */
    public static boolean isEqualsJaro(JaroWinklerDistance jaro, String text1, String text2, BigDecimal limit) {
        return BigDecimal.valueOf(jaro.apply(text1, text2)).compareTo(limit) > 0;
    }

    /**
     * Checks if given {@link Fichier} matches given criteria (author, filename,
     * category...).
     *
     * @param searchMethod searching method for file name
     * @param jaro         jaro instance
     * @param criteria     search criteria
     * @param fi           file to check
     * @return if true the file matchs, false otherwise
     */
    public static boolean filterFichier(final SearchMethod searchMethod, JaroWinklerDistance jaro,
            final Map<String, String> criteria, Fichier fi) {
        Predicate<String> byFileName = key -> {
            boolean result = true;
            String fileName = criteria.get(key);
            if (SearchMethod.CONTAINS == searchMethod) {
                result = Arrays.stream(fileName.split(Constant.REGEX_PUNCTUATION))
                        .filter(StringUtils::isNotBlank)
                        .allMatch(name -> compareString(name, fi.getFileName(), searchMethod, jaro));
            } else {
                result = compareString(fileName, fi.getFileName(), searchMethod, jaro);
            }
            return result;
        };
        Predicate<String> publishYear = key -> filterByPublishYear(fi, criteria.get(key),
                criteria.get(CRITERIA_PUBLISH_YEAR_RANGE));
        Predicate<String> author = key -> compareString(fi.getAuthor(), criteria.get(key), searchMethod, jaro);
        Predicate<String> category = key -> Arrays.stream(StringUtils.split(criteria.get(key), ";"))
                .anyMatch((c -> fi.getCategorie() == Cat.getByValue(c)));
        Predicate<String> rangeDateBegin = key -> fi.getRangeDateBegin() >= Integer.parseInt(criteria.get(key));
        Predicate<String> rangeDateEnd = key -> fi.getRangeDateEnd() <= Integer.parseInt(criteria.get(key));
        Predicate<String> sorted = key -> BooleanUtils.toBoolean(criteria.get(key)) == fi.getSorted();
        Predicate<String> top = key -> BooleanUtils.toBoolean(criteria.get(key)) && fi.getClassement() <= 10
                && fi.getSorted();

        return Map
                .of(CRITERIA_FILENAME, byFileName, CRITERIA_AUTHOR, author, CRITERIA_CAT, category, CRITERIA_DATE_BEGIN,
                        rangeDateBegin, CRITERIA_DATE_END, rangeDateEnd, CRITERIA_SORTED, sorted, CRITERIA_TOP, top,
                        CRITERIA_PUBLISH_YEAR, publishYear)
                .entrySet().stream().filter(entry -> criteria.containsKey(entry.getKey()))
                .allMatch(entry -> entry.getValue().test(entry.getKey()));
    }

    private static boolean filterByPublishYear(Fichier fi, final String publish, final String publishRange) {
        boolean result;
        switch (SearchRange.getByValue(publishRange)) {
        case EQUAL:
            result = fi.getPublishYear() == Integer.parseInt(publish);
            break;
        case GREATER:
            result = fi.getPublishYear() >= Integer.parseInt(publish);
            break;
        case LESS:
            result = fi.getPublishYear() <= Integer.parseInt(publish);
            break;
        default:
            result = false;
            break;
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
     * @param s1           a string
     * @param s2           another string
     * @param searchMethod {@link SearchMethod} the way of comparing the strings
     * @param jaro         a jaro wrinkler instance if needed
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
     * stripRegexCharacters(null)    = ""
     * stripRegexCharacters("")        = ""
     * stripRegexCharacters("aa")    = "aa"
     * stripRegexCharacters("a*")    = "a"
     * stripRegexCharacters("a(b")    = "ab"
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
}
