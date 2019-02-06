/**
 * 
 */
package pmb.music.AllMusic.XML;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FichierUtils;

/**
 * Classe pour exporter des fichiers au format XML.
 * 
 * @author pmbroca
 */
public final class ExportXML {

	private static final Logger LOG = Logger.getLogger(ExportXML.class);

	private static boolean finalFileChanged = false;

	private ExportXML() {
		throw new AssertionError("Must not be used");
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
		SimpleDateFormat sdfDttm = new Constant().getSdfDttm();

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

			for (int j = 0; j < compList.get(i).getFiles().size(); j++) {
				Element file = comp.addElement(CompoHandler.TAG_FILE);
				file.addAttribute(CompoHandler.TAG_AUTHOR,
						String.valueOf(compList.get(i).getFiles().get(j).getAuthor()));
				file.addAttribute(CompoHandler.TAG_FILENAME,
						String.valueOf(compList.get(i).getFiles().get(j).getFileName()));
				file.addAttribute(CompoHandler.TAG_PUBLISH_YEAR,
						String.valueOf(compList.get(i).getFiles().get(j).getPublishYear()));
				file.addAttribute(CompoHandler.TAG_CATEGORIE,
						String.valueOf(compList.get(i).getFiles().get(j).getCategorie()));
				file.addAttribute(CompoHandler.TAG_RANGE_DATE_BEGIN,
						String.valueOf(compList.get(i).getFiles().get(j).getRangeDateBegin()));
				file.addAttribute(CompoHandler.TAG_RANGE_DATE_END,
						String.valueOf(compList.get(i).getFiles().get(j).getRangeDateEnd()));
				file.addAttribute(CompoHandler.TAG_SORTED,
						String.valueOf(compList.get(i).getFiles().get(j).getSorted()));
				file.addAttribute(CompoHandler.TAG_CLASSEMENT,
						String.valueOf(compList.get(i).getFiles().get(j).getClassement()));
				file.addAttribute(CompoHandler.TAG_CREATION_DATE,
							sdfDttm.format(compList.get(i).getFiles().get(j).getCreationDate()));
					file.addAttribute(CompoHandler.TAG_SIZE,
							String.valueOf(compList.get(i).getFiles().get(j).getSize()));
			}
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
	private static void saveFile(String fileName, Document doc) throws IOException {
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
		FileOutputStream fos = new FileOutputStream(Constant.getXmlPath() + fullFileName);
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setIndent(true);
		format.setNewlines(true);
		XMLWriter xmlOut = new XMLWriter(fos, format);
		xmlOut.write(doc);
		xmlOut.close();
		LOG.debug("End saveFile");
	}

	public static boolean isFinalFileChanged() {
		return finalFileChanged;
	}
}
