/**
 * 
 */
package pmb.music.AllMusic.XML;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.dropbox.core.v2.files.WriteMode;

import pmb.music.AllMusic.file.CleanFile;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.utils.BatchUtils;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.DropBoxUtils;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.utils.SearchUtils;
import pmb.music.AllMusic.view.panel.OngletPanel;

/**
 * Classe pour exporter des fichiers au format XML pour être utilisé par une
 * application Angular.
 * 
 * @author pmbroca
 */
public final class NgExportXml extends AbstractExportXML {

	private NgExportXml() {
		super();
	}

	/**
	 * Converti en XML la liste de composition puis sauvegarde dans un fichier.
	 * 
	 * @param compList List<{@link Composition}> les compositions à sauvegarder
	 * @param fileName {@link String} le nom du fichier
	 * @throws IOException
	 */
	public static void exportXML(List<Composition> compList, String fileName) throws IOException {
		LOG.debug("Start ngExportXml");
		Document doc = DocumentHelper.createDocument();
		Element listComp = doc.addElement(CompoHandler.TAG_ROOT);
		DateTimeFormatter fullDTF = new Constant().getFullDTF();
		Set<Entry<String, String>> entrySet = CleanFile.getModifSet();

		for (int i = 0; i < compList.size(); i++) {
			// Ajout element <Order/>
			Element comp = listComp.addElement(CompoHandler.TAG_COMPOSITION);
			comp.addAttribute(CompoHandler.TAG_ARTIST, String.valueOf(compList.get(i).getArtist()));
			comp.addAttribute(CompoHandler.TAG_TITRE, String.valueOf(compList.get(i).getTitre()));

			// Clean artist and title
			String stripArtist = StringUtils
					.substringBefore(
							SearchUtils.removeParentheses(CleanFile.removeDiactriticals(
									BatchUtils.cleanLine(compList.get(i).getArtist().toLowerCase(), entrySet))),
							" and ");
			if (StringUtils.startsWith(stripArtist, "the ")) {
				stripArtist = StringUtils.substringAfter(stripArtist, "the ");
			}
			comp.addAttribute("s" + CompoHandler.TAG_ARTIST, SearchUtils.removePunctuation(stripArtist));
			comp.addAttribute("s" + CompoHandler.TAG_TITRE,
					SearchUtils.removePunctuation(SearchUtils.removeParentheses(CleanFile.removeDiactriticals(
							BatchUtils.cleanLine(compList.get(i).getTitre().toLowerCase(), entrySet)))));

			comp.addAttribute(CompoHandler.TAG_TYPE, String.valueOf(compList.get(i).getRecordType()));
			comp.addAttribute(CompoHandler.TAG_DELETED, String.valueOf(compList.get(i).isDeleted()));
			comp.addAttribute("size", String.valueOf(compList.get(i).getFiles().size()));
			comp.addAttribute("score",
					String.valueOf(CompositionUtils.calculateCompositionScore(
							OngletPanel.getScore().getLogMax(compList.get(i).getRecordType()),
							OngletPanel.getScore().getDoubleMedian(compList.get(i).getRecordType()), compList.get(i))));

			exportFichier(compList, fullDTF, i, comp);
		}
		saveFile(fileName, doc);
		LOG.debug("End ngExportXml");
	}

	/**
	 * Sauvegarde du document dans un fichier.
	 * 
	 * @param fileName Le nom du fichier
	 * @param doc Le document a enregistrer
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	protected static void saveFile(String fileName, Document doc) throws IOException {
		LOG.debug("Start saveFile");
		// Nom du fichier
		String fullFileName = fileName;
		if (StringUtils.endsWith(fullFileName, Constant.XML_EXTENSION)) {
			fullFileName = StringUtils.substringBeforeLast(fullFileName, Constant.XML_EXTENSION);
		}
		fullFileName += ";" + MiscUtils.dateNow() + Constant.XML_EXTENSION;

		// Sauvegarde du document dans le fichier
		writeCompositionInFile(doc, fullFileName);
		File pathFile = new File(Constant.getXmlPath() + fullFileName);
		try {
			DropBoxUtils.uploadFile(pathFile, "XML/" + fullFileName, WriteMode.OVERWRITE);
		} catch (MyException e) {
			LOG.error("Impossible d'enregistrer le fichier: " + fullFileName + " dans la dropbox", e);
		}
		try {
			Files.delete(pathFile.toPath());
		} catch (IOException e) {
			LOG.warn("Error when deleting file: " + Constant.getXmlPath() + fullFileName, e);
		}
		LOG.debug("End saveFile");
	}
}
