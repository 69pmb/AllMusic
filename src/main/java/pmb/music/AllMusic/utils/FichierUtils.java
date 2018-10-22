/**
 * 
 */
package pmb.music.AllMusic.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.view.panel.ImportPanel;

/**
 * Classe utilitaire pour la gestion des {@link Fichier}.
 * 
 * @author pmbroca
 */
public class FichierUtils {

	private static final Logger LOG = Logger.getLogger(FichierUtils.class);

	private FichierUtils() {
	}

	/**
	 * Convertit une liste de {@link Composition} en {@link Vector<Vector<Object>>}
	 * de Fichier.
	 * 
	 * @param compoList {@code List<Composition>} la liste à convertir
	 * @param getComposition si vrai récupère la composition associée pour chaque
	 *            fichier
	 * @param lineNumber if true add a column for line number
	 * @return Vector<Vector<Object>> la liste convertie
	 */
	public static Vector<Vector<Object>> convertCompositionListToFichierVector(List<Composition> compoList,
			boolean getComposition, boolean lineNumber) {
		LOG.debug("Start convertCompositionListToFichierVector, getComposition: " + getComposition + ", lineNumber: "
				+ lineNumber);
		Vector<Vector<Object>> result = new Vector<Vector<Object>>();
		compoList.parallelStream().forEach(c -> {
			c.getFiles().parallelStream().forEach(f -> {
				Vector<Object> v = new Vector<>();
				if (lineNumber) {
					// lineNumber
					v.addElement(0);
				}
				List<Composition> importXML = ImportXML
						.importXML(Constant.getXmlPath() + f.getFileName() + Constant.XML_EXTENSION);
				Optional<Composition> optCompo = Optional.empty();
				if (getComposition) {
					// If fetch composition details
					optCompo = CompositionUtils.findByFile(importXML, f, Optional.of(c.getArtist()),
							Optional.of(c.getTitre()));
					if (optCompo.isPresent()) {
						// Artist
						v.addElement(optCompo.get().getArtist());
						// Titre
						v.addElement(optCompo.get().getTitre());
						// Type
						v.addElement(optCompo.get().getRecordType().toString());
					} else {
						LOG.warn("No result when searching composition by its file: " + f + ", " + c.getArtist() + ", "
								+ c.getTitre());
						v.addElement(c.getArtist());
						v.addElement(c.getTitre());
						v.addElement(c.getRecordType().toString());
						v.addElement(Boolean.valueOf(c.isDeleted()).toString());
					}
				}
				// Author
				v.addElement(f.getAuthor());
				// File name
				v.addElement(f.getFileName());
				if (!getComposition) {
					// Type
					v.addElement(importXML.get(0).getRecordType().toString());
				}
				v.addElement(f.getPublishYear());
				v.addElement(f.getCategorie().getCat());
				v.addElement(f.getRangeDateBegin() + " - " + f.getRangeDateEnd());
				// % of deleted
				BigDecimal numberOfDeleted = new BigDecimal(importXML.stream().reduce(0,
						(sum, item) -> item.isDeleted() ? sum + 1 : sum, (sumA, sumB) -> sumA + sumB));
				BigDecimal size = new BigDecimal(f.getSize() == 0 ? importXML.size() : f.getSize());
				v.addElement(BigDecimal.valueOf(100D).setScale(2).multiply(numberOfDeleted)
						.divide(size, RoundingMode.HALF_UP).doubleValue() + " %");
				if (!getComposition) {
					v.addElement(f.getCreationDate());
				}
				v.addElement(f.getSize());
				if (getComposition) {
					v.addElement(f.getClassement());
					// Deleted
					v.addElement(Boolean.valueOf(optCompo.get().isDeleted()).toString());
				}
				v.addElement(f.getSorted() ? "Oui" : "Non");
				result.add(v);
			});
		});
		LOG.debug("End convertCompositionListToFichierVector");
		return result;
	}

