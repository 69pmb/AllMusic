package pmb.allmusic.utils;

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
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collector;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pmb.allmusic.model.Composition;
import pmb.my.starter.utils.VariousUtils;

/**
 * Utility class of generic methods.
 *
 * <p>Created by PBR on 29 mai 2018.
 */
public final class MiscUtils {

  private static final HttpClient httpClient =
      HttpClient.newBuilder()
          .version(HttpClient.Version.HTTP_2)
          .followRedirects(Redirect.ALWAYS)
          .build();
  private static final Configuration jsonPathConfig =
      Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
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
      objectMapper =
          new ObjectMapper()
              .registerModule(new ParameterNamesModule())
              .registerModule(new Jdk8Module())
              .registerModule(new JavaTimeModule());
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
  public static Map<String, List<Composition>> readValueAsMapOfList(String content)
      throws IOException {
    return getObjectMapper()
        .readValue(content, new TypeReference<Map<String, List<Composition>>>() {});
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
   * Adds multiples elements to a vector.
   *
   * @param v the vector
   * @param elements to add
   */
  public static void addElements(Vector<Object> v, Object... elements) {
    Arrays.stream(elements).forEach(v::addElement);
  }

  /**
   * Returns a {@code Collector} that accumulates the input elements into a new {@code Vector}.
   * There are no guarantees on the type, mutability, serializability, or thread-safety of the
   * {@code Vector} returned.
   *
   * @param <T> the type of the input elements
   * @return a {@code Collector} which collects all the input elements into a {@code Vector}, in
   *     encounter order
   */
  public static <T> Collector<T, Vector<T>, Vector<T>> toVector() {
    return Collector.of(
        Vector::new,
        Vector::addElement,
        (left, right) -> {
          left.addAll(right);
          return left;
        });
  }

  /**
   * Searches with wikipedia's API prefix by title, if no result returns basic search url.
   *
   * @param prefixSearchTerm term to use for prefix search
   * @param basicSearchTerm term to use in addition for basic search
   * @return wikipedia url
   */
  public static String wikipediaSearch(String prefixSearchTerm, String basicSearchTerm) {
    URI uri =
        URI.create(
            "https://en.wikipedia.org/w/api.php?action=query&format=json&list=prefixsearch&pssearch="
                + VariousUtils.urlEncode(prefixSearchTerm));
    HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();
    DocumentContext jsonContext = null;
    try {
      String body = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
      LOG.debug("body: {}", body);
      jsonContext = JsonPath.parse(body, jsonPathConfig);
    } catch (IOException | InterruptedException e) {
      Thread.currentThread().interrupt();
      LOG.warn("Error when calling wikipedia url: {}", uri, e);
    }
    return Optional.ofNullable(jsonContext)
        .map(s -> (String) s.read("$.query.prefixsearch[0].title"))
        .map(s -> "https://en.wikipedia.org/wiki/" + s.replaceAll("\\s", "_"))
        .orElse(
            "https://en.wikipedia.org/w/index.php?sort=relevance&search="
                + VariousUtils.urlEncode(prefixSearchTerm + " " + basicSearchTerm)
                + "+&title=Special:Search&profile=advanced&fulltext=1&advancedSearch-current=%7B%7D&ns0=1");
  }
}
