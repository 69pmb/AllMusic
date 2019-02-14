/**
 * 
 */
package pmb.music.AllMusic.XML;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FichierUtils;

/**
 * Classe pour exporter des fichiers au format XML.
 * 
 */
public final class ExportXML extends AbstractExportXML {

	private static boolean finalFileChanged;

	private ExportXML() {
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
		LOG.debug("Start exportXML");
		Document doc = DocumentHelper.createDocument();
		Element listComp = doc.addElement(CompoHandler.TAG_ROOT);
		DateTimeFormatter fullDTF = new Constant().getFullDTF();

		// Export file in Dropbox if final file
		if (Constant.getFinalFile().equals(fileName)) {
			finalFileChanged = true;
		}

		for (int i = 0; i < compList.size(); i++) {
			// Ajout element <Order/>
			Element comp = listComp.addElement(CompoHandler.TAG_COMPOSITION);
			comp.addAttribute(CompoHandler.TAG_ARTIST, String.valueOf(compList.get(i).getArtist()));
			comp.addAttribute(CompoHandler.TAG_TITRE, String.valueOf(compList.get(i).getTitre()));
			comp.addAttribute(CompoHandler.TAG_TYPE, String.valueOf(compList.get(i).getRecordType()));
			comp.addAttribute(CompoHandler.TAG_CAN_BE_MERGED, String.valueOf(compList.get(i).isCanBeMerged()));
			comp.addAttribute(CompoHandler.TAG_DELETED, String.valueOf(compList.get(i).isDeleted()));
			exportFichier(compList, fullDTF, i, comp);
		}
		saveFile(fileName, doc);
		LOG.debug("End exportXML");
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
		// Création du dossier resources
		FichierUtils.createFolderIfNotExists(Constant.getResourcesDir());

		// Création du dossier xml dans le dossier resources
		FichierUtils.createFolderIfNotExists(Constant.getXmlPath());

		// Nom des fichiers
		String fullFileName = fileName;
		if (!StringUtils.endsWith(fileName, Constant.XML_EXTENSION)) {
			fullFileName += Constant.XML_EXTENSION;
		}
		// Sauvegarde du document dans le fichier
		writeCompositionInFile(doc, fullFileName);
		LOG.debug("End saveFile");
	}

	public static boolean isFinalFileChanged() {
		return finalFileChanged;
	}
}
