package pmb.allmusic.file;

import java.io.File;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import pmb.allmusic.utils.Constant;
import pmb.allmusic.utils.FilesUtils;

/**
 * Classe pour nettoyer des fichiers.
 *
 * @see CleanFile#clearFile(File, boolean, String, String)
 * @author pmbroca
 */
public final class CleanFile {

    private static final Logger LOG = LogManager.getLogger(CleanFile.class);

    /**
     * Suffix of the cleaned file name.
     */
    public static final String SUFFIX_CLEAR = " - Cleaned.";

    /**
     * Suffix of the MEF file name.
     */
    public static final String SUFFIX_MEF = " - MEF.";

    private CleanFile() {
        throw new AssertionError("Must not be used");
    }

    /**
     * Deletes all lines which don't contain the separator or are too long, with the
     * possibility to remove specific characters.
     *
     * @param file to clean
     * @param isSorted if the file is sorted
     * @param separator separator of the file
     * @param characterToRemove character to remove
     * @param maxLength maximum length for a line
     * @param isBefore if true keep sub string before characterToRemove, false after
     * @return a new file containing lines cleaned
     */
    public static File clearFile(File file, boolean isSorted, String separator, String characterToRemove,
            Integer maxLength, boolean isBefore) {
        LOG.debug("Start clearFile");
        File exitFile = new File(buildGeneratedFilePath(file, SUFFIX_CLEAR));
        FilesUtils.writeFile(exitFile, FilesUtils.readFile(file).stream()
                .filter(line-> line.length() < maxLength)
                .map(line -> clearLine(line, separator, characterToRemove, isBefore, isSorted))
                .filter(Objects::nonNull).collect(Collectors.toList()));
        LOG.debug("End clearFile");
        return exitFile;
    }

    /**
     * Returns line if it contains given separator (or if the separator is blank) or
     * if beginning by a number for sorted files and then removes character at the
     * begin or at the end of the line.
     *
     * @param line line to clear
     * @param separator char separating artist and title
     * @param characterToRemove to remove from the line
     * @param isBefore if at start or at end
     * @param isSorted if file is sorted
     * @return the cleaned line or null if useless line
     */
    private static String clearLine(String line, String separator, String characterToRemove, boolean isBefore,
            boolean isSorted) {
        String newLine = line;
        if ((!isSorted || StringUtils.isNumeric(StringUtils.substring(line, 0, 1)))
                && (StringUtils.isBlank(separator) || StringUtils.containsIgnoreCase(newLine, separator))) {
            // If the file is sorted, only the lines beginning by a number are kept
            boolean containsToRemove = StringUtils.containsIgnoreCase(newLine, characterToRemove);
            if (containsToRemove && isBefore) {
                newLine = StringUtils.substringAfter(newLine, characterToRemove);
            } else if (containsToRemove) {
                newLine = StringUtils.substringBeforeLast(newLine, characterToRemove);
            }
            return newLine;
        } else {
            return null;
        }
    }

    /**
     * Mofifie les fichiers spécifiés: Remplace suivant le fichier modif.txt des
     * caractères par d'autres. Supprime tous les diacritiques.
     *
     * @param folder a folder or a file
     * @param isCompleteDirectory if true processes all files in folder, otherwise
     *            only given file
     * @param result a list of string that will holds the name of the file processed
     */
    public static void miseEnForme(File folder, boolean isCompleteDirectory, List<String> result) {
        LOG.debug("Start miseEnForme");
        Set<Entry<String, String>> entrySet = getModifSet();
        if (CollectionUtils.isEmpty(entrySet)) {
            LOG.warn("Modif file is empty");
            return;
        }

        List<File> files;
        if (!isCompleteDirectory) {
            // Un seul fichier
            files = Collections.singletonList(folder);
        } else {
            // Tous les fichiers du repertoire
            String extention = StringUtils.substringAfterLast(folder.getName(), Constant.DOT);
            files = FilesUtils.listFilesInFolder(folder.getParentFile(), extention, false);
        }
        files = files.stream().filter(f -> !StringUtils.equals(Constant.getFinalFile(), f.getName()))
                .collect(Collectors.toList());

        files.forEach(file -> {
            AtomicBoolean modify = new AtomicBoolean(false);
            String exitFile = buildGeneratedFilePath(file, SUFFIX_MEF);
            String name = file.getName();
            LOG.debug(name);
            List<String> writer = FilesUtils.readFile(file).stream()
                    .map(line -> formattingAndAppendingLine(entrySet, modify, name, line))
                    .collect(Collectors.toList());
            if (modify.get()) {
                LOG.debug("{} modifié", file);
                result.add(file.getName());
                FilesUtils.writeFile(exitFile, writer);
                }
        });
        LOG.debug("End miseEnForme");
    }

    /**
     * Formats given line with mofif rules and removes diactriticals.
     *
     * @param rules the modif rules
     * @param modify if the file has been edited, value can changed
     * @param name name of the file
     * @param line the line to format
     * @return line formatted
     */
    private static String formattingAndAppendingLine(Set<Entry<String, String>> rules, AtomicBoolean modify,
            String name, String line) {
        if (!ImportFile.isValidLine(line)) {
            // line doesn't contain a composition, ignores it
            return line;
        }
        for (Entry<String, String> rule : rules) {
            if (StringUtils.containsIgnoreCase(line, rule.getKey())) {
                line = StringUtils.replaceIgnoreCase(line, rule.getKey(), rule.getValue());
                modify.set(true);
            }
        }
        String transformed = line;
        if (StringUtils.endsWithIgnoreCase(name, Constant.TXT_EXTENSION)) {
            // Supprime les diacritiques et les accents
            String replaceAll = removeDiactriticals(transformed);
            if (!StringUtils.endsWithIgnoreCase(transformed, replaceAll)) {
                modify.set(true);
                transformed = replaceAll;
            }
        }
        return transformed;
    }

    /**
     * Removes all diactriticals from the given line.
     *
     * @param line the line to clean
     * @return the line cleaned
     */
    public static String removeDiactriticals(String line) {
        String replaceAll = Normalizer.normalize(line, Form.NFKD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        replaceAll = StringUtils.stripAccents(replaceAll);
        return replaceAll;
    }

    /**
     * Fetches the modif file where unwanted characters and their substitutes are
     * describes.
     *
     * @return an Entry of a Set with key unwanted characters and value its
     *         substitute
     */
    public static Set<Entry<String, String>> getModifSet() {
        return FilesUtils.readFile(new File(Constant.MODIF_FILE_PATH), "UTF-8").stream()
                .map(line -> StringUtils.split(line, ":"))
                .collect(Collectors.toMap(s -> s[0], s -> s.length > 1 ? s[1] : "")).entrySet();
    }

    /**
     * Builds the absolute path of a generated file.
     * @param file to be processed
     * @param suffix added at the end of file name
     * @return absolute path of generated file
     */
    public static String buildGeneratedFilePath(File file, String suffix) {
        return file.getParentFile().getAbsolutePath() + FileUtils.FS
                + StringUtils.substringBeforeLast(file.getName(), Constant.DOT) + suffix
                + StringUtils.substringAfterLast(file.getName(), Constant.DOT);
    }
}
