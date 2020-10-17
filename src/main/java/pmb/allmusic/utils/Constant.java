/**
 *
 */
package pmb.allmusic.utils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import org.kordamp.ikonli.fontawesome.FontAwesome;

import pmb.my.starter.exception.MinorException;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyProperties;

/**
 * Constants class.
 *
 * @author pmbroca
 */
public class Constant {

    public static final String MODIF_FILE_PATH = MyConstant.RESOURCES_DIRECTORY + "modif.txt";

    /**
     * Path of the file where compositions with a slash ignored are stored.
     */
    public static final String SLASH_FILE_PATH = getOutputDir() + "slash.txt";

    /**
     * Nom du fichier de résultat des batchs par défaut.
     */
    public static final String BATCH_FILE = "batch.txt";

    public static final String ACCESS_TOKEN = "G-_ZeiEAvB0AAAAAAAANQd4IMHRr7Y9aTvAiivg-8LImbDKmo9pdu95_SIioW3lR";

    public static final String COMMENT_PREFIX = "#";

    public static final String IMPORT_PARAMS_PREFIX = "@";

    public static final String PACKAGE_NAME = "pmb.music";

    private static final String[] NOT_SEPARATORS = { "(", ")", MyConstant.DOT, ",", "#", "'", "!", "*", "?", "/" };

    public static final String DEFAULT_TITLE = "AllMusic";

    private final DateTimeFormatter fullDTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final SimpleDateFormat sdfDt = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");

    private final DateTimeFormatter dateDTF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static final String ONGLET_IMPORT = "Import";

    public static final String ONGLET_ARTIST = "Artiste";

    public static final String ONGLET_SEARCH = "Rechercher";

    public static final String ONGLET_FICHIER = "Fichiers";

    public static final String ONGLET_BATCH_GENERATE = "Batch Generate";

    public static final String ONGLET_BATCH_EDIT = "Batch Edit";

    public static final String ONGLET_BATCH_CHECK = "Batch Check";

    public static final String ALBUM_FOLDER = "Album";

    public static final String YEAR_FOLDER = "Year";

    public static final String SONG_FOLDER = "Song";

    public static final String ARTIST_PANEL_RESULT_FILE = Constant.getOutputDir() + "artist.json";

    public static final String SEPARATOR_AND = " and ";

    public static final String FILE_NAME_SEPARATOR = " - ";

    public static final String VARIOUS_AUTHOR = "Divers";

    public static final String TRACK_NUMBER_SEPARATOR = " sur ";

    public static final String TWO_DIGITS = "[0-9]{2,2}";

    public static final String YEAR = "((19)" + TWO_DIGITS + "|(20)" + TWO_DIGITS + ")";

    public static final String YEAR_AT_THE_END = YEAR + ".txt$";

    public static final Pattern PATTERN_DECADE = Pattern
            .compile("((?i)decade)|(" + TWO_DIGITS + "'s)|(" + TWO_DIGITS + "s)");

    public static final Pattern PATTERN_ALBUM = Pattern.compile("((?i)album)");

    public static final Pattern PATTERN_SONG = Pattern.compile("((?i)single)|((?i)track)|((?i)tune)|((?i)song)");

    public static final Pattern PATTERN_ALL_TIME = Pattern.compile(
            "((?i)greatest)|((?i)epic)|((?i)all time)|((?i)ever)|((?i)ultimate)|((?i)before you)|((?i)changed)");

    public static final Pattern PATTERN_GENRE = Pattern.compile(
            "((?i)punk)|((?i)reggae)|((?i)motown)|((?i)soul)|((?i)indie)|((?i)electro)|((?i)hop)|((?i)folk)|((?i)\brock)|((?i)wave)|((?i)britpop)|((?i)psych)|((?i)pop)");

    public static final Pattern PATTERN_THEME = Pattern
            .compile("((?i)american)|((?i)british)|((?i)reader)|((?i)guitar)|((?i)love)");

    public static final Pattern PATTERN_YEAR = Pattern.compile(YEAR);

    public static final Pattern PATTERN_YEAR_AT_THE_END = Pattern.compile(YEAR_AT_THE_END);

    public static final Pattern PATTERN_CHART = Pattern.compile("^[0-9]{1,2}\\.?");

    public static final Pattern PATTERN_SIZE = Pattern.compile("[0-9]{1,3}");

    public static final Pattern PATTERN_PARENTHESES = Pattern.compile("\\(.*\\)|\\[.*\\]");

    public static final BigDecimal SCORE_LIMIT_TITLE_FUSION = BigDecimal.valueOf(0.94D);

    public static final BigDecimal SCORE_LIMIT_ARTIST_FUSION = BigDecimal.valueOf(0.95D);

