package pmb.music.AllMusic.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.file.CsvFile;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.view.Onglet;

public class BatchUtils {
	private static final Logger LOG = Logger.getLogger(BatchUtils.class);
	
	public static void detectsDuplicateFinal(boolean song, boolean album, boolean ignoreUnmergeableFiles) {
		LOG.debug("Start detectsDuplicateFinal");
		StringBuilder result = new StringBuilder();
		addLine(result, "DetectsDuplicateFinal: ");
		addLine(result, "Song: " + song);
		addLine(result, "Album: " + album);
		addLine(result, "Ignore Unmergeable Files: " + ignoreUnmergeableFiles);

		if(song) {
			detectsDuplicateFinal(RecordType.SONG.toString(), ignoreUnmergeableFiles, result);
		}
		if(album) {
			detectsDuplicateFinal(RecordType.ALBUM.toString(), ignoreUnmergeableFiles, result);
		}
		
		writeInFile(result);
		LOG.debug("End detectsDuplicateFinal");
	}

	/**
	 * Generates statistics of xml files.
	 */
	public static void stat() {
		LOG.debug("Start stat");
		StringBuilder result = new StringBuilder();

		List<Composition> importXML = ImportXML.importXML(Constant.FINAL_FILE_PATH);
		List<Integer> size = new ArrayList<>();
		for (Composition composition : importXML) {
			String s = composition.getArtist() + composition.getTitre();
			size.add(s.length());
		}
		addLine(result, "Min: " + size.stream().mapToInt(Integer::intValue).min());
		addLine(result, "Max: " + size.stream().mapToInt(Integer::intValue).max());
		addLine(result, "Moyenne: " + size.stream().mapToInt(Integer::intValue).average());
		addLine(result, "Summary: " + size.stream().mapToInt(Integer::intValue).summaryStatistics());
		addLine(result, "Size: " + size);

		writeInFile(result);
		LOG.debug("End stat");
	}

