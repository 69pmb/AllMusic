/**
 *
 */
package pmb.music.AllMusic.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.exception.MajorException;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FilesUtils;
import pmb.music.AllMusic.utils.MiscUtils;

/**
 * Classe permettant l'import de fichiers.
 *
 */
public final class ImportFile {

    private static final String LOG_NUMBER = " number: ";

    private static final Logger LOG = LogManager.getLogger(ImportFile.class);

    private ImportFile() {
        throw new AssertionError("Must not be used");
    }

    /**
     * Convertit un fichier {@link File} en {@link Fichier}.
     *
     * @param file le fichier à convertir
     * @return le fichier convertit
     */
    public static Fichier convertOneFile(File file) {
        LOG.debug("Start convertOneFile");
        Fichier fichier = new Fichier();
        String name = file.getName();
        fichier.setCreationDate(FilesUtils.getCreationDate(file));
        fichier.setFileName(StringUtils.substringBeforeLast(name, Constant.DOT));
        fichier.setCategorie(determineCategory(name));
        String auteur = file.getParentFile().getName();
        if (StringUtils.equalsAnyIgnoreCase(Constant.ALBUM_FOLDER, auteur)
                || StringUtils.equalsAnyIgnoreCase(Constant.SONG_FOLDER, auteur)
                || StringUtils.equalsAnyIgnoreCase(Constant.YEAR_FOLDER, auteur)) {
            LOG.debug("Pas d'auteur, on prend le nom du dossier");
            auteur = file.getParentFile().getParentFile().getName();
        }
        fichier.setAuthor(auteur);
        determineYears(name, fichier);
        LOG.debug("End convertOneFile");
        return fichier;
    }

