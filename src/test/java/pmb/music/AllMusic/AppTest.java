package pmb.music.AllMusic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.file.CsvFile;
import pmb.music.AllMusic.file.ImportFile;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.utils.SearchUtils;
import pmb.music.AllMusic.view.Onglet;

/**
 * Unit test for simple App.
 */
public class AppTest {

	private static final Logger LOG = Logger.getLogger(AppTest.class);

	public static void main(String[] args) {
		// List<Composition> importXML = ImportXML.importXML(Constant.FINAL_FILE_PATH);
		// topRecordsByPoints(importXML, RecordType.SONG, "Top All Years Songs");
		// topRecordsByPoints(importXML, RecordType.ALBUM, "Top All Years Albums");
		// stats(importXML, RecordType.ALBUM);
		// stats(importXML, RecordType.SONG);
		// gauss(importXML, RecordType.ALBUM);
		// sizeFileSuspicious(importXML);
		findImportParamsForAllFiles();
	}

	public static void findImportParamsForAllFiles() {
		List<String> authorList = Onglet.getAuthorList();
		for (String author : authorList) {
			List<File> files = new ArrayList<>();
			FichierUtils.listFilesForFolder(new File(Constant.MUSIC_ABS_DIRECTORY + FileUtils.FS + author), files,
					Constant.TXT_EXTENSION, true);
			for (File file : files) {
				String filename = StringUtils.substringBeforeLast(file.getName(), Constant.TXT_EXTENSION);
				if (StringUtils.startsWith(
						FichierUtils.getFirstLine(new File(FichierUtils.buildTxtFilePath(filename, author).get())),
						Constant.IMPORT_PARAMS_PREFIX)) {
					continue;
				}
				LOG.info("@");
				List<Composition> xml = ImportXML.importXML(Constant.XML_PATH + filename + Constant.XML_EXTENSION);
				Map<String, String> result = findImportParamsForOneFile(filename, author, xml);
				if (result.isEmpty()) {
					LOG.warn("### No result for: " + filename);
				} else {
					Composition composition = xml.get(0);
					try {
						Fichier fichier = composition.getFiles().get(0);
						List<Composition> txtList = ImportFile.getCompositionsFromFile(file, fichier,
								composition.getRecordType(), result.get("separator"), new ArrayList<>(),
								Boolean.valueOf(result.get("artistFirst")),
								Boolean.valueOf(result.get("reverseArtist")), Boolean.valueOf(result.get("parenthese")),
								Boolean.valueOf(result.get("upper")), Boolean.valueOf(result.get("removeAfter")));
						if (compareCompositionList(xml, txtList)) {
							result.put("name", fichier.getFileName());
							result.put("auteur", fichier.getAuthor());
							result.put("create", new Constant().getSdfDttm().format(fichier.getCreationDate()));
							result.put("type", composition.getRecordType().toString());
							result.put("cat", fichier.getCategorie().toString());
							result.put("rangeB", String.valueOf(fichier.getRangeDateBegin()));
							result.put("rangeE", String.valueOf(fichier.getRangeDateEnd()));
							result.put("sorted", String.valueOf(fichier.getSorted()));
							result.put("publish", String.valueOf(fichier.getPublishYear()));
							result.put("size", String.valueOf(fichier.getSize()));
							FichierUtils.writeMapInFile(file, result);
						}
					} catch (MyException e) {
						LOG.error("Error file: " + filename, e);
					}
				}
			}
		}
	}

	private static Map<String, String> findImportParamsForOneFile(String filename, String auteur, List<Composition> xml) {
		Map<String, String> result = new HashMap<>();
		List<String> randomLineAndLastLines = ImportFile
				.randomLineAndLastLines(new File(FichierUtils.buildTxtFilePath(filename, auteur).get()));
		if (randomLineAndLastLines.size() < 6) {
			LOG.warn("Too small: " + filename);
			return result;
		}
		String separator = ImportFile.getSeparator(randomLineAndLastLines.get(3));

		int lineNumber = findLineNumber(randomLineAndLastLines);
		int tryNb = 0;
		while (lineNumber + tryNb < 3) {
			result = findParams(filename, result, xml, randomLineAndLastLines, separator, lineNumber, tryNb);
			if (result.isEmpty()) {
				result = findParams(filename, result, xml, randomLineAndLastLines, separator, lineNumber + 1, tryNb);
				if (!result.isEmpty()) {
					break;
				}
			} else {
				break;
			}
			tryNb++;
		}
		return result;
	}

