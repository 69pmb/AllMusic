/**
 * 
 */
package pmb.music.AllMusic.XML;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;

/**
 * @author i2113mj
 * 
 */
public final class ImportXML {

	private static final Logger LOG = Logger.getLogger(ImportXML.class);

	private static volatile ImportXML importXML = null;

	private static List<Composition> compoList = new ArrayList<>();

	public static List<Composition> importXML(String uri) {
		SAXParserFactory fabrique = SAXParserFactory.newInstance();
		SAXParser parseur = null;
		try {
			parseur = fabrique.newSAXParser();
		} catch (ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}

		File fichier = new File(uri);
		CompoHandler handler = new CompoHandler();
		if (fichier.length() > 0 && parseur != null) {
			try {
				parseur.parse(fichier, handler);
			} catch (SAXException | IOException e) {
				e.printStackTrace();
			}
		}
		return handler.getCompoList();
	}

	public final static ImportXML getInstance(String fileName) {
		LOG.debug("Start getInstance");
		// Le "Double-Checked Singleton"/"Singleton doublement vérifié" permet
		// d'éviter un appel coûteux à synchronized,
		// une fois que l'instanciation est faite.
		if (ImportXML.importXML == null) {
			// Le mot-clé synchronized sur ce bloc empêche toute instanciation
			// multiple même par différents "threads".
			// Il est TRES important.
			synchronized (ImportXML.class) {
				if (ImportXML.importXML == null) {
					ImportXML.importXML = new ImportXML(fileName);
				}
			}
		}
		// CompositionUtils.printCompoList(ImportXML.importXML.compoList);
		LOG.debug("End getInstance");
		return ImportXML.importXML;
	}

	private ImportXML(String file) {
		super();
		ImportXML.importXML(file);
	}

	public static List<Composition> getAllCompo(String fileName) {
		if (compoList.isEmpty()) {
			getInstance(fileName);
		}
		return compoList;
	}

	/**
	 * Fusionne tous les fichiers à l'endroit indiqué dans {@code final.xml}.
	 * Export le fichier final (et crée une sauvegarde de ce fichier dans
	 * history) et renvoie la liste de Composition.
	 * 
	 * @param dirName le dossier où se situe les fichiers
	 * @param getFinal si on fusionne aussi le fichier {@code final.xml} avec
	 *            les autres fichiers
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
			boolean isFinal = fileXML.getName().equals("final.xml");
			if (!isFinal || (getFinal && isFinal)) {
				compoFusion.addAll(ImportXML.importXML(fileXML.getAbsolutePath()));
			}
		}
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
