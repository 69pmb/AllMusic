package pmb.music.AllMusic.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.file.CsvFile;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.model.SearchMethod;
import pmb.music.AllMusic.model.SearchRange;
import pmb.music.AllMusic.view.panel.BatchPanel;
import pmb.music.AllMusic.view.panel.OngletPanel;

public class BatchUtils {
	private static final Logger LOG = Logger.getLogger(BatchUtils.class);

	public static String detectsDuplicateFinal(boolean song, boolean album, boolean ignoreUnmergeableFiles,
			boolean byYear) {
		LOG.debug("Start detectsDuplicateFinal");
		StringBuilder result = new StringBuilder();
		addLine(result, "DetectsDuplicateFinal: ", true);
		addLine(result, "Song: " + song, true);
		addLine(result, "Album: " + album, true);
		addLine(result, "Ignore Unmergeable Files: " + ignoreUnmergeableFiles, true);

		if (song) {
			detectsDuplicateFinal(RecordType.SONG.toString(), ignoreUnmergeableFiles, byYear, result);
		}
		if (album) {
			detectsDuplicateFinal(RecordType.ALBUM.toString(), ignoreUnmergeableFiles, byYear, result);
		}
		try {
			ImportXML.synchroDeletedWithFinal();
		} catch (MyException e) {
			LOG.error("Erreur lors de la détection de composition supprimées", e);
			addLine(result, e.toString(), true);
		}

		LOG.debug("End detectsDuplicateFinal");
		return writeInFile(result, Constant.BATCH_FILE);
	}

	/**
	 * Generates statistics of xml files.
	 * 
	 * @return
	 */
	public static String stat() {
		LOG.debug("Start stat");
		StringBuilder result = new StringBuilder();
		addLine(result, "Statistiques sur la longueur artist + titre: ", true);
		List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
		List<Integer> size = importXML.stream().map(composition -> {
			String s = composition.getArtist() + composition.getTitre();
			return s.length();
		}).collect(Collectors.toList());
		addLine(result, "Min: " + size.stream().mapToInt(Integer::intValue).min().getAsInt(), true);
		addLine(result, "Max: " + size.stream().mapToInt(Integer::intValue).max().getAsInt(), true);
		addLine(result, "Moyenne: " + size.stream().mapToInt(Integer::intValue).average().getAsDouble(), true);
		addLine(result, "Summary: " + size.stream().mapToInt(Integer::intValue).summaryStatistics(), true);

		addLine(result, "Statistiques sur les tops annuels: ", true);
		Map<Integer, Integer> songs = importXML.stream().filter(c -> c.getRecordType().equals(RecordType.SONG))
				.map(Composition::getFiles).flatMap(List::stream).filter(f -> f.getCategorie().equals(Cat.YEAR))
				.collect(Collectors.groupingBy(Fichier::getRangeDateBegin, Collectors
						.collectingAndThen(Collectors.mapping(Fichier::getFileName, Collectors.toSet()), Set::size)));
		Map<Integer, Integer> albums = importXML.stream().filter(c -> c.getRecordType().equals(RecordType.ALBUM))
				.map(Composition::getFiles).flatMap(List::stream).filter(f -> f.getCategorie().equals(Cat.YEAR))
				.collect(Collectors.groupingBy(Fichier::getRangeDateBegin, Collectors
						.collectingAndThen(Collectors.mapping(Fichier::getFileName, Collectors.toSet()), Set::size)));
		int min = Stream.concat(songs.keySet().stream(), albums.keySet().stream()).mapToInt(Integer::intValue).min()
				.getAsInt();
		int max = Stream.concat(songs.keySet().stream(), albums.keySet().stream()).mapToInt(Integer::intValue).max()
				.getAsInt();
		IntStream.rangeClosed(min, max).forEach(i -> {
			Integer song = !songs.containsKey(i) ? 0 : songs.get(i);
			Integer album = !albums.containsKey(i) ? 0 : albums.get(i);
			addLine(result,
					i + ": Songs: " + song.toString() + ", Albums: " + album.toString() + ", Total: " + (song + album),
					true);
		});

		LOG.debug("End stat");
		return writeInFile(result, Constant.BATCH_FILE);
	}