	public static Map<String, String> findParams(String filename, Map<String, String> result, List<Composition> xml,
			List<String> randomLineAndLastLines, String separator, int lineNumber, int tryNb) {
		final JaroWinklerDistance jaro = new JaroWinklerDistance();
		String[] split = StringUtils.split(randomLineAndLastLines.get(lineNumber + tryNb), separator);
		LOG.info("File: " + filename);
		LOG.info("split size: " + split.length);
		if (split.length < 2) {
			// LOG.warn("Unsplittable: " + StringUtils.join(split, ",") + ", from: " +
			// filename + ", separator: " + separator);
			return buildResultMap("", true, false, false, true, false);
		}
		String txtArtist;
		if (xml.get(2).getFiles().get(0).getSorted()) {
			txtArtist = StringUtils.trim(removeRank(StringUtils.trim(split[0])));
		} else {
			txtArtist = StringUtils.trim(split[0]);
		}
		String txtTitre = StringUtils.trim(split[1]);
		String xmlArtist = xml.get(tryNb).getArtist();
		String xmlTitre = xml.get(tryNb).getTitre();
		String artistRevert = revertArtist(txtArtist);
		LOG.info("xmlArtist: " + xmlArtist);
		LOG.info("xmlTitre: " + xmlTitre);
		LOG.info("txtArtist: " + txtArtist);
		LOG.info("txtTitre: " + txtTitre);
		boolean artistEquals = SearchUtils.isEqualsJaro(jaro, xmlArtist, txtArtist, Constant.SCORE_LIMIT_ARTIST_FUSION);
		boolean titreEquals = SearchUtils.isEqualsJaro(jaro, xmlTitre, txtTitre, Constant.SCORE_LIMIT_TITLE_FUSION);
		boolean artistRevertEquals = SearchUtils.isEqualsJaro(jaro, xmlArtist, artistRevert,
				Constant.SCORE_LIMIT_ARTIST_FUSION);
		if (artistEquals && titreEquals) {
			LOG.info("# Artiste en 1er");
			return buildResultMap(separator, true, false, false, false, false);
		} else if (!artistEquals && !titreEquals) {
			if (SearchUtils.isEqualsJaro(jaro, xmlArtist, txtTitre, Constant.SCORE_LIMIT_ARTIST_FUSION)
					&& SearchUtils.isEqualsJaro(jaro, xmlTitre, txtArtist, Constant.SCORE_LIMIT_TITLE_FUSION)) {
				LOG.info("# Titre en 1er");
				return buildResultMap(separator, false, false, false, false, false);
			}
			String artistPar = removeParenthe(txtArtist);
			String titrePar = removeParenthe(txtTitre);
			if (SearchUtils.isEqualsJaro(jaro, xmlArtist, artistPar, Constant.SCORE_LIMIT_ARTIST_FUSION)
					&& SearchUtils.isEqualsJaro(jaro, xmlTitre, titrePar, Constant.SCORE_LIMIT_TITLE_FUSION)) {
				LOG.info("# Parenthèse");
				return buildResultMap(separator, true, false, true, false, false);

			} else if (SearchUtils.isEqualsJaro(jaro, xmlArtist, titrePar, Constant.SCORE_LIMIT_ARTIST_FUSION)
					&& SearchUtils.isEqualsJaro(jaro, xmlTitre, artistPar, Constant.SCORE_LIMIT_TITLE_FUSION)) {
				LOG.info("# Titre en 1er et Parenthèse");
				return buildResultMap(separator, false, false, true, false, false);
			}
		} else if (titreEquals && !artistEquals && artistRevertEquals) {
			LOG.info("# Artist reverse");
			return buildResultMap(separator, true, true, false, false, false);
		} else if (SearchUtils.isEqualsJaro(jaro, xmlArtist, revertArtist(txtTitre), Constant.SCORE_LIMIT_ARTIST_FUSION)
				&& SearchUtils.isEqualsJaro(jaro, xmlTitre, txtArtist, Constant.SCORE_LIMIT_TITLE_FUSION)) {
			LOG.info("# Titre en 1er et Artist reverse");
			return buildResultMap(separator, false, true, false, false, false);
		}
		return result;
	}

