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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.log4j.Logger;
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
import pmb.music.AllMusic.utils.SearchUtils;

/**
 * Unit test for simple App.
 */
public class AppTest {

	private static final Logger LOG = Logger.getLogger(AppTest.class);

	public static void main(String[] args) {
		List<Composition> importXML = ImportXML.importXML(Constant.FINAL_FILE_PATH);
		topRecordsByPoints(importXML, RecordType.SONG, "Top All Years Songs");
		topRecordsByPoints(importXML, RecordType.ALBUM, "Top All Years Albums");
		stats(importXML, RecordType.ALBUM);
//		stats(importXML, RecordType.SONG);
		gauss(importXML, RecordType.ALBUM);
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
		SortKey sortKey = new SortKey(3, SortOrder.DESCENDING);
		return CsvFile.writeCsvFromSearchResult(result, fileName, "", sortKey);
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

	/**
	 * Show all the duplicates for a year and a type regardless of the artist, only
	 * based on the song or album.
	 * 
	 * @deprecated replaced by {@link AppTest#detectsDuplicateFinal(String)}
	 */
	public static void detectsDuplicate(String type, int YEAR_TOP) {
		LOG.debug("Debut detectsDuplicate");
		final JaroWinklerDistance jaro = new JaroWinklerDistance();
		List<Composition> importXML = ImportXML.importXML(Constant.FINAL_FILE_PATH);
		if (CollectionUtils.isNotEmpty(importXML)) {
			// String year = String.valueOf(YEAR_TOP);
			Map<String, String> criteria = new HashMap<>();
			criteria.put("cat", Cat.YEAR.toString());
			// criteria.put("dateB", year);
			// criteria.put("dateE", year);
			// criteria.put("publish", year);
			criteria.put("type", type);
			List<Composition> yearList = SearchUtils.searchJaro(importXML, criteria, true);
			LOG.debug("Size: " + yearList.size());
			List<Integer[]> duplicate = new ArrayList<Integer[]>();
			for (int i = 0; i < yearList.size(); i++) {
				for (int j = 0; j < yearList.size(); j++) {
					if (i < j) {
						Composition composition1 = yearList.get(i);
						Composition composition2 = yearList.get(j);
						String titre1 = composition1.getTitre();
						String titre2 = composition2.getTitre();
						String newTitre1 = SearchUtils.removePunctuation2(titre1);
						String newTitre2 = SearchUtils.removePunctuation2(titre2);
						String artist1 = composition1.getArtist();
						String artist2 = composition2.getArtist();
						int publishYear1 = composition1.getFiles().get(0).getPublishYear();
						int publishYear2 = composition2.getFiles().get(0).getPublishYear();
						boolean result = (SearchUtils.isEqualsJaro(jaro, newTitre1, newTitre2,
								Constant.SCORE_LIMIT_TITLE_FUSION) || StringUtils.startsWithIgnoreCase(titre1, titre2)
								|| StringUtils.startsWithIgnoreCase(titre2, titre1))
								&& (StringUtils.startsWithIgnoreCase(artist1, artist2)
										|| StringUtils.startsWithIgnoreCase(artist2, artist1))
								&& publishYear1 == publishYear2;
						if (result) {
							duplicate.add(new Integer[] { i, j });
						}
					}
				}
			}
			LOG.debug("Size: " + duplicate.size());
			for (Integer[] integers : duplicate) {
				LOG.debug("###########################################");
				LOG.debug(yearList.get(integers[0]));
				LOG.debug(yearList.get(integers[1]));
			}
		}
		LOG.debug("Fin detectsDuplicate");
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
