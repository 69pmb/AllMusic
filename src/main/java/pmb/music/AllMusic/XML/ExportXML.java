/**
 * 
 */
package pmb.music.AllMusic.XML;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
 * @author pmbroca
 */
public class ExportXML {

	private static final Logger LOG = Logger.getLogger(ExportXML.class);
	
	private ExportXML(){}

	/**
	 * COnverti en XML la liste de composition puis sauvegarde dans un fichier.
	 * @param compList List<{@link Composition}> les compositions à sauvegarder
	 * @param fileName {@link String} le nom du fichier
	 * @throws IOException
	 */
	public static void exportXML(List<Composition> compList, String fileName) throws IOException {
		LOG.debug("Start exportXML");
		Document doc = DocumentHelper.createDocument();
		Element listComp = doc.addElement("ListCompositions");

		for (int i = 0; i < compList.size(); i++) {
			// Ajout element <Order/>
			Element comp = listComp.addElement("Composition");
			comp.addAttribute("artist", String.valueOf(compList.get(i).getArtist()));
			comp.addAttribute("titre", String.valueOf(compList.get(i).getTitre()));
			comp.addAttribute("type", String.valueOf(compList.get(i).getRecordType()));

			for (int j = 0; j < compList.get(i).getFiles().size(); j++) {
				Element file = comp.addElement("File");
				file.addAttribute("author", String.valueOf(compList.get(i).getFiles().get(j).getAuthor()));
				file.addAttribute("fileName", String.valueOf(compList.get(i).getFiles().get(j).getFileName()));
				file.addAttribute("publishYear", String.valueOf(compList.get(i).getFiles().get(j).getPublishYear()));
				file.addAttribute("categorie", String.valueOf(compList.get(i).getFiles().get(j).getCategorie()));
				file.addAttribute("rangeDateBegin",
						String.valueOf(compList.get(i).getFiles().get(j).getRangeDateBegin()));
				file.addAttribute("rangeDateEnd", String.valueOf(compList.get(i).getFiles().get(j).getRangeDateEnd()));
				file.addAttribute("sorted", String.valueOf(compList.get(i).getFiles().get(j).getSorted()));
				file.addAttribute("classement", String.valueOf(compList.get(i).getFiles().get(j).getClassement()));
				file.addAttribute("creationDate",
						new Constant().getSdfDttm().format(compList.get(i).getFiles().get(j).getCreationDate()));
				file.addAttribute("size", String.valueOf(compList.get(i).getFiles().get(j).getSize()));
			}
		}
		saveFile(fileName, doc);
		LOG.debug("End exportXML");
	}

	/**
	 * Historise du fichier précédent dans le dossier history dans les resources
	 * et sauvegarde du document dans un fichier.
	 * @param fileName Le nom du fichier
	 * @param doc Le document a enregistrer
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private static void saveFile(String fileName, Document doc) throws IOException {
		LOG.debug("Start saveFile");
		// Création du dossier resources
		FichierUtils.createFolderIfNotExists(Constant.RESOURCES_ABS_DIRECTORY);

		// Création du dossier history dans le dossier resources
		FichierUtils.createFolderIfNotExists(Constant.HISTORY_PATH);
		
		// Création du dossier xml dans le dossier resources
		FichierUtils.createFolderIfNotExists(Constant.XML_PATH);

		// Historisation du fichier précédent dans le dossier history
		File source = new File(Constant.XML_PATH + fileName + Constant.XML_EXTENSION);
		File destination = new File(Constant.HISTORY_PATH + fileName + Constant.SEPARATOR_DATE_HISTORY + dateNow() + Constant.XML_EXTENSION);
		source.renameTo(destination);

		// Sauvegarde du document dans le fichier
		FileOutputStream fos = new FileOutputStream(Constant.XML_PATH + fileName + Constant.XML_EXTENSION);
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setIndent(true);
		format.setNewlines(true);
		XMLWriter xmlOut = new XMLWriter(fos, format);
		xmlOut.write(doc);
		xmlOut.close();
		LOG.debug("End saveFile");
	}

	/**
	 * Retourne la date à l'instant de l'appel.
	 * @return la date au format dd-MM-yyyy HH-mm
	 */
	private static String dateNow() {
		Calendar greg = new GregorianCalendar();
		Date date = greg.getTime();
		return new Constant().getSdfHistory().format(date);
	}
}
