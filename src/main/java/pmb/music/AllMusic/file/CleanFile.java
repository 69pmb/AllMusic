package pmb.music.AllMusic.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FilesUtils;

/**
 * Classe pour nettoyer des fichiers.
 *
 * @see CleanFile#clearFile(File, boolean, String, String)
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
     * Supprime toutes les lignes du fichier ne contenant pas des séparateurs,
     * possibilité de supprimer dans ces lignes des caractères particuliers.
     *
     * @param file le fichier à nettoyer
     * @param isSorted si le fichier est trié
     * @param separator le séparateur du fichier
     * @param characterToRemove les caractères à supprimer
     * @param maxLength maximum length for a line
     * @param isBefore if true keep sub string before characterToRemove, false after
     * @return un nouveau fichier nettoyé
     * @throws IOException if an error occured when reading or writing files
     */
    public static File clearFile(File file, boolean isSorted, String separator, String characterToRemove,
            Integer maxLength, boolean isBefore) throws IOException {
        LOG.debug("Start clearFile");
        String line = "";
        String exitFile = buildGeneratedFilePath(file, SUFFIX_CLEAR);

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), Constant.ANSI_ENCODING));
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(exitFile), Constant.ANSI_ENCODING))) {
            while ((line = br.readLine()) != null) {
                boolean isDigit = true;
                if (isSorted) {
                    // Si le fichier est trié, on ne garde que les lignes commencant par un chiffre
                    isDigit = StringUtils.isNumeric(StringUtils.substring(line, 0, 1));
                }
                if (isDigit && line.length() < maxLength) {
                    writesLineIfContainsSepAndRemovesChar(characterToRemove, separator, line, writer, isBefore);
                }
            }
            writer.flush();
        }
        LOG.debug("End clearFile");
        return new File(exitFile);
    }

    /**
     * Builds the generated file absolute path.
     * @param file to be processed
     * @return absolute path of processed file
     */
    public static String buildGeneratedFilePath(File file, String suffix) {
        return file.getParentFile().getAbsolutePath() + FileUtils.FS
                + StringUtils.substringBeforeLast(file.getName(), Constant.DOT) + suffix
                + StringUtils.substringAfterLast(file.getName(), Constant.DOT);
    }

    private static void writesLineIfContainsSepAndRemovesChar(String characterToRemove, String separator, String line,
            BufferedWriter writer, boolean isBefore) throws IOException {
        String newLine = line;
        if (StringUtils.isBlank(separator) || StringUtils.containsIgnoreCase(newLine, separator)) {
            if (StringUtils.containsIgnoreCase(newLine, characterToRemove) && isBefore) {
                newLine = StringUtils.substringAfter(newLine, characterToRemove);
            } else if (StringUtils.containsIgnoreCase(newLine, characterToRemove) && !isBefore) {
                newLine = StringUtils.substringBeforeLast(newLine, characterToRemove);
            }
            writer.append(newLine).append(Constant.NEW_LINE);
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
        if (entrySet == null) {
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
            boolean modify = false;
            // Fichier de sortie
            String exitFile = buildGeneratedFilePath(file, SUFFIX_MEF);
            String name = file.getName();
            LOG.debug(name);
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), Constant.ANSI_ENCODING));
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(new FileOutputStream(exitFile), Constant.ANSI_ENCODING))) {
                String line;
                while ((line = br.readLine()) != null) {
                    modify = formattingAndAppendingLine(entrySet, modify, name, writer, line);
                }
            } catch (IOException e) {
                LOG.error("Erreur lors du netoyage de {}", file.getAbsolutePath(), e);
            }
            if (modify) {
                LOG.debug("{} modifié", file);
                result.add(file.getName());
            } else {
                try {
                    Files.delete(Paths.get(exitFile));
                } catch (IOException e) {
                    LOG.warn("{} n'a pas pu etre supprimé", exitFile, e);
                }
            }
        });
        LOG.debug("End miseEnForme");
    }

    /**
     * Formats given line with mofif rules and removes diactriticals. Then appends
     * the line to the writer.
     *
     * @param entrySet the modif rules
     * @param modify if the file has been edited
     * @param name name of the file
     * @param writer the writer
     * @param line the line to format
     * @return if the line has been edited
     * @throws IOException
     */
    private static boolean formattingAndAppendingLine(Set<Entry<String, String>> entrySet, boolean modify, String name,
            BufferedWriter writer, String line) throws IOException {
        if (!ImportFile.isValidLine(line)) {
            // line doesn't contain a composition, ignores it
            writer.append(line).append(Constant.NEW_LINE);
            return modify;
        }
        for (Entry<String, String> entry : entrySet) {
            if (StringUtils.containsIgnoreCase(line, entry.getKey())) {
                line = StringUtils.replaceIgnoreCase(line, entry.getKey(), entry.getValue());
                modify = true;
            }
        }
        if (StringUtils.endsWithIgnoreCase(name, Constant.TXT_EXTENSION)) {
            // Supprime les diacritiques et les accents
            String replaceAll = removeDiactriticals(line);
            if (!StringUtils.endsWithIgnoreCase(line, replaceAll)) {
                modify = true;
            }
            writer.append(replaceAll).append(Constant.NEW_LINE);
        } else {
            writer.append(line).append(Constant.NEW_LINE);
        }
        return modify;
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
        File modifFile = new File(Constant.MODIF_FILE_PATH);
        Map<String, String> modif = new HashMap<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(modifFile), Constant.ANSI_ENCODING))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = StringUtils.split(line, ":");
                if (split.length > 1) {
                    modif.put(split[0], split[1]);
                } else {
                    modif.put(split[0], "");
                }
            }
        } catch (IOException e) {
            LOG.error("Erreur lors du parsing " + modifFile.getAbsolutePath(), e);
            return new HashSet<>();
        }
        return modif.entrySet();
    }

}