	/**
	 * Modifie un fichier, dans le fichier final.xml, dans son fichier xml et
	 * renomme si besoin les fichiers XML et TXT.
	 * 
	 * @param fileName l'ancien nom du fichier
	 * @param newFileName le nouveau nom du fichier
	 * @param newPublish la nouvelle date de publication
	 * @param newRange le nouveau range
	 * @param newCat la nouvelle catégorie
	 * @param newSize la nouvelle taille
	 * @param newSorted le nouveau sort
	 * @return le nouveau fichier
	 * @throws MyException si une erreur surviens pendant les exports xml
	 */
	public static Fichier modifyFichier(String fileName, String newFileName, String newPublish, String newRange,
			String newCat, String newSize, String newSorted) throws MyException {
		// Modification du fichier xml
		List<Composition> compoList = ImportXML.importXML(Constant.getXmlPath() + fileName + Constant.XML_EXTENSION);
		compoList.stream()
				.forEach(modifyOneFile(fileName, newFileName, newPublish, newRange, newCat, newSize, newSorted));
		Fichier result = compoList.get(0).getFiles().get(0);
		try {
			// Sauvegarde des modifications sous le nouveau nom de fichier
			ExportXML.exportXML(compoList, newFileName);
		} catch (IOException e) {
			throw new MyException("Erreur lors de la modification d'une composition dans le fichier: " + fileName, e);
		}
		// Supprime l'ancien fichier
		if (!StringUtils.equals(fileName, newFileName)) {
			new File(Constant.getXmlPath() + fileName + Constant.XML_EXTENSION).delete();
		}
		// Modification du fichier final.xml
		List<Composition> finalList = ImportXML.importXML(Constant.getFinalFilePath());
		finalList.stream()
				.filter(c -> c.getFiles().stream().anyMatch(f -> StringUtils.equals(f.getFileName(), fileName)))
				.forEach(modifyOneFile(fileName, newFileName, newPublish, newRange, newCat, newSize, newSorted));
		try {
			// Sauvegarde des modifications
			ExportXML.exportXML(finalList, Constant.getFinalFile());
		} catch (IOException e) {
			throw new MyException("Erreur lors de la modification d'une composition dans le fichier final", e);
		}
		// Renomme le fichier txt
		String txtPath = buildTxtFilePath(fileName, result.getAuthor()).get();
		String newTxt = StringUtils
				.substringBeforeLast(StringUtils.substringBeforeLast(txtPath, Constant.TXT_EXTENSION), FileUtils.FS)
				+ FileUtils.FS + newFileName + Constant.TXT_EXTENSION;
		renameFile(txtPath, newTxt);
		// Modifie ses import params
		Optional<String> firstLine = FichierUtils.readFirstLine(newTxt);
		if (firstLine.isPresent() && StringUtils.startsWith(firstLine.get(), Constant.IMPORT_PARAMS_PREFIX)) {
			Map<String, String> value = new HashMap<>();
			try {
				value = MiscUtils.<String>readValueAsMap(
						StringUtils.substringAfter(firstLine.get(), Constant.IMPORT_PARAMS_PREFIX));
			} catch (IOException e) {
				LOG.error("Error while decoding import params:" + firstLine + " in file " + newTxt, e);
			}
			String[] split = StringUtils.split(newRange, " - ");
			value.put(ImportPanel.IMPORT_PARAM_NAME, newFileName);
			value.put(ImportPanel.IMPORT_PARAM_CATEGORIE, newCat);
			value.put(ImportPanel.IMPORT_PARAM_RANGE_BEGIN, split[0]);
			value.put(ImportPanel.IMPORT_PARAM_RANGE_END, split[1]);
			value.put(ImportPanel.IMPORT_PARAM_SORTED,
					StringUtils.equalsIgnoreCase(newSorted, "oui") ? Boolean.TRUE.toString()
							: Boolean.FALSE.toString());
			value.put(ImportPanel.IMPORT_PARAM_PUBLISH_YEAR, newPublish);
			value.put(ImportPanel.IMPORT_PARAM_SIZE, String.valueOf(newSize));
			FichierUtils.writeMapInTxtFile(new File(newTxt), value);
		}
		return result;
	}

