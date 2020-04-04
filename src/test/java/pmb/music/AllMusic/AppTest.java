package pmb.music.AllMusic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.exception.MajorException;
import pmb.music.AllMusic.file.CsvFile;
import pmb.music.AllMusic.file.ImportFile;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.model.SearchMethod;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FilesUtils;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.ScoreUtils;
import pmb.music.AllMusic.utils.SearchUtils;
import pmb.music.AllMusic.view.panel.ImportPanel;
import pmb.music.AllMusic.view.panel.OngletPanel;

/**
 * Unit test for simple App.
 */
@SuppressWarnings("unused")
public class AppTest {

    private static final Logger LOG = LogManager.getLogger(AppTest.class);
    static Comparator<Composition> byRank = (c1, c2) -> c1.getFiles().get(0).getClassement()
            .compareTo(c2.getFiles().get(0).getClassement());

    public static void main(String[] args) throws IOException {
        // topRecordsByPoints(importXML, RecordType.SONG, "Top All Years Songs");
        // topRecordsByPoints(importXML, RecordType.ALBUM, "Top All Years Albums");
        // stats(importXML, RecordType.ALBUM);
        // stats(importXML, RecordType.SONG);
        // gauss(importXML, RecordType.ALBUM);
        // findImportParamsForAllFiles();
        // duplicateRankInFiles(importXML);
        // setDeleted();
        // testString("Van Halen First", "Van Halen Second");
    }

    private static void setUuidForAllFiles() {
        FilesUtils.listFilesInFolder(new File(Constant.getXmlPath()), Constant.XML_EXTENSION, true).stream()
        .forEach(file -> {
            try {
                setUuid(file);
            } catch (MajorException e) {
                LOG.error("Error for: " + file.getName(), e);
            }
        });
    }

    private static void setUuid(File file) throws MajorException {
        String absolutePath = file.getAbsolutePath();
        List<Composition> importXML = ImportXML.importXML(absolutePath);
        importXML.forEach(c -> c.setUuids(Arrays.asList(MiscUtils.getUuid())));
        ExportXML.exportXML(importXML, file.getName());
    }

    // Suppression de la ponctuation
    private static void testString(String s1, String s2) {
        JaroWinklerDistance jaro = new JaroWinklerDistance();
        // Suppression de la ponctuation
        String compoTitre = MiscUtils.removePunctuation(s1);
        if (StringUtils.isBlank(compoTitre)) {
            // Si le titre n'est constitué que de ponctuation
            compoTitre = s1.toLowerCase();
        }
        // Suppression de la ponctuation
        String cTitre = MiscUtils.removePunctuation(s2);
        if (StringUtils.isBlank(cTitre)) {
            // Si le titre n'est constitué que de ponctuation
            cTitre = s2;
        }
        // Si le titre et l'artist sont similaires, on sort
        LOG.debug(s1 + " / " + s2 + " "
                + (SearchUtils.isEqualsJaro(jaro, compoTitre, cTitre, Constant.SCORE_LIMIT_TITLE_FUSION) ? "Egal"
                        : "Different"));
    }

    public static void duplicateRankInFiles() {
        List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
        List<String> nomFichier = importXML.stream().map(Composition::getFiles).flatMap(List::stream)
                .map(Fichier::getFileName).distinct().sorted().collect(Collectors.toList());
        for (String name : nomFichier) {
            List<Composition> xml = ImportXML.importXML(Constant.getXmlPath() + name + Constant.XML_EXTENSION);
            List<Integer> rankList = xml.stream().map(c -> c.getFiles().get(0).getClassement())
                    .collect(Collectors.toList());
            rankList.stream().filter(i -> Collections.frequency(rankList, i) > 1)
            .forEach(rank -> LOG.debug("name: " + name + ", rank: " + rank));
        }
    }