	public static String findUnknown() {
		LOG.debug("Start findUnknown");
		Comparator<String> byFileName = (String o1, String o2) -> {
			String s1 = StringUtils.substringBeforeLast(o1, ";");
			String s2 = StringUtils.substringBeforeLast(o2, ";");
			return s1.compareToIgnoreCase(s2);
		};
		Comparator<String> byCount = (String o1, String o2) -> {
			Long r1 = Long.valueOf(StringUtils.substringAfterLast(o1, ";"));
			Long r2 = Long.valueOf(StringUtils.substringAfterLast(o2, ";"));
			return Long.compare(r1, r2);
		};
		Map<String, List<String>> result = new TreeMap<>(byFileName.thenComparing(byCount.reversed()));
		StringBuilder sb = new StringBuilder();
		addLine(sb, "Find Unknown: ", true);
		Map<String, String> criteria = new HashMap<>();
		criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, RecordType.UNKNOWN.toString());
		List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
		List<Composition> unknown = SearchUtils.search(importXML, criteria, true, SearchMethod.WHOLE_WORD, true, true);
		AtomicInteger notFound = new AtomicInteger(0);
		unknown.parallelStream().forEach(u -> {
			Map<String, String> c = new HashMap<>();
			c.put(SearchUtils.CRITERIA_ARTIST, u.getArtist());
			c.put(SearchUtils.CRITERIA_TITRE, u.getTitre());
			List<Composition> search = SearchUtils.search(importXML, c, true, SearchMethod.CONTAINS, true, false);
			String fileName = u.getFiles().get(0).getFileName();
			String artistTitre = u.getArtist() + " - " + u.getTitre();
			String item;
			List<RecordType> types = search.stream().map(Composition::getRecordType)
					.filter(t -> !t.equals(RecordType.UNKNOWN)).collect(Collectors.toList());
			if (!types.isEmpty()) {
				long songCount = search.stream().filter(s -> s.getRecordType().equals(RecordType.SONG))
						.mapToInt(s -> s.getFiles().size()).sum();
				long albumCount = search.stream().filter(s -> s.getRecordType().equals(RecordType.ALBUM))
						.mapToInt(s -> s.getFiles().size()).sum();
				Long count;
				if (types.stream().allMatch(type -> RecordType.ALBUM.equals(type))) {
					item = artistTitre + ": " + RecordType.ALBUM + " (" + albumCount + ")";
					count = albumCount;
				} else if (types.stream().allMatch(type -> RecordType.SONG.equals(type))) {
					item = artistTitre + ": " + RecordType.SONG + " (" + songCount + ")";
					count = songCount;
				} else {
					item = artistTitre + Constant.NEW_LINE + RecordType.SONG + ": " + songCount + Constant.NEW_LINE
							+ RecordType.ALBUM + ": " + albumCount;
					count = (songCount + albumCount) / 2;
				}
				String key = fileName + ";" + count;
				if (result.containsKey(key)) {
					result.get(key).add(item);
				} else {
					result.put(key, new LinkedList<String>(Arrays.asList(item)));
				}
			} else {
				notFound.incrementAndGet();
			}
		});
		String currentKey = "";
		for (Entry<String, List<String>> e : result.entrySet()) {
			String key = StringUtils.substringBeforeLast(e.getKey(), ";");
			if (!StringUtils.equalsIgnoreCase(currentKey, key)) {
				sb.append(Constant.NEW_LINE + "### " + key + ": " + Constant.NEW_LINE);
				currentKey = key;
			}
			e.getValue().stream().forEach(v -> sb.append(v + Constant.NEW_LINE));
		}
		sb.append(Constant.NEW_LINE + "### Not Found: " + notFound.get());
		LOG.debug("End findUnknown");
		return writeInFile(sb, "Unknown.txt");
	}

	public static String findSuspiciousComposition() {
		LOG.debug("Start findSuspiciousComposition");
		StringBuilder result = new StringBuilder();
		addLine(result, "Suspicious: ", true);

		List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
		emptyTitleOrArtist(importXML, result);
		titleSlash(importXML, result);
		sizeZero(importXML, result);
		publishZero(importXML, result);
		rankZero(importXML, result);
		rankGreaterThanSize(importXML, result);
		duplicateCompositionInFile(importXML, result);

		LOG.debug("End findSuspiciousComposition");
		return writeInFile(result, "Suspicious.txt");
	}

	public static String findDuplicateTitleComposition() {
		LOG.debug("Start findDuplicateTitleComposition");
		StringBuilder result = new StringBuilder();
		addLine(result, "Duplicate Title: ", true);

		similarTitle(result);

		LOG.debug("End findDuplicateTitleComposition");
		return writeInFile(result, "Duplicate Title.txt");
	}

	public static String findIncorrectFileNames() {
		LOG.debug("Start findIncorrectFileNames");
		StringBuilder result = new StringBuilder();
		addLine(result, "IncorrectFileNames: ", true);

		List<String> authorList = OngletPanel.getAuthorList();
		List<String> res = new ArrayList<>();
		List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
		authorList.parallelStream().forEach(author -> {
			if (StringUtils.equalsIgnoreCase(author, "Divers")) {
				return;
			}
			Map<String, String> criteria = new HashMap<>();
			criteria.put(SearchUtils.CRITERIA_AUTHOR, author);
			res.addAll(SearchUtils.search(importXML, criteria, true, SearchMethod.CONTAINS, false, false)
					.parallelStream().map(Composition::getFiles).flatMap(List::stream)
					.filter(f -> (!StringUtils.startsWithIgnoreCase(f.getFileName(), f.getAuthor() + " - ")
							|| !StringUtils.endsWithIgnoreCase(f.getFileName(),
									" - " + String.valueOf(f.getPublishYear()))))
					.map(f -> f.getFileName() + " # " + String.valueOf(f.getPublishYear())).distinct().sorted()
					.collect(Collectors.toList()));
		});
		res.stream().forEach(f -> addLine(result, f, false));

		LOG.debug("End findIncorrectFileNames");
		return writeInFile(result, "Incorrect Filenames.txt");
	}

	private static void titleSlash(List<Composition> importXML, StringBuilder result) {
		addLine(result, "## Title Slash: ", true);
		importXML.stream().map(c -> {
			if (c.getFiles().size() == 1 && StringUtils.contains(c.getTitre(), "/")) {
				return c.getArtist() + " - " + c.getTitre();
			} else {
				return "";
			}
		}).distinct().filter(line -> StringUtils.isNotBlank(line)).sorted()
				.forEach(line -> addLine(result, line, false));
	}

	private static void similarTitle(StringBuilder result) {
		addLine(result, "## Same title but different artist: ", true);
		JaroWinklerDistance jaro = new JaroWinklerDistance();
		Map<String, String> criteria = new HashMap<>();
		criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, RecordType.SONG.toString());
		List<Composition> importXML = SearchUtils
				.search(ImportXML.importXML(Constant.getFinalFilePath()), criteria, true, SearchMethod.CONTAINS, false,
						false)
				.stream().sorted((c1, c2) -> StringUtils.compareIgnoreCase(c1.getTitre(), c2.getTitre()))
				.collect(Collectors.toList());
		for (int i = 0; i < importXML.size(); i++) {
			for (int j = 0; j < importXML.size(); j++) {
				if (i < j) {
					Composition c1 = importXML.get(i);
					Composition c2 = importXML.get(j);
					String c1Titre = SearchUtils.removePunctuation(c1.getTitre());
					String c2Titre = SearchUtils.removePunctuation(c2.getTitre());
					if (c1Titre.length() < 11 || c2Titre.length() < 11) {
						continue;
					}
					if (SearchUtils.isEqualsJaro(jaro, c1Titre, c2Titre, BigDecimal.valueOf(0.985D))
							&& CompositionUtils.artistJaroEquals(c1.getArtist(), c2.getArtist(), jaro,
									Constant.SCORE_LIMIT_ARTIST_FUSION) == null) {
						addLine(result, c1.getArtist() + " - " + c1.getTitre() + " // " + c2.getArtist() + " - "
								+ c2.getTitre(), false);
					}
				}
			}
		}
	}

	private static void rankZero(List<Composition> importXML, StringBuilder result) {
		addLine(result, "## Rank Zero: ", true);
		importXML
				.stream().filter(
						c -> c.getFiles().stream().anyMatch(f -> f.getClassement() == 0))
				.forEach(c -> addLine(result,
						c.getArtist() + " - " + c.getTitre() + " / "
								+ StringUtils.join(c.getFiles().stream().filter(f -> f.getClassement() == 0)
										.map(Fichier::getFileName).collect(Collectors.toList()), ","),
						false));
	}

	private static void rankGreaterThanSize(List<Composition> importXML, StringBuilder result) {
		addLine(result, "## Rank Greater Than Size: ", true);
		importXML
				.stream().map(Composition::getFiles).flatMap(List::stream).filter(
						f -> f.getClassement() > f.getSize() && f.getSize() != 0)
				.collect(
						Collectors
								.groupingBy(Fichier::getFileName,
										Collectors
												.collectingAndThen(
														Collectors
																.reducing((Fichier d1,
																		Fichier d2) -> d1.getClassement() > d2
																				.getClassement() ? d1 : d2),
														Optional::get)))
				.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.forEach(f -> addLine(result, f.getValue().getFileName() + ", size: " + f.getValue().getSize()
						+ ", classement max: " + f.getValue().getClassement(), false));
	}

	private static void duplicateCompositionInFile(List<Composition> importXML, StringBuilder result) {
		addLine(result, "## Duplicate Composition In File: ", true);
		importXML.parallelStream().forEach(c -> {
			Map<String, Long> collect = c.getFiles().stream()
					.collect(Collectors.groupingBy(Fichier::getFileName, Collectors.counting()));
			if (collect.values().stream().anyMatch(v -> v > 1)) {
				collect.entrySet().stream().forEach(e -> {
					if (e.getValue() > 1) {
						addLine(result, c.getArtist() + ", " + c.getTitre() + ". " + e.getKey() + ": " + e.getValue(),
								false);
					}
				});
			}
		});
	}

	private static void sizeZero(List<Composition> importXML, StringBuilder result) {
		addLine(result, "## File Size Zero: ", true);
		importXML.stream().map(Composition::getFiles).flatMap(List::stream).filter(f -> f.getSize() == 0)
				.map(Fichier::getFileName).distinct().sorted().forEach(f -> addLine(result, f, false));
	}

	private static void publishZero(List<Composition> importXML, StringBuilder result) {
		addLine(result, "## File Publish Year Zero: ", true);
		importXML.stream().map(Composition::getFiles).flatMap(List::stream).filter(f -> f.getPublishYear() == 0)
				.map(Fichier::getFileName).distinct().sorted().forEach(f -> addLine(result, f, false));
	}

	private static void emptyTitleOrArtist(List<Composition> importXML, StringBuilder result) {
		addLine(result, "## Empty Title or Artist: ", true);
		importXML.stream().forEach(c -> {
			if (StringUtils.equalsIgnoreCase(StringUtils.trim(c.getTitre()), "")
					|| StringUtils.equalsIgnoreCase(StringUtils.trim(c.getArtist()), "")) {
				addLine(result, c.getArtist() + " - " + c.getTitre(), false);
			}
		});
	}

	public static String averageOfFilesByFiles(BatchPanel batchPanel) {
		LOG.debug("Start averageOfFilesByFiles");
		StringBuilder text = new StringBuilder();
		addLine(text, "Start AverageOfFilesByFiles", true);
		// Moyenne par fichier du nombre de fichiers de chaque composition
		List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
		List<String> nomFichier = importXML.stream().map(Composition::getFiles).flatMap(List::stream)
				.map(Fichier::getFileName).distinct().sorted().collect(Collectors.toList());
		String[] header = { "Fichier", "Type", "Cat", "Average" };
		List<List<String>> result = new ArrayList<>();
		final AtomicInteger count = new AtomicInteger(0);
		final BigDecimal total = new BigDecimal(nomFichier.size());
		nomFichier.parallelStream().forEach((name) -> {
			List<String> row = new ArrayList<>();
			Map<String, String> criteria = new HashMap<>();
			criteria.put(SearchUtils.CRITERIA_FILENAME, name);
			List<Composition> xml = SearchUtils.search(importXML, criteria, false, SearchMethod.WHOLE_WORD, true,
					false);
			row.add(name);
			row.add(xml.get(0).getRecordType().toString());
			row.add(xml.get(0).getFiles().stream().filter(f -> StringUtils.equalsIgnoreCase(f.getFileName(), name))
					.findFirst().get().getCategorie().toString());
			row.add(NumberFormat.getNumberInstance()
					.format(xml.stream().map(c -> c.getFiles().size()).mapToInt(x -> x).average().getAsDouble()));
			result.add(row);
			if (count.incrementAndGet() % 10 == 0) {
				batchPanel.displayText(
						BigDecimal.valueOf(100D).setScale(2).multiply(new BigDecimal(count.get()))
								.divide(total, RoundingMode.HALF_UP).doubleValue() + "%",
						count.get() == 10 ? false : true);
			}
		});
		CsvFile.exportCsv("Average", result, new SortKey(3, SortOrder.ASCENDING), header);
		LOG.debug("End averageOfFilesByFiles");
		addLine(text, "End AverageOfFilesByFiles", true);
		return writeInFile(text, Constant.BATCH_FILE);
	}

	public static String weirdFileSize() {
		LOG.debug("Start weirdFileSize");
		StringBuilder text = new StringBuilder();
		addLine(text, "Start weirdFileSize", true);
		// Moyenne par fichier du nombre de fichiers de chaque composition
		List<String> nomFichier = ImportXML.importXML(Constant.getFinalFilePath()).stream().map(Composition::getFiles)
				.flatMap(List::stream).map(Fichier::getFileName).distinct().sorted().collect(Collectors.toList());
		String[] header = { "Fichier", "Type", "Real Size", "Theoric Size", "Ratio" };
		List<List<String>> result = new ArrayList<>();
		nomFichier.parallelStream().forEach((name) -> {
			List<Composition> xml = ImportXML.importXML(Constant.getXmlPath() + name + Constant.XML_EXTENSION);
			int realSize = xml.size();
			Integer theoricSize = xml.get(0).getFiles().get(0).getSize();
			if (theoricSize != 0 && realSize != theoricSize) {
				BigDecimal ratio = BigDecimal.valueOf(realSize).multiply(BigDecimal.valueOf(100D))
						.divide(BigDecimal.valueOf(theoricSize), BigDecimal.ROUND_DOWN);
				List<String> row = new ArrayList<>();
				row.add(name);
				row.add(xml.get(0).getRecordType().toString());
				row.add(NumberFormat.getNumberInstance().format(realSize));
				row.add(NumberFormat.getNumberInstance().format(theoricSize));
				row.add(NumberFormat.getNumberInstance().format(ratio.doubleValue()));
				result.add(row);
			}
		});
		CsvFile.exportCsv("Weird", result, new SortKey(4, SortOrder.ASCENDING), header);
		LOG.debug("End weirdOfFilesByFiles");
		addLine(text, "End weirdFileSize", true);
		return writeInFile(text, Constant.BATCH_FILE);
	}

	/**
	 * Search if a composition has similar files (same author and same rank).
	 * 
	 * @return
	 */
	public static String findDuplicateFiles() {
		LOG.debug("Start findDuplicateFiles");
		StringBuilder text = new StringBuilder();
		addLine(text, "FindDuplicateFiles: ", true);

		Map<String, Integer> result = new HashMap<String, Integer>();
		List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
		for (Composition composition : importXML) {
			for (int i = 0; i < composition.getFiles().size(); i++) {
				for (int j = 0; j < composition.getFiles().size(); j++) {
					if (i > j) {
						Fichier f1 = composition.getFiles().get(i);
						Fichier f2 = composition.getFiles().get(j);
						if (f1.getClassement() == f2.getClassement()
								&& StringUtils.equalsIgnoreCase(f1.getAuthor(), f2.getAuthor())) {
							String key = f1.getFileName() + ", " + f2.getFileName();
							if (!result.containsKey(key)) {
								result.put(key, 1);
							} else {
								result.put(key, result.get(key) + 1);
							}
						}
					}
				}
			}
		}
		result.keySet().stream().sorted().forEach(key -> {
			if (result.get(key) > 3) {
				addLine(text, key + ": " + result.get(key), true);
			}
		});

		LOG.debug("End findDuplicateFiles");
		return writeInFile(text, Constant.BATCH_FILE);
	}

	/**
	 * Search if there are txt files which are not convert to xml files.
	 * 
	 * @return
	 */
	public static String missingXML() {
		LOG.debug("Start missingXML");
		StringBuilder text = new StringBuilder();
		addLine(text, "MissingXML: ", true);

		// Recupère tous les nom des fichiers txt
		List<File> music = new ArrayList<>();
		FichierUtils.listFilesForFolder(new File(Constant.getMusicAbsDirectory()), music, Constant.TXT_EXTENSION, true);
		List<String> collectMusic = music.stream().map(File::getName)
				.map(s -> StringUtils.substringBeforeLast(s, Constant.TXT_EXTENSION)).collect(Collectors.toList());

		// Recupère tous les nom des fichiers xml
		List<File> xml = new ArrayList<>();
		FichierUtils.listFilesForFolder(new File(Constant.getXmlPath()), xml, Constant.XML_EXTENSION, true);
		List<String> collectXml = xml.stream().map(File::getName)
				.map(s -> StringUtils.substringBeforeLast(s, Constant.XML_EXTENSION)).collect(Collectors.toList());

		addLine(text, "TXT: ", true);
		for (String txt : collectMusic) {
			if (!collectXml.stream().anyMatch(s -> StringUtils.equalsAnyIgnoreCase(s, txt))) {
				addLine(text, "Missing: " + txt, true);
				LOG.debug("Missing: " + txt);
			}
		}
		addLine(text, "XML: ", true);
		for (String xmlFile : collectXml) {
			if (!collectMusic.stream().anyMatch(s -> StringUtils.equalsAnyIgnoreCase(s, xmlFile))) {
				addLine(text, "Missing: " + xmlFile, true);
				LOG.debug("Missing: " + xmlFile);
			}
		}

		LOG.debug("End missingXML");
		return writeInFile(text, Constant.BATCH_FILE);
	}

	public static String topYear(int yearBegin, int yearEnd, int albumLimit, int songLimit, boolean deleted) {
		LOG.debug("Start topYear");
		StringBuilder text = new StringBuilder();
		addLine(text, "Top Year: ", true);
		addLine(text, "Year Begin: " + yearBegin, true);
		addLine(text, "Year End: " + yearEnd, true);
		addLine(text, "Album Limit: " + albumLimit, true);
		addLine(text, "Song Limit: " + songLimit, true);

		List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
		for (int i = yearBegin; i <= yearEnd; i++) {
			topYear(i, importXML, albumLimit, songLimit, deleted, text);
		}
		if (yearBegin == 0 && yearEnd == 0) {
			topYear(0, importXML, albumLimit, songLimit, deleted, text);
		}

		LOG.debug("End topYear");
		return writeInFile(text, Constant.BATCH_FILE);
	}

	/**
	 * Supprime tous les fichiers historisés sauf le plus récent.
	 * 
	 * @return
	 */
	public static String cleanHistory() {
		LOG.debug("Start cleanHistory");
		StringBuilder text = new StringBuilder();
		addLine(text, "Clean History: ", true);

		// Création d'une map avec:
		// key nom du fichier sans date
		// value liste des dates du fichier
		List<File> files = new ArrayList<>();
		FichierUtils.listFilesForFolder(new File(Constant.getHistoryPath()), files, Constant.XML_EXTENSION, false);
		int size = files.size();
		Map<String, List<Date>> list = new HashMap<String, List<Date>>();
		for (File file : files) {
			if (!StringUtils.endsWithIgnoreCase(file.getName(), Constant.XML_EXTENSION)) {
				continue;
			}
			String nomFichier = StringUtils.substringBefore(file.getName(), Constant.SEPARATOR_DATE_HISTORY);
			String date = StringUtils.substringBetween(file.getName(), Constant.SEPARATOR_DATE_HISTORY,
					Constant.XML_EXTENSION);
			if (list.get(nomFichier) == null) {
				list.put(nomFichier, new ArrayList<Date>());
			}
			try {
				list.get(nomFichier).add(new Constant().getSdfHistory().parse(date));
			} catch (ParseException e) {
				addLine(text, "Erreur lors du parsing d'une date" + e.getMessage(), true);
				LOG.error("Erreur lors du parsing d'une date", e);
			}
		}

		Set<String> keySet = list.keySet();
		for (String key : keySet) {
			// Tri des dates, la plus récente en 1er
			Collections.sort(list.get(key), Collections.reverseOrder());
			String path = Constant.getHistoryPath() + key + Constant.SEPARATOR_DATE_HISTORY;
			// Suppression des fichiers sauf du 1er
			for (int i = 1; i < list.get(key).size(); i++) {
				String toDelete = path + new Constant().getSdfHistory().format(list.get(key).get(i))
						+ Constant.XML_EXTENSION;
				if (!new File(toDelete).delete()) {
					addLine(text, toDelete + " n'a pas pu etre supprimé", true);
					LOG.error(toDelete + " n'a pas pu etre supprimé");
				}
			}
		}
		files = new ArrayList<>();
		FichierUtils.listFilesForFolder(new File(Constant.getHistoryPath()), files, Constant.XML_EXTENSION, false);
		int result = size - files.size();
		addLine(text, "Nombres de fichiers avant: " + size, true);
		addLine(text, "Nombres de fichiers après: " + files.size(), true);
		addLine(text, "Nombres de fichiers supprimés: " + result, true);

		LOG.debug("End cleanHistory");
		return writeInFile(text, Constant.BATCH_FILE);
	}

	/**
	 * Show all the duplicates for a year and a type regardless of the artist, only
	 * based on the song or album.
	 */
	private static void detectsDuplicateFinal(String type, boolean ignoreUnmergeableFiles, boolean byYear,
			StringBuilder result) {
		LOG.debug("Start detectsDuplicateFinal");
		double startTime = System.currentTimeMillis();
		final JaroWinklerDistance jaro = new JaroWinklerDistance();
		int i = 0;
		while ((!byYear && findFirstDuplicate(type, jaro, ignoreUnmergeableFiles, result))
				|| (byYear && detectsDuplicate(type, jaro, result))) {
			i++;
		}
		double endTime = System.currentTimeMillis();
		addLine(result, "Time: " + (endTime - startTime) / 1000 + " secondes", true);
		addLine(result, "Nombre de compositions fusionnées: " + i, true);
		LOG.debug("End detectsDuplicateFinal");
	}

	private static boolean findFirstDuplicate(String type, final JaroWinklerDistance jaro,
			boolean ignoreUnmergeableFiles, StringBuilder result) {
		LOG.debug("Start findFirstDuplicate");
		List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
		if (!importXML.isEmpty()) {
			addLine(result, "Size: " + importXML.size(), true);
			for (int i = 0; i < importXML.size(); i++) {
				for (int j = 0; j < importXML.size(); j++) {
					Composition c1 = importXML.get(i);
					Composition c2 = importXML.get(j);
					if ((!c1.getRecordType().toString().equals(type) || !c2.getRecordType().toString().equals(type))
							|| (ignoreUnmergeableFiles && (!c1.isCanBeMerged() || !c2.isCanBeMerged()))) {
						continue;
					}
					boolean isCriteria = true;
					// importXML.get(i).getFiles().stream().anyMatch(
					// f -> f.getCategorie().equals(Cat.YEAR) && f.getRangeDateBegin() == YEAR_TOP
					// && f.getRangeDateEnd() == YEAR_TOP
					// && f.getPublishYear() == YEAR_TOP)
					// && importXML.get(j).getFiles().stream().anyMatch(
					// f -> f.getCategorie().equals(Cat.YEAR) && f.getRangeDateBegin() == YEAR_TOP
					// && f.getRangeDateEnd() == YEAR_TOP
					// && f.getPublishYear() == YEAR_TOP);
					if (i != j && isCriteria) {
						Composition composition1 = c1;
						Composition composition2 = c2;
						String artist1 = composition1.getArtist();
						String artist2 = composition2.getArtist();
						// boolean result = (SearchUtils.isEqualsJaro(jaro, newTitre1, newTitre2,
						// Constant.SCORE_LIMIT_TITLE_FUSION)
						// || StringUtils.startsWithIgnoreCase(titre1, titre2) ||
						// StringUtils.startsWithIgnoreCase(titre2, titre1))
						// && (StringUtils.startsWithIgnoreCase(artist1, artist2) ||
						// StringUtils.startsWithIgnoreCase(artist2, artist1))
						// && publishYear1 == publishYear2;
						boolean similarArtist = StringUtils.startsWithIgnoreCase(artist1, artist2)
								|| StringUtils.startsWithIgnoreCase(artist2, artist1);
						if (similarArtist) {
							String titre1 = composition1.getTitre().toLowerCase();
							String titre2 = composition2.getTitre().toLowerCase();
							String remParTitre1 = SearchUtils.removeParentheses(titre1);
							String parTitre1 = SearchUtils.removePunctuation(remParTitre1);
							String remParTitre2 = SearchUtils.removeParentheses(titre2);
							String parTitre2 = SearchUtils.removePunctuation(remParTitre2);
							boolean parTitreEqu = StringUtils.startsWithIgnoreCase(parTitre1, parTitre2)
									|| StringUtils.startsWithIgnoreCase(parTitre2, parTitre1);
							if (parTitreEqu
									&& (StringUtils.containsIgnoreCase(remParTitre1, " and ")
											|| StringUtils.containsIgnoreCase(remParTitre2, " and "))
									&& !StringUtils.containsIgnoreCase(remParTitre1, "/")
									&& !StringUtils.containsIgnoreCase(remParTitre2, "/")) {
								String andTitre1 = SearchUtils
										.removePunctuation(StringUtils.substringBefore(remParTitre1, " and "));
								String andTitre2 = SearchUtils
										.removePunctuation(StringUtils.substringBefore(remParTitre2, " and "));
								parTitre1 = andTitre1;
								parTitre2 = andTitre2;
								parTitreEqu = false;
							}
							boolean equalsJaroPar = SearchUtils.isEqualsJaro(jaro, parTitre1, parTitre2,
									Constant.SCORE_LIMIT_TITLE_FUSION);
							if (equalsJaroPar) {
								mergeTwoCompositions(importXML, i, j, result);
								LOG.debug("End findFirstDuplicate, find duplicate");
								return true;
							}
						}
					}
				}
			}
		}
		LOG.debug("End findFirstDuplicate, no result");
		return false;
	}

	/**
	 * Show all the duplicates for a year and a type regardless of the artist, only
	 * based on the song or album.
	 */
	private static boolean detectsDuplicate(String type, final JaroWinklerDistance jaro, StringBuilder result) {
		LOG.debug("Debut detectsDuplicate");
		List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
		int maxYear = importXML.stream().map(Composition::getFiles).flatMap(List::stream).map(Fichier::getPublishYear)
				.mapToInt(i -> i).max().getAsInt();
		int minYear = importXML.stream().map(Composition::getFiles).flatMap(List::stream).map(Fichier::getPublishYear)
				.mapToInt(i -> i).filter(y -> y != 0).min().getAsInt();
		for (int year = minYear; year <= maxYear; year++) {
			Map<String, String> criteria = new HashMap<>();
			criteria.put(SearchUtils.CRITERIA_CAT, Cat.YEAR.toString());
			criteria.put(SearchUtils.CRITERIA_PUBLISH_YEAR, String.valueOf(year));
			criteria.put(SearchUtils.CRITERIA_PUBLISH_YEAR_RANGE, SearchRange.EQUAL.getValue());
			criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, type);
			List<Composition> yearList = SearchUtils.search(importXML, criteria, true, SearchMethod.CONTAINS, false,
					false);
			addLine(result, "Year: " + year, true);
			addLine(result, "Size: " + yearList.size(), true);
			for (int i = 0; i < yearList.size(); i++) {
				for (int j = 0; j < yearList.size(); j++) {
					if (i < j) {
						Composition composition1 = yearList.get(i);
						Composition composition2 = yearList.get(j);
						String titre1 = composition1.getTitre();
						String titre2 = composition2.getTitre();
						String newTitre1 = SearchUtils.removePunctuation(titre1);
						String newTitre2 = SearchUtils.removePunctuation(titre2);
						String artist1 = composition1.getArtist();
						String artist2 = composition2.getArtist();
						Integer publishYear1 = composition1.getFiles().get(0).getPublishYear();
						Integer publishYear2 = composition2.getFiles().get(0).getPublishYear();
						boolean similarArtist = StringUtils.startsWithIgnoreCase(artist1, artist2)
								|| StringUtils.startsWithIgnoreCase(artist2, artist1);
						boolean equalsJaroPar = publishYear1 == publishYear2 && (SearchUtils.isEqualsJaro(jaro,
								newTitre1, newTitre2, Constant.SCORE_LIMIT_TITLE_FUSION)
								|| StringUtils.startsWithIgnoreCase(titre1, titre2)
								|| StringUtils.startsWithIgnoreCase(newTitre1, newTitre2)) && similarArtist;
						if (equalsJaroPar) {
							mergeTwoCompositions(yearList, i, j, result);
							LOG.debug("End detectsDuplicate, find duplicate");
							return true;
						}
					}
				}
			}
		}
		LOG.debug("Fin detectsDuplicate");
		return false;
	}

	private static void mergeTwoCompositions(List<Composition> importXML, int index1, int index2,
			StringBuilder result) {
		LOG.debug("Start mergeTwoCompositions");
		Composition c1 = importXML.get(index1);
		List<Fichier> files1 = c1.getFiles();
		Composition c2 = importXML.get(index2);

		boolean isDeleted = c1.isDeleted() || c2.isDeleted();
		c1.setDeleted(isDeleted);
		c2.setDeleted(isDeleted);

		addLine(result, "i: " + index1, true);
		addLine(result, "j: " + index2, true);
		addLine(result, "c1: " + c1, true);
		addLine(result, "c2: " + c2, true);
		Composition tempC2 = new Composition(c2);
		c2.getFiles().addAll(files1);
		if (((c1.getFiles().size() >= c2.getFiles().size() && !StringUtils.containsIgnoreCase(c1.getArtist(), " and "))
				|| StringUtils.containsIgnoreCase(c2.getArtist(), " and "))) {
			c2.setArtist(c1.getArtist());
			c2.setTitre(c1.getTitre());
			try {
				CompositionUtils.modifyCompositionsInFiles(tempC2, c1.getArtist(), c1.getTitre(),
						c1.getRecordType().toString(), isDeleted);
			} catch (MyException e) {
				addLine(result, "Erreur modif compo" + e.getMessage(), true);
				LOG.error("Erreur modif compo", e);
			}
		} else {
			try {
				CompositionUtils.modifyCompositionsInFiles(c1, tempC2.getArtist(), tempC2.getTitre(),
						c1.getRecordType().toString(), isDeleted);
			} catch (MyException e) {
				addLine(result, "Erreur modif compo" + e.getMessage(), true);
				LOG.error("Erreur modif compo", e);
			}
		}
		importXML.remove(c1);
		try {
			ExportXML.exportXML(importXML, Constant.getFinalFile());
		} catch (IOException e) {
			LOG.error("Error !!", e);
		}
		addLine(result, "Final size: " + importXML.size(), true);
		LOG.debug("End mergeTwoCompositions");
	}

	/**
	 * Generates the top excel files of a year.
	 * 
	 * @param list
	 * @param deleted if true all compositions, false only not deleted compositions
	 */
	private static void topYear(int yearTop, List<Composition> list, int albumLimit, int songLimit, boolean deleted,
			StringBuilder result) {
		LOG.debug("Start topYear");
		List<String> files = new ArrayList<>();
		String year = String.valueOf(yearTop);
		addLine(result, "Year: " + year, true);
		files.add(topOccurence(list, year, deleted));
		files.add(topRecords(list, RecordType.SONG, "Top Songs", songLimit, deleted, year));
		files.add(topRecords(list, RecordType.ALBUM, "Top Albums", albumLimit, deleted, year));
		files.add(topRecordsByPoints(list, RecordType.SONG, "Points Songs", deleted, year));
		files.add(topRecordsByPoints(list, RecordType.ALBUM, "Points Albums", deleted, year));
		files.add(topSongsParPublication(list, year, deleted));
		moveFilesInFolder(files, new File(Constant.getOutputDir() + "Top by Year" + FileUtils.FS + year), result);
		LOG.debug("End topYear");
	}

	private static void moveFilesInFolder(List<String> files, File folder, StringBuilder result) {
		FichierUtils.createFolderIfNotExists(folder.getAbsolutePath());
		Path pathFolder = folder.toPath();
		files.stream().forEach(f -> {
			Path pathFile = new File(f).toPath();
			try {
				Files.move(pathFile, pathFolder.resolve(pathFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				addLine(result, "Error while moving file: " + f + " " + e.getMessage(), true);
				LOG.error("Error while moving file: " + f, e);
			}
		});
	}

	/**
	 * Generates csv file with each top 10 songs of every publication.
	 * 
	 * @param list
	 * @param year
	 * @param deleted if true all compositions, false only not deleted compositions
	 */
	private static String topSongsParPublication(List<Composition> list, String year, boolean deleted) {
		List<String> authors = OngletPanel.getAuthorList();
		List<List<String>> result = new ArrayList<List<String>>();
		for (String author : authors) {
			Map<String, String> criteria = new HashMap<>();
			criteria.put(SearchUtils.CRITERIA_CAT, Cat.YEAR.toString());
			if (!"0".equals(year)) {
				criteria.put(SearchUtils.CRITERIA_DATE_BEGIN, year);
				criteria.put(SearchUtils.CRITERIA_DATE_END, year);
			}
			criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, RecordType.SONG.toString());
			criteria.put(SearchUtils.CRITERIA_AUTHOR, author);
			List<Composition> yearList = SearchUtils.search(list, criteria, true, SearchMethod.CONTAINS, deleted,
					false);
			if (yearList.isEmpty() || !yearList.get(0).getFiles().get(0).getSorted()) {
				// If no file for the given author and year or if the file is not sorted
				continue;
			}
			List<List<String>> temp = new ArrayList<List<String>>();
			for (int i = 0; i < yearList.size(); i++) {
				List<String> row = new ArrayList<>();
				Composition composition = yearList.get(i);
				if (composition.getFiles().get(0).getClassement() <= 10) {
					row.add(composition.getArtist());
					row.add(composition.getTitre());
					row.add(String.valueOf(composition.getFiles().get(0).getClassement()));
					if ("0".equals(year)) {
						row.add(String.valueOf(composition.getFiles().get(0).getRangeDateBegin()));
					}
					row.add(String.valueOf(composition.isDeleted()));
					temp.add(row);
				}
			}
			temp.add(0, initList(author, "-1"));
			temp.add(0, initList("", "-2"));
			if (temp.size() > 2) {
				List<List<String>> hello = temp.stream()
						.sorted((e1, e2) -> Integer.valueOf(e1.get(2)).compareTo(Integer.valueOf(e2.get(2))))
						.collect(Collectors.toList());
				result.addAll(hello);
			}
		}
		for (List<String> strings : result) {
			if (StringUtils.isBlank(strings.get(1))) {
				strings.set(2, "");
			}
		}
		String[] header = { "Artiste", "Titre", "Classement", "Supprimé" };
		if ("0".equals(year)) {
			String[] tmp = { "Artiste", "Titre", "Classement", "Année", "Supprimé" };
			header = tmp;
		}
		return CsvFile.exportCsv("Top Songs Par Publication - " + year, result, null, header);
	}

	private static List<String> initList(String author, String value) {
		List<String> row = new ArrayList<>();
		row.add(author);
		row.add("");
		row.add(value);
		return row;
	}

	/**
	 * Top songs or top albums.
	 * 
	 * @param list
	 * @param type Album or Song
	 * @param fileName the name of the result csv file
	 * @param limit the minimim of number of occurence a Composition to have to be
	 *            in the result file
	 * @param deleted if true all compositions, false only not deleted compositions
	 * @param year the year of the top
	 * @param score
	 */
	private static String topRecords(List<Composition> list, RecordType type, String fileName, int limit,
			boolean deleted, String year) {
		Map<String, String> criteria = new HashMap<>();
		criteria.put(SearchUtils.CRITERIA_CAT, Cat.YEAR.toString());
		if (!"0".equals(year)) {
			criteria.put(SearchUtils.CRITERIA_DATE_BEGIN, year);
			criteria.put(SearchUtils.CRITERIA_DATE_END, year);
		}
		criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, type.toString());
		List<Composition> yearList = SearchUtils.search(list, criteria, true, SearchMethod.CONTAINS, deleted, false);
		List<Vector<Object>> occurenceListTemp = CompositionUtils
				.convertCompositionListToVector(yearList, null, false, true, false, true, false).stream()
				.filter(c -> (int) c.get(3) >= limit).collect(Collectors.toList());
		Vector<Vector<Object>> occurenceList = new Vector<Vector<Object>>();
		for (Vector<Object> vector : occurenceListTemp) {
			occurenceList.add(vector);
		}
		String[] csvHeader = { "Artiste", "Titre", "Type", "Nombre de fichiers", "Score", "Supprimé",
				"Year: " + year + " Type: " + type.toString() };
		return CsvFile.exportCsv(fileName + " - " + year, MiscUtils.convertVectorToList(occurenceList),
				new SortKey(3, SortOrder.DESCENDING), csvHeader);
	}

	/**
	 * Top songs or top albums with a sytem of points.
	 * 
	 * @param list
	 * @param type Album or Song
	 * @param fileName the name of the result csv file
	 * @param deleted if true all compositions, false only not deleted compositions
	 * @param year the year of the top
	 */
	private static String topRecordsByPoints(List<Composition> list, RecordType type, String fileName, boolean deleted,
			String year) {
		Map<String, String> criteria = new HashMap<>();
		criteria.put(SearchUtils.CRITERIA_CAT, Cat.YEAR.toString());
		if (!"0".equals(year)) {
			criteria.put(SearchUtils.CRITERIA_DATE_BEGIN, year);
			criteria.put(SearchUtils.CRITERIA_DATE_END, year);
		}
		criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, type.toString());
		criteria.put(SearchUtils.CRITERIA_SORTED, Boolean.TRUE.toString());
		List<Composition> yearList = SearchUtils.search(list, criteria, true, SearchMethod.CONTAINS, deleted, false);
		List<List<String>> occurenceList = new ArrayList<>();
		if (yearList.stream().map(Composition::getFiles).flatMap(List::stream).map(Fichier::getAuthor)
				.map(WordUtils::capitalize).distinct().count() > 2) {
			for (Composition composition : yearList) {
				List<String> row = new ArrayList<>();
				row.add(composition.getArtist());
				row.add(composition.getTitre());
				if (!"0".equals(year)) {
					row.add(composition.getRecordType().toString());
				} else {
					row.add(String.valueOf(composition.getFiles().get(0).getRangeDateBegin()));
				}
				Integer points = composition.getFiles().stream().map(Fichier::getClassement).filter(rank -> rank < 10)
						.reduce(0, (a, b) -> a + 20 * (11 - b));
				if (points > 0) {
					row.add(String.valueOf(points));
					occurenceList.add(row);
				}
				row.add(String.valueOf(composition.isDeleted()));
			}
		}
		String[] csvHeader = { "Artiste", "Titre", "Type", "Score", "Supprimé",
				"Year: " + year + " Type: " + type.toString() };
		return CsvFile.exportCsv(fileName + " - " + year, occurenceList, new SortKey(3, SortOrder.DESCENDING),
				csvHeader);
	}

	/**
	 * Generates csv file with the most occurence of an artist, songs and albums
	 * combine. Limited with minimum 10 total occurences.
	 * 
	 * @param list
	 * @param year
	 * @param deleted if true all compositions, false only not deleted compositions
	 */
	private static String topOccurence(List<Composition> list, String year, boolean deleted) {
		Map<String, String> criteria = new HashMap<>();
		criteria.put(SearchUtils.CRITERIA_CAT, Cat.YEAR.toString());
		if (!"0".equals(year)) {
			criteria.put(SearchUtils.CRITERIA_DATE_BEGIN, year);
			criteria.put(SearchUtils.CRITERIA_DATE_END, year);
		}
		List<Composition> yearList = SearchUtils.search(list, criteria, true, SearchMethod.CONTAINS, deleted, false);
		List<Vector<Object>> occurenceListTemp = CompositionUtils
				.convertArtistPanelResultToVector(CompositionUtils.groupCompositionByArtist(yearList), false).stream()
				.filter(c -> (int) c.get(1) > 9).collect(Collectors.toList());
		Vector<Vector<Object>> occurenceList = new Vector<Vector<Object>>();
		for (Vector<Object> vector : occurenceListTemp) {
			occurenceList.add(vector);
		}
		String[] csvHeader = { "Artiste", "Nombre d'occurences totales", "Albums", "Chansons", "Score Total",
				"Score Album", "Score Chanson", "Year: " + year };
		return CsvFile.exportCsv("Top Occurence - " + year, MiscUtils.convertVectorToList(occurenceList),
				new SortKey(1, SortOrder.DESCENDING), csvHeader);
	}

	private static String writeInFile(StringBuilder sb, String fileName) {
		String filePath = Constant.getOutputDir() + FileUtils.FS + fileName;
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(filePath), Constant.ANSI_ENCODING));) {
			writer.append(sb);
			writer.flush();
		} catch (IOException e) {
			LOG.error("Error when write batch result in: " + filePath, e);
		}
		return filePath;
	}

	private static void addLine(StringBuilder sb, String text, boolean displayTime) {
		sb.append(displayTime ? MiscUtils.getCurrentTime() : "").append(displayTime ? ": " : "").append(text)
				.append(Constant.NEW_LINE);
	}
}