	public static Consumer<? super Composition> modifyOneFile(String fileName, String newFileName, String newPublish,
			String newRange, String newCat, String newSize, String newSorted) {
		return c -> {
			List<Fichier> list = c.getFiles().stream().filter(f -> StringUtils.equals(f.getFileName(), fileName))
					.collect(Collectors.toList());
			list.forEach(fichier -> {
				fichier.setFileName(newFileName);
				fichier.setPublishYear(Integer.valueOf(newPublish));
				String[] split = StringUtils.split(newRange, " - ");
				fichier.setRangeDateBegin(Integer.valueOf(split[0]));
				fichier.setRangeDateEnd(Integer.valueOf(split[1]));
				fichier.setCategorie(Cat.valueOf(newCat));
				fichier.setSize(Integer.valueOf(newSize));
				fichier.setSorted(StringUtils.equalsIgnoreCase(newSorted, "oui") ? Boolean.TRUE : Boolean.FALSE);
			});
		};
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

	public static void renameFile(String source, String destination) {
		new File(source).renameTo(new File(destination));
	}

	/**
	 * Récupère la liste des fichiers d'un dossier.
	 * 
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
	 * 
	 * @param filePath le chemin absolu du fichier
	 * @throws MyException something went wrong
	 */
	public static void openFileInNotepad(Optional<String> filePath) throws MyException {
		LOG.debug("Start openFileInNotepad");
		if (filePath.isPresent()) {
			String absPath = filePath.get();
			if (FileUtils.fileExists(absPath)) {
				try {
					Runtime.getRuntime().exec(Constant.getNotepadPath() + absPath);
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

	/**
	 * Ouvre le fichier donnée avec Excel si existe.
	 * 
	 * @param filePath le chemin absolu du fichier
	 * @throws MyException something went wrong
	 */
	public static void openFileInExcel(Optional<String> filePath) throws MyException {
		LOG.debug("Start openFileInExcel");
		if (filePath.isPresent()) {
			String absPath = filePath.get();
			if (FileUtils.fileExists(absPath)) {
				try {
					String[] commands = new String[] { Constant.getExcelPath(), absPath };
					Runtime.getRuntime().exec(commands);
				} catch (IOException e) {
					throw new MyException("Le chemin d'Excel dans le fichier de config est incorrect.", e);
				}
			} else {
				throw new MyException("Le fichier: " + absPath + " n'existe pas.");
			}
		} else {
			throw new MyException("Aucun fichier donné.");
		}
		LOG.debug("End openFileInExcel");
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
	 * 
	 * @param fileName le nom du fichier
	 * @param auteur l'auteur du fichier (pour connaitre le nom du dossier du
	 *            fichier)
	 * @return le chemin absolu du fichier
	 */
	public static Optional<String> buildTxtFilePath(String fileName, String auteur) {
		LOG.debug("Start buildTxtFilePath");
		String pathRoot = Constant.getMusicAbsDirectory() + auteur + FileUtils.FS;
		String nameWithExtension = fileName + Constant.TXT_EXTENSION;

		String pathShort = pathRoot + nameWithExtension;
		String pathSong = pathRoot + Constant.SONG_FOLDER + FileUtils.FS + nameWithExtension;
		String pathAlbum = pathRoot + Constant.ALBUM_FOLDER + FileUtils.FS + nameWithExtension;
		String pathYear = pathRoot + Constant.YEAR_FOLDER + FileUtils.FS + nameWithExtension;

		Optional<String> result = Arrays.asList(pathShort, pathSong, pathAlbum, pathYear).stream()
				.filter(path -> FileUtils.fileExists(path)).findFirst();
		if (!result.isPresent()) {
			LOG.warn("End buildTxtFilePath, no path built for: " + fileName + " - " + auteur);
		}
		LOG.debug("End buildTxtFilePath");
		return result;
	}

	/**
	 * Reconstruit le chemin absolu du fichier xml (du dossier XML) donnée.
	 * 
	 * @param fileName le nom du fichier
	 * @return le chemin absolu du fichier
	 */
	public static Optional<String> buildXmlFilePath(String fileName) {
		LOG.debug("Start buildXmlFilePath");
		String path = Constant.getXmlPath() + fileName + Constant.XML_EXTENSION;
		if (!FileUtils.fileExists(path)) {
			LOG.warn("End buildXmlFilePath, no path built for: " + fileName);
			LOG.warn("Path tested: " + path);
			return Optional.empty();
		}
		LOG.debug("End buildXmlFilePath");
		return Optional.of(path);
	}

	/**
	 * Export an object to json in a file.
	 * 
	 * @param o the object to export
	 * @param filePath the absolute path of the file
	 */
	public static void exportJsonInFile(Object o, String filePath) {
		LOG.debug("Start exportJsonInFile");
		String json = "";
		try {
			json = MiscUtils.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			LOG.error("Error when converting object to json", e);
		}
		File file = new File(filePath);
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file), Constant.ANSI_ENCODING));) {
			writer.append(json);
		} catch (IOException e) {
			LOG.error("Error exporting file: " + file.getName(), e);
		}
		LOG.debug("End exportJsonInFile");
	}

	/**
	 * Write (or overwrite if already present) import parameters in txt file.
	 * 
	 * @param file the file in which paramaters are written
	 * @param map a map of paramaters
	 */
	public static void writeMapInTxtFile(File file, Map<String, String> map) {
		LOG.debug("Start writeMapInFile");
		String s = "";
		try {
			s = MiscUtils.writeValueAsString(map);
		} catch (JsonProcessingException e1) {
			LOG.error("Error when convert map to string", e1);
			return;
		}
		// Read all the lines of the file
		StringBuilder lines = new StringBuilder();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), Constant.ANSI_ENCODING));) {
			String line = "";
			boolean isFirstLine = true;
			while ((line = br.readLine()) != null) {
				// Ignore the first line if old import params
				if (!StringUtils.startsWith(line, Constant.IMPORT_PARAMS_PREFIX) || !isFirstLine) {
					lines.append(line + Constant.NEW_LINE);
				}
				isFirstLine = false;
			}
		} catch (IOException e) {
			LOG.error("Error file: " + file.getName(), e);
			return;
		}
		// Delete the file
		file.delete();
		// Rewrite the file
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file), Constant.ANSI_ENCODING));) {
			// Appends imports params
			writer.append(Constant.IMPORT_PARAMS_PREFIX + s + Constant.NEW_LINE);
			// Then append all the read lines
			writer.append(lines);
		} catch (IOException e) {
			LOG.error("Error file: " + file.getName(), e);
		}
		LOG.debug("End writeMapInFile");
	}

	/**
	 * Renomme le fichier de log si il n'est pas vide.
	 * 
	 * @return le nouveau nom du fichier de log.
	 */
	public static Optional<String> saveLogFileIfNotEmpty() {
		String newFileLog = null;
		Optional<String> line = readFirstLine(Constant.FILE_LOG_PATH);
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

	/**
	 * Retourne la première ligne du fichier donné.
	 * 
	 * @param file le chemin absolu du fichier
	 * @return la 1ère ligne
	 */
	public static Optional<String> readFirstLine(String filePath) {
		LOG.debug("Start readFirstLine");
		String result = null;
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(new File(filePath)), Constant.ANSI_ENCODING));) {
			result = br.readLine();
		} catch (IOException e) {
			LOG.error("Erreur lors de la lecture du fichier " + filePath, e);
		}
		LOG.debug("End readFirstLine");
		return Optional.ofNullable(result);
	}

	public static Date getLastModifyDate(String filePath) {
		BasicFileAttributes attr = null;
		try {
			attr = Files.readAttributes(new File(filePath).toPath(), BasicFileAttributes.class);
		} catch (IOException e) {
			LOG.error("Impossible de récupérer les attributs de " + filePath, e);
		}
		if (attr == null) {
			return new Date();
		}
		Date lastModifyDate = null;
		long milliseconds = attr.lastModifiedTime().to(TimeUnit.MILLISECONDS);
		if ((milliseconds > Long.MIN_VALUE) && (milliseconds < Long.MAX_VALUE)) {
			lastModifyDate = new Date(milliseconds);
		}
		return lastModifyDate;
	}
}
