package pmb.music.AllMusic.utils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;

public class BatchUtils {
	private static final Logger LOG = Logger.getLogger(BatchUtils.class);
	
	public static void detectsDuplicateFinal(boolean song, boolean album, boolean ignoreUnmergeableFiles) {
		LOG.debug("Start detectsDuplicateFinal");
		StringBuilder result = new StringBuilder();
		addLine(result, "DetectsDuplicateFinal: ");

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
