package pmb.allmusic.utils;

import java.util.List;

import pmb.allmusic.view.panel.BatchPanel;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyFileUtils;
import pmb.my.starter.utils.VariousUtils;

/**
 * Utility class that contains all processes for the {@link BatchPanel}.
 *
 * @author PBR
 *
 */
public class BatchUtils {
    protected static final String CSV_HEADER_ARTIST = "Artiste";
    protected static final String CSV_HEADER_TITLE = "Titre";
    protected static final String CSV_HEADER_RANK = "Classement";
    protected static final String CSV_HEADER_DELETED = "Supprimé";
    protected static final String CSV_HEADER_ANNEE = "Année";
    protected static final String CSV_HEADER_YEAR = "Year: ";
    protected static final String CSV_HEADER_TYPE = "Type";
    protected static final String CSV_HEADER_CAT = "Cat";
    protected static final String CSV_HEADER_SCORE = "Score";
    protected static final String CSV_HEADER_DECILE = "Décile";
    protected static final String CSV_HEADER_FILE_SIZE = "Nombre de fichiers";
    protected static final String CSV_HEADER_ALBUMS = "Albums";
    protected static final String CSV_HEADER_SONG = "Chansons";
    protected static final String CSV_HEADER_PERCENT_DELETED = "% De Supprimés";
    protected static final String CSV_HEADER_SCORE_DELETED = "Score Supprimés";
    protected static final String CSV_HEADER_SCORE_SONG = "Score Chanson";
    protected static final String CSV_HEADER_SCORE_ALBUM = "Score Album";
    protected static final String CSV_HEADER_SCORE_TOTAL = "Score Total";
    protected static final String CSV_HEADER_OCCURENCY = "Nombre d'Occurrences";
    protected static final String CSV_HEADER_AVERAGE = "Average";
    protected static final String CSV_HEADER_AUTHOR = "Author";
    protected static final String CSV_HEADER_FICHIER = "Fichier";

    protected BatchUtils() {
        throw new AssertionError("Must not be used");
    }

    protected static String writeInFile(StringBuilder sb, String fileName) {
        String filePath = Constant.getOutputDir() + MyConstant.FS + fileName;
        MyFileUtils.writeFile(filePath, List.of(sb.toString()));
        return filePath;
    }

    protected static void addLine(StringBuilder sb, String text, boolean displayTime) {
        sb.append(displayTime ? VariousUtils.getCurrentTime() : "").append(displayTime ? ": " : "").append(text)
        .append(MyConstant.NEW_LINE);
    }
}
