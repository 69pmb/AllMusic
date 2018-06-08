/**
 * 
 */
package pmb.music.AllMusic.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;

/**
 * Classe utilitaire pour la gestion des {@link Fichier}.
 * @author pmbroca
 */
public class FichierUtils {

	private static final Logger LOG = Logger.getLogger(FichierUtils.class);

	private FichierUtils() {
	}

	/**
	 * Convertit une liste de {@link Fichier} en {@link Vector}.
	 * 
	 * @param fList {@code List<Fichier>} la liste à convertir
	 * @param getComposition si vrai récupère la composition associée pour chaque fichier
	 * @return Vector<Vector<Object>> la liste convertie
	 */
	@SuppressWarnings("rawtypes")
	public static Vector convertListForJTable(List<Fichier> fList, boolean getComposition) {
		LOG.debug("Start convertListForJTable");
		Vector<Vector<Object>> result = new Vector<Vector<Object>>();
		for (int i = 0; i < fList.size(); i++) {
			Fichier f = fList.get(i);
			Vector<Object> v = new Vector<>();
			if (getComposition) {
				List<Composition> compo = ImportXML
						.importXML(Constant.XML_PATH + f.getFileName() + Constant.XML_EXTENSION);
				Optional<Composition> c = CompositionUtils.findByFile(compo, f);
				if (c.isPresent()) {
					v.addElement(c.get().getArtist());
					v.addElement(c.get().getTitre());
					v.addElement(c.get().getRecordType().toString());
				} else {
					v.addElement("");
				}
			}
			v.addElement(f.getAuthor());
			v.addElement(f.getFileName());
			v.addElement(f.getPublishYear());
			v.addElement(f.getCategorie().toString());
			v.addElement(f.getRangeDateBegin() + " - " + f.getRangeDateEnd());
			if (!getComposition) {
				v.addElement(new Constant().getSdfDttm().format(f.getCreationDate()));
			}
			v.addElement(f.getSize());
			v.addElement(f.getClassement());
			v.addElement(f.getSorted().toString().toUpperCase());
			result.add(v);
		}
		LOG.debug("End convertListForJTable");
		return result;
	}

	/**
	 * Crée le dossier si il n'existe pas.
	 * 
	 * @param nomDir le chemin du dossier
	 */
	public static void createFolderIfNotExists(String nomDir) {
		if (!FileUtils.fileExists(nomDir)) {
			FileUtils.mkdir(nomDir);
		}
	}

	/**
	 * Récupère la liste des fichiers d'un dossier.
	 * @param folder le dossier où chercher
	 * @param files la liste qui contiendra les résultats
	 * @param extension l'extension des fichiers à chercher
	 * @param recursive si la recherche doit etre récursive ou non
	 */
	public static void listFilesForFolder(final File folder, List<File> files, String extension, boolean recursive) {
		if (!folder.isDirectory()) {
			files.add(folder);
			return;
		}
		for (final File fileEntry : folder.listFiles()) {
			if (recursive && fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry, files, extension, recursive);
			} else if (StringUtils.endsWith(fileEntry.getName(), extension)) {
				files.add(fileEntry);
			}
		}
	}

	/**
	 * Ouvre le fichier donnée avec Notepad++ si existe.
	 * @param filePath le chemin absolu du fichier
	 * @throws MyException something went wrong
	 */
	public static void openFileInNotepad(Optional<String> filePath) throws MyException {
		LOG.debug("Start openFileInNotepad");
		if (filePath.isPresent()) {
			String absPath = filePath.get();
			if (FileUtils.fileExists(absPath)) {
				try {
					Runtime.getRuntime().exec(Constant.NOTEPAD_PATH + absPath);
				} catch (IOException e) {
					throw new MyException("Le chemin de Notepad++ dans le fichier de config est incorrect.", e);
				}
			} else {
				throw new MyException("Le fichier: " + absPath + " n'existe pas.");
			}
		} else {
			throw new MyException("Aucun fichier donné.");
		}
		LOG.debug("End openFileInNotepad");
	}

	public static void copyFileInAnother(String source, String destination) throws IOException {
		String line = "";
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(source), Constant.ANSI_ENCODING));
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(destination), Constant.ANSI_ENCODING));) {
			while ((line = br.readLine()) != null) {
				writer.append(line).append(Constant.NEW_LINE);
			}
			writer.flush();
		}
	}
	
	/**
	 * Reconstruit le chemin absolu du fichier txt (du dossier Music) donnée.
	 * @param fileName le nom du fichier
	 * @param auteur l'auteur du fichier (pour connaitre le nom du dossier du fichier)
	 * @return le chemin absolu du fichier
	 */
	public static Optional<String> buildTxtFilePath(String fileName, String auteur) {
		LOG.debug("Start buildTxtFilePath");
		String pathRoot = Constant.MUSIC_ABS_DIRECTORY + auteur + Constant.JAVA_SLASH;
		String nameWithExtension = fileName + Constant.TXT_EXTENSION;

		String pathShort = pathRoot + nameWithExtension;
		String pathSong = pathRoot + Constant.SONG_FOLDER + Constant.JAVA_SLASH + nameWithExtension;
		String pathAlbum = pathRoot + Constant.ALBUM_FOLDER + Constant.JAVA_SLASH + nameWithExtension;
		String pathYear = pathRoot + Constant.YEAR_FOLDER + Constant.JAVA_SLASH + nameWithExtension;

		Optional<String> result = Arrays.asList(pathShort, pathSong, pathAlbum, pathYear).stream()
				.filter(path -> FileUtils.fileExists(path)).findFirst();
		if (!result.isPresent()) {
			LOG.warn("End buildTxtFilePath, no path built");
		}
		LOG.debug("End buildTxtFilePath");
		return result;
	}

	/**
	 * Renomme le fichier de log si il n'est pas vide.
	 * @return le nouveau nom du fichier de log.
	 */
	public static Optional<String> saveLogFileIfNotEmpty() {
		String newFileLog = null;
		Optional<String> line = readFileFirstLine(Constant.FILE_LOG_PATH);
		if (line.isPresent() && StringUtils.isNotBlank(line.get())) {
			newFileLog = Constant.FILE_LOG_PATH + Constant.SEPARATOR_DATE_HISTORY + MiscUtils.dateNow()
			+ Constant.TXT_EXTENSION;
			try {
				copyFileInAnother(Constant.FILE_LOG_PATH, newFileLog);
			} catch (IOException e) {
				LOG.error("Erreur lors de la sauvegarde du fichier de log", e);
			}
		}
		return Optional.ofNullable(newFileLog);
	}

	private static Optional<String> readFileFirstLine(String filePath) {
		String line = null;
		try (BufferedReader br = new BufferedReader(new FileReader(filePath));) {
			line = br.readLine();
		} catch (IOException e) {
			LOG.error("Erreur lors de l'ouverture du fichier: " + filePath, e);
		}
		return Optional.ofNullable(line);
	}
}
