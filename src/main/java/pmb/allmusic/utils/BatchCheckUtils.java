package pmb.allmusic.utils;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pmb.allmusic.model.Composition;
import pmb.allmusic.model.Fichier;
import pmb.allmusic.model.RecordType;
import pmb.allmusic.model.SearchMethod;
import pmb.allmusic.view.panel.BatchCheckPanel;
import pmb.allmusic.view.panel.OngletPanel;
import pmb.allmusic.xml.ImportXML;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyFileUtils;
import pmb.my.starter.utils.VariousUtils;

/**
 * Utility class that contains all processes for the {@link BatchCheckPanel}.
 *
 * @see {@link BatchUtils} for common method of batch process.
 */
public class BatchCheckUtils extends BatchUtils {
  private static final Logger LOG = LogManager.getLogger(BatchCheckUtils.class);

  /**
   * Search if a composition has similar files (same author and same rank).
   *
   * @return the file name of the result file
   */
  public static String findDuplicateFiles() {
    LOG.debug("Start findDuplicateFiles");
    StringBuilder text = new StringBuilder();
    addLine(text, "FindDuplicateFiles: ", true);

    Map<String, Integer> result = new HashMap<>();
    ImportXML.importXML(Constant.getFinalFilePath())
        .forEach(
            composition -> {
              for (int i = 0; i < composition.getFiles().size(); i++) {
                for (int j = i + 1; j < composition.getFiles().size(); j++) {
                  Fichier f1 = composition.getFiles().get(i);
                  Fichier f2 = composition.getFiles().get(j);
                  if (f1.getClassement().equals(f2.getClassement())
                      && StringUtils.equalsIgnoreCase(f1.getAuthor(), f2.getAuthor())) {
                    String key = f1.getFileName() + ", " + f2.getFileName();
                    result.put(key, result.getOrDefault(key, 0) + 1);
                  }
                }
              }
            });
    result.entrySet().stream()
        .filter(e -> e.getValue() > 3)
        .forEach(e -> addLine(text, e.getKey() + ": " + e.getValue(), true));

    LOG.debug("End findDuplicateFiles");
    return writeInFile(text, Constant.BATCH_FILE);
  }

  /**
   * Search if there are txt files which are not convert to xml files.
   *
   * @return the file name of the result file
   */
  public static String missingXML() {
    LOG.debug("Start missingXML");
    StringBuilder text = new StringBuilder();
    addLine(text, "MissingXML: ", true);

    // Recupère tous les nom des fichiers txt
    List<String> collectMusic =
        MyFileUtils.listFilesInFolder(
                new File(Constant.getMusicAbsDirectory()), MyConstant.TXT_EXTENSION, true)
            .stream()
            .map(File::getName)
            .map(s -> StringUtils.substringBeforeLast(s, MyConstant.TXT_EXTENSION))
            .collect(Collectors.toList());

    // Recupère tous les nom des fichiers xml
    List<String> collectXml =
        MyFileUtils.listFilesInFolder(
                new File(Constant.getXmlPath()), MyConstant.XML_EXTENSION, true)
            .stream()
            .map(File::getName)
            .map(s -> StringUtils.substringBeforeLast(s, MyConstant.XML_EXTENSION))
            .collect(Collectors.toList());

    addLine(text, "TXT: ", true);
    final String missingLog = "Missing: ";
    for (String txt : collectMusic) {
      if (collectXml.stream().noneMatch(s -> StringUtils.equalsAnyIgnoreCase(s, txt))) {
        addLine(text, missingLog + txt, true);
      }
    }
    addLine(text, "XML: ", true);
    for (String xmlFile : collectXml) {
      if (collectMusic.stream().noneMatch(s -> StringUtils.equalsAnyIgnoreCase(s, xmlFile))) {
        addLine(text, missingLog + xmlFile, true);
      }
    }

    LOG.debug("End missingXML");
    return writeInFile(text, Constant.BATCH_FILE);
  }

