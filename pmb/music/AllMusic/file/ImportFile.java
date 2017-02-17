/**
 * 
 */
package pmb.music.AllMusic.file;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import pmb.music.AllMusic.App;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;

/**
 * @author i2113mj
 *
 */
public class ImportFile {

    private static final String ALPHA_NUM = "^[a-zA-Z0-9]*";

    private static final String TWO_DIGITS = "[0-9]{2,2}";
    
    private static final String SIZE = "[0-9]{1,3}";
    
    private static final String CHART = "^[0-9]{1,2}\\.?";

    private static final String YEAR = "((19)" + TWO_DIGITS + "|(20)" + TWO_DIGITS + ")";

    private static final String DECADE = "((?i)decade)|(" + TWO_DIGITS + "'s)|(" + TWO_DIGITS + "s)";

    private static final String ALL_TIME = "((?i)greatest)|((?i)epic)|((?i)all time)|((?i)ever)|((?i)ultimate)|((?i)before you)|((?i)changed)";

    private static final String SONGS = "((?i)single)|((?i)track)|((?i)tune)|((?i)song)";

    private static final String ALBUMS = "((?i)album)";

    private static final String GENRE = "((?i)punk)|((?i)reggae)|((?i)motown)|((?i)soul)|((?i)indie)|((?i)electro)|((?i)hop)|((?i)folk)|((?i)rock)|((?i)wave)|((?i)britpop)|((?i)psych)|((?i)pop)";

    private static final String THEME = "((?i)american)|((?i)british)|((?i)reader)|((?i)guitar)|((?i)love)";

    private static final String YEAR_AT_THE_END = YEAR + ".txt$";

    private static final Pattern PATTERN_DECADE = Pattern.compile(DECADE);

    private static final Pattern PATTERN_ALBUM = Pattern.compile(ALBUMS);

    private static final Pattern PATTERN_SONG = Pattern.compile(SONGS);

    private static final Pattern PATTERN_ALL_TIME = Pattern.compile(ALL_TIME);

    private static final Pattern PATTERN_GENRE = Pattern.compile(GENRE);

    private static final Pattern PATTERN_THEME = Pattern.compile(THEME);

    private static final Pattern PATTERN_YEAR = Pattern.compile(YEAR);

    private static final Pattern PATTERN_YEAR_AT_THE_END = Pattern.compile(YEAR_AT_THE_END);
    
    private static final Pattern PATTERN_CHART = Pattern.compile(CHART);
    
    private static final Pattern PATTERN_ALPHA_NUM = Pattern.compile(ALPHA_NUM);
    
    private static final Pattern PATTERN_SIZE = Pattern.compile(SIZE);
    