	private static boolean compareCompositionList(List<Composition> xml, List<Composition> txt) {
		Comparator<Composition> byRank = (c1, c2) -> Integer.valueOf(c1.getFiles().get(0).getClassement())
				.compareTo(Integer.valueOf(c2.getFiles().get(0).getClassement()));
		xml = xml.stream().sorted(byRank).collect(Collectors.toList());
		txt = txt.stream().sorted(byRank).collect(Collectors.toList());
		int size = xml.size();
		if (size != txt.size()) {
			LOG.warn("Not the same size: " + size + " " + txt.size());
			return false;
		}
		final JaroWinklerDistance jaro = new JaroWinklerDistance();
		int nbEquals = 0;
		for (int i = 0; i < size; i++) {
			Composition xmlComp = xml.get(i);
			Composition txtComp = txt.get(i);
			// if (new Double(i) / new Double(xml.size()) > 0.5) {
			// LOG.info("Equals !!!");
			// return true;
			// }
			boolean artistEquals = SearchUtils.isEqualsJaro(jaro, xmlComp.getArtist(), txtComp.getArtist(),
					Constant.SCORE_LIMIT_ARTIST_FUSION);
			boolean titreEquals = SearchUtils.isEqualsJaro(jaro, xmlComp.getTitre(), txtComp.getTitre(),
					Constant.SCORE_LIMIT_TITLE_FUSION);
			if (!artistEquals && !titreEquals) {
				LOG.warn("Not Equals: " + i + " " + size);
				LOG.warn("XML: " + xmlComp.getArtist() + " - " + xmlComp.getTitre());
				LOG.warn("TXT: " + txtComp.getArtist() + " - " + txtComp.getTitre());
			} else if (artistEquals && titreEquals) {
				nbEquals++;
			}
		}
		double ratio = new Double(nbEquals) / new Double(size);
		if (ratio >= 0.9 || (size - nbEquals) < 3) {
			LOG.warn("Equals !!! : " + ratio + " " + nbEquals + "/" + size);
			return true;
		} else {
			LOG.warn("Not the same list: " + ratio + " " + nbEquals + "/" + size);
			return false;
		}
	}

	private static Map<String, String> buildResultMap(String separator, boolean artistFirst, boolean reverseArtist,
			boolean parenthese, boolean upper, boolean removeAfter) {
		Map<String, String> result = new HashMap<>();
		result.put("separator", separator);
		result.put("artistFirst", Boolean.toString(artistFirst));
		result.put("reverseArtist", Boolean.toString(reverseArtist));
		result.put("parenthese", Boolean.toString(parenthese));
		result.put("upper", Boolean.toString(upper));
		result.put("removeAfter", Boolean.toString(removeAfter));
		return result;
	}

	public static String removeRank(String txt) {
		String result = txt;
		String res = StringUtils.trim(StringUtils.substringBefore(txt, Constant.DOT));
		if (StringUtils.isNumeric(res)) {
			result = StringUtils.substringAfter(txt, Constant.DOT);
		} else {
			result = StringUtils.substringAfterLast(txt, txt.split(" ")[0]);
		}
		return result;
	}
	
	public static int findLineNumber(List<String> randomLineAndLastLines) {
		int i = 0;
		String line = "";
		while (i < 6) {
			if (i != 3) {
				line = randomLineAndLastLines.get(i);
				if (!StringUtils.startsWith(line, Constant.COMMENT_PREFIX) && !StringUtils.isBlank(line) && line.length() >= 5) {
					break;
				}
			}
			i++;
		}
		return i;
	}
	
	public static String removeParenthe(String txt) {
		String result = txt;
		int countMatches = StringUtils.countMatches(txt, "(");
		if (countMatches == 1) {
			result = StringUtils.trim(StringUtils.substringBefore(txt, "("));
		}
		if (countMatches == 0) {
//			LOG.info("Pas de parenthèse, line: " + txt);
		} else if (countMatches > 1) {
			LOG.info("###Trop de parenthèses, line: " + txt);
		}
		return result;
	}