  /**
   * Finds all suspicous compositions:
   *
   * <ul>
   *   <li>Empty title or artist
   *   <li>Title with slash
   *   <li>Size, publish year or rank to zero
   *   <li>Rank greater than file size
   *   <li>Duplicate composition in same file
   * </ul>
   *
   * @return the file name of the result file
   */
  public static String findSuspiciousComposition() {
    LOG.debug("Start findSuspiciousComposition");
    StringBuilder result = new StringBuilder();
    addLine(result, "Suspicious: ", true);

    List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
    emptyTitleOrArtist(importXML, result);
    addLine(result, "", false);
    titleSlash(importXML, result);
    addLine(result, "", false);
    sizeZero(importXML, result);
    addLine(result, "", false);
    publishZero(importXML, result);
    addLine(result, "", false);
    rankZero(importXML, result);
    addLine(result, "", false);
    rankGreaterThanSize(importXML, result);
    addLine(result, "", false);
    duplicateCompositionInFile(importXML, result);

    LOG.debug("End findSuspiciousComposition");
    return writeInFile(result, "Suspicious.txt");
  }

  private static void titleSlash(List<Composition> importXML, StringBuilder result) {
    addLine(result, "## Title Slash: ", true);
    importXML.stream()
        .map(
            c -> {
              if (c.getFiles().size() == 1 && StringUtils.contains(c.getTitre(), "/")) {
                return c.getArtist() + " - " + c.getTitre();
              } else {
                return "";
              }
            })
        .distinct()
        .filter(StringUtils::isNotBlank)
        .sorted()
        .forEach(line -> addLine(result, line, false));
  }

  private static void rankZero(List<Composition> importXML, StringBuilder result) {
    addLine(result, "## Rank Zero: ", true);
    importXML.stream()
        .filter(c -> c.getFiles().stream().anyMatch(f -> f.getClassement() == 0))
        .forEach(
            c ->
                addLine(
                    result,
                    c.getArtist()
                        + " - "
                        + c.getTitre()
                        + " / "
                        + StringUtils.join(
                            c.getFiles().stream()
                                .filter(f -> f.getClassement() == 0)
                                .map(Fichier::getFileName)
                                .collect(Collectors.toList()),
                            ","),
                    false));
  }

  private static void rankGreaterThanSize(List<Composition> importXML, StringBuilder result) {
    addLine(result, "## Rank Greater Than Size: ", true);
    importXML.stream()
        .map(Composition::getFiles)
        .flatMap(List::stream)
        .filter(f -> f.getClassement() > f.getSize() && f.getSize() != 0)
        .collect(
            Collectors.groupingBy(
                Fichier::getFileName,
                Collectors.collectingAndThen(
                    Collectors.reducing(
                        (Fichier d1, Fichier d2) ->
                            d1.getClassement() > d2.getClassement() ? d1 : d2),
                    Optional::get)))
        .entrySet()
        .stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(
            f ->
                addLine(
                    result,
                    f.getValue().getFileName()
                        + ", size: "
                        + f.getValue().getSize()
                        + ", classement max: "
                        + f.getValue().getClassement(),
                    false));
  }

  private static void duplicateCompositionInFile(
      List<Composition> importXML, StringBuilder result) {
    addLine(result, "## Duplicate Composition In File: ", true);
    importXML.parallelStream()
        .forEach(
            c -> {
              Map<String, Long> collect =
                  c.getFiles().stream()
                      .collect(Collectors.groupingBy(Fichier::getFileName, Collectors.counting()));
              if (collect.values().stream().anyMatch(v -> v > 1)) {
                collect.entrySet().stream()
                    .forEach(
                        e -> {
                          if (e.getValue() > 1) {
                            addLine(
                                result,
                                c.getArtist()
                                    + ", "
                                    + c.getTitre()
                                    + ". "
                                    + e.getKey()
                                    + ": "
                                    + e.getValue(),
                                false);
                          }
                        });
              }
            });
  }

  private static void sizeZero(List<Composition> importXML, StringBuilder result) {
    addLine(result, "## File Size Zero: ", true);
    importXML.stream()
        .map(Composition::getFiles)
        .flatMap(List::stream)
        .filter(f -> f.getSize() == 0)
        .map(Fichier::getFileName)
        .distinct()
        .sorted()
        .forEach(f -> addLine(result, f, false));
  }

  private static void publishZero(List<Composition> importXML, StringBuilder result) {
    addLine(result, "## File Publish Year Zero: ", true);
    importXML.stream()
        .map(Composition::getFiles)
        .flatMap(List::stream)
        .filter(f -> f.getPublishYear() == 0)
        .map(Fichier::getFileName)
        .distinct()
        .sorted()
        .forEach(f -> addLine(result, f, false));
  }

