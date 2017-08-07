package pmb.music.AllMusic.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;

/**
 * Classe pour nettoyer des fichiers.
 * @see CleanFile#clearFile(File, boolean, String, String)
 * @author pmbroca
 */
public class CleanFile {

	private static final Logger LOG = Logger.getLogger(CleanFile.class);

	private CleanFile() {
	}

	/**
	 * Supprime toutes les lignes du fichier ne contenant pas des séparateurs,
	 * possibilité de supprimer dans ces lignes des caractères particuliers.
	 * @param file le fichier à nettoyer
	 * @param isSorted si le fichier est trié
	 * @param sep le séparateur du fichier
	 * @param characterToRemove les caractères à supprimer
	 * @return un nouveau fichier nettoyé
	 * @throws IOException
	 */
	public static File clearFile(File file, boolean isSorted, String sep, String characterToRemove) throws IOException {
		LOG.debug("Start clearFile");
		List<String> sepAsList = new LinkedList<>(Arrays.asList(Constant.getSeparators()));
		if (StringUtils.isNotBlank(sep)) {
			sepAsList.add(sep);
		}
		String line = "";
		String exitFile = file.getParentFile().getAbsolutePath() + "\\" + StringUtils.substringBeforeLast(file.getName(), ".") + " - Cleaned."
				+ StringUtils.substringAfterLast(file.getName(), ".");

		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constant.ANSI_ENCODING));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exitFile), Constant.ANSI_ENCODING));) {
			while ((line = br.readLine()) != null) {
				boolean isDigit = true;
				if (isSorted) {
					// Si le fichier est trié, on ne garde que les lignes commencant par un chiffre
					isDigit = StringUtils.isNumeric(StringUtils.substring(line, 0, 1));
				}
				if (isDigit) {
					writesLineIfContainsSepAndRemovesChar(characterToRemove, sepAsList, line, writer);
				}
			}
			writer.flush();
		}
		LOG.debug("End clearFile");
		return new File(exitFile);
	}

	private static void writesLineIfContainsSepAndRemovesChar(String characterToRemove, List<String> sepAsList, String line, BufferedWriter writer)
			throws IOException {
		String newLine = line;
		for (String separator : sepAsList) {
			if (StringUtils.containsIgnoreCase(line, separator)) {
				if (StringUtils.isNotBlank(characterToRemove)) {
					newLine = StringUtils.substringAfter(newLine, characterToRemove);
				}
				writer.append(newLine).append("\n");
				break;
			}
		}
	}

	public static void main(String[] args) {
		LOG.debug("Start clearFile");
		List<File> files = new ArrayList<>();
		File modifFile = new File(Constant.RESOURCES_ABS_DIRECTORY + "modif.txt");
		Map<String, String> modif = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(modifFile), Constant.UTF8_ENCODING));) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] split = StringUtils.split(line, ":");
				if(split.length>1) {
					modif.put(split[0], split[1]);
				} else {
					modif.put(split[0], "");
				}
			}
		} catch (IOException e) {
			LOG.error("Erreur lors du parsing " + modifFile.getAbsolutePath(), e);
		}
		Set<Entry<String, String>> entrySet = modif.entrySet();
		 CompositionUtils.listFilesForFolder(new File(Constant.MUSIC_ABS_DIRECTORY),
		 files, "*.txt", true);
//		CompositionUtils.listFilesForFolder(
//				new File(Constant.XML_PATH), files, Constant.XML_EXTENSION, true);
		for (File file : files) {
			boolean modify = false;
			String exitFile = file.getParentFile().getAbsolutePath() + "\\" + StringUtils.substringBeforeLast(file.getName(), ".") + " - Cleaned."
					+ StringUtils.substringAfterLast(file.getName(), ".");
			String name = file.getName();
			if (!"final.xml".equals(name)) {
				try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constant.UTF8_ENCODING));
						BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exitFile), Constant.UTF8_ENCODING));) {
					String line;
					while ((line = br.readLine()) != null) {
						for (Entry<String, String> entry : entrySet) {
							if (StringUtils.containsIgnoreCase(line, entry.getKey())) {
								line = StringUtils.replaceAll(line, entry.getKey(), entry.getValue());
								modify = true;
							}
						}
						writer.append(line).append("\r\n");
					}
				} catch (IOException e) {
					LOG.error("Erreur lors du netoyage de " + file.getAbsolutePath(), e);
				}
			}
			if (modify) {
				LOG.debug(file + " modifié");
				if (!file.delete()) {
					LOG.debug(file + " n'a pas pu etre supprimé");
				}
				new File(exitFile).renameTo(file);
			} else {
				if(!new File(exitFile).delete()) {
					LOG.debug(exitFile + " n'a pas pu etre supprimé");
				}
			}
		}
		LOG.debug("End clearFile");
	}

}
