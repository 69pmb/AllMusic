package pmb.music.AllMusic.utils;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import pmb.music.AllMusic.exception.MinorException;
import pmb.music.AllMusic.model.Composition;

/**
 * Utility class of generic methods.
 *
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

    private static final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2)
            .followRedirects(Redirect.ALWAYS).build();
    private static final Configuration jsonPathConfig = Configuration.defaultConfiguration()
            .addOptions(Option.SUPPRESS_EXCEPTIONS);
    private static ObjectMapper objectMapper;
    private static final Logger LOG = LogManager.getLogger(MiscUtils.class);

    private MiscUtils() {
        throw new AssertionError("Must not be used");
    }

    /**
     * Gets the object mapper parser initialized with appropriated modules.
     *
     * @return the object mapper
     */
    public static synchronized ObjectMapper getObjectMapper() {
        if (null == objectMapper) {
            objectMapper = new ObjectMapper().registerModule(new ParameterNamesModule())
                    .registerModule(new Jdk8Module()).registerModule(new JavaTimeModule());
        }
        return objectMapper;
    }

    /**
     * Converts to json the given object.
     *
     * @param o the object to format
     * @return a string representing the given object in json format
     * @throws JsonProcessingException if the process fails
     */
    public static String writeValueAsString(Object o) throws JsonProcessingException {
        return getObjectMapper().writeValueAsString(o);
    }

    /**
     * Parse a string representing a {@code Map<String, T>}.
     *
     * @param <T> the type of map values
     * @param content the string to parse
     * @return the map parsed
     * @throws IOException if parser fails
     */
    public static <T> Map<String, T> readValueAsMap(String content) throws IOException {
        return getObjectMapper().readValue(content, new TypeReference<Map<String, T>>() {});
    }

    /**
     * Parse a string representing a {@code Map<String, List<Composition>}.
     *
     * @param content the string to parse
     * @return the map parsed
     * @throws IOException if parser fails
     */
    public static Map<String, List<Composition>> readValueAsMapOfList(String content) throws IOException {
        return getObjectMapper().readValue(content, new TypeReference<Map<String, List<Composition>>>() {});
    }

    /**
     * Copy in the clipboard the given text.
     *
     * @param text the text to put in the clipboard
     */
    public static void clipBoardAction(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    /**
     * Clean given line with given rules.
     *
     * @param line the line to clean
     * @param entrySet key: character to replace, value: character to replace with
     * @return the line cleaned
     */
    public static String cleanLine(String line, Set<Entry<String, String>> entrySet) {
        for (Entry<String, String> entry : entrySet) {
            if (StringUtils.containsIgnoreCase(line, entry.getKey())) {
                line = StringUtils.replaceIgnoreCase(line, entry.getKey(), entry.getValue());
            }
        }
        return line;
    }

    /**
     * Retourne la date à l'instant de l'appel.
     *
     * @return la date au format dd-MM-yyyy HH-mm-ss
     */
    public static String dateNow() {
        Calendar greg = new GregorianCalendar();
        Date date = greg.getTime();
        return new SimpleDateFormat("yyyy-MM-dd HH-mm").format(date);
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
     * Sets global log level.
     * @param level the wanted level
     * @see Level
     */
    public static void setLogLevel(String level) {
        if (StringUtils.isNotBlank(level)) {
            Configurator.setLevel(Constant.PACKAGE_NAME, Level.valueOf(level));
        }
    }

    /**
     * Convert Vector<Vector<Object>> to List<String[]>.
     *
     * @param vector the vector to convert
     * @return the list converted
     */
    public static List<List<String>> convertVectorToList(Vector<Vector<Object>> vector) {
        List<List<String>> result = new ArrayList<>();
        for (Vector<Object> rowVector : vector) {
            List<String> row = new ArrayList<>();
            for (Object obj : rowVector) {
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
     * Project the given list and capitalize. Can add an other String transformation.
     *
     * @param <T> type of the given collection
     * @param list given list
     * @param projection to get the wanted field
     * @param transformations an other transformation than capitalize
     * @return a stream of String
     */
    public static <T> Supplier<Stream<String>> projectAndCapitalize(List<T> list, Function<T, String> projection, UnaryOperator<String> transformations) {
        return () -> list.parallelStream().map(projection).map(StringUtils::trim).map(WordUtils::capitalize)
                .map(s -> Optional.ofNullable(transformations).map(transfo -> transfo.apply(s)).orElse(s));
    }

    /**
     * Distinct, sort and collect to String array a given stream of String.
     *
     * @param stream a stream of String
     * @return an array of String
     */
    public static String[] distinctSortToArray(Supplier<Stream<String>> stream) {
        return stream.get().distinct().sorted().toArray(String[]::new);
    }

    /**
     * When given values of a enum, project and transform to array of String.
     *
     * @param <T> type of the enum
     * @param values all values of the enum
     * @param projection to get enum value
     * @return array of String
     */
    public static <T> String[] getEnumValues(T[] values, Function<T, String> projection) {
        return Arrays.stream(values).map(projection).toArray(String[]::new);
    }

    /**
     * Calculates the median.
     *
     * @param numArray a list of number
     * @return the median calculated
     */
    public static Double median(List<BigDecimal> numArray) {
        if (numArray.isEmpty()) {
            return 0D;
        }
        Collections.sort(numArray);
        double median;
        int halfSize = numArray.size() / 2;
        if (numArray.size() % 2 == 0) {
            median = numArray.get(halfSize).add(numArray.get(halfSize - 1)).divide(BigDecimal.valueOf(2D)).doubleValue();
        } else {
            median = numArray.get(halfSize).doubleValue();
        }
        return median;
    }

    /**
     * Calculates the standard deviation.
     *
     * @param numArray a list of number
     * @param average mean
     * @param count size of the list
     * @return the standard deviation calculated
     */
    public static Double calculateSD(List<Double> numArray, Double average, long count) {
        double standardDeviation = 0.0;
        for (Double num : numArray) {
            standardDeviation += Math.pow(num - average, 2);
        }

        return Math.sqrt(standardDeviation / count);
    }

    /**
     * Gets a random uuid without hyphens.
     * @return an uuid
     * @see UUID
     */
    public static String getUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Joins a list of uuids to a single {@link String}.
     * @param uuids list of uuids
     * @return a string comma separated
     */
    public static String uuidsToString(List<String> uuids) {
        return Optional.ofNullable(uuids).map(list -> StringUtils.join(list, ",")).orElse("");
    }

    /**
     * Splits a {@link String} containing uuids comma separated to a list.
     * @param uuids the string to split
     * @return a list of uuid
     */
    public static List<String> stringToUuids(String uuids) {
        return Optional.ofNullable(uuids).map(list -> new LinkedList<>(Arrays.asList(StringUtils.split(list, ","))))
                .orElse(new LinkedList<String>());
    }

    /**
     * Remove all punctuation and lower the case of the given text. The string is return if it's only made of punctuation.
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

    /**
     * Removes parentheses of the given text.
     *
     * @param text the text to clean
     * @return given text without parenthese and the text in it
     */
    public static String removeParentheses(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        String res = Constant.PATTERN_PARENTHESES.matcher(text).replaceAll("").toLowerCase();
        return StringUtils.isBlank(res) ? text : res;
    }

    /**
     * Returns a {@code Collector} that accumulates the input elements into a new
     * {@code Vector}. There are no guarantees on the type, mutability,
     * serializability, or thread-safety of the {@code Vector} returned.
     *
     * @param <T> the type of the input elements
     * @return a {@code Collector} which collects all the input elements into a
     *         {@code Vector}, in encounter order
     */
    public static <T> Collector<T, Vector<T>, Vector<T>> toVector() {
        return Collector.of(Vector::new, Vector::addElement, (left, right) -> {
            left.addAll(right);
            return left;
        });
    }

    /**
     * Searches with wikipedia's API prefix by title, if no result returns basic search url.
     * @param prefixSearchTerm term to use for prefix search
     * @param basicSearchTerm term to use in addition for basic search
     * @return wikipedia url
     */
    public static String wikipediaSearch(String prefixSearchTerm, String basicSearchTerm) {
        URI uri = URI.create("https://en.wikipedia.org/w/api.php?action=query&format=json&list=prefixsearch&pssearch="
                + urlEncode(prefixSearchTerm));
        HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();
        DocumentContext jsonContext = null;
        try {
            String body = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            LOG.debug("body: {}", body);
            jsonContext = JsonPath.parse(body, jsonPathConfig);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MinorException("Error when calling wikipedia url: " + uri.toString(), e);
        }
        return Optional.ofNullable(((String) jsonContext.read("$.query.prefixsearch[0].title")))
                .map(s -> "https://en.wikipedia.org/wiki/" + s.replaceAll("\\s", "_"))
                .orElse("https://en.wikipedia.org/w/index.php?sort=relevance&search="
                        + urlEncode(prefixSearchTerm + " " + basicSearchTerm)
                        + "+&title=Special:Search&profile=advanced&fulltext=1&advancedSearch-current=%7B%7D&ns0=1");
    }

    /**
     * Encodes given url.
     * @param url url to encode
     * @return encoded url
     */
    public static String urlEncode(String url) {
        return URLEncoder.encode(url, StandardCharsets.UTF_8);
    }

    /**
     * Opens given url in default browser.
     * @param url url to open
     */
    public static void openUrl(String url) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                throw new MinorException("Error when opening url: " + url, e);
            }
        } else {
			LOG.warn("Desktop not supported");
		}
    }
}
