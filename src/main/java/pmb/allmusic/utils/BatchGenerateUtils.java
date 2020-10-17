package pmb.allmusic.utils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.allmusic.file.CsvFile;
import pmb.allmusic.file.CustomColumnPositionMappingStrategy;
import pmb.allmusic.model.Cat;
import pmb.allmusic.model.Composition;
import pmb.allmusic.model.Fichier;
import pmb.allmusic.model.RecordType;
import pmb.allmusic.model.SearchMethod;
import pmb.allmusic.model.csv.Stats;
import pmb.allmusic.view.panel.BatchGeneratePanel;
import pmb.allmusic.view.panel.BatchPanel;
import pmb.allmusic.view.panel.OngletPanel;
import pmb.allmusic.xml.ImportXML;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyFileUtils;
import pmb.my.starter.utils.VariousUtils;

/**
 * Utility class that contains all processes for the {@link BatchGeneratePanel}.
 *
 * @see {@link BatchUtils} for common method of batch process.
 */
public class BatchGenerateUtils extends BatchUtils {
    private static final Logger LOG = LogManager.getLogger(BatchGenerateUtils.class);

    /**
     * Creates for each year top year file (top by songs, by albums, by occurences,
     * by points, by publications).
     *
     * @param yearBegin  the begining year
     * @param yearEnd    the ending year
     * @param albumLimit the limit for albums top
     * @param songLimit  the limit for songs top
     * @param deleted    if the deleted compositions are included
     * @return the file name of the result file
     */
    public static String topYear(int yearBegin, int yearEnd, int albumLimit, int songLimit, boolean deleted) {
        LOG.debug("Start topYear");
        StringBuilder text = new StringBuilder();
        addLine(text, "Top Year: ", true);
        addLine(text, "Year Begin: " + yearBegin, true);
        addLine(text, "Year End: " + yearEnd, true);
        addLine(text, "Album Limit: " + albumLimit, true);
        addLine(text, "Song Limit: " + songLimit, true);

        List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
        for (int i = yearBegin; i <= yearEnd; i++) {
            topYear(i, importXML, albumLimit, songLimit, deleted, text);
        }
        if (yearBegin == 0 && yearEnd == 0) {
            topYear(0, importXML, albumLimit, songLimit, deleted, text);
        }

        LOG.debug("End topYear");
        return writeInFile(text, Constant.BATCH_FILE);
    }

    /**
     * Generates the top excel files of a year.
     *
     * @param list
     * @param deleted if true all compositions, false only not deleted compositions
     */
    private static void topYear(int yearTop, List<Composition> list, int albumLimit, int songLimit, boolean deleted,
            StringBuilder result) {
        LOG.debug("Start topYear");
        List<String> files = new ArrayList<>();
        String year = String.valueOf(yearTop);
        addLine(result, "Year: " + year, true);
        files.add(topOccurence(list, year, deleted));
        files.add(topRecords(list, RecordType.SONG, "Top Songs", songLimit, deleted, year));
        files.add(topRecords(list, RecordType.ALBUM, "Top Albums", albumLimit, deleted, year));
        files.add(topRecordsByPoints(list, RecordType.SONG, "Points Songs", deleted, year));
        files.add(topRecordsByPoints(list, RecordType.ALBUM, "Points Albums", deleted, year));
        files.add(topSongsParPublication(list, year, deleted));
        moveFilesInFolder(files, new File(Constant.getOutputDir() + "Top by Year" + MyConstant.FS + year), result);
        LOG.debug("End topYear");
    }