    public static final BigDecimal SCORE_LIMIT_SEARCH = BigDecimal.valueOf(0.92D);

    // ICONS //
    public static final FontAwesome ICON_SEARCH = FontAwesome.SEARCH;
    public static final FontAwesome ICON_ERASE = FontAwesome.ERASER;
    public static final FontAwesome ICON_DELETE = FontAwesome.TRASH;
    public static final FontAwesome ICON_EDIT = FontAwesome.PENCIL;
    public static final FontAwesome ICON_HIDE = FontAwesome.MINUS_SQUARE;
    public static final FontAwesome ICON_SHOW = FontAwesome.PLUS_SQUARE;
    public static final FontAwesome ICON_FOLDER = FontAwesome.FOLDER_OPEN_O;
    public static final FontAwesome ICON_FILE = FontAwesome.FILE_O;
    public static final FontAwesome ICON_TXT_FILE = FontAwesome.FILE_TEXT_O;
    public static final FontAwesome ICON_XML_FILE = FontAwesome.FILE_CODE_O;
    public static final FontAwesome ICON_REFRESH = FontAwesome.REFRESH;
    public static final FontAwesome ICON_UPLOAD = FontAwesome.UPLOAD;
    public static final FontAwesome ICON_DOWNLOAD = FontAwesome.DOWNLOAD;
    public static final FontAwesome ICON_CLEAN = FontAwesome.PAINT_BRUSH;
    public static final FontAwesome ICON_ALIGN = FontAwesome.ALIGN_LEFT;
    public static final FontAwesome ICON_FUSION = FontAwesome.MAGIC;
    public static final FontAwesome ICON_GO = FontAwesome.PLAY;
    public static final FontAwesome ICON_CHECK = FontAwesome.CHECK;
    public static final FontAwesome ICON_UNCHECK = FontAwesome.SQUARE_O;

    public Constant() {
        // Nothing to do
    }

    /**
     * Chemin abs du fichier de configuration.
     */
    public static String getConfigPath() {
        return MyConstant.RESOURCES_DIRECTORY + "config.properties";
    }

    /**
     * @return Le chemin où seront générés les fichiers de l'application.
     */
    public static String getOutputDir() {
        return MyProperties.get("output").map(output -> output + MyConstant.FS).orElse(MyConstant.USER_DIRECTORY);
    }

    /**
     * @return Le chemin absolu du dossier des ressources utilisées par l'appli.
     */
    public static String getResourcesDir() {
        return MyProperties.get("resources").map(resources -> resources + MyConstant.FS).orElse(MyConstant.RESOURCES_DIRECTORY);
    }

    /**
     * @return Nom du fichier final.
     */
    public static String getFinalFile() {
        return MyProperties.get("final").orElseThrow(() -> new MinorException("Final file undefined"));
    }

    /**
     * Chemin abs du fichier aggrégeant tous les fichiers.
     */
    public static String getFinalFilePath() {
        return getXmlPath() + getFinalFile();
    }

    /**
     * @return Chemin abs du dossier contenant les fichiers txt des classements.
     */
    public static String getMusicAbsDirectory() {
        return MyProperties.get("music").map(music -> getResourcesDir() + music + MyConstant.FS)
                .orElseThrow(() -> new MinorException("Music Directory undefined"));
    }

    /**
     * @return Chemin abs du dossier contenant tous les fichiers xml importés.
     */
    public static String getXmlPath() {
        return MyProperties.get("xml").map(xml -> getResourcesDir() + xml + MyConstant.FS)
                .orElseThrow(() -> new MinorException("XML Directory undefined"));
    }

    /**
     * @return chemin de Notepad++.
     */
    public static String getNotepadPath() {
        return MyProperties.get("notepad").map(notepad -> MyConstant.QUOTE + notepad + MyConstant.QUOTE + " -alwaysOnTop ")
                .orElseThrow(() -> new MinorException("Notepad path undefined"));
    }

    /**
     * @return chemin d'Excel.
     */
    public static String getExcelPath() {
        return MyProperties.get("excel").map(excel -> excel + " ")
                .orElseThrow(() -> new MinorException("Excel path undefined"));
    }

    public DateTimeFormatter getFullDTF() {
        return fullDTF;
    }

    public SimpleDateFormat getSdfDt() {
        return sdfDt;
    }

    /**
     * "dd/MM/yyyy"
     *
     * @return a formatter
     */
    public DateTimeFormatter getDateDTF() {
        return dateDTF;
    }

    public static String[] getNotSeparators() {
        return NOT_SEPARATORS;
    }

    public static char getCsvSeparator() {
        return MyProperties.get("csv_separator").map(s -> s.charAt(0)).orElse(';');
    }
}
