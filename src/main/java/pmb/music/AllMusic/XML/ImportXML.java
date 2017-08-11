/**
 * 
 */
package pmb.music.AllMusic.XML;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;

/**
 * Classe pour manipuler les fichiers XML.
 * @see #importXML(String)
 * @see #fusionFiles(String, boolean)
 * @author pmbroca
 */
public final class ImportXML {

	private static final Logger LOG = Logger.getLogger(ImportXML.class);
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.00", new DecimalFormatSymbols(Locale.US));
	private static Comparator<Composition> byRecord = (c1, c2) -> c1.getRecordType().toString().compareToIgnoreCase(c2.getRecordType().toString());
	private static Comparator<Composition> byTitre = (c1, c2) -> c1.getTitre().compareToIgnoreCase(c2.getTitre());
	private static Comparator<Composition> byArtist = (c1, c2) -> c1.getArtist().compareToIgnoreCase(c2.getArtist());
	
	private ImportXML() {}

	/**
	 * Import le fichier donné et extrait les {@link Composition}.
	 * @param uri {@link String} le chemin absolu du fichier
	 * @return les compos extraites
	 */
	public static List<Composition> importXML(String uri) {
		SAXParserFactory fabrique = SAXParserFactory.newInstance();
		SAXParser parseur = null;
		try {
			parseur = fabrique.newSAXParser();
		} catch (ParserConfigurationException | SAXException e) {
			LOG.error("Erreur lors de la création du parseur", e);
		}

		File fichier = new File(uri);
		CompoHandler handler = new CompoHandler();
		if (fichier.length() > 0 && parseur != null) {
			try {
				parseur.parse(fichier, handler);
			} catch (SAXException | IOException e) {
				LOG.error("Erreur lors de la lecture du fichier", e);
				LOG.error(uri);
			}
		}
		return handler.getCompoList();
	}

	/**
	 * Fusionne tous les fichiers à l'endroit indiqué dans {@code final.xml}.
	 * Export le fichier final (et crée une sauvegarde de ce fichier dans
	 * history) et renvoie la liste de Composition.
	 * 
	 * @param dirName le dossier où se situe les fichiers
	 * @param getFinal si on fusionne aussi le fichier {@code final.xml} avec les autres fichiers
	 * @return la liste des {@link Composition} des fichiers
	 * @throws IOException
	 */
	public static List<Composition> fusionFiles(String dirName, boolean getFinal) throws IOException {
		LOG.debug("Start fusionFiles");
		File dir = new File(dirName);
		List<File> files = new ArrayList<>();
		CompositionUtils.listFilesForFolder(dir, files, Constant.XML_EXTENSION, false);
		List<Composition> compoFusion = new ArrayList<>();
		for (File fileXML : files) {
			boolean isFinal = "final.xml".equalsIgnoreCase(fileXML.getName());
			if (!isFinal || (getFinal && isFinal)) {
				compoFusion.addAll(ImportXML.importXML(fileXML.getAbsolutePath()));
			}
		}
		// compoFusion = compoFusion.stream().sorted(byRecord.thenComparing(byTitre).thenComparing(byArtist)).collect(Collectors.toList());
		List<Composition> compoFinal = new ArrayList<>();
		int size = compoFusion.size();
		Double i = 0D;
		for (Composition compo : compoFusion) {
			Composition compoExist = CompositionUtils.compoExist(compoFinal, compo);
			if (compoExist == null) {
				compoFinal.add(compo);
			} else {
				Composition composition = compoFusion.get(compoFusion.indexOf(compoExist));
				composition.getFiles().addAll(compo.getFiles());
			}
			if(i%800==0) {
				LOG.debug("Fusion à " + DECIMAL_FORMAT.format(100*i/size) + "%");
			}
			i++;
		}
		ExportXML.exportXML(compoFinal, "final");
		LOG.debug("End fusionFiles");
		return compoFinal;
	}

	/**
	 * Fusionne le fichier donné avec {@code final.xml}. Export le fichier final
	 * (et crée une sauvegarde de ce fichier dans history) et renvoie la liste
	 * de Composition.
	 * 
	 * @param filePath le chemin du fichier
	 * @return la liste des {@link Composition} des fichiers
	 * @throws IOException
	 */
	public static List<Composition> fusionOneFile(String filePath) throws IOException {
		LOG.debug("Start fusionOneFile");
		File file = new File(filePath);
		File finalFile = new File(Constant.FINAL_FILE_PATH);
		List<Composition> compoFusion = new ArrayList<>();
		compoFusion.addAll(ImportXML.importXML(file.getAbsolutePath()));
		compoFusion.addAll(ImportXML.importXML(finalFile.getAbsolutePath()));

		List<Composition> compoFinal = new ArrayList<>();
		for (Composition compo : compoFusion) {
			Composition compoExist = CompositionUtils.compoExist(compoFinal, compo);
			if (compoExist == null) {
				compoFinal.add(compo);
			} else {
				Composition composition = compoFusion.get(compoFusion.indexOf(compoExist));
				composition.getFiles().addAll(compo.getFiles());
			}
		}
		ExportXML.exportXML(compoFinal, "final");
		LOG.debug("End fusionOneFile");
		return compoFinal;
	}

}
