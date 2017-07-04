package pmb.music.AllMusic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import pmb.music.AllMusic.file.ImportFile;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;

/**
 * Unit test for simple App.
 */
public class AppTest {

	private static final Logger LOG = Logger.getLogger(AppTest.class);

	public static void main(String[] args) {
		List<File> files = new ArrayList<>();
		String pathname = Constant.MUSIC_ABS_DIRECTORY + "Pitchfork";
		CompositionUtils.listFilesForFolder(new File(pathname), files, ".txt", true);
		for (File file : files) {
			LOG.debug(file.getName());
			Fichier fichier = ImportFile.convertOneFile(file);
			List<String> randomLine = ImportFile.randomLineAndLastLines(file);
			fichier.setSorted(ImportFile.isSorted(randomLine.get(3)));
			fichier.setSize(ImportFile.determineSize(fichier, randomLine, file.getAbsolutePath()));
			RecordType determineType = ImportFile.determineType(file.getName());
			if (Cat.MISCELLANEOUS.equals(fichier.getCategorie()) && !RecordType.UNKNOWN.equals(determineType)
					&& fichier.getPublishYear() != 0 && fichier.getRangeDateBegin() == 0
					&& fichier.getRangeDateEnd() == 0) {
				fichier.setCategorie(Cat.YEAR);
				fichier.setRangeDateBegin(fichier.getPublishYear());
				fichier.setRangeDateEnd(fichier.getPublishYear());
			}
			LOG.debug(randomLine);
			LOG.debug("Fichier: " + fichier);
			LOG.debug("Type: " + determineType);
			LOG.debug("Separateur: " + ImportFile.getSeparator(randomLine.get(0)));
			LOG.debug("############################################################################################################################################################################################################################################################\n");
		}
	}

	@Test
	public void searchArtist() {
		LOG.debug(StringUtils.containsIgnoreCase("Beatles", "beat"));
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
}
