/**
 * 
 */
package pmb.music.AllMusic.utils;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/**
 * @author i2113mj
 *
 */
public class Constant {

    public static final String RESOURCES_DIRECTORY = "\\src\\main\\resources\\";
    
    public static final String RESOURCES_ABS_DIRECTORY = System.getProperty("user.dir") + RESOURCES_DIRECTORY;

    public static final String HISTORY_PATH = RESOURCES_ABS_DIRECTORY + "history";

    public static final String FINAL_FILE_PATH = System.getProperty("user.dir") + Constant.RESOURCES_DIRECTORY + "final.xml";

    public static final String MUSIC_ABS_DIRECTORY = System.getProperty("user.dir") + Constant.RESOURCES_DIRECTORY + "Music\\";
    
    public static final String NOTEPAD_EXE = "C:\\Program Files (x86)\\Notepad++\\notepad++.exe ";

    public static final String ANSI_ENCODING = "Cp1252";
    
    public static final String UTF8_ENCODING = "UTF-8";

    public static final String REGULAR_DASH = "-";

    public static final String DOT = ". ";

    public static final String[] SEPARATORS = { "-", "-", "‒", "–", "—", "―", "-", " - ", " - ", " – ", " — ", " - " };

    public static final String DEFAULT_TITLE = "Ma Fenetre";

    public static final SimpleDateFormat SDF_DTTM = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

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
}
