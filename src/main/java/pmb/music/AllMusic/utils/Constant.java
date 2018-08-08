/**
 * 
 */
package pmb.music.AllMusic.utils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.FileUtils;
import org.kordamp.ikonli.fontawesome.FontAwesome;

/**
 * Classe de constances.
 * 
 */
public class Constant {

	/**
	 * Chemin du dossier ressources.
	 */
	public static final String RESOURCES_DIRECTORY = FileUtils.FS + "src" + FileUtils.FS + "main" + FileUtils.FS
			+ "resources" + FileUtils.FS;

	/**
	 * Chemin du pgm.
	 */
	public static final String USER_DIR = System.getProperty("user.dir");

	/**
	 * Chemin absolu du dossier ressources.
	 */
	public static final String RESOURCES_ABS_DIRECTORY = USER_DIR + RESOURCES_DIRECTORY;

	/**
	 * Chemin abs du fichier de log.
	 */
	public static final String FILE_LOG_PATH = Constant.USER_DIR + FileUtils.FS + "error.log";

	/**
	 * Chemin abs du fichier de configuration.
	 */
	public static final String CONFIG_PATH = RESOURCES_ABS_DIRECTORY + FileUtils.FS + "config.properties";

	/**
	 * @return Nom du fichier final.
	 */
	public static String getFinalFile() {
		return GetProperties.getProperty("final");
	}

	/**
	 * Chemin abs du fichier aggrégeant tous les fichiers.
	 */
	public static String getFinalFilePath() {
		return getXmlPath() + getFinalFile();
	}

	/**
	 * @return Chemin abs du dossier d'historique.
	 */
	public static String getHistoryPath() {
		return RESOURCES_ABS_DIRECTORY + GetProperties.getProperty("history") + FileUtils.FS;
	}

	/**
	 * @return Chemin abs du dossier contenant les fichiers txt des classements.
	 */
	public static String getMusicAbsDirectory() {
		return RESOURCES_ABS_DIRECTORY + GetProperties.getProperty("music") + FileUtils.FS;
	}

	/**
	 * @return Chemin abs du dossier contenant tous les fichiers xml importés.
	 */
	public static String getXmlPath() {
		return RESOURCES_ABS_DIRECTORY + GetProperties.getProperty("xml") + FileUtils.FS;
	}

	/**
	 * @return chemin de Notepad++.
	 */
	public static String getNotepadPath() {
		return GetProperties.getProperty("notepad") + " ";
	}

	/**
	 * @return chemin d'Excel.
	 */
	public static String getExcelPath() {
		return GetProperties.getProperty("excel") + " ";
	}

	/**
	 * Nom du fichier de résultat des batchs par défaut.
	 */
	public static final String BATCH_FILE = "batch.txt";

	public static final String NEW_LINE = "\r\n";

	public static final String ANSI_ENCODING = "Cp1252";

	public static final String UTF8_ENCODING = "UTF-8";

	public static final String SEPARATOR_DATE_HISTORY = " ; ";

	public static final String REGULAR_DASH = "-";

	public static final String DOT = ".";

	public static final String COMMENT_PREFIX = "#";

	public static final String IMPORT_PARAMS_PREFIX = "@";

	public static final String XML_EXTENSION = ".xml";

	public static final String CSV_EXTENSION = ".csv";

	public static final String TXT_EXTENSION = ".txt";

	private static final String[] SEPARATORS = { "-", "-", "‒", "–", "—", "―", "-", " - ", " - ", " – ", " — ", " - " };

	private static final String[] NOT_SEPARATORS = { "(", ")", DOT, ",", "#", "'", "!", "*", "?", "/" };

	public static final String DEFAULT_TITLE = "AllMusic";

	private final SimpleDateFormat sdfDttm = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	private final SimpleDateFormat sdfHistory = new SimpleDateFormat("dd-MM-yyyy HH-mm");

	public static final String ONGLET_IMPORT = "Import";

	public static final String ONGLET_ARTIST = "Artiste";

	public static final String ONGLET_SEARCH = "Rechercher";

	public static final String ONGLET_FICHIER = "Fichiers";

	public static final String ONGLET_BATCH = "Batchs";

	public static final String ALBUM_FOLDER = "Album";

	public static final String YEAR_FOLDER = "Year";

	public static final String SONG_FOLDER = "Song";

	public static final String ALPHA_NUM = "^[a-zA-Z0-9]*";

	public static final String TWO_DIGITS = "[0-9]{2,2}";

	public static final String SIZE = "[0-9]{1,3}";

	public static final String CHART = "^[0-9]{1,2}\\.?";

	public static final String YEAR = "((19)" + TWO_DIGITS + "|(20)" + TWO_DIGITS + ")";

	public static final String DECADE = "((?i)decade)|(" + TWO_DIGITS + "'s)|(" + TWO_DIGITS + "s)";

	public static final String ALL_TIME = "((?i)greatest)|((?i)epic)|((?i)all time)|((?i)ever)|((?i)ultimate)|((?i)before you)|((?i)changed)";

	public static final String SONGS = "((?i)single)|((?i)track)|((?i)tune)|((?i)song)";

	public static final String ALBUMS = "((?i)album)";

	public static final String GENRE = "((?i)punk)|((?i)reggae)|((?i)motown)|((?i)soul)|((?i)indie)|((?i)electro)|((?i)hop)|((?i)folk)|((?i)rock)|((?i)wave)|((?i)britpop)|((?i)psych)|((?i)pop)";

	public static final String THEME = "((?i)american)|((?i)british)|((?i)reader)|((?i)guitar)|((?i)love)";

	public static final String YEAR_AT_THE_END = YEAR + ".txt$";

	public static final Pattern PATTERN_DECADE = Pattern.compile(DECADE);

	public static final Pattern PATTERN_ALBUM = Pattern.compile(ALBUMS);

	public static final Pattern PATTERN_SONG = Pattern.compile(SONGS);

	public static final Pattern PATTERN_ALL_TIME = Pattern.compile(ALL_TIME);

	public static final Pattern PATTERN_GENRE = Pattern.compile(GENRE);

	public static final Pattern PATTERN_THEME = Pattern.compile(THEME);

	public static final Pattern PATTERN_YEAR = Pattern.compile(YEAR);

	public static final Pattern PATTERN_YEAR_AT_THE_END = Pattern.compile(YEAR_AT_THE_END);

	public static final Pattern PATTERN_CHART = Pattern.compile(CHART);

	public static final Pattern PATTERN_ALPHA_NUM = Pattern.compile(ALPHA_NUM);

	public static final Pattern PATTERN_SIZE = Pattern.compile(SIZE);

	public static final Pattern PATTERN_PUNCTUATION = Pattern.compile("\\p{Punct}|\\s");

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

	public Constant() {
		// Nothing to do
	}

	public SimpleDateFormat getSdfDttm() {
		return sdfDttm;
	}

	public SimpleDateFormat getSdfHistory() {
		return sdfHistory;
	}

	public static String[] getSeparators() {
		return SEPARATORS;
	}

	public static String[] getNotSeparators() {
		return NOT_SEPARATORS;
	}
}