    /**
     * Récupère les compositions d'un fichier txt.
     *
     * @param file {@link File} contenant les compos
     * @param fichier {@link Fichier} contenant des infos récupérées précédemment
     * @param type {@link RecordType} chanson ou album
     * @param separator {@link String} le séparateur entre l'artiste et le titre
     * @param result {@code List<String>} le message qui sera affiché à la fin du traitement
     * @param artistFirst si l'artiste est en 1er
     * @param reverseArtist si l'artiste est par exemple: {@literal Beatles, The} et doit etre retourné
     * @param parenthese si des parenthèse sont à supprimer à la fin de chaque ligne
     * @param upper si il n'y a pas de séparateur mais que l'artiste est en majuscule et pas le titre
     * @param removeAfter si plusieurs séparateurs, supprimer après le dernier
     * @return {@code List<Composition>} la liste de compos extraite du fichier
     * @throws MajorException if a line can't be parsed
     */
    public static List<Composition> getCompositionsFromFile(File file, Fichier fichier, RecordType type,
            String separator, List<String> result, boolean artistFirst, boolean reverseArtist, boolean parenthese,
            boolean upper, boolean removeAfter) throws MajorException {
        LOG.debug("Start getCompositionsFromFile");
        List<Composition> compoList = new ArrayList<>();
        String line = "";
        Integer i = 1;
        Integer lineNb = 1;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), Constant.ANSI_ENCODING))) {
            while ((line = br.readLine()) != null) {
                lineNb++;
                if (!isValidLine(line)) {
                    continue;
                }
                getCompositionFromOneLine(compoList, fichier, line, separator, result, type, artistFirst, removeAfter,
                        upper, reverseArtist, parenthese, lineNb, i);
                i++;
            }
        } catch (NumberFormatException | IOException e1) {
            throw new MajorException(e1.toString(), e1);
        }
        if (!StringUtils.equalsIgnoreCase(fichier.getAuthor(), Constant.VARIOUS_AUTHOR)
                && (!StringUtils.startsWithIgnoreCase(fichier.getFileName(), fichier.getAuthor() + Constant.FILE_NAME_SEPARATOR)
                        || !StringUtils.endsWithIgnoreCase(fichier.getFileName(), Constant.FILE_NAME_SEPARATOR + String.valueOf(fichier.getPublishYear())))) {
            result.add("### Incorrect file name: " + fichier.getFileName() + ", it must start with author and end with publish year");
        }
        LOG.debug(result);
        LOG.debug("End getCompositionsFromFile");
        return compoList;
    }

    private static void getCompositionFromOneLine(List<Composition> compoList, Fichier fichier, String entryLine,
            String separator, List<String> result, RecordType type, boolean artistFirst, boolean removeAfter,
            boolean upper, boolean reverseArtist, boolean parenthese, Integer lineNb, Integer i) throws MajorException {
        String line = entryLine;
        if (removeAfter) {
            line = StringUtils.substringBeforeLast(line, separator);
        }
        String[] split = null;
        if (!upper) {
            split = splitLineWithSeparator(line, separator, result, lineNb);
        }

        Composition composition = new Composition();
        composition.setCanBeMerged(true);
        Integer rank = setArtistAndTitreGetRank(composition, line, upper, split, fichier.getSorted(), i);
        List<Fichier> files = new ArrayList<>();
        Fichier fich = new Fichier(fichier);
        fich.setClassement(rank);
        files.add(fich);
        composition.setFiles(files);
        composition.setRecordType(type);

        if (!artistFirst) {
            String artist2 = composition.getArtist();
            composition.setArtist(composition.getTitre());
            composition.setTitre(artist2);
        }

        if (reverseArtist) {
            String[] arrayArtist = composition.getArtist().split(",");
            if (arrayArtist.length == 2) {
                composition.setArtist(
                        StringUtils.trim(StringUtils.trim(arrayArtist[1]) + " " + StringUtils.trim(arrayArtist[0])));
            }
        }
        composition.setArtist(WordUtils
                .capitalize(StringUtils.removeEnd(StringUtils.removeStart(composition.getArtist(), "\""), "\"")));
        composition.setTitre(WordUtils
                .capitalize(StringUtils.removeEnd(StringUtils.removeStart(composition.getTitre(), "\""), "\"")));
        if (parenthese) {
            removeParentheseFromTitreAndArtist(result, line, lineNb, composition);
        }

        // Checks if title or artist not empty
        if (StringUtils.isBlank(composition.getArtist())) {
            result.add("### Error Artist empty for: " + line + LOG_NUMBER + (lineNb - 1));
        }
        if (StringUtils.isBlank(composition.getTitre())) {
            result.add("### Error Title empty for: " + line + LOG_NUMBER + (lineNb - 1));
        }
        composition.setUuids(new LinkedList<>(Arrays.asList(MiscUtils.getUuid())));
        compoList.add(composition);
    }

    private static Integer setArtistAndTitreGetRank(Composition composition, String line, boolean upper, String[] split,
            Boolean sorted, Integer i) {
        // Reconnaissance du titre et de l'artiste
        String artist;
        String titre;
        if (!upper) {
            titre = StringUtils.trim(split[1]);
            artist = StringUtils.trim(split[0]);
        } else {
            char[] array = line.toCharArray();
            int cut = 0;
            for (int k = 0 ; k < array.length ; k++) {
                if (!Character.isUpperCase(array[k]) && Character.isAlphabetic(array[k])) {
                    cut = k - 1;
                    break;
                }
            }
            artist = StringUtils.trim(StringUtils.substring(line, 0, cut));
            titre = StringUtils.trim(StringUtils.substring(line, cut));
        }

        Integer rank;
        if (sorted) {
            String res = StringUtils.trim(StringUtils.substringBefore(artist, Constant.DOT));
            if (StringUtils.isNumeric(res)) {
                rank = Integer.parseInt(res);
                artist = StringUtils.substringAfter(artist, Constant.DOT);
            } else {
                res = artist.split(" ")[0];
                rank = parseStringToInt(res);
                artist = StringUtils.substringAfterLast(artist, res);
            }
        } else {
            rank = i;
        }
        composition.setArtist(StringUtils.trim(artist));
        composition.setTitre(titre);
        return rank;
    }

    /**
     * Fragmente en 2 normalement une ligne d'un fichier avec le séparateur donné.
     *
     * @param line {@link String} laligne à couper
     * @param separator {@link String} le séparateur
     * @param result {@code List<String>} la liste des messages à afficher à l'utilisateur
     * @param lineNb le numéro de la ligne dans le fichier
     * @return {@code String[]} la ligne coupée
     * @throws MajorException si la ligne est coupée en plus de 2 morceaux
     */
    private static String[] splitLineWithSeparator(String line, String separator, List<String> result, Integer lineNb)
            throws MajorException {
        String[] split = line.split(separator);
        if (split.length < 2) {
            // Le séparateur ne convient pas, on essaye avec un tiret classique
            split = line.split("-");
        }
        if (split.length < 2) {
            // ça ne marche toujours pas, on arrete tout
            throw new MajorException(
                    "Separator " + separator + " is not suitable for line " + (lineNb - 1) + " : " + line);
        }
        if (split.length > 2) {
            // Il y a plusieurs séparateur dans la ligne
            String newSep = " " + separator + " ";
            split = new String[2];
            split[0] = StringUtils.replace(StringUtils.substringBeforeLast(line, newSep), newSep, ", ");
            split[1] = StringUtils.substringAfterLast(line, newSep);
            if (StringUtils.countMatches(line, newSep) > 1) {
                result.add("### Error Size (" + split.length + ") for: " + line + LOG_NUMBER + (lineNb - 1));
            }
        }
        return split;
    }

    private static void removeParentheseFromTitreAndArtist(List<String> result, String line, Integer lineNumber,
            Composition compo) {
        String titre = compo.getTitre();
        String artist = compo.getArtist();
        int countMatchesTitre = StringUtils.countMatches(titre, "(");
        int countMatchesArtist = StringUtils.countMatches(artist, "(");
        if (countMatchesTitre == 1) {
            titre = StringUtils.trim(StringUtils.substringBefore(titre, "("));
        }
        if (countMatchesArtist == 1) {
            artist = StringUtils.trim(StringUtils.substringBefore(artist, "("));
        }
        if (countMatchesTitre == 0 && countMatchesArtist == 0) {
            result.add("Pas de parenthèse, line: " + line + LOG_NUMBER + lineNumber);
        } else if (countMatchesTitre > 1 || countMatchesArtist > 1) {
            result.add("###Trop de parenthèses, line: " + line + LOG_NUMBER + lineNumber);
            artist = StringUtils.trim(StringUtils.substringBeforeLast(artist, "("));
            titre = StringUtils.trim(StringUtils.substringBeforeLast(titre, "("));
        }
        compo.setTitre(titre);
        compo.setArtist(artist);
    }

    /**
     * Cherche le nombre d'enregistrement dans le fichier.
     *
     * @param fichier le fichier
     * @param randomLines une ligne tirée aléatoirement
     * @param absolutePath le chemin du fichier
     * @return number of compositions in the given file
     */
    public static Integer determineSize(Fichier fichier, List<String> randomLines, String absolutePath) {
        LOG.debug("Start determineSize");
        Integer res = 0;
        res = determineSizeSorted(fichier, randomLines, res);
        res = determineSizeNotSorted(fichier, res);
        if (res == 0) {
            LOG.debug("Taille égale à zéro, on compte le nombre de ligne du fichier");
            try {
                res = countLines(absolutePath, true);
            } catch (IOException e) {
                LOG.error("Erreur dans countLines", e);
            }
        }
        LOG.debug("End determineSize");
        return res;
    }

    private static Integer determineSizeSorted(Fichier fichier, List<String> randomLines, Integer res) {
        Integer result = res;
        if (fichier.getSorted()) {
            LOG.debug("Fichier trié");
            String first = "";
            int i = 0;
            while (StringUtils.isBlank(first)) {
                first = randomLines.get(i);
                i++;
            }
            String last = "";
            i = 1;
            while (StringUtils.isBlank(last)) {
                last = randomLines.get(randomLines.size() - i);
                i++;
            }
            Integer sizeFirst = extractRankFromString(first);
            Integer sizeLast = extractRankFromString(last);
            if (sizeFirst > sizeLast) {
                result = sizeFirst;
            } else {
                result = sizeLast;
            }
        }
        return result;
    }

    private static Integer determineSizeNotSorted(Fichier fichier, Integer res) {
        Integer result = res;
        if (!fichier.getSorted() || result % 5 != 0) {
            LOG.debug("Fichier pas trié ou taille trouvée précedemment incorrecte");
            Matcher mSize = Constant.PATTERN_SIZE.matcher(fichier.getFileName());
            Matcher mDeca = Constant.PATTERN_DECADE.matcher(fichier.getFileName());
            Matcher yDeca = Constant.PATTERN_YEAR.matcher(fichier.getFileName());
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
                    result = Integer.parseInt(size.trim());
                }
            }
        }
        return result;
    }

    /**
     * Extrait le classement d'une string.
     *
     * @param line la string contenant le classement
     * @return le classement
     */
    private static Integer extractRankFromString(String line) {
        LOG.debug("Start extractRankFromString");
        Integer sizeInt;
        String size = StringUtils.trim(StringUtils.substringBefore(line, Constant.DOT));
        if (StringUtils.isNumeric(size)) {
            sizeInt = Integer.parseInt(size);
        } else {
            size = line.split(" ")[0];
            sizeInt = parseStringToInt(size);
        }
        LOG.debug("End extractRankFromString");
        return sizeInt;
    }

    private static Integer parseStringToInt(String string) {
        Integer number;
        try {
            number = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            number = 0;
        }
        return number;
    }

    /**
     * Détermine le type du fichier.
     *
     * @param name le nom du fichier
     * @return {@link RecordType} album, chanson ou inconnu.
     */
    public static RecordType determineType(String name) {
        LOG.debug("Start determineType");
        RecordType res;
        if (Constant.PATTERN_SONG.matcher(name).find()) {
            res = RecordType.SONG;
        } else if (Constant.PATTERN_ALBUM.matcher(name).find()) {
            res = RecordType.ALBUM;
        } else {
            res = RecordType.UNKNOWN;
        }
        LOG.debug("End determineType");
        return res;
    }

    private static Cat determineCategory(String name) {
        LOG.debug("Start determineCategory");
        Cat res;
        if (Constant.PATTERN_DECADE.matcher(name).find()) {
            res = Cat.DECADE;
        } else if (name.matches(Constant.YEAR)) {
            res = Cat.YEAR;
        } else if (Constant.PATTERN_THEME.matcher(name).find()) {
            res = Cat.THEME;
        } else if (Constant.PATTERN_GENRE.matcher(name).find()) {
            res = Cat.GENRE;
        } else if (Constant.PATTERN_ALL_TIME.matcher(name).find()) {
            res = Cat.ALL_TIME;
        } else {
            res = Cat.MISCELLANEOUS;
        }
        LOG.debug("End determineCategory");
        return res;
    }

    private static void determineYears(String name, Fichier file) {
        LOG.debug("Start determineYears");
        String[] split = strip(name);

        if (Constant.PATTERN_YEAR_AT_THE_END.matcher(name).find()) {
            file.setPublishYear(Integer.parseInt(split[split.length - 2]));
        }

        List<String> date = matchPart(split, Constant.YEAR);
        if (file.getCategorie() == Cat.DECADE) {
            determineYearsDecade(file, split, date);
        } else if (file.getCategorie() == Cat.YEAR) {
            file.setRangeDateBegin(Integer.parseInt(split[0]));
            file.setRangeDateEnd(Integer.parseInt(split[0]));
        } else if (date.size() == 2) {
            file.setRangeDateBegin(Integer.parseInt(date.get(0)));
            file.setRangeDateEnd(Integer.parseInt(date.get(1)));
        } else if (file.getCategorie() == Cat.ALL_TIME) {
            file.setRangeDateBegin(0);
            file.setRangeDateEnd(file.getPublishYear());
        } else {
            file.setRangeDateBegin(0);
            file.setRangeDateEnd(0);
        }
        boolean isYearOrDecade = file.getCategorie() == Cat.YEAR || file.getCategorie() == Cat.DECADE;
        if (file.getPublishYear() == 0
                && (isYearOrDecade || (file.getCategorie() == Cat.ALL_TIME && file.getRangeDateEnd() != 0))) {
            file.setPublishYear(file.getRangeDateEnd());
        }
        LOG.debug("End determineYears");
    }

    private static void determineYearsDecade(Fichier file, String[] split, List<String> date) {
        List<String> decadeMatch = matchPart(split, Constant.TWO_DIGITS);
        if (decadeMatch.size() == 1) {
            Integer begin = convertTwoDigitsToYear(decadeMatch);
            file.setRangeDateBegin(begin);
            file.setRangeDateEnd(begin + 9);
        } else {
            String res = "";
            if (date.size() > 1) {
                res = date.stream().filter(s -> !s.matches(Constant.YEAR_AT_THE_END)).findFirst().orElse("");
            } else if (date.size() == 1) {
                res = date.get(0);
            }
            file.setRangeDateBegin(Integer.parseInt(res));
            file.setRangeDateEnd(Integer.parseInt(res) + 9);
        }
    }

    private static String[] strip(String name) {
        return name.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
    }

    private static Integer convertTwoDigitsToYear(List<String> decadeMatch) {
        String substring = decadeMatch.get(0).substring(0, 2);
        if (substring.contains("00") || substring.contains("10") || substring.contains("20")) {
            substring = "20" + substring;
        } else {
            substring = "19" + substring;
        }
        return Integer.parseInt(substring);
    }

    private static List<String> matchPart(String[] split, String regex) {
        List<String> res = new ArrayList<>();
        if (regex.equals(Constant.TWO_DIGITS)) {
            for (int i = 0 ; i < split.length ; i++) {
                String str = split[i].trim();
                if (str.matches(regex) && (split[i + 1].startsWith("s") || split[i + 1].startsWith("'"))) {
                    res.add(str);
                }
            }
        } else {
            for (String element : split) {
                String str = element.trim();
                if (str.matches(regex)) {
                    res.add(str);
                }
            }
        }
        return res;
    }

    /**
     * Détermine si les enregistrements sont triés (ou numérotés) ou non dans le fichier.
     *
     * @param line une ligne au hasard.
     * @return {@code true} si oui, {@code false} sinon
     */
    public static boolean isSorted(String line) {
        return Constant.PATTERN_CHART.matcher(line).find();
    }

    /**
     * Détermine le séparateur entre l'artiste et le titre utilisé dans le fichier.
     *
     * @param line une ligne du fichier
     * @return {@link String} le séparateur
     */
    public static String getSeparator(String line) {
        LOG.debug("Start getSeparator");
        String[] split = line.split(" ");
        String result = "-";
        int i = 0;
        while (i < split.length && "-".equals(result)) {
            String string = split[i].trim();
            if (isSuitableSeparator(string)) {
                result = string;
            } else {
                String first = StringUtils.substring(string, 0, 1);
                if (isSuitableSeparator(first)) {
                    result = first;
                } else {
                    String last = StringUtils.substring(string, string.length() - 1, string.length());
                    if (isSuitableSeparator(last)) {
                        result = last;
                    }
                }

            }
            i++;
        }
        LOG.debug("End getSeparator");
        return result;
    }

    private static boolean isSuitableSeparator(String sep) {
        return !StringUtils.isAlphanumeric(sep) && sep.length() == 1
                && !Arrays.asList(Constant.getNotSeparators()).contains(sep);
    }

    /**
     * Retourne dans l'ordre les 3 premières lignes, une ligne au hasard et les 2 dernières lignes du fichier donné et la derniere ligne non vide.
     *
     * @param file le fichier
     * @return une liste de 6 String
     */
    public static List<String> randomLineAndLastLines(File file) {
        LOG.debug("Start randomLineAndLastLines");
        List<String> lines = new ArrayList<>();
        String line = "";
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), Constant.ANSI_ENCODING))) {
            Integer countLines = countLines(file.getAbsolutePath(), false);
            if (countLines < 6) {
                LOG.warn("File " + file.getName() + " trop trop petit");
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
                return lines;
            }
            int startingRandom = 3;
            String firstLine = StringUtils.trim(br.readLine());
            if (StringUtils.startsWith(firstLine, Constant.IMPORT_PARAMS_PREFIX)) {
                // Ignore first ligne if import params
                lines.add(StringUtils.trim(br.readLine()));
                startingRandom++;
            } else {
                lines.add(firstLine);
            }
            // 2nd and 3thd lines
            lines.add(StringUtils.trim(br.readLine()));
            lines.add(StringUtils.trim(br.readLine()));
            // Random line
            int rand = ThreadLocalRandom.current().nextInt(startingRandom + 1, countLines - 1);
            for (int i = startingRandom ; i < rand ; i++) {
                line = StringUtils.trim(br.readLine());
            }
            int count = rand;
            while (!isValidLine(line)) {
                line = StringUtils.trim(br.readLine());
                count++;
            }
            lines.add(line);

            // The last lines
            while (count < countLines - 2) {
                br.readLine();
                count++;
            }
            lines.add(StringUtils.trim(br.readLine()));
            lines.add(StringUtils.trim(br.readLine()));
        } catch (IOException e) {
            LOG.error("Erreur lors de la lecture du fichier " + file.getAbsolutePath(), e);
        }
        LOG.debug("End randomLineAndLastLines");
        return lines;
    }

    /**
     * Counts the number of comma in the given file.
     *
     * @param file the file
     * @return a number
     */
    public static int countComma(File file) {
        LOG.debug("Start countComma");
        int result = 0;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), Constant.ANSI_ENCODING))) {
            String readLine = "";
            while (readLine != null) {
                readLine = br.readLine();
                if (StringUtils.contains(readLine, ",")) {
                    result++;
                }
            }
        } catch (IOException e) {
            LOG.error("Erreur lors du comptage de virgule dans le fichier: {}", file.getAbsolutePath(), e);
        }
        LOG.debug("End countComma: {}", result);
        return result;
    }

    /**
     * Checks if the given line is valid, ie might contains a composition.
     *
     * @param line the line to check
     * @return false if the line is too short, a comment, import params or empty, true otherwise
     */
    public static boolean isValidLine(String line) {
        return StringUtils.isNotBlank(line) && !StringUtils.startsWith(line, Constant.IMPORT_PARAMS_PREFIX)
                && !StringUtils.startsWith(line, Constant.COMMENT_PREFIX) && line.length() > 4;
    }

    /**
     * Compte le nombre de ligne dans le fichier.
     *
     * @param filename le nom du fichier
     * @param validLine true on compte seulement les lignes valides, plus longue que 5 caractères et non commentées, false on compte toutes les
     *            lignes.
     * @return un nombre
     * @throws IOException if error when readind file
     */
    public static Integer countLines(String filename, boolean validLine) throws IOException {
        LOG.debug("Start countLines");
        Integer count = 0;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(new File(filename)), Constant.ANSI_ENCODING))) {
            String readLine = "";
            while (readLine != null) {
                readLine = br.readLine();
                if ((validLine && isValidLine(readLine)) || (!validLine && readLine != null)) {
                    count++;
                }
            }
            LOG.debug("End countLines");
        }
        return count;
    }

}