    public static Fichier convertOneFile(File file) {
        System.out.println("Start convertOneFile");
        Fichier fichier = new Fichier();
        String name = file.getName();
        fichier.setCreationDate(getCreationDate(file));
        fichier.setFileName(StringUtils.substringBeforeLast(name,"."));
        fichier.setCategorie(determineCategory(name));
        fichier.setAuthor(file.getParentFile().getName());
        determineYears(name, fichier);
        fichier.setSize(determineSize(name));
        if (fichier.getSize() == 0) {
            try {
                fichier.setSize(countLines(file.getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("End convertOneFile");
        return fichier;
    }

    public static List<Composition> getCompositionsFromFile(File file, Fichier fichier, RecordType type, String separator, boolean artistFirst) throws Exception {
        System.out.println("Start getCompositionsFromFile");
        List<Composition> compoList = new ArrayList<Composition>();
        String line = "";
        BufferedReader br = null;
        int i=1;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), App.ANSI_ENCODING));
            while ((line = br.readLine()) != null) {
                if(StringUtils.isBlank(line) || line.length()<5) {
                    continue;
                }
//                System.out.println(line+" " +separator);
                String[] split = line.split(separator);
                
                Composition composition = new Composition();
                if(split.length<2){
                    split = line.split("-");
                }
                if(split.length<2){
                    throw new Exception("Separator " + separator + " is not suitable for line: " + line);
                }
                List<Fichier> files = new ArrayList<>();
                composition.setFiles(files);
                composition.setOeuvre(StringUtils.trim(split[1]));
                String artist = StringUtils.trim(split[0]);
                int rank = 0;
                if(fichier.getSorted()) {
                    String res = StringUtils.substringBefore(artist, ".");
                    if(StringUtils.isNumeric(res)) {
                        rank = Integer.parseInt(res);
                        artist = StringUtils.substringAfter(artist, ".");
                    } else {
                        res = artist.split(" ")[0];
                        rank = Integer.parseInt(res);
                        artist = StringUtils.substringAfterLast(artist, res);
                    }
                } else {
                    rank = i;
                }
                Fichier fich = new Fichier(fichier);
                fich.setClassement(rank);
                files.add(fich);
                composition.setArtist(StringUtils.trim(artist));
                composition.setRecordType(type);
                
                if(!artistFirst) {
                    String artist2 = composition.getArtist();
                    composition.setArtist(composition.getOeuvre());
                    composition.setOeuvre(artist2);
                }
                
                compoList.add(composition);
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("End getCompositionsFromFile");
        return compoList;
    }

    /**
     * @param name
     * @param fichier
     */
    private static int determineSize(String name) {
        int res = 0;
        Matcher mSize = PATTERN_SIZE.matcher(name);
        Matcher mDeca = PATTERN_DECADE.matcher(name);
        Matcher yDeca = PATTERN_YEAR.matcher(name);
        String deca = "";
        if (mDeca.find()) {
            deca = mDeca.group();
        }
        String y = "";
        if (yDeca.find()) {
            y = yDeca.group();
        }
        if (mSize.find()) {
            String size = mSize.group();
            if (!deca.contains(size) && !y.contains(size)) {
                res = Integer.parseInt(size.trim());
            }
        }
        return res;
    }

    public static RecordType determineType(String name) {
        RecordType res = null;
        if (PATTERN_SONG.matcher(name).find()) {
            res = RecordType.SONG;
        } else if (PATTERN_ALBUM.matcher(name).find()) {
            res = RecordType.ALBUM;
        } else {
            res = RecordType.UNKNOWN;
        }
        return res;
    }

    private static Cat determineCategory(String name) {
        Cat res = null;
        if (PATTERN_DECADE.matcher(name).find()) {
            res = Cat.DECADE;
        } else if (name.matches(YEAR)) {
            res = Cat.YEAR;
        } else if (PATTERN_THEME.matcher(name).find()) {
            res = Cat.THEME;
        } else if (PATTERN_GENRE.matcher(name).find()) {
            res = Cat.GENRE;
        } else if (PATTERN_ALL_TIME.matcher(name).find()) {
            res = Cat.ALL_TIME;
        } else {
            res = Cat.MISCELLANEOUS;
        }
        return res;
    }

    private static void determineYears(String name, Fichier file) {
        String[] split = strip(name);

        if (PATTERN_YEAR_AT_THE_END.matcher(name).find()) {
            file.setPublishYear(Integer.parseInt(split[split.length - 2]));
        }

        List<String> date = matchPart(split, YEAR);
        if (file.getCategorie() == Cat.DECADE) {
            List<String> decadeMatch = matchPart(split, TWO_DIGITS);
            if (decadeMatch.size() == 1) {
                int begin = convertTwoDigitsToYear(decadeMatch);
                file.setRangeDateBegin(begin);
                file.setRangeDateEnd(begin + 9);
            } else {
                String res = "";
                if (date.size() > 1) {
                    for (String str : date) {
                        if (!str.matches(YEAR_AT_THE_END)) {
                            res = str;
                            break;
                        }
                    }
                } else if (date.size() == 1) {
                    res = date.get(0);
                }
                file.setRangeDateBegin(Integer.parseInt(res));
                file.setRangeDateEnd(Integer.parseInt(res) + 9);
            }
        } else if (file.getCategorie() == Cat.YEAR) {
            file.setRangeDateBegin(Integer.parseInt(split[0]));
            file.setRangeDateEnd(Integer.parseInt(split[0]));
        } else if (file.getCategorie() == Cat.ALL_TIME) {
            if (date.size() == 2) {
                file.setRangeDateBegin(Integer.parseInt(date.get(0)));
                file.setRangeDateEnd(Integer.parseInt(date.get(1)));
            } else {
                file.setRangeDateBegin(0);
                file.setRangeDateEnd(0);
            }
        } else {
//            Calendar cal = new GregorianCalendar();
            // file.setRangeDateBegin(cal.get(Calendar.YEAR));
            // file.setRangeDateEnd(cal.get(Calendar.YEAR));
            file.setRangeDateBegin(0);
            file.setRangeDateEnd(0);
        }
        if (file.getPublishYear() == 0
                && (file.getCategorie() == Cat.YEAR || file.getCategorie() == Cat.DECADE || (file.getCategorie() == Cat.ALL_TIME && file.getRangeDateEnd() != 0))) {
            file.setPublishYear(file.getRangeDateEnd());
        }
    }

    private static String[] strip(String name) {
        String[] split = name.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        return split;
    }

    private static int convertTwoDigitsToYear(List<String> decadeMatch) {
        String substring = decadeMatch.get(0).substring(0, 2);
        if (substring.contains("00") || substring.contains("10") || substring.contains("20")) {
            substring = "20" + substring;
        } else {
            substring = "19" + substring;
        }
        int begin = Integer.parseInt(substring);
        return begin;
    }

    private static List<String> matchPart(String[] split, String regex) {
        List<String> res = new ArrayList<>();
        if (regex == TWO_DIGITS) {
            for (int i = 0; i < split.length; i++) {
                String str = split[i].trim();
                if (str.matches(regex) && (split[i + 1].startsWith("s") || split[i + 1].startsWith("'"))) {
                    res.add(str);
                }
            }
        } else {
            for (int i = 0; i < split.length; i++) {
                String str = split[i].trim();
                if (str.matches(regex)) {
                    res.add(str);
                }
            }
        }
        return res;
    }

    private static Date getCreationDate(File file) {
        BasicFileAttributes attr = null;
        try {
            attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Date creationDate = null;
        long milliseconds = attr.creationTime().to(TimeUnit.MILLISECONDS);
        if ((milliseconds > Long.MIN_VALUE) && (milliseconds < Long.MAX_VALUE)) {
            creationDate = new Date(milliseconds);
        }
        return creationDate;
    }

    public static boolean isSorted(String line) {
        if(PATTERN_CHART.matcher(line).find()){
            return true;
        } else {
            return false;
        }
    }

    public static String getSeparator(String line) {
        String[] split = line.split(" ");
        String res = "-";
        for (int i = 0; i < split.length; i++) {
            String string = split[i].trim();
            if(!StringUtils.isAlphanumeric(string)) {
                res = string;
            }
        }
        return res;
    }

    public static List<String> randomLineAndLastLines(File file) {
        System.out.println("Start randomLineAndLastLines");
        List<String> lines = new ArrayList<>();
        String line = "";
        BufferedReader br = null;
        try {
            int countLines = countLines(file.getAbsolutePath());
            int rand = ThreadLocalRandom.current().nextInt(1, countLines);
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), App.ANSI_ENCODING));
            for (int i = 0; i < rand; i++) {
                line = br.readLine();
            }
            int count = rand;
            while(StringUtils.startsWith(line, "#") || StringUtils.isBlank(line) && line.length() < 5) {
                line = br.readLine();
                count++;
            }
            lines.add(line);
            while(count<countLines-1) {
                br.readLine();
                count++;
            }
            lines.add(br.readLine());
            lines.add(br.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("End randomLineAndLastLines");
        return lines;
    }

    public static int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }

}