	public static String revertArtist(String txtArtist) {
		String artistRevert = "";
		String[] arrayArtist = txtArtist.split(",");
		if (arrayArtist.length == 2) {
			artistRevert = StringUtils.trim(StringUtils.trim(arrayArtist[1]) + " " + StringUtils.trim(arrayArtist[0]));
		} else if(arrayArtist.length > 2) {
			LOG.warn("Error when revert artist: " + txtArtist + ", size: " + arrayArtist.length);
		}
		return artistRevert;
	}

	private static void sizeFileSuspicious(List<Composition> importXML) {
//		// TODO tous les fichiers qui ont une taille non multiple de 10
//		importXML.stream().map(Composition::getFiles).flatMap(List::stream).filter(f -> f.getSize() % 10 != 0 && f.getSize() % 5 != 0)
//				.map(f->f.getFileName() + " " + f.getSize()).distinct().sorted().forEach(LOG::debug);
//		// TODO tous les fichiers dont le classement de la derniere composition est
//		// different de la taille
//		List<String> fileNames = importXML.stream().map(Composition::getFiles).flatMap(List::stream).filter(f->!f.getSorted())
//				.map(Fichier::getFileName).distinct().sorted().collect(Collectors.toList());
//		for (String name : fileNames) {
//			List<Composition> xml = ImportXML.importXML(Constant.XML_PATH + name + Constant.XML_EXTENSION);
//			xml.sort((a, b) -> Integer.valueOf(a.getFiles().get(0).getClassement())
//					.compareTo(Integer.valueOf(b.getFiles().get(0).getClassement())));
//			Composition lastCompo = xml.get(xml.size() - 1);
//			if (xml.get(0).getFiles().get(0).getSize() != 0
//					&& lastCompo.getFiles().get(0).getClassement() != xml.get(0).getFiles().get(0).getSize()) {
//				LOG.debug(name + ": " + lastCompo.getFiles().get(0).getClassement() + " "
//						+ xml.get(0).getFiles().get(0).getSize());
//			}
//		}
		// TODO missing compositions
		List<String> fileNames = importXML.stream().map(Composition::getFiles).flatMap(List::stream)
				.map(Fichier::getFileName).distinct().sorted().collect(Collectors.toList());
		for (String name : fileNames) {
			List<Composition> xml = ImportXML.importXML(Constant.XML_PATH + name + Constant.XML_EXTENSION);
			if (xml.get(0).getFiles().get(0).getSize() != 0 && xml.size() != xml.get(0).getFiles().get(0).getSize()) {
				LOG.debug(name + ": " + xml.size() + " " + xml.get(0).getFiles().get(0).getSize());
			}
		}
	}
	