  private static void emptyTitleOrArtist(List<Composition> importXML, StringBuilder result) {
    addLine(result, "## Empty Title or Artist: ", true);
    importXML.stream()
        .forEach(
            c -> {
              if (StringUtils.equalsIgnoreCase(StringUtils.trim(c.getTitre()), "")
                  || StringUtils.equalsIgnoreCase(StringUtils.trim(c.getArtist()), "")) {
                addLine(result, c.getArtist() + " - " + c.getTitre(), false);
              }
            });
  }

  /**
   * Finds compositions that have the same title but with different artists.
   *
   * @return the file name of the result file
   */
  public static String findDuplicateTitleComposition() {
    LOG.debug("Start findDuplicateTitleComposition");
    StringBuilder result = new StringBuilder();
    addLine(result, "Duplicate Title: ", true);

    similarTitle(result);

    LOG.debug("End findDuplicateTitleComposition");
    return writeInFile(result, "Duplicate Title.txt");
  }

  private static void similarTitle(StringBuilder result) {
    addLine(result, "## Same title but different artist: ", true);
    JaroWinklerSimilarity jaro = new JaroWinklerSimilarity();
    List<Composition> songs =
        SearchUtils.search(
                ImportXML.importXML(Constant.getFinalFilePath()),
                Map.of(SearchUtils.CRITERIA_RECORD_TYPE, RecordType.SONG.toString()),
                true,
                SearchMethod.CONTAINS,
                true,
                false)
            .parallelStream()
            .filter(c -> VariousUtils.removePunctuation(c.getTitre()).length() >= 11)
            .collect(Collectors.toList());
    List<Composition> res =
        songs.parallelStream()
            .flatMap(
                i -> {
                  final List<Composition> duplicated = new ArrayList<>();
                  String c2Titre = VariousUtils.removePunctuation(i.getTitre());
                  songs.parallelStream()
                      .forEach(
                          p -> {
                            String c1Titre = VariousUtils.removePunctuation(p.getTitre());
                            if ((i.getFiles().size() == 1 || p.getFiles().size() == 1)
                                && SearchUtils.isEqualsJaro(
                                    jaro, c1Titre, c2Titre, BigDecimal.valueOf(0.985D))
                                && CompositionUtils.artistJaroEquals(
                                        p.getArtist(),
                                        i.getArtist(),
                                        jaro,
                                        Constant.SCORE_LIMIT_ARTIST_FUSION)
                                    == null) {
                              duplicated.add(i);
                            }
                          });
                  return duplicated.parallelStream();
                })
            .distinct()
            .collect(Collectors.toList());
    res.stream()
        .sorted(CompositionUtils.compareTitre)
        .forEach(e -> addLine(result, e.getArtist() + " - " + e.getTitre(), false));
  }

  /**
   * Finds files with file names doesn't respect the pattern: "{@code Author} - {@code Txt filename}
   * - {@code Publish year}".
   *
   * @return the file name of the result file
   */
  public static String findIncorrectFileNames() {
    LOG.debug("Start findIncorrectFileNames");
    StringBuilder result = new StringBuilder();
    addLine(result, "IncorrectFileNames: ", true);

    List<String> res = new ArrayList<>();
    List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
    Arrays.asList(OngletPanel.getAuthorList()).parallelStream()
        .forEach(
            author -> {
              if (StringUtils.equalsIgnoreCase(author, Constant.VARIOUS_AUTHOR)) {
                return;
              }
              Map<String, String> criteria = new HashMap<>();
              criteria.put(SearchUtils.CRITERIA_AUTHOR, author);
              res.addAll(
                  SearchUtils.search(importXML, criteria, true, SearchMethod.CONTAINS, false, false)
                      .parallelStream()
                      .map(Composition::getFiles)
                      .flatMap(List::stream)
                      .filter(
                          f ->
                              (!StringUtils.startsWithIgnoreCase(
                                      f.getFileName(), f.getAuthor() + Constant.FILE_NAME_SEPARATOR)
                                  || !StringUtils.endsWithIgnoreCase(
                                      f.getFileName(),
                                      Constant.FILE_NAME_SEPARATOR
                                          + String.valueOf(f.getPublishYear()))))
                      .map(f -> f.getFileName() + " # " + f.getPublishYear())
                      .distinct()
                      .sorted()
                      .collect(Collectors.toList()));
            });
    res.stream().forEach(f -> addLine(result, f, false));

    LOG.debug("End findIncorrectFileNames");
    return writeInFile(result, "Incorrect Filenames.txt");
  }
}