    public static void findImportParamsForAllFiles() {
        List<String> authorList = Arrays.asList(OngletPanel.getAuthorList());
        for (String author : authorList) {
            FilesUtils.listFilesInFolder(new File(Constant.getMusicAbsDirectory() + FileUtils.FS + author),
                    Constant.TXT_EXTENSION, true).forEach(file -> {
                        String filename = StringUtils.substringBeforeLast(file.getName(), Constant.TXT_EXTENSION);
                        if (StringUtils.startsWith(
                                FilesUtils.readFirstLine(FilesUtils.buildTxtFilePath(filename, author).get()),
                                Constant.IMPORT_PARAMS_PREFIX)) {
                            return;
                        }
                        StringBuilder log = new StringBuilder(Constant.IMPORT_PARAMS_PREFIX + Constant.NEW_LINE);
                        List<Composition> xml = ImportXML
                                .importXML(Constant.getXmlPath() + filename + Constant.XML_EXTENSION);
                        xml = xml.stream().sorted(byRank).collect(Collectors.toList());
                        List<Map<String, String>> list = findImportParamsForOneFile(filename, author, xml, log);
                        if (list.isEmpty()) {
                            log.append("### No result for: " + filename + Constant.NEW_LINE);
                        } else {
                            Map<String, String> result = extractResult(list);
                            Composition composition = xml.get(0);
                            Fichier fichier = composition.getFiles().get(0);
                            List<Composition> txtList = ImportFile.getCompositionsFromFile(file, fichier,
                                    composition.getRecordType(), result.get(ImportPanel.IMPORT_PARAM_SEPARATOR),
                                    new ArrayList<>(),
                                    Boolean.valueOf(result.get(ImportPanel.IMPORT_PARAM_ARTIST_FIRST)),
                                    Boolean.valueOf(result.get(ImportPanel.IMPORT_PARAM_REVERSE_ARTIST)),
                                    Boolean.valueOf(result.get(ImportPanel.IMPORT_PARAM_PARENTHESE)),
                                    Boolean.valueOf(result.get(ImportPanel.IMPORT_PARAM_UPPER)),
                                    Boolean.valueOf(result.get(ImportPanel.IMPORT_PARAM_REMOVE_AFTER)));
                            if (compareCompositionList(xml, txtList, log)) {
                                result.remove("type");
                                result.put(ImportPanel.IMPORT_PARAM_NAME, fichier.getFileName());
                                result.put(ImportPanel.IMPORT_PARAM_AUTEUR, fichier.getAuthor());
                                result.put(ImportPanel.IMPORT_PARAM_CREATE,
                                        new Constant().getFullDTF().format(fichier.getCreationDate()));
                                result.put(ImportPanel.IMPORT_PARAM_RECORD_TYPE,
                                        composition.getRecordType().toString());
                                result.put(ImportPanel.IMPORT_PARAM_CATEGORIE, fichier.getCategorie().toString());
                                result.put(ImportPanel.IMPORT_PARAM_RANGE_BEGIN,
                                        String.valueOf(fichier.getRangeDateBegin()));
                                result.put(ImportPanel.IMPORT_PARAM_RANGE_END,
                                        String.valueOf(fichier.getRangeDateEnd()));
                                result.put(ImportPanel.IMPORT_PARAM_SORTED, String.valueOf(fichier.getSorted()));
                                result.put(ImportPanel.IMPORT_PARAM_PUBLISH_YEAR,
                                        String.valueOf(fichier.getPublishYear()));
                                result.put(ImportPanel.IMPORT_PARAM_SIZE, String.valueOf(fichier.getSize()));
                                FilesUtils.writeMapInTxtFile(file, result);
                            }
                        }
                        if (!StringUtils.containsIgnoreCase(log, "Not the same size: ")) {
                            LOG.warn(log.toString());
                        }
                    });
        }
    }

    private static Map<String, String> extractResult(List<Map<String, String>> list) {
        Map<String, List<String>> collect = list.stream().map(x -> x.get("type"))
                .collect(Collectors.groupingBy(Function.identity()));
        int max = 0;
        String key = "";
        for (Entry<String, List<String>> entry : collect.entrySet()) {
            if (entry.getValue().size() >= max) {
                max = entry.getValue().size();
                key = entry.getKey();
            }
        }
        final String cle = key;
        return list.stream().filter(x -> StringUtils.equals(x.get("type"), cle)).findFirst().get();
    }

