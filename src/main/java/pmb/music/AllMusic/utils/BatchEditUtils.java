package pmb.music.AllMusic.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import com.opencsv.bean.CsvBindByName;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.exception.MajorException;
import pmb.music.AllMusic.exception.MinorException;
import pmb.music.AllMusic.file.CleanFile;
import pmb.music.AllMusic.file.CsvFile;
import pmb.music.AllMusic.file.CustomColumnPositionMappingStrategy;
import pmb.music.AllMusic.file.ImportFile;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.model.SearchMethod;
import pmb.music.AllMusic.model.SearchRange;
import pmb.music.AllMusic.model.csv.ItunesComposition;
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.dialog.DeleteCompoDialog;
import pmb.music.AllMusic.view.dialog.SlashEditDialog;
import pmb.music.AllMusic.view.panel.BatchEditPanel;
import pmb.music.AllMusic.view.panel.BatchPanel;
import pmb.music.AllMusic.view.panel.OngletPanel;

/**
 * Utility class that contains all processes for the {@link BatchEditPanel}.
 *
 * @see {@link BatchUtils} for common method of batch process.
 */
public class BatchEditUtils extends BatchUtils {
    private static final Logger LOG = LogManager.getLogger(BatchEditUtils.class);

    private static final Comparator<ItunesComposition> compareByTrackNumber = (ItunesComposition c1,
            ItunesComposition c2) -> {
                String s1 = c1.getTrackNumber();
                if (StringUtils.contains(s1, Constant.TRACK_NUMBER_SEPARATOR)) {
                    s1 = StringUtils.substringBefore(s1, Constant.TRACK_NUMBER_SEPARATOR);
                }
                String s2 = c2.getTrackNumber();
                if (StringUtils.contains(s2, Constant.TRACK_NUMBER_SEPARATOR)) {
                    s2 = StringUtils.substringBefore(s2, Constant.TRACK_NUMBER_SEPARATOR);
                }
                Integer int1 = 0;
                if (StringUtils.isNotBlank(s1)) {
                    int1 = Integer.valueOf(s1);
                }
                Integer int2 = 0;
                if (StringUtils.isNotBlank(s2)) {
                    int2 = Integer.valueOf(s2);
                }
                return int1.compareTo(int2);
            };

            /**
             * Detects if there are compositions that should be merged.
             *
             * @param song                   if song compositions are treated
             * @param album                  if album compositions are treated
             * @param ignoreUnmergeableFiles if ignore file with merge equals to false
             * @param byYear                 if processes compositions by year files cat
             * @param batchPanel             for logging purpose
             * @return file name of the result file
             */
            public static String detectsDuplicateFinal(boolean song, boolean album, boolean ignoreUnmergeableFiles,
                    boolean byYear, BatchPanel batchPanel) {
                LOG.debug("Start detectsDuplicateFinal");
                StringBuilder result = new StringBuilder();
                addLine(result, "DetectsDuplicateFinal: ", true);
                addLine(result, "Song: " + song, true);
                addLine(result, "Album: " + album, true);
                addLine(result, "Ignore Unmergeable Files: " + ignoreUnmergeableFiles, true);

                if (song) {
                    detectsDuplicateFinal(RecordType.SONG.toString(), ignoreUnmergeableFiles, byYear, result, batchPanel);
                }
                if (album) {
                    detectsDuplicateFinal(RecordType.ALBUM.toString(), ignoreUnmergeableFiles, byYear, result, batchPanel);
                }
                try {
                    ImportXML.synchroDeletedWithFinal();
                } catch (MinorException e) {
                    LOG.error("Erreur lors de la détection de composition supprimées", e);
                    addLine(result, e.toString(), true);
                }

                LOG.debug("End detectsDuplicateFinal");
                return writeInFile(result, Constant.BATCH_FILE);
            }

            /**
             * Show all the duplicates for a year and a type regardless of the artist, only
             * based on the song or album.
             *
             * @param type                   type of compositions
             * @param ignoreUnmergeableFiles if true, ignore composition with canBeMerged to
             *                               false
             * @param byYear                 if detects duplicates by year
             * @param result                 result string
             * @param batchPanel             batch panel to display progression
             */
            private static void detectsDuplicateFinal(String type, boolean ignoreUnmergeableFiles, boolean byYear,
                    StringBuilder result, BatchPanel batchPanel) {
                LOG.debug("Start detectsDuplicateFinal");
                double startTime = System.currentTimeMillis();
                final JaroWinklerDistance jaro = new JaroWinklerDistance();
                int i = 0;
                while ((!byYear && findFirstDuplicate(type, jaro, ignoreUnmergeableFiles, result, batchPanel))
                        || (byYear && detectsDuplicate(type, jaro, result))) {
                    i++;
                }
                double endTime = System.currentTimeMillis();
                addLine(result, "Time: " + (endTime - startTime) / 1000 + " secondes", true);
                addLine(result, "Nombre de compositions fusionnées: " + i, true);
                LOG.debug("End detectsDuplicateFinal");
            }