	/**
	 * Search if a composition has similar files (same author and same rank).
	 */
	public static void findDuplicateFiles() {
		LOG.debug("Start findDuplicateFiles");
		StringBuilder text = new StringBuilder();
		addLine(text, "FindDuplicateFiles: ");
		
		Map<String, Integer> result = new HashMap<String, Integer>();
		List<Composition> importXML = ImportXML.importXML(Constant.FINAL_FILE_PATH);
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
			if (result.get(key) > 1) {
				addLine(text, key + ": " + result.get(key));
			}
		});
		
		writeInFile(text);
		LOG.debug("End findDuplicateFiles");
	}

	/**
	 * Search if there are txt files which are not convert to xml files.
	 */
	public static void missingXML() {
		LOG.debug("Start missingXML");
		StringBuilder text = new StringBuilder();
		addLine(text, "MissingXML: ");

		// Recupère tous les nom des fichiers txt
		List<File> music = new ArrayList<>();
		CompositionUtils.listFilesForFolder(new File(Constant.MUSIC_ABS_DIRECTORY), music, ".txt", true);
		List<String> collectMusic = music.stream().map(File::getName)
				.map(s -> StringUtils.substringBeforeLast(s, ".txt")).collect(Collectors.toList());

		// Recupère tous les nom des fichiers xml
		List<File> xml = new ArrayList<>();
		CompositionUtils.listFilesForFolder(new File(Constant.XML_PATH), xml, Constant.XML_EXTENSION, true);
		List<String> collectXml = xml.stream().map(File::getName)
				.map(s -> StringUtils.substringBeforeLast(s, Constant.XML_EXTENSION)).collect(Collectors.toList());

		addLine(text, "TXT: ");
		for (String txt : collectMusic) {
			if (!collectXml.stream().anyMatch(s -> StringUtils.equalsAnyIgnoreCase(s, txt))) {
				addLine(text, "Missing: " + txt);
				LOG.debug("Missing: " + txt);
			}
		}
		addLine(text, "XML: ");
		for (String xmlFile : collectXml) {
			if (!collectMusic.stream().anyMatch(s -> StringUtils.equalsAnyIgnoreCase(s, xmlFile))) {
				addLine(text, "Missing: " + xmlFile);
				LOG.debug("Missing: " + xmlFile);
			}
		}

		writeInFile(text);
		LOG.debug("End missingXML");
	}

	public static void topYear(int yearBegin, int yearEnd, int albumLimit, int songLimit) {
		LOG.debug("Start topYear");
		StringBuilder text = new StringBuilder();
		addLine(text, "Top Year: ");
		addLine(text, "Year Begin: " + yearBegin);
		addLine(text, "Year End: " + yearEnd);
		addLine(text, "Album Limit: " + albumLimit);
		addLine(text, "Song Limit: " + songLimit);

		for (int i = yearBegin; i <= yearEnd; i++) {
			topYear(i, albumLimit, songLimit, text);
		}

		writeInFile(text);
		LOG.debug("End topYear");
	}

	/**
	 * Supprime tous les fichiers historisés sauf le plus récent.
	 */
	public static void cleanHistory() {
		LOG.debug("Start cleanHistory");
		StringBuilder text = new StringBuilder();
		addLine(text, "Clean History: ");
		
		// Création d'une map avec:
		// key nom du fichier sans date
		// value liste des dates du fichier
		List<File> files = new ArrayList<>();
		CompositionUtils.listFilesForFolder(new File(Constant.HISTORY_PATH), files, Constant.XML_EXTENSION, false);
		Map<String, List<Date>> list = new HashMap<String, List<Date>>();
		for (File file : files) {
			String nomFichier = StringUtils.substringBefore(file.getName(), Constant.SEPARATOR_DATE_HISTORY);
			String date = StringUtils.substringBetween(file.getName(), Constant.SEPARATOR_DATE_HISTORY, Constant.XML_EXTENSION);
			if (list.get(nomFichier) == null) {
				list.put(nomFichier, new ArrayList<Date>());
			}
			try {
				list.get(nomFichier).add(new Constant().getSdfHistory().parse(date));
			} catch (ParseException e) {
				addLine(text, "Erreur lors du parsing d'une date" + e.getMessage());
				LOG.error("Erreur lors du parsing d'une date", e);
			}
		}

		Set<String> keySet = list.keySet();
		for (String key : keySet) {
			// Tri des dates, la plus récente en 1er
			Collections.sort(list.get(key), Collections.reverseOrder());
			String path = Constant.HISTORY_PATH + key + Constant.SEPARATOR_DATE_HISTORY;
			// Suppression des fichiers sauf du 1er
			for (int i = 1; i < list.get(key).size(); i++) {
				String toDelete = path + new Constant().getSdfHistory().format(list.get(key).get(i)) + Constant.XML_EXTENSION;
				if (!new File(toDelete).delete()) {
					addLine(text, toDelete + " n'a pas pu etre supprimé");
					LOG.error(toDelete + " n'a pas pu etre supprimé");
				}
			}
		}
		
		writeInFile(text);
		LOG.debug("End cleanHistory");
	}

	/**
	 * Show all the duplicates for a year and a type regardless of the artist, only
	 * based on the song or album.
	 */
	public static void detectsDuplicateFinal(String type, boolean ignoreUnmergeableFiles, StringBuilder result) {
		LOG.debug("Start detectsDuplicateFinal");
		double startTime = System.currentTimeMillis();
		final JaroWinklerDistance jaro = new JaroWinklerDistance();
		int i = 0;
		while (findFirstDuplicate(type, jaro, ignoreUnmergeableFiles, result)) {
			i++;
		}
		double endTime = System.currentTimeMillis();
		addLine(result, "Time: " + (endTime - startTime) / 1000 + " secondes");
		addLine(result, "Nombre de compositions fusionnées: " + i);
		LOG.debug("End detectsDuplicateFinal");
	}
	

	private static boolean findFirstDuplicate(String type, final JaroWinklerDistance jaro,
			boolean ignoreUnmergeableFiles, StringBuilder result) {
		LOG.debug("Start findFirstDuplicate");
		List<Composition> importXML = ImportXML.importXML(Constant.FINAL_FILE_PATH);
		if (CollectionUtils.isNotEmpty(importXML)) {
			addLine(result, "Size: " + importXML.size());
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
							String parTitre1 = SearchUtils.removePunctuation2(remParTitre1);
							String remParTitre2 = SearchUtils.removeParentheses(titre2);
							String parTitre2 = SearchUtils.removePunctuation2(remParTitre2);
							boolean parTitreEqu = StringUtils.startsWithIgnoreCase(parTitre1, parTitre2)
									|| StringUtils.startsWithIgnoreCase(parTitre2, parTitre1);
							if (parTitreEqu
									&& (StringUtils.containsIgnoreCase(remParTitre1, " and ")
											|| StringUtils.containsIgnoreCase(remParTitre2, " and "))
									&& !StringUtils.containsIgnoreCase(remParTitre1, "/")
									&& !StringUtils.containsIgnoreCase(remParTitre2, "/")) {
								String andTitre1 = SearchUtils
										.removePunctuation2(StringUtils.substringBefore(remParTitre1, " and "));
								String andTitre2 = SearchUtils
										.removePunctuation2(StringUtils.substringBefore(remParTitre2, " and "));
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

	public static void mergeTwoCompositions(List<Composition> importXML, int index1, int index2, StringBuilder result) {
		LOG.debug("Start mergeTwoCompositions");
		Composition c1 = importXML.get(index1);
		List<Fichier> files1 = c1.getFiles();
		Composition c2 = importXML.get(index2);
		addLine(result, "i: " + index1);
		addLine(result, "j: " + index2);
		addLine(result, "c1: " + c1);
		addLine(result, "c2: " + c2);
		Composition tempC2 = new Composition(c2);
		c2.getFiles().addAll(files1);
		if (((c1.getFiles().size() >= c2.getFiles().size() && !StringUtils.containsIgnoreCase(c1.getArtist(), " and "))
				|| StringUtils.containsIgnoreCase(c2.getArtist(), " and "))) {
			c2.setArtist(c1.getArtist());
			c2.setTitre(c1.getTitre());
			try {
				CompositionUtils.modifyCompositionsInFiles(tempC2, c1.getArtist(), c1.getTitre());
			} catch (MyException e) {
				addLine(result, "Erreur modif compo" + e.getMessage());
				LOG.error("Erreur modif compo", e);
			}
		} else {
			try {
				CompositionUtils.modifyCompositionsInFiles(c1, tempC2.getArtist(), tempC2.getTitre());
			} catch (MyException e) {
				addLine(result, "Erreur modif compo" + e.getMessage());
				LOG.error("Erreur modif compo", e);
			}
		}
		importXML.remove(c1);
		try {
			ExportXML.exportXML(importXML, Constant.FINAL_FILE);
		} catch (IOException e) {
			LOG.error("Error !!", e);
		}
		addLine(result, "Final size: " + importXML.size());
		LOG.debug("End mergeTwoCompositions");
	}

	/**
	 * Generates the top excel files of a year.
	 */
	public static void topYear(int yearTop, int albumLimit, int songLimit, StringBuilder result) {
		LOG.debug("Start topYear");
		List<Composition> importXML = ImportXML.importXML(Constant.FINAL_FILE_PATH);
		List<String> files = new ArrayList<>();
		String year = String.valueOf(yearTop);
		addLine(result, "Year: " + year);
		if (CollectionUtils.isNotEmpty(importXML)) {
			files.add(topOccurence(importXML, year));
			files.add(topRecords(importXML, RecordType.SONG, "Top Songs", songLimit, year));
			files.add(topRecords(importXML, RecordType.ALBUM, "Top Albums", albumLimit, year));
			files.add(topSongsParPublication(year));
		}
		File folder = new File(Constant.USER_DIR + "\\Top by Year\\" + year);
		folder.mkdir();
		moveFilesInFolder(files, folder, result);
		LOG.debug("End topYear");
	}

	public static void moveFilesInFolder(List<String> files, File folder, StringBuilder result) {
		FichierUtils.createFolderIfNotExists(folder.getAbsolutePath());
		Path pathFolder = folder.toPath();
		files.stream().forEach(f -> {
			Path pathFile = new File(f).toPath();
			try {
				Files.move(pathFile, pathFolder.resolve(f), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				addLine(result, "Error while moving file: " + f + " " + e.getMessage());
				LOG.error("Error while moving file: " + f, e);
			}
		});
	}
	
	/**
	 * Generates csv file with each top 10 songs of every publication.
	 * 
	 * @param year
	 */
	private static String topSongsParPublication(String year) {
		List<String> authors = Onglet.getAuthorList();
		List<String[]> result = new ArrayList<String[]>();
		for (String author : authors) {
			List<Composition> arrayList = ImportXML.importXML(Constant.FINAL_FILE_PATH);
			Map<String, String> criteria = new HashMap<>();
			criteria.put("cat", Cat.YEAR.toString());
			criteria.put("dateB", year);
			criteria.put("dateE", year);
			criteria.put("publish", year);
			criteria.put("type", RecordType.SONG.toString());
			criteria.put("auteur", author);
			List<Composition> yearList = SearchUtils.searchJaro(arrayList, criteria, true);
			List<String[]> temp = new ArrayList<String[]>();
			String[] v = initArray(author, "-1");
			String[] w = initArray("", "-2");
			temp.add(v);
			temp.add(w);
			for (int i = 0; i < yearList.size(); i++) {
				Composition composition = yearList.get(i);
				if (composition.getFiles().get(0).getSorted() && composition.getFiles().get(0).getClassement() <= 10) {
					v = new String[3];
					v[0] = composition.getArtist();
					v[1] = composition.getTitre();
					v[2] = String.valueOf(composition.getFiles().get(0).getClassement());
					temp.add(v);
				}
			}
			if (temp.size() > 2) {
				List<String[]> hello = temp.stream()
						.sorted((e1, e2) -> Integer.valueOf(e1[2]).compareTo(Integer.valueOf(e2[2])))
						.collect(Collectors.toList());
				result.addAll(hello);
			}
		}
		for (String[] strings : result) {
			if (StringUtils.isBlank(strings[1])) {
				strings[2] = "";
			}
		}
		String[] header = { "Artiste", "Titre", "Classement" };
		return CsvFile.exportCsv("Top Songs Par Publication - " + year, result, header);
	}

	private static String[] initArray(String author, String value) {
		String[] v = new String[3];
		v[0] = author;
		v[1] = "";
		v[2] = value;
		return v;
	}

	/**
	 * Top songs or top albums.
	 * 
	 * @param importXML
	 * @param type
	 * @param fileName
	 * @param limit
	 * @param year
	 */
	private static String topRecords(List<Composition> importXML, RecordType type, String fileName, int limit,
			String year) {
		Map<String, String> criteria = new HashMap<>();
		criteria.put("cat", Cat.YEAR.toString());
		criteria.put("dateB", year);
		criteria.put("dateE", year);
		criteria.put("publish", year);
		criteria.put("type", type.toString());
		List<Composition> yearList = SearchUtils.searchJaro(importXML, criteria, true);
		List<Vector<Object>> occurenceListTemp = CompositionUtils.convertCompositionListToVector(yearList).stream()
				.filter(c -> (int) c.get(3) >= limit).collect(Collectors.toList());
		Vector<Vector<Object>> occurenceList = new Vector<Vector<Object>>();
		for (Vector<Object> vector : occurenceListTemp) {
			occurenceList.add(vector);
		}
		SortKey sortKey = new SortKey(3, SortOrder.DESCENDING);
		return CsvFile.writeCsvFromSearchResult(occurenceList, fileName + " - " + year, year, sortKey);
	}

	/**
	 * Generates csv file with the most occurence of an artist, songs and albums
	 * combine.
	 * 
	 * @param importXML
	 * @param year
	 */
	private static String topOccurence(List<Composition> importXML, String year) {
		Map<String, String> criteria = new HashMap<>();
		criteria.put("cat", Cat.YEAR.toString());
		criteria.put("dateB", year);
		criteria.put("dateE", year);
		criteria.put("publish", year);
		List<Composition> yearList = SearchUtils.searchJaro(importXML, criteria, true);
		List<Vector<Object>> occurenceListTemp = CompositionUtils.convertCompositionListToArtistVector(yearList)
				.stream().filter(c -> (int) c.get(1) > 9).collect(Collectors.toList());
		Vector<Vector<Object>> occurenceList = new Vector<Vector<Object>>();
		for (Vector<Object> vector : occurenceListTemp) {
			occurenceList.add(vector);
		}
		SortKey sortKey = new SortKey(1, SortOrder.DESCENDING);
		return CsvFile.writeCsvFromArtistPanel(occurenceList, "Top Occurence - " + year, year, sortKey);
	}

	public static void writeInFile(StringBuilder sb) {
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(Constant.BATCH_FILE_PATH), Constant.ANSI_ENCODING));) {
			writer.append(sb);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void addLine(StringBuilder sb, String text) {
		sb.append(getCurrentTime()).append(": ").append(text).append(Constant.NEW_LINE);
	}
	
	public static String getCurrentTime() {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
}