	private static void gauss(List<Composition> importXML, RecordType type) {
		Map<Integer, Integer> map = new TreeMap<>();
		Map<String, String> criteria = new HashMap<>();
		criteria.put("sorted", Boolean.TRUE.toString());
		criteria.put("type", type.toString());
		List<Integer> yearList = SearchUtils.searchJaro(importXML, criteria, true).stream().map(Composition::getFiles)
				.flatMap(List::stream).map(Fichier::getClassement).collect(Collectors.toList());
		for (Integer rank : yearList) {
			if (map.containsKey(rank)) {
				map.replace(rank, map.get(rank) + 1);
			} else {
				map.put(rank, 1);
			}
		}
		LOG.debug("Value;Count");
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			LOG.debug(entry.getKey() + ";" + entry.getValue());
		}
	}

	private static void stats(List<Composition> importXML, RecordType type) {
		LOG.debug(type.toString());
		Map<String, String> criteria = new HashMap<>();
		criteria.put("type", type.toString());
		criteria.put("sorted", Boolean.TRUE.toString());
		List<Integer> yearList = SearchUtils.searchJaro(importXML, criteria, true).stream().map(Composition::getFiles)
				.flatMap(List::stream).map(Fichier::getClassement).collect(Collectors.toList());
		LOG.debug("Moyenne: " + yearList.stream().mapToInt(i -> i).average());
		LOG.debug("Stats: " + yearList.stream().mapToInt(i -> i).summaryStatistics());
		LOG.debug("Medianne: " + MiscUtils.median(yearList));
		LOG.debug("SD: " + MiscUtils.calculateSD(yearList, yearList.stream().mapToInt(i -> i).average(),
				yearList.stream().mapToInt(i -> i).sum(), yearList.stream().mapToInt(i -> i).count()));
	}

	public static String topRecordsByPoints(List<Composition> importXML, RecordType type, String fileName) {
		BigDecimal doubleMedian = CompositionUtils.getDoubleMedian(type);
		BigDecimal logMax = CompositionUtils.getLogMax(type);

		Map<String, String> criteria = new HashMap<>();
		criteria.put("type", type.toString());
		List<Composition> yearList = SearchUtils.searchJaro(importXML, criteria, true);

		Vector<Vector<Object>> result = new Vector<Vector<Object>>();
		for (Composition composition : yearList) {
			Vector<Object> vector = new Vector<Object>();
			vector.add(composition.getArtist());
			vector.add(composition.getTitre());
			vector.add(type.toString());
			vector.add(String.valueOf(composition.getFiles().stream().filter(f -> f.getCategorie() != Cat.YEAR)
					.findFirst().orElse(composition.getFiles().get(0)).getPublishYear()));
			long sumPts = CompositionUtils.calculateCompositionScore(logMax, doubleMedian, composition);
			if (sumPts > 0) {
				vector.add(sumPts);
				result.add(vector);
			}
		}
		return CsvFile.exportCsv(fileName, MiscUtils.convertVectorToList(result), new SortKey(3, SortOrder.DESCENDING),
				null);
	}

	public static void sortedFilesByYear() {
		for (int i = 1950; i <= 2017; i++) {
			List<Composition> importXML = ImportXML.importXML(Constant.FINAL_FILE_PATH);
			String year = String.valueOf(i);
			Map<String, String> criteria = new HashMap<>();
			criteria.put("cat", Cat.YEAR.toString());
			criteria.put("dateB", year);
			criteria.put("dateE", year);
			criteria.put("publish", year);
			criteria.put("sorted", Boolean.TRUE.toString());
			criteria.put("type", RecordType.SONG.toString());
			List<Composition> yearList = SearchUtils.searchJaro(importXML, criteria, true);			
//			LOG.debug("year: " + year + " size: " + yearList.size());
			LOG.debug("year: " + year + " file size: " + yearList.stream().map(Composition::getFiles).flatMap(List::stream).map(Fichier::getAuthor)
					.map(WordUtils::capitalize).distinct().count());
		}
	}

	/**
	 * Tests the recover of information from txt files.
	 */
	public static void randomLineTest() {
		List<File> files = new ArrayList<>();
		FichierUtils.listFilesForFolder(new File(Constant.MUSIC_ABS_DIRECTORY), files, Constant.TXT_EXTENSION, true);
		for (File file : files) {
			LOG.error(file.getName());
			Fichier fichier = ImportFile.convertOneFile(file);
			List<String> randomLineAndLastLines = ImportFile.randomLineAndLastLines(file);
			fichier.setSorted(ImportFile.isSorted(randomLineAndLastLines.get(3)));
			fichier.setSize(ImportFile.determineSize(fichier, randomLineAndLastLines, file.getAbsolutePath()));
			LOG.error(fichier.getSize());
		}
	}

	/**
	 * Merge similar txt files.
	 * 
	 * @param args
	 */
	public static void mergeFile(String[] args) {
		LOG.debug("Debut");
		File first = new File(Constant.MUSIC_ABS_DIRECTORY + "Rolling Stone\\Rolling Stone - 500 Albums.txt");
		File sec = new File(Constant.MUSIC_ABS_DIRECTORY + "Rolling Stone\\Rolling Stone - 500 Albums - 2012.txt");
		List<String> list = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(first), Constant.ANSI_ENCODING));) {
			String line;
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		JaroWinklerDistance jaro = new JaroWinklerDistance();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(sec), Constant.ANSI_ENCODING));) {
			String line;
			while ((line = br.readLine()) != null) {
				final String str = StringUtils.substringAfter(line, ". ");
				if (!list.stream().anyMatch(s -> StringUtils.equalsIgnoreCase(StringUtils.substringAfter(s, ". "), str))
						&& !list.stream().anyMatch(
								s -> CompositionUtils.artistJaroEquals(str, StringUtils.substringAfter(s, ". "), jaro,
										Constant.SCORE_LIMIT_ARTIST_FUSION) != null)) {
					list.add(line);
				}
			}
		} catch (IOException e1) {
		}
		list = list.stream().sorted((s1, s2) -> Integer.compare(Integer.valueOf(StringUtils.substringBefore(s1, ". ")),
				Integer.valueOf(StringUtils.substringBefore(s2, ". ")))).collect(Collectors.toList());
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(Constant.MUSIC_ABS_DIRECTORY + "RS.txt"), Constant.ANSI_ENCODING));) {
			for (String str : list) {
				writer.append(str).append(Constant.NEW_LINE);
			}
			writer.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		LOG.debug("Fin");
	}

	@Test
	public void removeParenthese() {
		String test1 = "hello (and truc)";
		String test2 = "(and truc) hello";
		String test3 = "hello, bonjour (and truc)";
		String test4 = "(and truc) hello, bonjour";
		String test5 = "hello, bonjour";
		String test6 = "hello, bonjour";
		String test7 = "hello, bonjour [and thed]";
		String test8 = "[and thed] hello, bonjour";

		Assert.assertEquals("hello ", SearchUtils.removeParentheses(test1));
		Assert.assertEquals(" hello", SearchUtils.removeParentheses(test2));
		Assert.assertEquals("hello, bonjour ", SearchUtils.removeParentheses(test3));
		Assert.assertEquals(" hello, bonjour", SearchUtils.removeParentheses(test4));
		Assert.assertEquals("hello, bonjour", SearchUtils.removeParentheses(test5));
		Assert.assertEquals("hello, bonjour", SearchUtils.removeParentheses(test6));
		Assert.assertEquals("hello, bonjour ", SearchUtils.removeParentheses(test7));
		Assert.assertEquals(" hello, bonjour", SearchUtils.removeParentheses(test8));
	}

	@Test
	public void removeParentheseAndPunctuation() {
		String test1 = "hello (and truc)";
		String test2 = "(and truc) hello";
		String test3 = "hello, bonjour (and truc)";
		String test4 = "(and truc) hello, bonjour";
		String test5 = "hello, bonjour";
		String test6 = "hello, bonjour";
		String test7 = "hello, bonjour [and thed]";
		String test8 = "[and thed] hello, bonjour";

		Assert.assertEquals("hello", SearchUtils.removePunctuation2(SearchUtils.removeParentheses(test1)));
		Assert.assertEquals("hello", SearchUtils.removePunctuation2(SearchUtils.removeParentheses(test2)));
		Assert.assertEquals("hellobonjour", SearchUtils.removePunctuation2(SearchUtils.removeParentheses(test3)));
		Assert.assertEquals("hellobonjour", SearchUtils.removePunctuation2(SearchUtils.removeParentheses(test4)));
		Assert.assertEquals("hellobonjour", SearchUtils.removePunctuation2(SearchUtils.removeParentheses(test5)));
		Assert.assertEquals("hellobonjour", SearchUtils.removePunctuation2(SearchUtils.removeParentheses(test6)));
		Assert.assertEquals("hellobonjour", SearchUtils.removePunctuation2(SearchUtils.removeParentheses(test7)));
		Assert.assertEquals("hellobonjour", SearchUtils.removePunctuation2(SearchUtils.removeParentheses(test8)));
	}

	public static void setCanBeMerged() {
		List<Composition> importXML = ImportXML.importXML(Constant.FINAL_FILE_PATH);
		importXML.stream().map(Composition::getFiles).flatMap(List::stream).map(Fichier::getFileName).distinct()
				.forEach(fileName -> {
					List<Composition> importFile = ImportXML
							.importXML(Constant.XML_PATH + fileName + Constant.XML_EXTENSION);
					List<Composition> newImportFile = importFile.stream().map(compo -> {
						compo.setCanBeMerged(true);
						return compo;
					}).collect(Collectors.toList());
					try {
						ExportXML.exportXML(newImportFile, fileName);
					} catch (IOException e) {
						LOG.error("Erreur lors de l'export du fichier: " + fileName);
					}
				});
		List<Composition> newImportXML = importXML.stream().map(compo -> {
			compo.setCanBeMerged(true);
			return compo;
		}).collect(Collectors.toList());
		try {
			ExportXML.exportXML(newImportXML, Constant.FINAL_FILE);
		} catch (IOException e) {
			LOG.error("Erreur lors de l'export du fichier final");
		}
	}

	@Test
	public void detectUpper() {
		String test = "05. GERRY & THE PACEMAKERS Ferry Cross the Mersey";
		boolean sorted = true;
		char[] array = test.toCharArray();
		int cut = 0;
		for (int i = 0; i < array.length; i++) {
			if (Character.isUpperCase(array[i]) || !Character.isAlphabetic(array[i])) {
				LOG.debug("upper: " + array[i]);
			} else {
				cut = i - 1;
				LOG.debug("else: " + array[i]);
				break;
			}
		}
		String artist = StringUtils.substring(test, 0, cut);
		String titre = StringUtils.substring(test, cut);
		LOG.debug("cut: " + cut);
		LOG.debug("artist: " + artist);
		LOG.debug("titre: " + titre);
		int rank = 0;
		if (sorted) {
			String res = StringUtils.substringBefore(artist, Constant.DOT);
			if (StringUtils.isNumeric(res)) {
				rank = Integer.parseInt(res);
				artist = StringUtils.substringAfter(artist, Constant.DOT);
			} else {
				res = artist.split(" ")[0];
				rank = Integer.parseInt(res);
				artist = StringUtils.substringAfterLast(artist, res);
			}
		} else {
			rank = 1;
		}
		LOG.debug("rank: " + rank);
	}

	@Test
	public void detectQuote() {
		String test = "05. GERRY & THE PACEMAKERS Ferry Cross the Mersey";
		boolean sorted = true;
		char[] array = test.toCharArray();
		int cut = 0;
		for (int i = 0; i < array.length; i++) {
			if (Character.isUpperCase(array[i]) || !Character.isAlphabetic(array[i])) {
				LOG.debug("upper: " + array[i]);
			} else {
				cut = i - 1;
				LOG.debug("else: " + array[i]);
				break;
			}
		}
		String artist = StringUtils.substring(test, 0, cut);
		String titre = StringUtils.substring(test, cut);
		LOG.debug("cut: " + cut);
		LOG.debug("artist: " + artist);
		LOG.debug("titre: " + titre);
		int rank = 0;
		if (sorted) {
			String res = StringUtils.substringBefore(artist, Constant.DOT);
			if (StringUtils.isNumeric(res)) {
				rank = Integer.parseInt(res);
				artist = StringUtils.substringAfter(artist, Constant.DOT);
			} else {
				res = artist.split(" ")[0];
				rank = Integer.parseInt(res);
				artist = StringUtils.substringAfterLast(artist, res);
			}
		} else {
			rank = 1;
		}
		LOG.debug("rank: " + rank);
	}

	@Test
	public void distanceJaro() {
		List<Composition> guardian = ImportXML.importXML(Constant.FINAL_FILE_PATH);
		String test = "beachboys";
		Map<Double, String> jaroRes = new TreeMap<>();
		JaroWinklerDistance jaro = new JaroWinklerDistance();
		for (Composition composition : guardian) {
			String titre = SearchUtils.removePunctuation2(composition.getArtist());
			Double apply = jaro.apply(titre, test);
			jaroRes.put(apply, titre);
		}
		for (Entry<Double, String> entry : jaroRes.entrySet()) {
			LOG.debug("Key : " + entry.getKey() + " Value : " + entry.getValue());
		}
	}

	@Test
	public void distanceJaroLine() {
		String s1 = "Fun House - The Stooges";
		String s2 = "Funhouse - The Stooges";
		JaroWinklerDistance jaro = new JaroWinklerDistance();
		Double apply = jaro.apply(s1, s2);
		LOG.debug("Key : " + s1 + " Value : " + s2 + " " + apply);
	}
}
