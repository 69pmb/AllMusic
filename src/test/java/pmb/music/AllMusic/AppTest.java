package pmb.music.AllMusic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.log4j.Logger;
import org.junit.Test;

import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.file.ImportFile;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;

/**
 * Unit test for simple App.
 */
public class AppTest {

	private static final Logger LOG = Logger.getLogger(AppTest.class);

	public static void main(String[] args) {
		missingXML();
	}
	
	public static void randomLineTest() {
		List<File> files = new ArrayList<>();
		CompositionUtils.listFilesForFolder(new File(Constant.MUSIC_ABS_DIRECTORY), files, ".txt", true);
		for (File file : files) {
			LOG.error(file.getName());
			Fichier fichier = ImportFile.convertOneFile(file);
			List<String> randomLineAndLastLines = ImportFile.randomLineAndLastLines(file);
			fichier.setSorted(ImportFile.isSorted(randomLineAndLastLines.get(3)));
			fichier.setSize(ImportFile.determineSize(fichier, randomLineAndLastLines, file.getAbsolutePath()));
			LOG.error(fichier.getSize());
		}
	}

	public static void missingXML() {
		LOG.debug("Debut");
		List<File> music = new ArrayList<>();
		CompositionUtils.listFilesForFolder(new File(Constant.MUSIC_ABS_DIRECTORY), music, ".txt", true);
		List<String> collectMusic = music.stream().map(File::getName).map(s -> StringUtils.substringBeforeLast(s, ".txt")).collect(Collectors.toList());

		List<File> xml = new ArrayList<>();
		CompositionUtils.listFilesForFolder(new File(Constant.XML_PATH), xml, Constant.XML_EXTENSION, true);
		List<String> collectXml = xml.stream().map(File::getName).map(s -> StringUtils.substringBeforeLast(s, Constant.XML_EXTENSION)).collect(Collectors.toList());

		LOG.debug("TXT: ");
		for (String txt : collectMusic) {
			if(!collectXml.stream().anyMatch(s -> StringUtils.equalsAnyIgnoreCase(s, txt))) {
				LOG.debug("Error: " + txt);
			}
		}
		LOG.debug("XML: ");
		for (String xmlFile : collectXml) {
			if(!collectMusic.stream().anyMatch(s -> StringUtils.equalsAnyIgnoreCase(s, xmlFile))) {
				LOG.debug("Error: " + xmlFile);
			}
		}
		LOG.debug("Fin");
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
			String res = StringUtils.substringBefore(artist, ".");
			if (StringUtils.isNumeric(res)) {
				rank = Integer.parseInt(res);
				artist = StringUtils.substringAfter(artist, ".");
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
			String res = StringUtils.substringBefore(artist, ".");
			if (StringUtils.isNumeric(res)) {
				rank = Integer.parseInt(res);
				artist = StringUtils.substringAfter(artist, ".");
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
			String titre = Constant.PATTERN_PUNCTUATION.matcher(composition.getArtist()).replaceAll("").toLowerCase();
			Double apply = jaro.apply(titre, test);
			jaroRes.put(apply, titre);
		}
		for (Entry<Double, String> entry : jaroRes.entrySet()) {
			LOG.debug("Key : " + entry.getKey() + " Value : " + entry.getValue());
		}
	}
}