            /**
             * Goes through final file and stops when detect 2 similar compositions but not
             * merged.
             *
             * @param type                   record type
             * @param jaro                   a jaro instance
             * @param ignoreUnmergeableFiles if ignore composition with <i>mergeable</i> at
             *                               true
             * @param result                 result holder
             * @param batchPanel             for result purpose
             * @return true if found mergeable compositions, false if nothing found
             */
            private static boolean findFirstDuplicate(String type, final JaroWinklerDistance jaro,
                    boolean ignoreUnmergeableFiles, StringBuilder result, BatchPanel batchPanel) {
                LOG.debug("Start findFirstDuplicate");
                List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
                if (!importXML.isEmpty()) {
                    int size = importXML.size();
                    addLine(result, "Size: " + size, true);
                    for (int i = 0; i < size; i++) {
                        for (int j = 0; j < size; j++) {
                            Composition c1 = importXML.get(i);
                            Composition c2 = importXML.get(j);
                            if ((!c1.getRecordType().toString().equals(type) || !c2.getRecordType().toString().equals(type))
                                    || (ignoreUnmergeableFiles && (!c1.isCanBeMerged() || !c2.isCanBeMerged()))) {
                                continue;
                            }
                            boolean isCriteria = true;
                            if (i != j && isCriteria) {
                                Composition composition1 = c1;
                                Composition composition2 = c2;
                                boolean similarArtist = isArtistSimilar(composition1, composition2);
                                if (similarArtist) {
                                    String titre1 = composition1.getTitre().toLowerCase();
                                    String remParTitre1 = MiscUtils.removeParentheses(titre1);
                                    String parTitre1 = MiscUtils.removePunctuation(remParTitre1);
                                    String titre2 = composition2.getTitre().toLowerCase();
                                    String remParTitre2 = MiscUtils.removeParentheses(titre2);
                                    String parTitre2 = MiscUtils.removePunctuation(remParTitre2);
                                    boolean parTitreEqu = StringUtils.startsWithIgnoreCase(parTitre1, parTitre2)
                                            || StringUtils.startsWithIgnoreCase(parTitre2, parTitre1);
                                    if (parTitreEqu
                                            && (StringUtils.containsIgnoreCase(remParTitre1, Constant.SEPARATOR_AND)
                                                    || StringUtils.containsIgnoreCase(remParTitre2, Constant.SEPARATOR_AND))
                                            && !StringUtils.containsIgnoreCase(remParTitre1, "/")
                                            && !StringUtils.containsIgnoreCase(remParTitre2, "/")) {
                                        String andTitre1 = MiscUtils.removePunctuation(
                                                StringUtils.substringBefore(remParTitre1, Constant.SEPARATOR_AND));
                                        String andTitre2 = MiscUtils.removePunctuation(
                                                StringUtils.substringBefore(remParTitre2, Constant.SEPARATOR_AND));
                                        parTitre1 = andTitre1;
                                        parTitre2 = andTitre2;
                                    }
                                    boolean equalsJaroPar = SearchUtils.isEqualsJaro(jaro, parTitre1, parTitre2,
                                            Constant.SCORE_LIMIT_TITLE_FUSION);
                                    if (equalsJaroPar) {
                                        mergeTwoCompositions(importXML, i, j, result);
                                        LOG.debug("End findFirstDuplicate, find duplicate");
                                        batchPanel.displayText(
                                                BigDecimal.valueOf(100D).setScale(2).multiply(new BigDecimal(i))
                                                .divide(new BigDecimal(size), RoundingMode.HALF_UP).doubleValue() + "%",
                                                true);
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
                LOG.debug("End findFirstDuplicate, no result");
                return false;
            }

            private static boolean isArtistSimilar(Composition composition1, Composition composition2) {
                String artist1 = composition1.getArtist();
                String artist2 = composition2.getArtist();
                return StringUtils.startsWithIgnoreCase(artist1, artist2) || StringUtils.startsWithIgnoreCase(artist2, artist1);
            }

            /**
             * Show all the duplicates for a year and a type regardless of the artist, only
             * based on the song or album.
             */
            private static boolean detectsDuplicate(String type, final JaroWinklerDistance jaro, StringBuilder result) {
                LOG.debug("Debut detectsDuplicate");
                List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
                int maxYear = importXML.stream().map(Composition::getFiles).flatMap(List::stream).map(Fichier::getPublishYear)
                        .mapToInt(i -> i).max().getAsInt();
                int minYear = importXML.stream().map(Composition::getFiles).flatMap(List::stream).map(Fichier::getPublishYear)
                        .mapToInt(i -> i).filter(y -> y != 0).min().getAsInt();
                for (int year = minYear; year <= maxYear; year++) {
                    Map<String, String> criteria = new HashMap<>();
                    criteria.put(SearchUtils.CRITERIA_CAT, Cat.YEAR.toString());
                    criteria.put(SearchUtils.CRITERIA_PUBLISH_YEAR, String.valueOf(year));
                    criteria.put(SearchUtils.CRITERIA_PUBLISH_YEAR_RANGE, SearchRange.EQUAL.getValue());
                    criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, type);
                    List<Composition> yearList = SearchUtils.search(importXML, criteria, true, SearchMethod.CONTAINS, false,
                            false);
                    addLine(result, CSV_HEADER_YEAR + year, true);
                    addLine(result, "Size: " + yearList.size(), true);
                    for (int i = 0; i < yearList.size(); i++) {
                        for (int j = 0; j < yearList.size(); j++) {
                            if (i < j) {
                                Composition composition1 = yearList.get(i);
                                Composition composition2 = yearList.get(j);
                                String titre1 = composition1.getTitre();
                                String titre2 = composition2.getTitre();
                                String newTitre1 = MiscUtils.removePunctuation(titre1);
                                String newTitre2 = MiscUtils.removePunctuation(titre2);
                                String artist1 = composition1.getArtist();
                                String artist2 = composition2.getArtist();
                                Integer publishYear1 = composition1.getFiles().get(0).getPublishYear();
                                Integer publishYear2 = composition2.getFiles().get(0).getPublishYear();
                                boolean similarArtist = StringUtils.startsWithIgnoreCase(artist1, artist2)
                                        || StringUtils.startsWithIgnoreCase(artist2, artist1);
                                boolean equalsJaroPar = publishYear1.equals(publishYear2) && (SearchUtils.isEqualsJaro(jaro,
                                        newTitre1, newTitre2, Constant.SCORE_LIMIT_TITLE_FUSION)
                                        || StringUtils.startsWithIgnoreCase(titre1, titre2)
                                        || StringUtils.startsWithIgnoreCase(newTitre1, newTitre2)) && similarArtist;
                                if (equalsJaroPar) {
                                    mergeTwoCompositions(yearList, i, j, result);
                                    LOG.debug("End detectsDuplicate, find duplicate");
                                    return true;
                                }
                            }
                        }
                    }
                }
                LOG.debug("Fin detectsDuplicate");
                return false;
            }

            /**
             * Merges 2 compositions.
             *
             * @param importXML the list holding the compositions to merge
             * @param index1    index of the 1st
             * @param index2    index of the 2nd
             * @param result    for logging
             */
            private static void mergeTwoCompositions(List<Composition> importXML, int index1, int index2,
                    StringBuilder result) {
                LOG.debug("Start mergeTwoCompositions");
                Composition c1 = importXML.get(index1);
                Composition c2 = importXML.get(index2);

                boolean isDeleted = c1.isDeleted() || c2.isDeleted();
                c1.setDeleted(isDeleted);
                c2.setDeleted(isDeleted);

                addLine(result, "i: " + index1, true);
                addLine(result, "j: " + index2, true);
                addLine(result, "c1: " + c1, true);
                addLine(result, "c2: " + c2, true);
                if (((c1.getFiles().size() >= c2.getFiles().size()
                        && !StringUtils.containsIgnoreCase(c1.getArtist(), Constant.SEPARATOR_AND))
                        || StringUtils.containsIgnoreCase(c2.getArtist(), Constant.SEPARATOR_AND))) {
                    c2.setArtist(c1.getArtist());
                    c2.setTitre(c1.getTitre());
                    try {
                        CompositionUtils.editCompositionsInFiles(c2, isDeleted);
                    } catch (MajorException e) {
                        addLine(result, "Erreur modif compo" + e.getMessage(), true);
                        LOG.error("Erreur modif compo", e);
                    }
                } else {
                    try {
                        c1.setArtist(c2.getArtist());
                        c1.setTitre(c2.getTitre());
                        CompositionUtils.editCompositionsInFiles(c1, isDeleted);
                    } catch (MajorException e) {
                        addLine(result, "Erreur modif compo" + e.getMessage(), true);
                        LOG.error("Erreur modif compo", e);
                    }
                }
                c2.getFiles().addAll(c1.getFiles());
                c2.getUuids().addAll(c1.getUuids());
                importXML.remove(index1);
                try {
                    ExportXML.exportXML(importXML, Constant.getFinalFile());
                } catch (MajorException e) {
                    LOG.error("Error !!", e);
                }
                addLine(result, "Final size: " + importXML.size(), true);
                LOG.debug("End mergeTwoCompositions");
            }

            /**
             * Launchs a dialog to edit composition with a slash in their title.
             *
             * @return the file name of the result file
             */
            public static String slashEdit() {
                LOG.debug("Start slashEdit");
                StringBuilder result = new StringBuilder();
                try {
                    List<String> slashFile = readSlashFile();
                    List<Composition> slashComposition = ImportXML.importXML(Constant.getFinalFilePath()).stream()
                            .filter(c -> c.getFiles().size() == 1 && StringUtils.contains(c.getTitre(), "/"))
                            .filter(c -> c.getUuids().stream().noneMatch(slashFile::contains)).collect(Collectors.toList());
                    int size = slashComposition.size();
                    SlashEditDialog dialog = new SlashEditDialog(size);
                    for (int i = 0; i < size; i++) {
                        Composition c = slashComposition.get(i);
                        if (StringUtils.split(c.getTitre(), "/").length != 2) {
                            LOG.warn("Warning composition title is not splittable in 2 pieces: {} - {}", c.getArtist(),
                                    c.getTitre());
                            continue;
                        }
                        dialog.updateDialog(c, i);
                        dialog.setVisible(true);
                        Boolean action = dialog.getSendData();
                        if (action == null) {
                            // stop everything
                            LOG.debug("Stop");
                            org.apache.commons.io.FileUtils.writeStringToFile(new File(Constant.SLASH_FILE_PATH),
                                    StringUtils.join(slashFile, ","), Constant.ANSI_ENCODING);
                            break;
                        } else if (Boolean.TRUE.equals(action)) {
                            // Edit composition
                            Fichier file = c.getFiles().get(0);
                            String newUuid = MiscUtils.getUuid();

                            List<Composition> finalFile = ImportXML.importXML(Constant.getFinalFilePath());
                            finalFile = PanelUtils.splitComposition(finalFile, dialog.getTitle1(), dialog.getTitle2(),
                                    c.getUuids(), Arrays.asList(newUuid), true);

                            List<Composition> xmlFile = ImportXML
                                    .importXML(FilesUtils.buildXmlFilePath(file.getFileName()).orElse(null));
                            xmlFile = PanelUtils.splitComposition(xmlFile, dialog.getTitle1(), dialog.getTitle2(), c.getUuids(),
                                    Arrays.asList(newUuid), false);

                            ExportXML.exportXML(finalFile, Constant.getFinalFile());
                            ExportXML.exportXML(xmlFile, file.getFileName());
                        } else {
                            // Skip composition
                            LOG.debug("Skip");
                            slashFile.addAll(c.getUuids());
                        }
                    }
                } catch (MajorException | IOException e1) {
                    result.append(e1.getMessage());
                    LOG.error("Error when slash editing", e1);
                }

                LOG.debug("End slashEdit");
                return writeInFile(result, Constant.BATCH_FILE);
            }

            private static List<String> readSlashFile() throws MajorException {
                String content = "";
                File file = new File(Constant.SLASH_FILE_PATH);
                try {
                    if (FileUtils.fileExists(file.getAbsolutePath())) {
                        content = org.apache.commons.io.FileUtils.readFileToString(file, Constant.ANSI_ENCODING);
                    } else {
                        FileUtils.fileWrite(file, "");
                    }
                } catch (IOException e1) {
                    LOG.error("Error when reading slash file", e1);
                    throw new MajorException("Error when reading slash file: " + e1.getMessage());
                }
                return new LinkedList<>(Arrays.asList(StringUtils.split(content, ",")));
            }

            /**
             * Checks if the compositions in the given file or directory are deleted.
             *
             * @param source the file or directory
             * @param type   record type of the file
             */
            public static void checksIfDeleted(File source, RecordType type) {
                LOG.debug("Start checksIfDeleted");

                Map<File, RecordType> files;
                if (source.isDirectory()) {
                    files = FilesUtils.listFilesInFolder(source, Constant.TXT_EXTENSION, false).stream()
                            .collect(Collectors.toMap(f -> f, f -> ImportFile.determineType(f.getName())));
                } else {
                    files = Map.of(source, type);
                }

                files.forEach((file, recordType) -> {
                    StringBuilder text = new StringBuilder();
                    addLine(text, "Checks If Deleted: ", true);
                    if (RecordType.UNKNOWN != recordType) {
                        List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
                        final AtomicInteger count = new AtomicInteger(1);
                        Map<String, List<String>> result = new TreeMap<>();
                        FilesUtils.readFile(file).stream().forEach(line -> {
                            String[] split = StringUtils.splitByWholeSeparator(line, " - ");
                            if (split.length == 2) {
                                Arrays.stream(StringUtils.split(split[1], "/"))
                                .forEach(title -> addsToChecksIfDeletedResult(result, checksOneIfDeleted(importXML,
                                        StringUtils.trim(split[0]), StringUtils.trim(title), count.get(), recordType)));
                            } else {
                                addsToChecksIfDeletedResult(result,
                                        new String[] { "Unsplittable", line + ", line: " + count.get() });
                            }
                            count.incrementAndGet();
                        });
                        result.forEach((key, value) -> {
                            addLine(text, Constant.NEW_LINE + key, false);
                            value.forEach(v -> addLine(text, v, false));
                        });
                    } else {
                        addLine(text, "Can't guess record type of the file", false);
                    }
                    writeInFile(text, file.getName() + "_ChecksIfDeleted.txt");
                });
                LOG.debug("End checksIfDeleted");
            }

            private static void addsToChecksIfDeletedResult(Map<String, List<String>> result, String[] checks) {
                if (checks.length > 0) {
                    String key = checks[0];
                    result.putIfAbsent(key, new ArrayList<String>());
                    List<String> list = result.get(key);
                    list.add(checks[1]);
                    result.put(key, list);
                }
            }

            private static String[] checksOneIfDeleted(List<Composition> importXML, String artist, String title, int index,
                    RecordType type) {
                Map<String, String> criteria = fillSearchCriteriaForMassDeletion(type.toString(), artist, title);
                List<Composition> compoFound = SearchUtils.search(importXML, criteria, false, SearchMethod.CONTAINS, true,
                        false);
                String key = null;
                String extra = "";
                if (compoFound.isEmpty()) {
                    // nothing found
                    key = "Not Found";
                } else if (compoFound.size() > 1) {
                    // Multiple result
                    key = "Size";
                    extra = ": " + compoFound.size() + " " + compoFound.stream().map(c -> c.getArtist() + " - " + c.getTitre())
                            .collect(Collectors.joining(" / ", "[", "]"));
                } else if (compoFound.get(0).isDeleted()) {
                    // Already deleted
                    key = "Already";
                }
                List<String> result = new ArrayList<>();
                if (key != null) {
                    result = List.of(key, artist + " - " + title + extra + ", line " + index);
                }
                return result.toArray(new String[0]);
            }

            /**
             * Reads csv file, searchs its compositions in final file and aks if user wants
             * to delete found compositions.
             *
             * @param type record type: song or album
             * @param file csv file
             * @return the file name of the result file
             */
            public static String massDeletion(String type, File file) {
                LOG.debug("Start massDeletion");
                StringBuilder text = new StringBuilder();
                addLine(text, "Start massDeletion", true);

                List<ItunesComposition> compoCsv = CsvFile.importCsv(file, ItunesComposition.class);
                addLine(text, "Import csv file successfully", true);

                List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
                OngletPanel.getArtist().interruptUpdateArtist(true);
                if (type.equals(RecordType.SONG.toString())) {
                    massDeletionForSongs(text, compoCsv, importXML);
                } else {
                    massDeletionForAlbums(text, compoCsv, importXML);
                }

                // Modifies csv entry file
                CsvFile.exportBeanList(file, compoCsv, new CustomColumnPositionMappingStrategy<>(ItunesComposition.class, null),
                        false);
                addLine(text, "Csv file successfully exported", true);

                try {
                    ExportXML.exportXML(importXML, Constant.getFinalFile());
                    OngletPanel.getArtist().updateArtistPanel();
                    addLine(text, "Final file successfully exported", true);
                } catch (MajorException e1) {
                    LOG.error("Erreur lors de l'export du fichier final", e1);
                    addLine(text, "Erreur lors de l'export du fichier final !!" + e1, true);
                }

                LOG.debug("End massDeletion");
                addLine(text, "End massDeletion", true);
                return writeInFile(text, "Delete.txt");
            }

            /**
             * Mass deletion for songs.
             *
             * @param text      log
             * @param compoCsv  compo to delete
             * @param importXML compo from final file
             */
            private static void massDeletionForSongs(StringBuilder text, List<ItunesComposition> compoCsv,
                    List<Composition> importXML) {
                DeleteCompoDialog deleteDialog = new DeleteCompoDialog(null, compoCsv.size());
                for (int i = 0; i < compoCsv.size(); i++) {
                    // Search composition
                    ItunesComposition compoToDelete = compoCsv.get(i);
                    if (!StringUtils.isNotBlank(compoToDelete.getDeletedSong())) {
                        // Not already processed
                        Map<String, String> criteria = fillSearchCriteriaForMassDeletion(RecordType.SONG.toString(),
                                compoToDelete.getArtist(), compoToDelete.getTitre());

                        // Search composition
                        List<Composition> compoFound = SearchUtils.search(importXML, criteria, false, SearchMethod.CONTAINS,
                                true, false);
                        if (processComposition(RecordType.SONG, importXML, deleteDialog, i, Arrays.asList(compoToDelete),
                                compoFound) == null) {
                            break;
                        }
                    }
                }
                addLine(text, "End of deleting Song", true);
            }

            /**
             * Process a composition to choose if it should be deleted.
             *
             * @param type          record type of compositions
             * @param importXML     all compositions
             * @param deleteDialog  the dialog to choose if the composition found should be
             *                      deleted or not
             * @param i             counter
             * @param compoToDelete composition from the csv to delete
             * @param compoFound    composition found
             */
            private static String processComposition(RecordType type, List<Composition> importXML,
                    DeleteCompoDialog deleteDialog, int i, List<ItunesComposition> compoToDelete,
                    List<Composition> compoFound) {
                String result = null;
                if (compoFound.isEmpty()) {
                    // nothing found
                    result = "Not Found";
                } else if (compoFound.get(0).isDeleted()) {
                    // Already deleted
                    result = "Already";
                } else if (compoFound.size() > 1) {
                    // Multiple result
                    result = "Size: " + compoFound.size();
                } else {
                    // Composition can be deleted
                    Composition found = compoFound.get(0);
                    // update dialog
                    if (RecordType.ALBUM == type) {
                        deleteDialog.updateDialog(prettyPrintForAlbum(compoToDelete), found, i, warningForAlbum(compoToDelete));
                    } else {
                        deleteDialog.updateDialog(prettyPrintForSong(compoToDelete.get(0)), found, i,
                                warningForSong(compoToDelete.get(0)));
                    }
                    deleteDialog.setVisible(true);
                    Boolean action = deleteDialog.getSendData();
                    if (action == null) {
                        // stop everything
                        LOG.debug("Stop");
                        return null;
                    } else if (Boolean.TRUE.equals(action)) {
                        // Delete
                        result = deleteComposition(importXML, found);
                    } else {
                        // Skip composition
                        result = "KO";
                    }
                }
                final String deleted = result;
                if (RecordType.ALBUM == type) {
                    compoToDelete.forEach(csv -> csv.setDeletedAlbum(deleted));
                } else {
                    compoToDelete.forEach(csv -> csv.setDeletedSong(deleted));
                }
                return deleted;
            }

            private static String deleteComposition(List<Composition> importXML, Composition found) {
                // Delete composition
                return CompositionUtils.findByUuid(importXML, found.getUuids()).map(toRemove -> {
                    String result;
                    try {
                        toRemove.setDeleted(true);
                        CompositionUtils.removeCompositionInFiles(toRemove);
                        result = "OK";
                    } catch (MajorException e) {
                        LOG.error("Error when deleting compostion: {}", found, e);
                        result = "Error";
                    }
                    return result;
                }).orElseGet(() -> {
                    LOG.error("Can't find compostion in final: {}", found);
                    return "Error";
                });
            }

            private static String prettyPrintForSong(ItunesComposition csv) {
                StringBuilder sb = new StringBuilder();
                List<String> ignoreField = Arrays.asList("deletedSong", "deletedAlbum", "artist", "titre", "trackNumber",
                        "cdNumber");
                sb.append(Constant.NEW_LINE).append(csv.getArtist()).append(" - ").append(csv.getTitre());
                try {
                    Field[] declaredFields = ItunesComposition.class.getDeclaredFields();
                    for (Field field : declaredFields) {
                        if (ignoreField.contains(field.getName())) {
                            continue;
                        }
                        CsvBindByName annotation = field.getAnnotationsByType(CsvBindByName.class)[0];
                        Object fieldValue = FieldUtils.readField(field, csv, true);
                        if (fieldValue != null) {
                            sb.append(Constant.NEW_LINE).append(annotation.column()).append(": ")
                            .append(convertValueField(field, fieldValue));
                        }
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    LOG.error("This should not append", e);
                }
                return sb.toString();
            }

            /**
             * Converts or formats given value depending on the field type and name.
             *
             * @param field      the field
             * @param fieldValue the value of the field
             * @return formats dates, if rank field converts to stars
             */
            private static String convertValueField(Field field, Object fieldValue) {
                String result;
                if (field.getType().equals(Date.class)) {
                    result = fieldValue != null ? new Constant().getFullDTF()
                            .format(LocalDateTime.ofInstant(((Date) fieldValue).toInstant(), ZoneId.systemDefault())) : "";
                } else if (field.getName().equals("rank")) {
                    result = fieldValue != null ? String.valueOf((Integer) fieldValue / 20) + " Stars" : "0 Stars";
                } else if (field.getType().equals(Integer.class)) {
                    result = fieldValue != null ? String.valueOf(fieldValue) : "0";
                } else {
                    result = fieldValue != null ? String.valueOf(fieldValue) : "";
                }
                return result;
            }

            private static String warningForSong(ItunesComposition csv) {
                List<String> result = new ArrayList<>();
                if (csv.getPlayCount() != null && csv.getPlayCount() < 10) {
                    result.add("Nombre de lecture < 10");
                } else if (csv.getPlayCount() == null) {
                    result.add("Nombre de lecture 0 !");
                }
                if (csv.getBitRate() != null) {
                    String[] split = StringUtils.split(csv.getBitRate(), " ");
                    if (split.length == 2 && NumberUtils.isDigits(split[0]) && Integer.valueOf(split[0]) < 128) {
                        result.add("Bit Rate < 128");
                    }
                }
                if (csv.getRank() != null && csv.getRank() < 90) {
                    result.add("Classement < 5 Étoiles");
                } else if (csv.getRank() == null) {
                    result.add("Classement 0 Étoiles !");
                }
                return result.stream().collect(Collectors.joining(Constant.NEW_LINE));
            }

            private static String warningForAlbum(List<ItunesComposition> list) {
                List<String> result = new ArrayList<>();
                int thirdOfSize = Math.floorDiv(list.size(), 3);
                if (list.stream()
                        .filter(csv -> csv.getPlayCount() == null || (csv.getPlayCount() != null && csv.getPlayCount() < 10))
                        .count() >= thirdOfSize) {
                    result.add("Beaucoup de lecture < 10");
                }
                if (list.stream().filter(csv -> csv.getRank() == null || (csv.getRank() != null && csv.getRank() < 90))
                        .count() >= thirdOfSize) {
                    result.add("Beaucoup de classement < 5 Étoiles");
                }
                return result.stream().collect(Collectors.joining(Constant.NEW_LINE));
            }

            /**
             * Mass deletion for albums.
             *
             * @param text      log
             * @param compoCsv  compo ffrom csv file
             * @param importXML all compo from final file
             */
            private static void massDeletionForAlbums(StringBuilder text, List<ItunesComposition> compoCsv,
                    List<Composition> importXML) {
                List<String> albumList = compoCsv.stream()
                        .sorted(Comparator.comparing(ItunesComposition::getAlbum)
                                .thenComparing(compareByTrackNumber.reversed()))
                        .map(ItunesComposition::getAlbum).filter(StringUtils::isNotBlank).distinct()
                        .collect(Collectors.toList());
                DeleteCompoDialog deleteDialog = new DeleteCompoDialog(null, albumList.size());
                for (int i = 0; i < albumList.size(); i++) {
                    String album = albumList.get(i);
                    List<ItunesComposition> compoAlbum = compoCsv.stream().filter(csv -> csv.getAlbum().equals(album))
                            .collect(Collectors.toList());
                    if (compoAlbum.stream().allMatch(csv -> StringUtils.isNotBlank(csv.getDeletedAlbum()))) {
                        // Already processed
                        continue;
                    }
                    ItunesComposition compoToDelete = compoAlbum.get(0);
                    String trackNumber = compoToDelete.getTrackNumber();
                    String albumToSearch = "";
                    if (compoAlbum.size() < 5 || StringUtils.isBlank(trackNumber)) {
                        compoAlbum.forEach(csv -> csv.setDeletedAlbum("Invalid"));
                        continue;
                    } else if (StringUtils.contains(trackNumber, Constant.TRACK_NUMBER_SEPARATOR)) {
                        String[] split = StringUtils.split(trackNumber, Constant.TRACK_NUMBER_SEPARATOR);
                        String max = split[1];
                        if (compoAlbum.size() >= Integer.valueOf(max)) {
                            albumToSearch = album;
                        } else {
                            compoAlbum.forEach(csv -> csv.setDeletedAlbum("Incomplete"));
                            continue;
                        }
                    } else if (compoAlbum.size() >= Integer.valueOf(trackNumber)) {
                        albumToSearch = album;
                    } else {
                        compoAlbum.forEach(csv -> csv.setDeletedAlbum("Too Small"));
                        continue;
                    }
                    Map<String, String> criteria = fillSearchCriteriaForMassDeletion(RecordType.ALBUM.toString(),
                            compoToDelete.getArtist(), albumToSearch);
                    // Search composition
                    List<Composition> compoFound = SearchUtils.search(importXML, criteria, false, SearchMethod.CONTAINS, true,
                            true);
                    if (processComposition(RecordType.ALBUM, importXML, deleteDialog, i, compoAlbum, compoFound) == null) {
                        break;
                    }
                }
                addLine(text, "End of deleting Album", true);
            }

            private static String prettyPrintForAlbum(List<ItunesComposition> list) {
                StringBuilder sb = new StringBuilder();
                try {
                    sb.append(Constant.NEW_LINE).append(groupByField(list, "artist"));
                    sb.append(Constant.NEW_LINE).append("Album: " + list.get(0).getAlbum());
                    sb.append(Constant.NEW_LINE).append(groupByField(list, "added"));
                    sb.append(Constant.NEW_LINE).append(groupByField(list, "year"));
                    sb.append(Constant.NEW_LINE).append(groupByField(list, "playCount"));
                    sb.append(Constant.NEW_LINE).append(groupByField(list, "rank"));
                } catch (IllegalArgumentException e) {
                    LOG.error("This should not append", e);
                }
                return sb.toString();
            }

            private static String groupByField(List<ItunesComposition> list, String field) {
                String result = "";
                Map<Object, Long> collect = list.stream().collect(Collectors.groupingBy(csv -> {
                    try {
                        return Optional.ofNullable(FieldUtils.readField(csv, field, true));
                    } catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
                        LOG.error("This should not append", e);
                        return "";
                    }
                }, Collectors.counting()));
                Field declaredField = FieldUtils.getDeclaredField(ItunesComposition.class, field, true);
                if (!collect.isEmpty()) {
                    if (collect.size() == 1) {
                        result = convertValueField(declaredField,
                                ((Optional<?>) collect.keySet().iterator().next()).orElse(null));
                    } else {
                        result = "{ "
                                + collect.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getValue, e -> new ArrayList<>(Arrays.asList(
                                        convertValueField(declaredField, ((Optional<?>) e.getKey()).orElse(null)))),
                                        (o, n) -> {
                                            o.addAll(n);
                                            if (declaredField.getType().equals(Integer.class)
                                                    && !declaredField.getName().equals("rank")) {
                                                o.sort(MiscUtils.compareInteger.reversed());
                                            }
                                            return o;
                                        }))
                                .entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                                .map(e -> {
                                    String res = e.getKey() + "x = ";
                                    if (e.getValue().size() > 1) {
                                        return res + "[" + StringUtils.join(e.getValue(), ", ") + "]";
                                    } else {
                                        return res + e.getValue().get(0);
                                    }
                                }).collect(Collectors.joining(", "))
                                + " }";
                    }
                }
                if (StringUtils.isNotBlank(result)) {
                    return declaredField.getAnnotationsByType(CsvBindByName.class)[0].column() + ": " + result;
                } else {
                    return "";
                }
            }

            private static Map<String, String> fillSearchCriteriaForMassDeletion(String type, String artist, String titre) {
                Map<String, String> criteria = new HashMap<>();
                criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, type);
                // Clean artist and title
                Set<Entry<String, String>> entrySet = CleanFile.getModifSet();
                String stripArtist = StringUtils.substringBefore(
                        MiscUtils.removeParentheses(
                                CleanFile.removeDiactriticals(MiscUtils.cleanLine(artist.toLowerCase(), entrySet))),
                        Constant.SEPARATOR_AND);
                if (StringUtils.startsWith(stripArtist, "the ")) {
                    stripArtist = StringUtils.substringAfter(stripArtist, "the ");
                }
                criteria.put(SearchUtils.CRITERIA_ARTIST, MiscUtils.removePunctuation(stripArtist));
                criteria.put(SearchUtils.CRITERIA_TITRE, MiscUtils.removePunctuation(MiscUtils
                        .removeParentheses(CleanFile.removeDiactriticals(MiscUtils.cleanLine(titre.toLowerCase(), entrySet)))));
                return criteria;
            }
}
