package pmb.music.AllMusic.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import pmb.music.AllMusic.exception.MajorException;
import pmb.music.AllMusic.exception.MinorException;

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
     * Recovers the list of files contained in a folder.
     *
     * @param folder directory containing files
     * @param extension extension of files to research
     * @param recursive if the search is recursive or not
     * @return a list of files
     */
    public static List<File> listFilesInFolder(final File folder, String extension, boolean recursive) {
        List<File> result = new ArrayList<>();
        listFilesForFolder(folder, result, extension, recursive);
        return result;
    }

    private static void listFilesForFolder(final File folder, List<File> files, String extension, boolean recursive) {
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
        UnaryOperator<String> buildPath = s -> pathRoot + s + FileUtils.FS + nameWithExtension;

        return Stream
                .concat(Stream.of(pathShort), Stream
                        .of(Constant.SONG_FOLDER, Constant.ALBUM_FOLDER, Constant.YEAR_FOLDER).map(buildPath::apply))
                .filter(FileUtils::fileExists).findFirst().or(() -> {
                    LOG.warn("End buildTxtFilePath, no path built for: {} - {}", fileName, auteur);
                    return Optional.empty();
                });
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
        writeFile(filePath, List.of(json));
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
            throw new MinorException("Error when convert map to string", e1);
        }
        // Read all the lines of the file
        // Ignoring first line if old import params
        AtomicBoolean isFirstLine = new AtomicBoolean(true);
        String lines = FilesUtils.readFile(file).stream().filter(line -> {
            if (StringUtils.startsWith(line, Constant.IMPORT_PARAMS_PREFIX) && isFirstLine.get()) {
                isFirstLine.set(false);
                return false;
            } else {
                return true;
            }
        }).collect(Collectors.joining(Constant.NEW_LINE));
        // Delete the file
        try {
            Files.delete(file.toPath());
        } catch (IOException e1) {
            LOG.error("Error when deleting file: {}", file.getName(), e1);
        }
        // Rewrite the file by appending imports params and all the read lines
        writeFile(file, List.of(Constant.IMPORT_PARAMS_PREFIX + s, lines));
        LOG.debug("End writeMapInFile");
    }

    /**
     * Retourne la première ligne du fichier donné.
     *
     * @param filePath le chemin absolu du fichier
     * @return la 1ère ligne
     */
    public static String readFirstLine(String filePath) {
        return FilesUtils.readFile(filePath).stream().limit(1).reduce("", (a, b) -> b);
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
            LOG.error("Impossible de récupérer la date de création de {}", file.getAbsolutePath(), e);
        }
        if (attr == null) {
            return LocalDateTime.now();
        }
        LocalDateTime creationDate = LocalDateTime.ofInstant(attr.creationTime().toInstant(), ZoneId.systemDefault());
        LOG.debug("End getCreationDate");
        return creationDate;
    }

    /**
     * Zip the given file.
     *
     * @param file to zip
     * @return {@link ByteArrayOutputStream} the files zipped
     * @throws MajorException if something went wrong
     */
    public static File zipFile(File file) throws MajorException {
        LOG.debug("Start zipFiles");
        String zipName = file.getParent() + FileUtils.FS + StringUtils.substringBeforeLast(file.getName(), ".")
        + ".zip";
        try (FileOutputStream fos = new FileOutputStream(zipName);
                ZipOutputStream zipOut = new ZipOutputStream(fos);
                FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        } catch (IOException e) {
            throw new MajorException("Exception thrown when zipping file: " + file.getName(), e);
        }
        LOG.debug("End zipFiles");
        return new File(zipName);
    }

    /**
     * Reads completely the given file.
     * @param file to read
     * @param charsetName encoding
     * @return a list of String
     */
    public static List<String> readFile(File file, String charsetName) {
        try (Stream<String> lines = Files.lines(file.toPath(), Charset.forName(charsetName))) {
            return lines.collect(Collectors.toList());
        } catch (IOException e) {
            throw new MinorException("Error when reading file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Reads completely the given file with {@code ANSI} default encoding.
     * @param file path of the file to read
     * @return a list of String
     */
    public static List<String> readFile(String file) {
        return readFile(new File(file), Constant.ANSI_ENCODING);
    }

    /**
     * Reads completely the given file with {@code ANSI} default encoding.
     * @param file to read
     * @return a list of String
     */
    public static List<String> readFile(File file) {
        return readFile(file, Constant.ANSI_ENCODING);
    }

    /**
     * Writes in given file the given content.
     * @param file to write into
     * @param lines content to write
     * @param charsetName encoding
     */
    public static void writeFile(File file, List<String> lines, String charsetName) {
        try {
            Files.write(file.toPath(), lines, Charset.forName(charsetName));
        } catch (IOException e) {
            throw new MinorException("Error when writing in file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Writes in given file the given content with {@code ANSI} default encoding.
     * @param file path of the file to write into
     * @param lines content to write
     */
    public static void writeFile(String file, List<String> lines) {
        writeFile(new File(file), lines, Constant.ANSI_ENCODING);
    }

    /**
     * Writes in given file the given content with {@code ANSI} default encoding.
     * @param file to write into
     * @param lines content to write
     */
    public static void writeFile(File file, List<String> lines) {
        writeFile(file, lines, Constant.ANSI_ENCODING);
    }
}
