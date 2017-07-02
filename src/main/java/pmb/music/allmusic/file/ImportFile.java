/**
 * 
 */
package pmb.music.allmusic.file;

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

import pmb.music.allmusic.model.Cat;
import pmb.music.allmusic.model.Composition;
import pmb.music.allmusic.model.Fichier;
import pmb.music.allmusic.model.RecordType;
import pmb.music.allmusic.utils.Constant;
import pmb.music.allmusic.utils.MyException;

/**
 * @author i2113mj
 * 
 */
public class ImportFile {

	private static final String LOG_NUMBER = " number: ";

	private ImportFile() {
	};

	public static Fichier convertOneFile(File file) {
		System.out.println("Start convertOneFile");
		Fichier fichier = new Fichier();
		String name = file.getName();
		fichier.setCreationDate(getCreationDate(file));
		fichier.setFileName(StringUtils.substringBeforeLast(name, "."));
		fichier.setCategorie(determineCategory(name));
		String auteur = file.getParentFile().getName();
		if ("album".equalsIgnoreCase(auteur) || "song".equalsIgnoreCase(auteur)) {
			auteur = file.getParentFile().getParentFile().getName();
		}
		fichier.setAuthor(auteur);
		determineYears(name, fichier);
		System.out.println("End convertOneFile");
		return fichier;
	}

