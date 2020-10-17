package pmb.allmusic.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

import pmb.my.starter.exception.MajorException;
import pmb.my.starter.exception.MinorException;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.VariousUtils;

/**
 * Utility class for handling files.
 */
public final class FilesUtils {

    private static final Logger LOG = LogManager.getLogger(FilesUtils.class);

    private FilesUtils() {
        throw new AssertionError("Must not be used");
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
            if (new File(filePath).exists()) {
                String lineNb = "";
                // calculates the specific line number
                if (lineNumber != null && StringUtils.endsWith(filePath, MyConstant.TXT_EXTENSION)) {
                    // offset with import settings
                    lineNb = "-n" + (lineNumber + 1) + " ";
                } else if (lineNumber != null && StringUtils.endsWith(filePath, MyConstant.XML_EXTENSION)) {
                    // offset with root tags and each compo is 3 lines long
                    lineNb = "-n" + ((lineNumber - 1) * 3 + 4) + " ";
                }
                try {
                    Runtime.getRuntime()
                    .exec(Constant.getNotepadPath() + lineNb + MyConstant.QUOTE + filePath + MyConstant.QUOTE);
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
            if (new File(filePath).exists()) {
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
        String pathRoot = Constant.getMusicAbsDirectory() + auteur + MyConstant.FS;
        String nameWithExtension = fileName + MyConstant.TXT_EXTENSION;
        String pathShort = pathRoot + nameWithExtension;
        UnaryOperator<String> buildPath = s -> pathRoot + s + MyConstant.FS + nameWithExtension;

        return Stream
                .concat(Stream.of(pathShort), Stream
                        .of(Constant.SONG_FOLDER, Constant.ALBUM_FOLDER, Constant.YEAR_FOLDER).map(buildPath::apply))
                .filter(p -> new File(p).exists()).findFirst().or(() -> {
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
        path += StringUtils.endsWithIgnoreCase(fileName, MyConstant.XML_EXTENSION) ? "" : MyConstant.XML_EXTENSION;
        if (!new File(path).exists()) {
            LOG.warn("End buildXmlFilePath, no path built for: {}", fileName);
            LOG.warn("Path tested: {}", path);
            return Optional.empty();
        }
        LOG.debug("End buildXmlFilePath");
        return Optional.of(path);
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
            s = VariousUtils.writeValueAsString(map);
        } catch (JsonProcessingException e1) {
            throw new MinorException("Error when convert map to string", e1);
        }
        // Read all the lines of the file
        // Ignoring first line if old import params
        AtomicBoolean isFirstLine = new AtomicBoolean(true);
        String lines = pmb.my.starter.utils.MyFileUtils.readFile(file).stream().filter(line -> {
            if (StringUtils.startsWith(line, Constant.IMPORT_PARAMS_PREFIX) && isFirstLine.get()) {
                isFirstLine.set(false);
                return false;
            } else {
                return true;
            }
        }).collect(Collectors.joining(MyConstant.NEW_LINE));
        // Delete the file
        try {
            Files.delete(file.toPath());
        } catch (IOException e1) {
            LOG.error("Error when deleting file: {}", file.getName(), e1);
        }
        // Rewrite the file by appending imports params and all the read lines
        pmb.my.starter.utils.MyFileUtils.writeFile(file, List.of(Constant.IMPORT_PARAMS_PREFIX + s, lines));
        LOG.debug("End writeMapInFile");
    }
}
