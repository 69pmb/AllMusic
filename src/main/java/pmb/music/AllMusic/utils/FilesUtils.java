package pmb.music.AllMusic.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import pmb.music.AllMusic.exception.MajorException;

/**
 * Utility class for handling files.
 */
public final class FilesUtils {

    private static final Logger LOG = LogManager.getLogger(FilesUtils.class);

    private FilesUtils() {
        throw new AssertionError("Must not be used");
    }

    /**
     * Crée le dossier si il n'existe pas.
     *
     * @param nomDir le chemin du dossier
     */
    public static void createFolderIfNotExists(String nomDir) {
        if (!FileUtils.fileExists(nomDir)) {
            FileUtils.mkdir(nomDir);
        }
    }

    /**
     * Récupère la liste des fichiers d'un dossier.
     *
     * @param folder le dossier où chercher
     * @param files la liste qui contiendra les résultats
     * @param extension l'extension des fichiers à chercher
     * @param recursive si la recherche doit etre récursive ou non
     */
    public static void listFilesForFolder(final File folder, List<File> files, String extension, boolean recursive) {
        if (!folder.isDirectory()) {
            files.add(folder);
            return;
        }
        for (final File fileEntry : folder.listFiles()) {
            if (recursive && fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry, files, extension, recursive);
            } else if (StringUtils.endsWith(fileEntry.getName(), extension)) {
                files.add(fileEntry);
            }
        }
    }

    /**
     * Ouvre le fichier donnée avec Notepad++ si existe.
     *
     * @param filePath le chemin absolu du fichier
     * @param lineNumber open the file to this specific line
     * @throws MajorException something went wrong
     */
    public static void openFileInNotepad(String filePath, Integer lineNumber) throws MajorException {
        LOG.debug("Start openFileInNotepad");
        if (filePath != null) {
            if (FileUtils.fileExists(filePath)) {
                String lineNb = "";
                // calculates the specific line number
                if (lineNumber != null && StringUtils.endsWith(filePath, Constant.TXT_EXTENSION)) {
                    // offset with import settings
                    lineNb = "-n" + (lineNumber + 1) + " ";
                } else if (lineNumber != null && StringUtils.endsWith(filePath, Constant.XML_EXTENSION)) {
                    // offset with root tags and each compo is 3 lines long
                    lineNb = "-n" + ((lineNumber - 1) * 3 + 4) + " ";
                }
                try {
                    Runtime.getRuntime()
                    .exec(Constant.getNotepadPath() + lineNb + Constant.QUOTE + filePath + Constant.QUOTE);
                } catch (IOException e) {
                    throw new MajorException("Le chemin de Notepad++ dans le fichier de config est incorrect.", e);
                }
            } else {
                throw new MajorException("Le fichier: " + filePath + " n'existe pas.");
            }
        } else {
            throw new MajorException("Aucun fichier donné.");
        }
        LOG.debug("End openFileInNotepad");
    }

    /**
     * Ouvre le fichier donnée avec Excel si existe.
     *
     * @param filePath le chemin absolu du fichier
     * @throws MajorException something went wrong
     */
    public static void openFileInExcel(String filePath) throws MajorException {
        LOG.debug("Start openFileInExcel");
        if (filePath != null) {
            if (FileUtils.fileExists(filePath)) {
                try {
                    String[] commands = new String[] { Constant.getExcelPath(), filePath };
                    Runtime.getRuntime().exec(commands);
                } catch (IOException e) {
                    throw new MajorException("Le chemin d'Excel dans le fichier de config est incorrect.", e);
                }
            } else {
                throw new MajorException("Le fichier: " + filePath + " n'existe pas.");
            }
        } else {
            throw new MajorException("Aucun fichier donné.");
        }
        LOG.debug("End openFileInExcel");
    }

    /**
     * Reconstruit le chemin absolu du fichier txt (du dossier Music) donnée.
     *
     * @param fileName le nom du fichier
     * @param auteur l'auteur du fichier (pour connaitre le nom du dossier du
     *            fichier)
     * @return le chemin absolu du fichier
     */
    public static Optional<String> buildTxtFilePath(String fileName, String auteur) {
        LOG.debug("Start buildTxtFilePath");
        String pathRoot = Constant.getMusicAbsDirectory() + auteur + FileUtils.FS;
        String nameWithExtension = fileName + Constant.TXT_EXTENSION;

        String pathShort = pathRoot + nameWithExtension;
        String pathSong = pathRoot + Constant.SONG_FOLDER + FileUtils.FS + nameWithExtension;
        String pathAlbum = pathRoot + Constant.ALBUM_FOLDER + FileUtils.FS + nameWithExtension;
        String pathYear = pathRoot + Constant.YEAR_FOLDER + FileUtils.FS + nameWithExtension;

        Optional<String> result = Arrays.asList(pathShort, pathSong, pathAlbum, pathYear).stream()
                .filter(FileUtils::fileExists).findFirst();
        if (!result.isPresent()) {
            LOG.warn("End buildTxtFilePath, no path built for: {} - {}", fileName, auteur);
        }
        LOG.debug("End buildTxtFilePath");
        return result;
    }

    /**
     * Reconstruit le chemin absolu du fichier xml (du dossier XML) donnée.
     *
     * @param fileName le nom du fichier
     * @return le chemin absolu du fichier
     */
    public static Optional<String> buildXmlFilePath(String fileName) {
        LOG.debug("Start buildXmlFilePath");
        String path = Constant.getXmlPath() + fileName;
        path += StringUtils.endsWithIgnoreCase(fileName, Constant.XML_EXTENSION) ? "" : Constant.XML_EXTENSION;
        if (!FileUtils.fileExists(path)) {
            LOG.warn("End buildXmlFilePath, no path built for: {}", fileName);
            LOG.warn("Path tested: {}", path);
            return Optional.empty();
        }
        LOG.debug("End buildXmlFilePath");
        return Optional.of(path);
    }

    /**
     * Export an object to json in a file.
     *
     * @param o the object to export
     * @param filePath the absolute path of the file
     */
    public static void exportJsonInFile(Object o, String filePath) {
        LOG.debug("Start exportJsonInFile");
        String json = "";
        try {
            json = MiscUtils.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            LOG.error("Error when converting object to json", e);
        }
        File file = new File(filePath);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), Constant.ANSI_ENCODING))) {
            writer.append(json);
        } catch (IOException e) {
            LOG.error("Error exporting file: {}", file.getName(), e);
        }
        LOG.debug("End exportJsonInFile");
    }

    /**
     * Write (or overwrite if already present) import parameters in txt file.
     *
     * @param file the file in which paramaters are written
     * @param map a map of paramaters
     */
    public static void writeMapInTxtFile(File file, Map<String, String> map) {
        LOG.debug("Start writeMapInFile");
        String s = "";
        try {
            s = MiscUtils.writeValueAsString(map);
        } catch (JsonProcessingException e1) {
            LOG.error("Error when convert map to string", e1);
            return;
        }
        // Read all the lines of the file
        StringBuilder lines = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), Constant.ANSI_ENCODING))) {
            String line = "";
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                // Ignore the first line if old import params
                if (!StringUtils.startsWith(line, Constant.IMPORT_PARAMS_PREFIX) || !isFirstLine) {
                    lines.append(line + Constant.NEW_LINE);
                }
                isFirstLine = false;
            }
        } catch (IOException e) {
            LOG.error("Error file: {}", file.getName(), e);
            return;
        }
        // Delete the file
        try {
            Files.delete(file.toPath());
        } catch (IOException e1) {
            LOG.error("Error when deleting file: {}", file.getName(), e1);
        }
        // Rewrite the file
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), Constant.ANSI_ENCODING))) {
            // Appends imports params
            writer.append(Constant.IMPORT_PARAMS_PREFIX + s + Constant.NEW_LINE);
            // Then append all the read lines
            writer.append(lines);
        } catch (IOException e) {
            LOG.error("Error file: {}", file.getName(), e);
        }
        LOG.debug("End writeMapInFile");
    }

    /**
     * Retourne la première ligne du fichier donné.
     *
     * @param filePath le chemin absolu du fichier
     * @return la 1ère ligne
     */
    public static Optional<String> readFirstLine(String filePath) {
        LOG.debug("Start readFirstLine");
        String result = null;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(new File(filePath)), Constant.ANSI_ENCODING))) {
            result = br.readLine();
        } catch (IOException e) {
            LOG.error("Erreur lors de la lecture du fichier " + filePath, e);
        }
        LOG.debug("End readFirstLine");
        return Optional.ofNullable(result);
    }

    /**
     * Recovers the creation date of the given file.
     *
     * @param file the file of which the creation date is wanted
     * @return the creation date, now if error
     */
    public static LocalDateTime getCreationDate(File file) {
        LOG.debug("Start getCreationDate");
        BasicFileAttributes attr = null;
        try {
            attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        } catch (IOException e) {
            LOG.error("Impossible de récupérer la date de création de " + file.getAbsolutePath(), e);
        }
        if (attr == null) {
            return LocalDateTime.now();
        }
        LocalDateTime creationDate = LocalDateTime.ofInstant(attr.creationTime().toInstant(), ZoneId.systemDefault());
        LOG.debug("End getCreationDate");
        return creationDate;
    }
}