    private static List<Map<String, String>> findImportParamsForOneFile(String filename, String auteur,
            List<Composition> xml, StringBuilder log) {
        log.append("File: " + filename + Constant.NEW_LINE);
        File file = new File(FilesUtils.buildTxtFilePath(filename, auteur).get());
        boolean guessIfRevertArtist = guessIfRevertArtist(file);
        List<Map<String, String>> list = new ArrayList<>();
        List<String> randomLineAndLastLines = ImportFile.randomLineAndLastLines(file);
        if (randomLineAndLastLines.size() < 6) {
            log.append("Too small: " + filename + Constant.NEW_LINE);
            String first = FilesUtils.readFirstLine(file.getAbsolutePath());
            Map<String, String> findParams = findParams(filename, xml, Arrays.asList(first),
                    ImportFile.getSeparator(first), 0, 0, log);
            if (!findParams.isEmpty()) {
                return Arrays.asList(findParams);
            } else {
                return new ArrayList<>();
            }
        }
        String separator = ImportFile.getSeparator(randomLineAndLastLines.get(3));
        int offset = 0;
        for (String line : randomLineAndLastLines) {
            if (!ImportFile.isValidLine(line)) {
                offset++;
            } else {
                break;
            }
        }
        for (int i = 0; i < randomLineAndLastLines.size(); i++) {
            if (i + offset > 4) {
                offset = 0;
            }
            Map<String, String> result = findParams(filename, xml, randomLineAndLastLines, separator, i, offset, log);
            log.append("## result: " + result.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue())
                    .collect(Collectors.joining(", ")) + Constant.NEW_LINE);
            if (!result.isEmpty()) {
                if (guessIfRevertArtist) {
                    result.put(ImportPanel.IMPORT_PARAM_REVERSE_ARTIST, Boolean.toString(guessIfRevertArtist));
                }
                list.add(result);
            }
        }
        return list;
    }

    private static boolean guessIfRevertArtist(File file) {
        boolean result = false;
        int count = 0;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), Constant.ANSI_ENCODING));) {
            int countLines = ImportFile.countLines(file.getAbsolutePath(), true);
            String line = "";
            while (line != null) {
                line = br.readLine();
                if (StringUtils.contains(line, ",")) {
                    count++;
                }
            }
            double ratio = Double.valueOf(count) / Double.valueOf(countLines);
            if (ratio > 0.1) {
                result = true;
            }
        } catch (IOException e) {
            LOG.error("Erreur lors de la lecture du fichier " + file.getAbsolutePath(), e);
        }
        return result;
    }

    public static Map<String, String> findParams(String filename, List<Composition> xml,
            List<String> randomLineAndLastLines, String separator, int lineNumber, int offset, StringBuilder log) {
        final JaroWinklerDistance jaro = new JaroWinklerDistance();
        String[] split = StringUtils.split(randomLineAndLastLines.get(lineNumber + offset), separator);
        if (split == null) {
            return new HashMap<>();
        }
        log.append("split size: " + split.length + Constant.NEW_LINE);
        boolean removeAfter = false;
        if (split.length < 2) {
            return buildResultMap("0", "", true, false, false, true, false);
        } else if (split.length == 3) {
            removeAfter = true;
        }
        String txtArtist;
        int number = lineNumber;
        if (xml.get(0).getFiles().get(0).getSorted()) {
            String[] array = removeRank(StringUtils.trim(split[0]));
            txtArtist = StringUtils.trim(array[1]);
            try {
                number = Integer.valueOf(array[0]);
            } catch (NumberFormatException e) {
                txtArtist = StringUtils.trim(split[0]);
                LOG.error("split: " + StringUtils.join(Arrays.asList(split), ","));
            }
        } else {
            txtArtist = StringUtils.trim(split[0]);
        }
        String txtTitre = StringUtils.trim(split[1]);
        String xmlArtist;
        String xmlTitre;
        if (lineNumber > 4) {
            if (lineNumber == 4) {
                xmlArtist = xml.get(xml.size() - 2).getArtist();
                xmlTitre = xml.get(xml.size() - 2).getTitre();
            } else {
                xmlArtist = xml.get(xml.size() - 1).getArtist();
                xmlTitre = xml.get(xml.size() - 1).getTitre();
            }
        } else {
            final int tmp = number;
            Optional<Composition> compo = xml.stream().filter(c -> c.getFiles().get(0).getClassement() == tmp)
                    .findFirst();
            if (compo.isPresent()) {
                xmlArtist = compo.get().getArtist();
                xmlTitre = compo.get().getTitre();
            } else {
                xmlArtist = xml.get(tmp).getArtist();
                xmlTitre = xml.get(tmp).getTitre();
            }
        }
        String artistRevert = revertArtist(txtArtist);
        String titreRevert = revertArtist(txtTitre);
        String titrePar = removeParenthe(txtTitre);
        String artistPar = removeParenthe(txtArtist);
        log.append("xmlArtist: " + xmlArtist + Constant.NEW_LINE);
        log.append("xmlTitre: " + xmlTitre + Constant.NEW_LINE);
        log.append("txtArtist: " + txtArtist + Constant.NEW_LINE);
        log.append("txtTitre: " + txtTitre + Constant.NEW_LINE);
        boolean artistEquals = SearchUtils.isEqualsJaro(jaro, xmlArtist, txtArtist, Constant.SCORE_LIMIT_ARTIST_FUSION);
        boolean titreEquals = SearchUtils.isEqualsJaro(jaro, xmlTitre, txtTitre, Constant.SCORE_LIMIT_TITLE_FUSION);
        boolean titreParEqu = SearchUtils.isEqualsJaro(jaro, xmlTitre, titrePar, Constant.SCORE_LIMIT_TITLE_FUSION);
        boolean artistRevertEquals = SearchUtils.isEqualsJaro(jaro, xmlArtist, artistRevert,
                Constant.SCORE_LIMIT_ARTIST_FUSION);
        boolean titreRevertEquals = SearchUtils.isEqualsJaro(jaro, xmlArtist, titreRevert,
                Constant.SCORE_LIMIT_ARTIST_FUSION);
        if (artistEquals && titreEquals && titreParEqu
                && jaro.apply(xmlTitre, titrePar) > jaro.apply(xmlTitre, txtTitre)) {
            log.append("# Parenthèse" + Constant.NEW_LINE);
            return buildResultMap("3", separator, true, false, true, false, removeAfter);
        } else if (SearchUtils.isEqualsJaro(jaro, xmlArtist, txtTitre, Constant.SCORE_LIMIT_ARTIST_FUSION)
                && SearchUtils.isEqualsJaro(jaro, xmlTitre, artistPar, Constant.SCORE_LIMIT_TITLE_FUSION)
                && jaro.apply(xmlArtist, artistPar) > jaro.apply(xmlArtist, txtArtist)) {
            log.append("# Titre en 1er et Parenthèse" + Constant.NEW_LINE);
            return buildResultMap("4", separator, false, false, true, false, removeAfter);
        } else if (artistEquals && titreEquals) {
            log.append("# Artiste en 1er" + Constant.NEW_LINE);
            return buildResultMap("1", separator, true, false, false, false, removeAfter);
        } else if (!artistEquals && !titreEquals) {
            if (SearchUtils.isEqualsJaro(jaro, xmlArtist, txtTitre, Constant.SCORE_LIMIT_ARTIST_FUSION)
                    && SearchUtils.isEqualsJaro(jaro, xmlTitre, txtArtist, Constant.SCORE_LIMIT_TITLE_FUSION)) {
                log.append("# Titre en 1er" + Constant.NEW_LINE);
                return buildResultMap("2", separator, false, false, false, false, removeAfter);
            } else if (titreRevertEquals) {
                if (SearchUtils.isEqualsJaro(jaro, xmlTitre, txtArtist, Constant.SCORE_LIMIT_TITLE_FUSION)) {
                    log.append("# Titre en 1er et Artist reverse" + Constant.NEW_LINE);
                    return buildResultMap("6", separator, false, true, false, false, removeAfter);
                }
            }
            if (SearchUtils.isEqualsJaro(jaro, xmlArtist, artistPar, Constant.SCORE_LIMIT_ARTIST_FUSION)
                    && titreParEqu) {
                log.append("# Parenthèse" + Constant.NEW_LINE);
                return buildResultMap("3", separator, true, false, true, false, removeAfter);

            } else if (SearchUtils.isEqualsJaro(jaro, xmlArtist, titrePar, Constant.SCORE_LIMIT_ARTIST_FUSION)
                    && SearchUtils.isEqualsJaro(jaro, xmlTitre, artistPar, Constant.SCORE_LIMIT_TITLE_FUSION)) {
                log.append("# Titre en 1er et Parenthèse" + Constant.NEW_LINE);
                return buildResultMap("4", separator, false, false, true, false, removeAfter);
            }
        } else if (artistEquals && !titreEquals) {
            if (SearchUtils.isEqualsJaro(jaro, xmlTitre, titrePar, Constant.SCORE_LIMIT_TITLE_FUSION)) {
                log.append("# Parenthèse" + Constant.NEW_LINE);
                return buildResultMap("3", separator, true, false, true, false, removeAfter);
            }
        } else if (titreEquals && !artistEquals && artistRevertEquals) {
            log.append("# Artist reverse" + Constant.NEW_LINE);
            return buildResultMap("5", separator, true, true, false, false, false);
        }
        return new HashMap<>();
    }

    private static boolean compareCompositionList(List<Composition> xml, List<Composition> txt, StringBuilder log) {
        xml = xml.stream().sorted(byRank).collect(Collectors.toList());
        txt = txt.stream().sorted(byRank).collect(Collectors.toList());
        int size = xml.size();
        // if (size != txt.size()) {
        // log.append("Not the same size: " + size + " " + txt.size() + " diff: " +
        // Math.abs(size - txt.size()) + Constant.NEW_LINE);
        // return false;
        // }
        final JaroWinklerDistance jaro = new JaroWinklerDistance();
        int nbEquals = 0;
        List<Integer> rankList = txt.stream().map(Composition::getFiles).flatMap(List::stream)
                .map(f -> f.getClassement()).collect(Collectors.toList());
        for (Integer rank : rankList) {
            Optional<Composition> xmlOptional = xml.stream().filter(c -> c.getFiles().get(0).getClassement() == rank)
                    .findFirst();
            Optional<Composition> txtOptional = txt.stream().filter(c -> c.getFiles().get(0).getClassement() == rank)
                    .findFirst();
            if (!xmlOptional.isPresent() || !txtOptional.isPresent()) {
                continue;
            }
            Composition xmlComp = xmlOptional.get();
            Composition txtComp = txtOptional.get();
            boolean artistEquals = SearchUtils.isEqualsJaro(jaro, xmlComp.getArtist(), txtComp.getArtist(),
                    Constant.SCORE_LIMIT_ARTIST_FUSION);
            boolean titreEquals = SearchUtils.isEqualsJaro(jaro, xmlComp.getTitre(), txtComp.getTitre(),
                    Constant.SCORE_LIMIT_TITLE_FUSION);
            if (!artistEquals || !titreEquals) {
                log.append("Not Equals: " + rank + " " + size + Constant.NEW_LINE);
                log.append("XML: " + xmlComp.getArtist() + " - " + xmlComp.getTitre() + Constant.NEW_LINE);
                log.append("TXT: " + txtComp.getArtist() + " - " + txtComp.getTitre() + Constant.NEW_LINE);
            } else if (artistEquals && titreEquals) {
                nbEquals++;
            }
        }
        double ratio = Double.valueOf(nbEquals) / Double.valueOf(size);
        if (ratio >= 0.8 || (size - nbEquals) < 3 || (ratio >= 0.75 && size <= 30)) {
            log.append("Equals !!! : " + ratio + " " + nbEquals + "/" + size + Constant.NEW_LINE);
            return true;
        } else {
            log.append("Not the same list: " + ratio + " " + nbEquals + "/" + size + Constant.NEW_LINE);
            return false;
        }
    }

    private static Map<String, String> buildResultMap(String type, String separator, boolean artistFirst,
            boolean reverseArtist, boolean parenthese, boolean upper, boolean removeAfter) {
        Map<String, String> result = new HashMap<>();
        result.put(ImportPanel.IMPORT_PARAM_RECORD_TYPE, type);
        result.put(ImportPanel.IMPORT_PARAM_SEPARATOR, separator);
        result.put(ImportPanel.IMPORT_PARAM_ARTIST_FIRST, Boolean.toString(artistFirst));
        result.put(ImportPanel.IMPORT_PARAM_REVERSE_ARTIST, Boolean.toString(reverseArtist));
        result.put(ImportPanel.IMPORT_PARAM_PARENTHESE, Boolean.toString(parenthese));
        result.put(ImportPanel.IMPORT_PARAM_UPPER, Boolean.toString(upper));
        result.put(ImportPanel.IMPORT_PARAM_REMOVE_AFTER, Boolean.toString(removeAfter));
        return result;
    }

    public static String[] removeRank(String txt) {
        String result = txt;
        String res = StringUtils.trim(StringUtils.substringBefore(txt, Constant.DOT));
        if (StringUtils.isNumeric(res)) {
            result = StringUtils.substringAfter(txt, Constant.DOT);
        } else {
            result = StringUtils.substringAfterLast(txt, txt.split(" ")[0]);
            res = txt.split(" ")[0];
        }
        String[] array = { res, result };
        return array;
    }

    public static String removeParenthe(String txt) {
        String result = txt;
        int countMatches = StringUtils.countMatches(txt, "(");
        if (countMatches == 1) {
            result = StringUtils.trim(StringUtils.substringBefore(txt, "("));
        }
        if (countMatches > 1) {
            LOG.warn("###Trop de parenthèses, line: " + txt);
        }
        return result;
    }

    public static String revertArtist(String txtArtist) {
        String artistRevert = "";
        String[] arrayArtist = txtArtist.split(",");
        if (arrayArtist.length == 2) {
            artistRevert = StringUtils.trim(StringUtils.trim(arrayArtist[1]) + " " + StringUtils.trim(arrayArtist[0]));
        } else if (arrayArtist.length > 2) {
            LOG.warn("Error when revert artist: " + txtArtist + ", size: " + arrayArtist.length);
        }
        return artistRevert;
    }

    public static void gauss(List<Composition> importXML, RecordType type) {
        Map<Integer, Integer> map = new TreeMap<>();
        Map<String, String> criteria = new HashMap<>();
        criteria.put(SearchUtils.CRITERIA_SORTED, Boolean.TRUE.toString());
        criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, type.toString());
        List<Integer> yearList = SearchUtils.search(importXML, criteria, true, SearchMethod.CONTAINS, false, true)
                .stream().map(Composition::getFiles).flatMap(List::stream).map(Fichier::getClassement)
                .collect(Collectors.toList());
        for (Integer rank : yearList) {
            if (map.containsKey(rank)) {
                map.replace(rank, map.get(rank) + 1);
            } else {
                map.put(rank, 1);
            }
        }
        LOG.debug("Value;Count");
        for (Entry<Integer, Integer> entry : map.entrySet()) {
            LOG.debug(entry.getKey() + ";" + entry.getValue());
        }
    }

    public static void stats(List<Composition> importXML, RecordType type) {
        LOG.debug(type.toString());
        Map<String, String> criteria = new HashMap<>();
        criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, type.toString());
        criteria.put(SearchUtils.CRITERIA_SORTED, Boolean.TRUE.toString());
        List<Integer> yearList = SearchUtils.search(importXML, criteria, true, SearchMethod.CONTAINS, false, true)
                .stream().map(Composition::getFiles).flatMap(List::stream).map(Fichier::getClassement)
                .collect(Collectors.toList());
        LOG.debug("Moyenne: " + yearList.stream().mapToInt(i -> i).average());
        LOG.debug("Stats: " + yearList.stream().mapToInt(i -> i).summaryStatistics());
        // LOG.debug("Medianne: " + MiscUtils.median(yearList));
        // LOG.debug("SD: " + MiscUtils.calculateSD(yearList,
        // yearList.stream().mapToInt(i -> i).average().getAsDouble(),
        // yearList.stream().mapToInt(i -> i).count()));
    }

    public static String topRecordsByPoints(List<Composition> importXML, RecordType type, String fileName) {
        Map<String, String> criteria = new HashMap<>();
        criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, type.toString());
        List<Composition> yearList = SearchUtils.search(importXML, criteria, true, SearchMethod.CONTAINS, false, true);

        Vector<Vector<Object>> result = new Vector<>();
        for (Composition composition : yearList) {
            Vector<Object> vector = new Vector<>();
            vector.add(composition.getArtist());
            vector.add(composition.getTitre());
            vector.add(type.toString());
            vector.add(String.valueOf(composition.getFiles().stream().filter(f -> f.getCategorie() != Cat.YEAR)
                    .findFirst().orElse(composition.getFiles().get(0)).getPublishYear()));
            long sumPts = ScoreUtils.getCompositionScore(composition);
            if (sumPts > 0) {
                vector.add(sumPts);
                result.add(vector);
            }
        }
        return CsvFile.exportCsv(fileName, MiscUtils.convertVectorToList(result),
                Arrays.asList(new SortKey(3, SortOrder.DESCENDING)), null);
    }

    public static void sortedFilesByYear() {
        for (int i = 1950; i <= 2017; i++) {
            List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
            String year = String.valueOf(i);
            Map<String, String> criteria = new HashMap<>();
            criteria.put(SearchUtils.CRITERIA_CAT, Cat.YEAR.toString());
            criteria.put(SearchUtils.CRITERIA_DATE_BEGIN, year);
            criteria.put(SearchUtils.CRITERIA_DATE_END, year);
            criteria.put(SearchUtils.CRITERIA_PUBLISH_YEAR, year);
            criteria.put(SearchUtils.CRITERIA_SORTED, Boolean.TRUE.toString());
            criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, RecordType.SONG.toString());
            List<Composition> yearList = SearchUtils.search(importXML, criteria, true, SearchMethod.CONTAINS, false,
                    true);
            // LOG.debug("year: " + year + " size: " + yearList.size());
            LOG.debug("year: " + year + " file size: " + yearList.stream().map(Composition::getFiles)
                    .flatMap(List::stream).map(Fichier::getAuthor).map(WordUtils::capitalize).distinct().count());
        }
    }

    /**
     * Tests the recover of information from txt files.
     */
    public static void randomLineTest() {
        FilesUtils.listFilesInFolder(new File(Constant.getMusicAbsDirectory()), Constant.TXT_EXTENSION, true)
        .forEach(file -> {
            LOG.error(file.getName());
            Fichier fichier = ImportFile.convertOneFile(file);
            List<String> randomLineAndLastLines = ImportFile.randomLineAndLastLines(file);
            fichier.setSorted(ImportFile.isSorted(randomLineAndLastLines.get(3)));
            fichier.setSize(ImportFile.determineSize(fichier, randomLineAndLastLines, file.getAbsolutePath()));
            LOG.error(fichier.getSize());
        });
    }

    /**
     * Merge similar txt files.
     *
     * @param args
     */
    public static void mergeFile(String[] args) {
        LOG.debug("Debut");
        File first = new File(Constant.getMusicAbsDirectory() + "Rolling Stone\\Rolling Stone - 500 Albums.txt");
        File sec = new File(Constant.getMusicAbsDirectory() + "Rolling Stone\\Rolling Stone - 500 Albums - 2012.txt");
        List<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(first), Constant.ANSI_ENCODING));) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        JaroWinklerDistance jaro = new JaroWinklerDistance();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(sec), Constant.ANSI_ENCODING));) {
            String line;
            while ((line = br.readLine()) != null) {
                final String str = StringUtils.substringAfter(line, ". ");
                if (!list.stream().anyMatch(s -> StringUtils.equalsIgnoreCase(StringUtils.substringAfter(s, ". "), str))
                        && !list.stream().anyMatch(
                                s -> CompositionUtils.artistJaroEquals(str, StringUtils.substringAfter(s, ". "), jaro,
                                        Constant.SCORE_LIMIT_ARTIST_FUSION) != null)) {
                    list.add(line);
                }
            }
        } catch (IOException e1) {
        }
        list = list.stream().sorted((s1, s2) -> Integer.compare(Integer.valueOf(StringUtils.substringBefore(s1, ". ")),
                Integer.valueOf(StringUtils.substringBefore(s2, ". ")))).collect(Collectors.toList());
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(Constant.getMusicAbsDirectory() + "RS.txt"), Constant.ANSI_ENCODING));) {
            for (String str : list) {
                writer.append(str).append(Constant.NEW_LINE);
            }
            writer.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        LOG.debug("Fin");
    }

    @Test
    public void removeParenthese() {
        String test1 = "hello (and truc)";
        String test2 = "(and truc) hello";
        String test3 = "hello, bonjour (and truc)";
        String test4 = "(and truc) hello, bonjour";
        String test5 = "hello, bonjour";
        String test6 = "hello, bonjour";
        String test7 = "hello, bonjour [and thed]";
        String test8 = "[and thed] hello, bonjour";

        Assert.assertEquals("hello ", MiscUtils.removeParentheses(test1));
        Assert.assertEquals(" hello", MiscUtils.removeParentheses(test2));
        Assert.assertEquals("hello, bonjour ", MiscUtils.removeParentheses(test3));
        Assert.assertEquals(" hello, bonjour", MiscUtils.removeParentheses(test4));
        Assert.assertEquals("hello, bonjour", MiscUtils.removeParentheses(test5));
        Assert.assertEquals("hello, bonjour", MiscUtils.removeParentheses(test6));
        Assert.assertEquals("hello, bonjour ", MiscUtils.removeParentheses(test7));
        Assert.assertEquals(" hello, bonjour", MiscUtils.removeParentheses(test8));
    }

    @Test
    public void removeParentheseAndPunctuation() {
        String test1 = "hello (and truc)";
        String test2 = "(and truc) hello";
        String test3 = "hello, bonjour (and truc)";
        String test4 = "(and truc) hello, bonjour";
        String test5 = "hello, bonjour";
        String test6 = "hello, bonjour";
        String test7 = "hello, bonjour [and thed]";
        String test8 = "[and thed] hello, bonjour";

        Assert.assertEquals("hello", MiscUtils.removePunctuation(MiscUtils.removeParentheses(test1)));
        Assert.assertEquals("hello", MiscUtils.removePunctuation(MiscUtils.removeParentheses(test2)));
        Assert.assertEquals("hellobonjour", MiscUtils.removePunctuation(MiscUtils.removeParentheses(test3)));
        Assert.assertEquals("hellobonjour", MiscUtils.removePunctuation(MiscUtils.removeParentheses(test4)));
        Assert.assertEquals("hellobonjour", MiscUtils.removePunctuation(MiscUtils.removeParentheses(test5)));
        Assert.assertEquals("hellobonjour", MiscUtils.removePunctuation(MiscUtils.removeParentheses(test6)));
        Assert.assertEquals("hellobonjour", MiscUtils.removePunctuation(MiscUtils.removeParentheses(test7)));
        Assert.assertEquals("hellobonjour", MiscUtils.removePunctuation(MiscUtils.removeParentheses(test8)));
    }

    public static void setDeleted() {
        List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
        importXML.stream().map(Composition::getFiles).flatMap(List::stream).map(Fichier::getFileName).distinct()
        .forEach(fileName -> {
            List<Composition> importFile = ImportXML
                    .importXML(Constant.getXmlPath() + fileName + Constant.XML_EXTENSION);
            List<Composition> newImportFile = importFile.stream().map(compo -> {
                compo.setDeleted(false);
                return compo;
            }).collect(Collectors.toList());
            try {
                ExportXML.exportXML(newImportFile, fileName);
            } catch (MajorException e) {
                LOG.error("Erreur lors de l'export du fichier: " + fileName);
            }
        });
        List<Composition> newImportXML = importXML.stream().map(compo -> {
            compo.setDeleted(false);
            return compo;
        }).collect(Collectors.toList());
        try {
            ExportXML.exportXML(newImportXML, Constant.getFinalFile());
        } catch (MajorException e) {
            LOG.error("Erreur lors de l'export du fichier final");
        }
    }

    @Test
    public void detectUpper() {
        String test = "05. GERRY & THE PACEMAKERS Ferry Cross the Mersey";
        boolean sorted = true;
        char[] array = test.toCharArray();
        int cut = 0;
        for (int i = 0; i < array.length; i++) {
            if (Character.isUpperCase(array[i]) || !Character.isAlphabetic(array[i])) {
                LOG.debug("upper: " + array[i]);
            } else {
                cut = i - 1;
                LOG.debug("else: " + array[i]);
                break;
            }
        }
        String artist = StringUtils.substring(test, 0, cut);
        String titre = StringUtils.substring(test, cut);
        LOG.debug("cut: " + cut);
        LOG.debug("artist: " + artist);
        LOG.debug("titre: " + titre);
        int rank = 0;
        if (sorted) {
            String res = StringUtils.substringBefore(artist, Constant.DOT);
            if (StringUtils.isNumeric(res)) {
                rank = Integer.parseInt(res);
                artist = StringUtils.substringAfter(artist, Constant.DOT);
            } else {
                res = artist.split(" ")[0];
                rank = Integer.parseInt(res);
                artist = StringUtils.substringAfterLast(artist, res);
            }
        } else {
            rank = 1;
        }
        LOG.debug("rank: " + rank);
    }

    @Test
    public void detectQuote() {
        String test = "05. GERRY & THE PACEMAKERS Ferry Cross the Mersey";
        boolean sorted = true;
        char[] array = test.toCharArray();
        int cut = 0;
        for (int i = 0; i < array.length; i++) {
            if (Character.isUpperCase(array[i]) || !Character.isAlphabetic(array[i])) {
                LOG.debug("upper: " + array[i]);
            } else {
                cut = i - 1;
                LOG.debug("else: " + array[i]);
                break;
            }
        }
        String artist = StringUtils.substring(test, 0, cut);
        String titre = StringUtils.substring(test, cut);
        LOG.debug("cut: " + cut);
        LOG.debug("artist: " + artist);
        LOG.debug("titre: " + titre);
        int rank = 0;
        if (sorted) {
            String res = StringUtils.substringBefore(artist, Constant.DOT);
            if (StringUtils.isNumeric(res)) {
                rank = Integer.parseInt(res);
                artist = StringUtils.substringAfter(artist, Constant.DOT);
            } else {
                res = artist.split(" ")[0];
                rank = Integer.parseInt(res);
                artist = StringUtils.substringAfterLast(artist, res);
            }
        } else {
            rank = 1;
        }
        LOG.debug("rank: " + rank);
    }

    @Test
    public void distanceJaro() {
        List<Composition> guardian = ImportXML.importXML(Constant.getFinalFilePath());
        String test = "beachboys";
        Map<Double, String> jaroRes = new TreeMap<>();
        JaroWinklerDistance jaro = new JaroWinklerDistance();
        for (Composition composition : guardian) {
            String titre = MiscUtils.removePunctuation(composition.getArtist());
            Double apply = jaro.apply(titre, test);
            jaroRes.put(apply, titre);
        }
        for (Entry<Double, String> entry : jaroRes.entrySet()) {
            LOG.debug("Key : " + entry.getKey() + " Value : " + entry.getValue());
        }
    }

    @Test
    public void distanceJaroLine() {
        String s1 = "Fun House - The Stooges";
        String s2 = "Funhouse - The Stooges";
        JaroWinklerDistance jaro = new JaroWinklerDistance();
        Double apply = jaro.apply(s1, s2);
        LOG.debug("Key : " + s1 + " Value : " + s2 + " " + apply);
    }

    @Test
    public void panelArtist() {
        List<Composition> list = ImportXML.importXML(Constant.getFinalFilePath());
        Map<String, String> criteria = new HashMap<>();
        criteria.put(SearchUtils.CRITERIA_ARTIST, "prince");
        List<Composition> search = SearchUtils.search(list, criteria, false, SearchMethod.CONTAINS, false, true);
        Map<String, List<Composition>> groupByArtist = CompositionUtils.groupCompositionByArtist(search);
        LOG.debug("panelArtist");
        groupByArtist.entrySet().stream()
        .forEach(e -> LOG.debug(e.getKey() + " / "
                + StringUtils.join(e.getValue().stream().map(c -> c.getArtist() + " - " + c.getTitre())
                        .collect(Collectors.toList()), ",")
                + " / " + e.getValue().stream().mapToInt(c -> c.getFiles().size()).sum() + " / "
                + e.getValue().stream().filter(c -> c.getRecordType().equals(RecordType.SONG))
                .mapToInt(c -> c.getFiles().size()).sum()
                + " / " + e.getValue().stream().filter(c -> c.getRecordType().equals(RecordType.ALBUM))
                .mapToInt(c -> c.getFiles().size()).sum()));
        LOG.debug("panelArtist");
    }

    @Test
    public void getSeparator() {
        Assert.assertEquals("-", ImportFile.getSeparator("artist - Title"));
        Assert.assertEquals("&", ImportFile.getSeparator("artist & Tilte"));
        Assert.assertEquals("-", ImportFile.getSeparator("art ist- tit le"));
        Assert.assertEquals("-", ImportFile.getSeparator("art ist -tit le"));
        Assert.assertEquals("-", ImportFile.getSeparator("art ist -tit le"));
        Assert.assertEquals("&", ImportFile.getSeparator("art - ist & tit le"));
        Assert.assertEquals("&", ImportFile.getSeparator("art - ist &tit le"));
        Assert.assertEquals("&", ImportFile.getSeparator("art- ist &tit le"));
        Assert.assertEquals("-", ImportFile.getSeparator("art- ist ? tit le"));
    }
}