	public static List<Composition> getCompositionsFromFile(File file, Fichier fichier, RecordType type,
			String separator, List<String> result, boolean artistFirst, boolean reverseArtist, boolean parenthese,
			boolean upper, boolean removeAfter) throws MyException {
		System.out.println("Start getCompositionsFromFile");
		List<Composition> compoList = new ArrayList<>();
		String line = "";
		int i = 1;
		int lineNb = 1;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),
				Constant.ANSI_ENCODING));) {
			while ((line = br.readLine()) != null) {
				lineNb++;
				if (StringUtils.isBlank(line) || line.length() < 5 || StringUtils.startsWith(line, "#")) {
					continue;
				}

				if (removeAfter) {
					line = StringUtils.substringBeforeLast(line, separator);
				}

				// Reconnaissance du titre et de l'artiste
				String artist;
				String titre = "";
				if (!upper) {
					String[] split = splitLineWithSeparator(separator, result, line, lineNb);
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
				int rank = 0;
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
				composition.setArtist(StringUtils.removeEnd(StringUtils.removeStart(composition.getArtist(), "\""),
						"\""));
				composition
						.setTitre(StringUtils.removeEnd(StringUtils.removeStart(composition.getTitre(), "\""), "\""));
				if (parenthese) {
					composition.setTitre(removeParenthese(result, line, lineNb, composition.getTitre()));
					composition.setArtist(removeParenthese(result, line, lineNb, composition.getArtist()));
				}

				// composition.setArtist(StringUtils.stripAccents(composition.getArtist()));
				// composition.setTitre(StringUtils.stripAccents(composition.getTitre()));
				compoList.add(composition);
				i++;
			}
		} catch (NumberFormatException | IOException e1) {
			throw new MyException(e1.toString());
		}
		System.out.println(result);
		System.out.println("End getCompositionsFromFile");
		return compoList;
	}

	/**
	 * @param separator
	 * @param result
	 * @param line
	 * @param lineNb
	 * @return
	 * @throws MyException
	 */
	private static String[] splitLineWithSeparator(String separator, List<String> result, String line, int lineNb)
			throws MyException {
		String[] split = line.split(separator);
		if (split.length < 2) {
			split = line.split("-");
		}
		if (split.length < 2) {
			throw new MyException("Separator " + separator + " is not suitable for line " + lineNb + " : " + line);
		}
		if (split.length > 2) {
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
	 * @param fichier le fichier
	 * @param randomLines une ligne tirée aléatoirement
	 * @param absolutePath le chemin du fichier
	 * @return
	 */
	public static int determineSize(Fichier fichier, List<String> randomLines, String absolutePath) {
		int res = 0;
		if (fichier.getSorted()) {
			String first = randomLines.get(0);
			String last = randomLines.get(randomLines.size() - 1);
			if (StringUtils.isBlank(last)) {
				last = randomLines.get(randomLines.size() - 2);
			}
			int sizeFirst = extractRankFromString(first);
			int sizeLast = extractRankFromString(last);
			if (sizeFirst > sizeLast) {
				res = sizeFirst;
			} else {
				res = sizeLast;
			}
		}
		if (!fichier.getSorted() || res % 10 != 0) {
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
					res = Integer.parseInt(size.trim());
				}
			}
		}
		if (res == 0) {
			try {
				fichier.setSize(countLines(absolutePath));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	private static int extractRankFromString(String line) {
		int sizeInt;
		String size = StringUtils.trim(StringUtils.substringBefore(line, "."));
		if (StringUtils.isNumeric(size)) {
			sizeInt = Integer.parseInt(size);
		} else {
			size = line.split(" ")[0];
			sizeInt = parseStringToInt(size);
		}
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
	 * @param name le nom du fichier
	 * @return {@link RecordType} album, chanson ou inconnu.
	 */
	public static RecordType determineType(String name) {
		RecordType res;
		if (Constant.PATTERN_SONG.matcher(name).find()) {
			res = RecordType.SONG;
		} else if (Constant.PATTERN_ALBUM.matcher(name).find()) {
			res = RecordType.ALBUM;
		} else {
			res = RecordType.UNKNOWN;
		}
		return res;
	}

	private static Cat determineCategory(String name) {
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
		return res;
	}

	private static void determineYears(String name, Fichier file) {
		System.out.println("Start determineYears");
		String[] split = strip(name);

		if (Constant.PATTERN_YEAR_AT_THE_END.matcher(name).find()) {
			file.setPublishYear(Integer.parseInt(split[split.length - 2]));
		}

		List<String> date = matchPart(split, Constant.YEAR);
		if (file.getCategorie() == Cat.DECADE) {
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
			// Calendar cal = new GregorianCalendar();
			// file.setRangeDateBegin(cal.get(Calendar.Constant.YEAR));
			// file.setRangeDateEnd(cal.get(Calendar.Constant.YEAR));
			file.setRangeDateBegin(0);
			file.setRangeDateEnd(0);
		}
		if (file.getPublishYear() == 0
				&& (file.getCategorie() == Cat.YEAR || file.getCategorie() == Cat.DECADE || (file.getCategorie() == Cat.ALL_TIME && file
						.getRangeDateEnd() != 0))) {
			file.setPublishYear(file.getRangeDateEnd());
		}
		System.out.println("End determineYears");
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
		BasicFileAttributes attr = null;
		try {
			attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (attr == null) {
			return new Date();
		}
		Date creationDate = null;
		long milliseconds = attr.creationTime().to(TimeUnit.MILLISECONDS);
		if ((milliseconds > Long.MIN_VALUE) && (milliseconds < Long.MAX_VALUE)) {
			creationDate = new Date(milliseconds);
		}
		return creationDate;
	}

	/**
	 * Détermine si les enregistrements sont triés (ou numérotés) ou non dans le
	 * fichier.
	 * @param line une ligne au hasard.
	 * @return {@code true} si oui, {@code false} sinon
	 */
	public static boolean isSorted(String line) {
		if (Constant.PATTERN_CHART.matcher(line).find()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Détermine le séparateur entre l'artiste et le titre utilisé dans le
	 * fichier.
	 * @param line une ligne du fichier
	 * @return {@link String} le séparateur
	 */
	public static String getSeparator(String line) {
		String[] split = line.split(" ");
		String res = "-";
		for (int i = 0; i < split.length; i++) {
			String string = split[i].trim();
			if (!StringUtils.isAlphanumeric(string)) {
				res = string;
			}
		}
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
		System.out.println("Start randomLineAndLastLines");
		List<String> lines = new ArrayList<>();
		String line = "";
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),
				Constant.ANSI_ENCODING));) {
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
			e.printStackTrace();
		}
		System.out.println("End randomLineAndLastLines");
		return lines;
	}

	/**
	 * Compte le nombre de ligne dans le fichier.
	 * @param filename le nom du fichier
	 * @return un nombre
	 * @throws IOException
	 */
	public static int countLines(String filename) throws IOException {
		System.out.println("Start countLines");
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
			System.out.println("End countLines");
			return (count == 0 && !empty) ? 1 : count;
		} finally {
			is.close();
		}
	}

}
