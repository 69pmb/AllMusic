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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MyException;

/**
 * Classe permettant l'import de fichiers.
 * 
 */
public class ImportFile {

	private static final String LOG_NUMBER = " number: ";

	private static final Logger LOG = Logger.getLogger(ImportFile.class);

	private ImportFile() {
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
		fichier.setCreationDate(getCreationDate(file));
		fichier.setFileName(StringUtils.substringBeforeLast(name, "."));
		fichier.setCategorie(determineCategory(name));
		String auteur = file.getParentFile().getName();
		if ("album".equalsIgnoreCase(auteur) || "song".equalsIgnoreCase(auteur)) {
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
	 * @throws MyException
	 */
	public static List<Composition> getCompositionsFromFile(File file, Fichier fichier, RecordType type, String separator, List<String> result,
			boolean artistFirst, boolean reverseArtist, boolean parenthese, boolean upper, boolean removeAfter) throws MyException {
		LOG.debug("Start getCompositionsFromFile");
		List<Composition> compoList = new ArrayList<>();
		String line = "";
		int i = 1;
		int lineNb = 1;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constant.ANSI_ENCODING));) {
			while ((line = br.readLine()) != null) {
				lineNb++;
				getCompositionFromOneLine(compoList, fichier, line, separator, result, type, artistFirst, removeAfter, upper, reverseArtist, parenthese, lineNb,
						i);
				i++;
			}
		} catch (NumberFormatException | IOException e1) {
			throw new MyException(e1.toString(), e1);
		}
		LOG.debug(result);
		LOG.debug("End getCompositionsFromFile");
		return compoList;
	}

	private static void getCompositionFromOneLine(List<Composition> compoList, Fichier fichier, String entryLine, String separator, List<String> result,
			RecordType type, boolean artistFirst, boolean removeAfter, boolean upper, boolean reverseArtist, boolean parenthese, int lineNb, int i)
			throws MyException {
		String line = entryLine;
		if (StringUtils.isBlank(line) || line.length() < 5 || StringUtils.startsWith(line, "#")) {
			return;
		}

		if (removeAfter) {
			line = StringUtils.substringBeforeLast(line, separator);
		}

		// Reconnaissance du titre et de l'artiste
		String artist;
		String titre;
		if (!upper) {
			String[] split = splitLineWithSeparator(line, separator, result, lineNb);
			titre = StringUtils.trim(split[1]);
			artist = StringUtils.trim(split[0]);
		} else {
			char[] array = line.toCharArray();
			int cut = 0;
			for (int k = 0; k < array.length; k++) {
				if (!Character.isUpperCase(array[k]) && Character.isAlphabetic(array[k])) {
					cut = k - 1;
					break;
				}
			}
			artist = StringUtils.trim(StringUtils.substring(line, 0, cut));
			titre = StringUtils.trim(StringUtils.substring(line, cut));
		}

		Composition composition = new Composition();
		List<Fichier> files = new ArrayList<>();
		composition.setFiles(files);
		composition.setTitre(titre);
		int rank;
		if (fichier.getSorted()) {
			String res = StringUtils.trim(StringUtils.substringBefore(artist, "."));
			if (StringUtils.isNumeric(res)) {
				rank = Integer.parseInt(res);
				artist = StringUtils.substringAfter(artist, ".");
			} else {
				res = artist.split(" ")[0];
				rank = parseStringToInt(res);
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

		if (!artistFirst) {
			String artist2 = composition.getArtist();
			composition.setArtist(composition.getTitre());
			composition.setTitre(artist2);
		}

		if (reverseArtist) {
			String[] arrayArtist = composition.getArtist().split(",");
			if (arrayArtist.length == 2) {
				artist = StringUtils.trim(arrayArtist[1]) + " " + StringUtils.trim(arrayArtist[0]);
				composition.setArtist(StringUtils.trim(artist));
			}
		}
		composition.setArtist(StringUtils.removeEnd(StringUtils.removeStart(composition.getArtist(), "\""), "\""));
		composition.setTitre(StringUtils.removeEnd(StringUtils.removeStart(composition.getTitre(), "\""), "\""));
		if (parenthese) {
			composition.setTitre(removeParenthese(result, line, lineNb, composition.getTitre()));
			composition.setArtist(removeParenthese(result, line, lineNb, composition.getArtist()));
		}

		compoList.add(composition);
	}

	/**
	 * Fragmente en 2 normalement une ligne d'un fichier avec le séparateur
	 * donné.
	 * 
	 * @param line {@link String} laligne à couper
	 * @param separator {@link String} le séparateur
	 * @param result {@code List<String>} la liste des messages à afficher à l'utilisateur
	 * @param lineNb le numéro de la ligne dans le fichier
	 * @return {@code String[]} la ligne coupée
	 * @throws MyException si la ligne est coupée en plus de 2 morceaux
	 */
	private static String[] splitLineWithSeparator(String line, String separator, List<String> result, int lineNb) throws MyException {
		String[] split = line.split(separator);
		if (split.length < 2) {
			// Le séparateur ne convient pas, on essaye avec un tiret classique
			split = line.split("-");
		}
		if (split.length < 2) {
			// ça ne marche toujours pas, on arrete tout
			throw new MyException("Separator " + separator + " is not suitable for line " + lineNb + " : " + line);
		}
		if (split.length > 2) {
			// Il y a plusieurs séparateur dans la ligne
			String newSep = " " + separator + " ";
			split = new String[2];
			split[0] = StringUtils.replace(StringUtils.substringBeforeLast(line, newSep), newSep, ", ");
			split[1] = StringUtils.substringAfterLast(line, newSep);
			if (StringUtils.countMatches(line, newSep) > 1) {
				result.add("### Error Size (" + split.length + ") for: " + line + LOG_NUMBER + lineNb);
				result.add("### split: " + Arrays.toString(split));
			}
		}
		return split;
	}

	private static String removeParenthese(List<String> result, String line, int lineNumber, String chaine) {
		int countMatches = StringUtils.countMatches(chaine, "(");
		String res = chaine;
		if (countMatches == 1) {
			res = StringUtils.trim(StringUtils.substringBefore(chaine, "("));
		} else if (countMatches == 0) {
			result.add("Pas de parenthèse, line: " + line + LOG_NUMBER + lineNumber);
		} else {
			result.add("###Trop de parenthèses, line: " + line + LOG_NUMBER + lineNumber);
		}
		return res;
	}

	/**
	 * Cherche le nombre d'enregistrement dans le fichier.
	 * 
	 * @param fichier le fichier
	 * @param randomLines une ligne tirée aléatoirement
	 * @param absolutePath le chemin du fichier
	 * @return
	 */
	public static int determineSize(Fichier fichier, List<String> randomLines, String absolutePath) {
		LOG.debug("Start determineSize");
		int res = 0;
		res = determineSizeSorted(fichier, randomLines, res);
		res = determineSizeNotSorted(fichier, res);
		if (res == 0) {
			LOG.debug("Taille égale à zéro, on compte le nombre de ligne du fichier");
			try {
				fichier.setSize(countLines(absolutePath));
			} catch (IOException e) {
				LOG.error("Erreur dans countLines", e);
			}
		}
		LOG.debug("End determineSize");
		return res;
	}

	private static int determineSizeSorted(Fichier fichier, List<String> randomLines, int res) {
		int result = res;
		if (fichier.getSorted()) {
			LOG.debug("Fichier trié");
			String first = randomLines.get(0);
			String last = randomLines.get(randomLines.size() - 1);
			if (StringUtils.isBlank(last)) {
				last = randomLines.get(randomLines.size() - 2);
			}
			int sizeFirst = extractRankFromString(first);
			int sizeLast = extractRankFromString(last);
			if (sizeFirst > sizeLast) {
				result = sizeFirst;
			} else {
				result = sizeLast;
			}
		}
		return result;
	}

	private static int determineSizeNotSorted(Fichier fichier, int res) {
		int result = res;
		if (!fichier.getSorted() || result % 10 != 0) {
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
	private static int extractRankFromString(String line) {
		LOG.debug("Start extractRankFromString");
		int sizeInt;
		String size = StringUtils.trim(StringUtils.substringBefore(line, "."));
		if (StringUtils.isNumeric(size)) {
			sizeInt = Integer.parseInt(size);
		} else {
			size = line.split(" ")[0];
			sizeInt = parseStringToInt(size);
		}
		LOG.debug("End extractRankFromString");
		return sizeInt;
	}

	private static int parseStringToInt(String string) {
		int number;
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
		} else if (file.getCategorie() == Cat.ALL_TIME) {
			if (date.size() == 2) {
				file.setRangeDateBegin(Integer.parseInt(date.get(0)));
				file.setRangeDateEnd(Integer.parseInt(date.get(1)));
			} else {
				file.setRangeDateBegin(0);
				file.setRangeDateEnd(file.getPublishYear());
			}
		} else if (date.size() == 2) {
			file.setRangeDateBegin(Integer.parseInt(date.get(0)));
			file.setRangeDateEnd(Integer.parseInt(date.get(1)));
		} else {
			file.setRangeDateBegin(0);
			file.setRangeDateEnd(0);
		}
		if (file.getPublishYear() == 0 && (file.getCategorie() == Cat.YEAR || file.getCategorie() == Cat.DECADE
				|| (file.getCategorie() == Cat.ALL_TIME && file.getRangeDateEnd() != 0))) {
			file.setPublishYear(file.getRangeDateEnd());
		}
		LOG.debug("End determineYears");
	}

	private static void determineYearsDecade(Fichier file, String[] split, List<String> date) {
		List<String> decadeMatch = matchPart(split, Constant.TWO_DIGITS);
		if (decadeMatch.size() == 1) {
			int begin = convertTwoDigitsToYear(decadeMatch);
			file.setRangeDateBegin(begin);
			file.setRangeDateEnd(begin + 9);
		} else {
			String res = "";
			if (date.size() > 1) {
				for (String str : date) {
					if (!str.matches(Constant.YEAR_AT_THE_END)) {
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
	}

	private static String[] strip(String name) {
		return name.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
	}

	private static int convertTwoDigitsToYear(List<String> decadeMatch) {
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
		if (regex == Constant.TWO_DIGITS) {
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
		LOG.debug("Start getCreationDate");
		BasicFileAttributes attr = null;
		try {
			attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
		} catch (IOException e) {
			LOG.error("Impossible de récupérer la date de création de " + file.getAbsolutePath(), e);
		}
		if (attr == null) {
			return new Date();
		}
		Date creationDate = null;
		long milliseconds = attr.creationTime().to(TimeUnit.MILLISECONDS);
		if ((milliseconds > Long.MIN_VALUE) && (milliseconds < Long.MAX_VALUE)) {
			creationDate = new Date(milliseconds);
		}
		LOG.debug("End getCreationDate");
		return creationDate;
	}

	/**
	 * Détermine si les enregistrements sont triés (ou numérotés) ou non dans le
	 * fichier.
	 * 
	 * @param line une ligne au hasard.
	 * @return {@code true} si oui, {@code false} sinon
	 */
	public static boolean isSorted(String line) {
		return Constant.PATTERN_CHART.matcher(line).find();
	}

	/**
	 * Détermine le séparateur entre l'artiste et le titre utilisé dans le
	 * fichier.
	 * 
	 * @param line une ligne du fichier
	 * @return {@link String} le séparateur
	 */
	public static String getSeparator(String line) {
		LOG.debug("Start getSeparator");
		String[] split = line.split(" ");
		String res = "-";
		for (int i = 0; i < split.length; i++) {
			String string = split[i].trim();
			if (!StringUtils.isAlphanumeric(string)) {
				res = string;
			}
		}
		LOG.debug("End getSeparator");
		return res;
	}

	/**
	 * Retourne dans l'ordre les 3 premières lignes, une ligne au hasard et les
	 * 2 dernières lignes du fichier donné.
	 * 
	 * @param file le fichier
	 * @return une liste de 6 String
	 */
	public static List<String> randomLineAndLastLines(File file) {
		LOG.debug("Start randomLineAndLastLines");
		List<String> lines = new ArrayList<>();
		String line = "";
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constant.ANSI_ENCODING));) {
			int countLines = countLines(file.getAbsolutePath());
			int rand = ThreadLocalRandom.current().nextInt(4, countLines);
			lines.add(StringUtils.trim(br.readLine()));
			lines.add(StringUtils.trim(br.readLine()));
			lines.add(StringUtils.trim(br.readLine()));
			for (int i = 3; i < rand; i++) {
				line = StringUtils.trim(br.readLine());
			}
			int count = rand;
			while (StringUtils.startsWith(line, "#") || StringUtils.isBlank(line) && line.length() < 5) {
				line = StringUtils.trim(br.readLine());
				count++;
			}
			lines.add(line);
			while (count < countLines - 1) {
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
	 * Compte le nombre de ligne dans le fichier.
	 * 
	 * @param filename le nom du fichier
	 * @return un nombre
	 * @throws IOException
	 */
	public static int countLines(String filename) throws IOException {
		LOG.debug("Start countLines");
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
			LOG.debug("End countLines");
			return (count == 0 && !empty) ? 1 : count;
		} finally {
			is.close();
		}
	}

}