    /**
     * Generates csv file with each top 10 songs of every publication.
     *
     * @param list
     * @param year
     * @param deleted if true all compositions, false only not deleted compositions
     */
    private static String topSongsParPublication(List<Composition> list, String year, boolean deleted) {
        List<String> authors = Arrays.asList(OngletPanel.getAuthorList());
        List<List<String>> result = new ArrayList<>();
        for (String author : authors) {
            Map<String, String> criteria = new HashMap<>();
            criteria.put(SearchUtils.CRITERIA_CAT, Cat.YEAR.toString());
            if (!"0".equals(year)) {
                criteria.put(SearchUtils.CRITERIA_DATE_BEGIN, year);
                criteria.put(SearchUtils.CRITERIA_DATE_END, year);
            }
            criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, RecordType.SONG.toString());
            criteria.put(SearchUtils.CRITERIA_AUTHOR, author);
            List<Composition> yearList = SearchUtils.search(list, criteria, true, SearchMethod.CONTAINS, deleted,
                    false);
            if (!yearList.isEmpty() && BooleanUtils.isTrue(yearList.get(0).getFiles().get(0).getSorted())) {
                // If got composition for the given author and year or if the file is sorted
                List<List<String>> byAuthor = yearList.stream().filter(c -> c.getFiles().get(0).getClassement() <= 10)
                        .map(c -> {
                            List<String> row = new ArrayList<>();
                            row.add(c.getArtist());
                            row.add(c.getTitre());
                            row.add(String.valueOf(c.getFiles().get(0).getClassement()));
                            if ("0".equals(year)) {
                                row.add(String.valueOf(c.getFiles().get(0).getRangeDateBegin()));
                            }
                            row.add(String.valueOf(c.isDeleted()));
                            return row;
                        }).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(byAuthor)) {
                    byAuthor.add(0, initList(author, "-1"));
                    byAuthor.add(0, initList("", "-2"));
                    result.addAll(byAuthor.stream()
                            .sorted((e1, e2) -> Integer.valueOf(e1.get(2)).compareTo(Integer.valueOf(e2.get(2))))
                            .collect(Collectors.toList()));
                }
            }
        }
        result.stream().filter(s -> StringUtils.isBlank(s.get(1))).forEach(s -> s.set(2, ""));
        String[] header = { CSV_HEADER_ARTIST, CSV_HEADER_TITLE, CSV_HEADER_RANK, CSV_HEADER_DELETED };
        if ("0".equals(year)) {
            String[] tmp = { CSV_HEADER_ARTIST, CSV_HEADER_TITLE, CSV_HEADER_RANK, CSV_HEADER_ANNEE,
                    CSV_HEADER_DELETED };
            header = tmp;
        }
        return CsvFile.exportCsv("Top Songs Par Publication - " + year, result, null, header);
    }

    /**
     * Generates csv file with the most occurence of an artist, songs and albums
     * combine. Limited with minimum 10 total occurences.
     *
     * @param list
     * @param year
     * @param deleted if true all compositions, false only not deleted compositions
     */
    private static String topOccurence(List<Composition> list, String year, boolean deleted) {
        Map<String, String> criteria = new HashMap<>();
        criteria.put(SearchUtils.CRITERIA_CAT, Cat.YEAR.toString());
        if (!"0".equals(year)) {
            criteria.put(SearchUtils.CRITERIA_DATE_BEGIN, year);
            criteria.put(SearchUtils.CRITERIA_DATE_END, year);
        }
        List<Composition> yearList = SearchUtils.search(list, criteria, true, SearchMethod.CONTAINS, deleted, false);
        Vector<Vector<Object>> occurenceList = CompositionUtils
                .convertArtistPanelResultToVector(CompositionUtils.groupCompositionByArtist(yearList), false).stream()
                .filter(c -> (int) c.get(1) > 9).collect(MiscUtils.toVector());
        String[] csvHeader = { CSV_HEADER_ARTIST, CSV_HEADER_OCCURENCY, CSV_HEADER_ALBUMS, CSV_HEADER_SONG,
                CSV_HEADER_PERCENT_DELETED, CSV_HEADER_SCORE_TOTAL, CSV_HEADER_SCORE_ALBUM, CSV_HEADER_SCORE_SONG,
                CSV_HEADER_SCORE_DELETED, CSV_HEADER_YEAR + year };
        return CsvFile.exportCsv("Top Occurence - " + year, MiscUtils.convertVectorToList(occurenceList),
                Arrays.asList(new SortKey(1, SortOrder.DESCENDING), new SortKey(5, SortOrder.DESCENDING)), csvHeader);
    }

    /**
     * Top songs or top albums with a sytem of points.
     *
     * @param list
     * @param type     Album or Song
     * @param fileName the name of the result csv file
     * @param deleted  if true all compositions, false only not deleted compositions
     * @param year     the year of the top
     */
    private static String topRecordsByPoints(List<Composition> list, RecordType type, String fileName, boolean deleted,
            String year) {
        Map<String, String> criteria = new HashMap<>();
        criteria.put(SearchUtils.CRITERIA_CAT, Cat.YEAR.toString());
        if (!"0".equals(year)) {
            criteria.put(SearchUtils.CRITERIA_DATE_BEGIN, year);
            criteria.put(SearchUtils.CRITERIA_DATE_END, year);
        }
        criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, type.toString());
        criteria.put(SearchUtils.CRITERIA_SORTED, Boolean.TRUE.toString());
        List<Composition> yearList = SearchUtils.search(list, criteria, true, SearchMethod.CONTAINS, deleted, false);
        List<List<String>> occurenceList = new ArrayList<>();
        if (yearList.stream().map(Composition::getFiles).flatMap(List::stream).map(Fichier::getAuthor)
                .map(WordUtils::capitalize).distinct().count() > 2) {
            for (Composition composition : yearList) {
                List<String> row = new ArrayList<>();
                row.add(composition.getArtist());
                row.add(composition.getTitre());
                if (!"0".equals(year)) {
                    row.add(composition.getRecordType().toString());
                } else {
                    row.add(String.valueOf(composition.getFiles().get(0).getRangeDateBegin()));
                }
                Integer points = composition.getFiles().stream().map(Fichier::getClassement).filter(rank -> rank < 10)
                        .reduce(0, (a, b) -> a + 20 * (11 - b));
                if (points > 0) {
                    row.add(String.valueOf(points));
                    occurenceList.add(row);
                }
                row.add(String.valueOf(composition.isDeleted()));
            }
        }
        String[] csvHeader = { CSV_HEADER_ARTIST, CSV_HEADER_TITLE, CSV_HEADER_TYPE, CSV_HEADER_SCORE,
                CSV_HEADER_DELETED, CSV_HEADER_YEAR + year + " Type: " + type.toString() };
        return CsvFile.exportCsv(fileName + " - " + year, occurenceList,
                Arrays.asList(new SortKey(3, SortOrder.DESCENDING)), csvHeader);
    }

    /**
     * Top songs or top albums.
     *
     * @param list
     * @param type     Album or Song
     * @param fileName the name of the result csv file
     * @param limit    the minimim of number of occurence a Composition to have to
     *                 be in the result file
     * @param deleted  if true all compositions, false only not deleted compositions
     * @param year     the year of the top
     * @param score
     */
    private static String topRecords(List<Composition> list, RecordType type, String fileName, int limit,
            boolean deleted, String year) {
        Map<String, String> criteria = new HashMap<>();
        criteria.put(SearchUtils.CRITERIA_CAT, Cat.YEAR.toString());
        if (!"0".equals(year)) {
            criteria.put(SearchUtils.CRITERIA_DATE_BEGIN, year);
            criteria.put(SearchUtils.CRITERIA_DATE_END, year);
        }
        criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, type.toString());
        Vector<Vector<Object>> occurenceList = CompositionUtils.convertCompositionListToVector(
                SearchUtils.search(list, criteria, true, SearchMethod.CONTAINS, deleted, false).stream()
                .filter(c -> c.getFiles().size() >= limit).collect(Collectors.toList()),
                null, false, true, false, true, false);

        String[] csvHeader = { CSV_HEADER_ARTIST, CSV_HEADER_TITLE, CSV_HEADER_TYPE, CSV_HEADER_FILE_SIZE,
                CSV_HEADER_SCORE, CSV_HEADER_DECILE, CSV_HEADER_DELETED, "",
                CSV_HEADER_YEAR + year + " Type: " + type.toString() };
        return CsvFile.exportCsv(fileName + " - " + year, MiscUtils.convertVectorToList(occurenceList),
                Arrays.asList(new SortKey(3, SortOrder.DESCENDING), new SortKey(4, SortOrder.DESCENDING)), csvHeader);
    }

    private static List<String> initList(String author, String value) {
        List<String> row = new ArrayList<>();
        row.add(author);
        row.add("");
        row.add(value);
        return row;
    }

    private static void moveFilesInFolder(List<String> files, File folder, StringBuilder result) {
        MyFileUtils.createFolderIfNotExists(folder.getAbsolutePath());
        Path pathFolder = folder.toPath();
        files.stream().forEach(f -> {
            Path pathFile = new File(f).toPath();
            try {
                Files.move(pathFile, pathFolder.resolve(pathFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                addLine(result, "Error while moving file: " + f + " " + e.getMessage(), true);
                LOG.error("Error while moving file: {}", f, e);
            }
        });
    }

    /**
     * For each file calculates the average of files each compositions are linked
     * to.
     *
     * @param batchPanel to log progression
     * @return the file name of the result file
     */
    public static String averageOfFilesByFiles(BatchPanel batchPanel) {
        LOG.debug("Start averageOfFilesByFiles");
        StringBuilder text = new StringBuilder();
        addLine(text, "Start AverageOfFilesByFiles", true);
        // Moyenne par fichier du nombre de fichiers de chaque composition
        List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
        List<String> nomFichier = importXML.stream().map(Composition::getFiles).flatMap(List::stream)
                .map(Fichier::getFileName).distinct().sorted().collect(Collectors.toList());
        List<List<String>> result = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger(0);
        final BigDecimal total = new BigDecimal(nomFichier.size());
        DecimalFormat decimalFormat = MyConstant.getDecimalFormat();
        nomFichier.parallelStream().forEach(name -> {
            List<String> row = new ArrayList<>();
            Map<String, String> criteria = new HashMap<>();
            criteria.put(SearchUtils.CRITERIA_FILENAME, name);
            List<Composition> xml = SearchUtils.search(importXML, criteria, false, SearchMethod.WHOLE_WORD, true,
                    false);
            Fichier fichier = xml.get(0).getFiles().stream()
                    .filter(f -> StringUtils.equalsIgnoreCase(f.getFileName(), name)).findFirst().get();
            row.add(name);
            row.add(fichier.getAuthor());
            row.add(xml.get(0).getRecordType().toString());
            row.add(fichier.getCategorie().getValue());
            row.add(decimalFormat
                    .format(xml.stream().map(c -> c.getFiles().size()).mapToInt(x -> x).average().getAsDouble()));
            result.add(row);
            if (count.incrementAndGet() % 10 == 0) {
                batchPanel.displayText(BigDecimal.valueOf(100D).setScale(2).multiply(new BigDecimal(count.get()))
                        .divide(total, RoundingMode.HALF_UP).doubleValue() + "%", count.get() != 10);
            }
        });
        CsvFile.exportCsv(CSV_HEADER_AVERAGE, result,
                Arrays.asList(new SortKey(4, SortOrder.ASCENDING), new SortKey(0, SortOrder.ASCENDING)), new String[] {
                        CSV_HEADER_FICHIER, CSV_HEADER_AUTHOR, CSV_HEADER_TYPE, CSV_HEADER_CAT, CSV_HEADER_AVERAGE });
        addLine(text, "Average.csv generated", true);
        statsByAuthorTypeAndCat(result, text);
        LOG.debug("End averageOfFilesByFiles");
        addLine(text, "End AverageOfFilesByFiles", true);
        return writeInFile(text, Constant.BATCH_FILE);
    }

    private static void statsByAuthorTypeAndCat(List<List<String>> data, StringBuilder text) {
        LOG.debug("Start statsByAuthorTypeAndCat");
        String separator = new String(new char[] { Constant.getCsvSeparator() });
        Map<String, List<List<String>>> groupBy = data.stream().collect(
                Collectors.groupingBy(list -> list.get(1) + separator + list.get(2) + separator + list.get(3)));
        DecimalFormat decimalFormat = MyConstant.getDecimalFormat();
        List<List<String>> collect = groupBy.entrySet().stream().map(by -> {
            StringBuilder sb = new StringBuilder(by.getKey()).append(separator);
            List<Double> average = new ArrayList<>();
            average = by.getValue().stream().map(t -> parseDouble(decimalFormat, t.get(4)))
                    .filter(ObjectUtils::allNotNull).collect(Collectors.toList());
            DoubleSummaryStatistics stats = average.stream().mapToDouble(Double::doubleValue).summaryStatistics();
            if (stats.getCount() >= 5) {
                sb.append(decimalFormat.format(stats.getMin())).append(separator);
                sb.append(decimalFormat.format(stats.getMax())).append(separator);
                double statsAverage = stats.getAverage();
                sb.append(decimalFormat.format(statsAverage)).append(separator);
                sb.append(decimalFormat.format(
                        VariousUtils.median(average.stream().map(BigDecimal::valueOf).collect(Collectors.toList()))))
                .append(separator);
                Double statSd = VariousUtils.calculateSD(average, statsAverage, stats.getCount());
                sb.append(decimalFormat.format(statSd)).append(separator);
                sb.append(stats.getCount()).append(separator);
                sb.append(by.getValue().stream().filter(v -> {
                    Double avgFile = parseDouble(decimalFormat, v.get(4));
                    return avgFile < (statsAverage - statSd * 1.5);
                }).map(v -> v.get(0) + " (" + v.get(4) + ")").collect(Collectors.joining(","))).append(separator);
                return Arrays.asList(sb.toString().split(separator));
            } else {
                return null;
            }
        }).collect(Collectors.toList());
        CsvFile.exportCsv("GroupBy", collect.stream().filter(ObjectUtils::allNotNull).collect(Collectors.toList()),
                Arrays.asList(new SortKey(0, SortOrder.ASCENDING), new SortKey(1, SortOrder.ASCENDING),
                        new SortKey(2, SortOrder.ASCENDING)),
                new String[] { CSV_HEADER_AUTHOR, CSV_HEADER_TYPE, CSV_HEADER_CAT, "Min", "Max", CSV_HEADER_AVERAGE,
                        "Median", "SD", "Size", "Files" });
        addLine(text, "GroupBy.csv generated", true);
        LOG.debug("End statsByAuthorTypeAndCat");
    }

    private static Double parseDouble(DecimalFormat decimalFormat, String strDouble) {
        try {
            return decimalFormat.parse(strDouble).doubleValue();
        } catch (ParseException e) {
            LOG.error("Parsing error: {}", strDouble);
        }
        return null;
    }

    /**
     * Creates a csv file with for every file looks its calculated size, its real
     * size and makes a ratio of it.
     *
     * @return the file name of the result file
     */
    public static String weirdFileSize() {
        LOG.debug("Start weirdFileSize");
        StringBuilder text = new StringBuilder();
        DecimalFormat decimalFormat = MyConstant.getDecimalFormat();
        addLine(text, "Start weirdFileSize", true);
        // Moyenne par fichier du nombre de fichiers de chaque composition
        List<String> nomFichier = ImportXML.importXML(Constant.getFinalFilePath()).stream().map(Composition::getFiles)
                .flatMap(List::stream).map(Fichier::getFileName).distinct().sorted().collect(Collectors.toList());
        String[] header = { CSV_HEADER_FICHIER, CSV_HEADER_TYPE, "Real Size", "Theoric Size", "Ratio" };
        List<List<String>> result = new ArrayList<>();
        nomFichier.parallelStream().forEach(name -> {
            List<Composition> xml = ImportXML.importXML(Constant.getXmlPath() + name + MyConstant.XML_EXTENSION);
            int realSize = xml.size();
            Integer theoricSize = xml.get(0).getFiles().get(0).getSize();
            if (theoricSize != 0 && realSize != theoricSize) {
                BigDecimal ratio = BigDecimal.valueOf(realSize).multiply(BigDecimal.valueOf(100D))
                        .divide(BigDecimal.valueOf(theoricSize), RoundingMode.DOWN);
                List<String> row = new ArrayList<>();
                row.add(name);
                row.add(xml.get(0).getRecordType().toString());
                row.add(decimalFormat.format(realSize));
                row.add(decimalFormat.format(theoricSize));
                row.add(decimalFormat.format(ratio.doubleValue()));
                result.add(row);
            }
        });
        String path = CsvFile.exportCsv("Weird", result, Arrays.asList(new SortKey(4, SortOrder.ASCENDING)), header);
        LOG.debug("End weirdOfFilesByFiles");
        return path;
    }

    /**
     * For each compositions with unknown type searches if there is compositions to
     * merge with. The goal is to guess their types.
     *
     * @return the file name of the result file
     */
    public static String findUnknown() {
        LOG.debug("Start findUnknown");
        Comparator<String> byFileName = (String o1, String o2) -> {
            String s1 = StringUtils.substringBeforeLast(o1, ";");
            String s2 = StringUtils.substringBeforeLast(o2, ";");
            return s1.compareToIgnoreCase(s2);
        };
        Comparator<String> byCount = (String o1, String o2) -> {
            Long r1 = Long.valueOf(StringUtils.substringAfterLast(o1, ";"));
            Long r2 = Long.valueOf(StringUtils.substringAfterLast(o2, ";"));
            return Long.compare(r1, r2);
        };
        Map<String, List<String>> result = new TreeMap<>(byFileName.thenComparing(byCount.reversed()));
        StringBuilder sb = new StringBuilder();
        addLine(sb, "Find Unknown: ", true);
        Map<String, String> criteria = new HashMap<>();
        criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, RecordType.UNKNOWN.toString());
        List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
        List<Composition> unknown = SearchUtils.search(importXML, criteria, true, SearchMethod.WHOLE_WORD, true, true);
        AtomicInteger notFound = new AtomicInteger(0);
        unknown.parallelStream().forEach(u -> {
            Map<String, String> c = new HashMap<>();
            c.put(SearchUtils.CRITERIA_ARTIST, u.getArtist());
            c.put(SearchUtils.CRITERIA_TITRE, u.getTitre());
            List<Composition> search = SearchUtils.search(importXML, c, true, SearchMethod.CONTAINS, true, false);
            String fileName = u.getFiles().get(0).getFileName();
            String artistTitre = u.getArtist() + " - " + u.getTitre();
            String item;
            List<RecordType> types = search.stream().map(Composition::getRecordType)
                    .filter(t -> t != RecordType.UNKNOWN).collect(Collectors.toList());
            if (!types.isEmpty()) {
                long songCount = search.stream().filter(s -> s.getRecordType() == RecordType.SONG)
                        .mapToInt(s -> s.getFiles().size()).sum();
                long albumCount = search.stream().filter(s -> s.getRecordType() == RecordType.ALBUM)
                        .mapToInt(s -> s.getFiles().size()).sum();
                Long count;
                if (types.stream().allMatch(RecordType.ALBUM::equals)) {
                    item = artistTitre + ": " + RecordType.ALBUM + " (" + albumCount + ")";
                    count = albumCount;
                } else if (types.stream().allMatch(RecordType.SONG::equals)) {
                    item = artistTitre + ": " + RecordType.SONG + " (" + songCount + ")";
                    count = songCount;
                } else {
                    item = artistTitre + MyConstant.NEW_LINE + RecordType.SONG + ": " + songCount + MyConstant.NEW_LINE
                            + RecordType.ALBUM + ": " + albumCount;
                    count = (songCount + albumCount) / 2;
                }
                String key = fileName + ";" + count;
                if (result.containsKey(key)) {
                    result.get(key).add(item);
                } else {
                    result.put(key, new LinkedList<>(Arrays.asList(item)));
                }
            } else {
                notFound.incrementAndGet();
            }
        });
        String currentKey = "";
        for (Entry<String, List<String>> e : result.entrySet()) {
            String key = StringUtils.substringBeforeLast(e.getKey(), ";");
            if (!StringUtils.equalsIgnoreCase(currentKey, key)) {
                sb.append(MyConstant.NEW_LINE + "### " + key + ": " + MyConstant.NEW_LINE);
                currentKey = key;
            }
            e.getValue().stream().forEach(v -> sb.append(v + MyConstant.NEW_LINE));
        }
        sb.append(MyConstant.NEW_LINE + "### Not Found: " + notFound.get());
        LOG.debug("End findUnknown");
        return writeInFile(sb, "Unknown.txt");
    }

    /**
     * Generates statistics of xml files.
     *
     * @return the file name of the result file
     */
    public static String stats() {
        LOG.debug("Start stats");
        StringBuilder result = new StringBuilder();
        addLine(result, "Statistiques sur la longueur artiste et titre: ", true);
        List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
        statsLength(result, importXML.stream().map(Composition::getArtist).distinct().map(String::length)
                .collect(Collectors.toList()), "Artiste: ");
        statsLength(result, importXML.stream().filter(c -> c.getRecordType() == RecordType.ALBUM)
                .map(composition -> composition.getTitre().length()).collect(Collectors.toList()), "Album: ");
        statsLength(result, importXML.stream().filter(c -> c.getRecordType() == RecordType.SONG)
                .map(composition -> composition.getTitre().length()).collect(Collectors.toList()), "Chanson: ");

        addLine(result, "Statistiques sur les tops annuels: ", true);
        Map<Integer, Integer> songs = importXML.stream().filter(c -> c.getRecordType() == RecordType.SONG)
                .map(Composition::getFiles).flatMap(List::stream).filter(f -> f.getCategorie() == Cat.YEAR)
                .collect(Collectors.groupingBy(Fichier::getRangeDateBegin, Collectors
                        .collectingAndThen(Collectors.mapping(Fichier::getFileName, Collectors.toSet()), Set::size)));
        Map<Integer, Integer> albums = importXML.stream().filter(c -> c.getRecordType() == RecordType.ALBUM)
                .map(Composition::getFiles).flatMap(List::stream).filter(f -> f.getCategorie() == Cat.YEAR)
                .collect(Collectors.groupingBy(Fichier::getRangeDateBegin, Collectors
                        .collectingAndThen(Collectors.mapping(Fichier::getFileName, Collectors.toSet()), Set::size)));
        String resultFile = writeInFile(result, "stats.csv");

        List<Composition> songYear = importXML.stream()
                .filter(c -> c.getRecordType() == RecordType.SONG
                && c.getFiles().stream().anyMatch(f -> f.getCategorie() == Cat.YEAR))
                .collect(Collectors.toList());
        List<Composition> albumYear = importXML.stream()
                .filter(c -> c.getRecordType() == RecordType.ALBUM
                && c.getFiles().stream().anyMatch(f -> f.getCategorie() == Cat.YEAR))
                .collect(Collectors.toList());

        int min = Stream.concat(songs.keySet().stream(), albums.keySet().stream()).mapToInt(Integer::intValue).min()
                .getAsInt();
        int max = Stream.concat(songs.keySet().stream(), albums.keySet().stream()).mapToInt(Integer::intValue).max()
                .getAsInt();
        CsvFile.exportBeanList(new File(resultFile), IntStream.rangeClosed(min, max).mapToObj(i -> {
            Integer song = !songs.containsKey(i) ? 0 : songs.get(i);
            Integer album = !albums.containsKey(i) ? 0 : albums.get(i);
            long songCount = songYear.stream().filter(c -> c.getFiles().stream()
                    .anyMatch(f -> f.getCategorie() == Cat.YEAR && f.getRangeDateBegin().equals(i))).count();
            long albumCount = albumYear.stream().filter(c -> c.getFiles().stream()
                    .anyMatch(f -> f.getCategorie() == Cat.YEAR && f.getRangeDateBegin().equals(i))).count();
            return new Stats(i, song, songCount, album, albumCount, song + album, songCount + albumCount);
        }).map(Stats.class::cast).collect(Collectors.toList()),
                new CustomColumnPositionMappingStrategy<>(Stats.class, null), true);
        LOG.debug("End stats");
        return resultFile;
    }

    private static void statsLength(StringBuilder result, List<Integer> size, String title) {
        addLine(result, title, true);
        IntSummaryStatistics summaryStatistics = size.stream().mapToInt(Integer::intValue).summaryStatistics();
        addLine(result, "Min: " + summaryStatistics.getMin(), false);
        addLine(result, "Max: " + summaryStatistics.getMax(), false);
        addLine(result, "Moyenne: " + summaryStatistics.getAverage(), false);
        addLine(result,
                "Mediane: " + VariousUtils.median(size.stream().map(BigDecimal::valueOf).collect(Collectors.toList())),
                false);
        addLine(result,
                "Ecart-Type: " + VariousUtils.calculateSD(size.stream().map(Double::valueOf).collect(Collectors.toList()),
                        summaryStatistics.getAverage(), summaryStatistics.getCount()),
                false);
        addLine(result, "Summary: " + summaryStatistics, false);
        addLine(result, "", false);
    }

}
