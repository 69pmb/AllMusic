/**
 *
 */
package pmb.allmusic.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.allmusic.model.Cat;
import pmb.allmusic.model.Composition;
import pmb.allmusic.model.Fichier;
import pmb.allmusic.model.RecordType;
import pmb.allmusic.model.SearchMethod;
import pmb.allmusic.model.SearchRange;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.VariousUtils;

/**
 * Contient les méthodes de recherche dans une liste de {@link Composition} avec
 * des critères de recherche.
 *
 * @author pmbroca
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
        String removePunctuation2 = VariousUtils.removePunctuation(s2);
        String removePunctuation1 = VariousUtils.removePunctuation(s1);
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
                result = Arrays.stream(fileName.split(MyConstant.REGEX_PUNCTUATION))
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
     * Checks if a given term can be find in a given string depending on the search
     * method.
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
     * <li>{@link SearchMethod#REGEX}:
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
     * @param term         searching term
     * @param s            string to search on
     * @param searchMethod {@link SearchMethod} the way of comparing the strings
     * @param jaro         a jaro wrinkler instance if needed
     * @return true if the strings are equals according to the search method, false
     *         otherwise
     */
    public static boolean compareString(String term, String s, SearchMethod searchMethod, JaroWinklerDistance jaro) {
        boolean result;
        switch (searchMethod) {
        case CONTAINS:
            result = isEqualsJaroForSearch(jaro, term, s);
            break;
        case BEGINS_WITH:
            result = StringUtils.startsWithIgnoreCase(term, s) || StringUtils.startsWithIgnoreCase(s, term);
            break;
        case REGEX:
            result = compareRegex(term, s);
            break;
        case WHOLE_WORD:
            result = StringUtils.equalsIgnoreCase(term, s);
            break;
        default:
            result = false;
            break;
        }
        return result;
    }

    private static boolean compareRegex(String regex, String s) {
        try {
            return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(s).find();
        } catch (PatternSyntaxException e) {
            LOG.info("Regex not valid", e);
            return false;
        }
    }

    /**
     * Find the first composition matching given predicate.
     *
     * @param list {@link List<Composition>} a composition list
     * @param criteria {@link Predicate} condition that should been fulfilled by sought composition
     * @return {@link Optional} of Composition
     */
    public static Optional<Composition> findBy(List<Composition> list, Predicate<Composition> criteria) {
        return list.stream().filter(criteria::test).findFirst();
    }
}
