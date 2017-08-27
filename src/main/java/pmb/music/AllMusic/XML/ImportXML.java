/**
 * 
 */
package pmb.music.AllMusic.XML;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JTextArea;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;

/**
 * Classe pour manipuler les fichiers XML.
 * 
 * @see #importXML(String)
 * @see #fusionFiles(String, boolean)
 * @author pmbroca
 */
public final class ImportXML {

	private static final Logger LOG = Logger.getLogger(ImportXML.class);

	private ImportXML() {
	}

	/**
	 * Import le fichier donné et extrait les {@link Composition}.
	 * 
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
	 * @param resultLabel {@link JTextArea} la zone de texte pour afficher
	 * @return la liste des {@link Composition} des fichiers
	 * @throws IOException
	 */
	public static List<Composition> fusionFiles(String dirName, final JTextArea resultLabel) throws IOException {
		LOG.debug("Start fusionFiles");
		File dir = new File(dirName);
		List<File> files = new ArrayList<>();
		CompositionUtils.listFilesForFolder(dir, files, Constant.XML_EXTENSION, false);
		List<Composition> compoFusionSong = new ArrayList<>();
		List<Composition> compoFusionAlbum = new ArrayList<>();
		for (File fileXML : files) {
			boolean isFinal = Constant.FINAL_FILE.equalsIgnoreCase(fileXML.getName());
			if (!isFinal) {
				List<Composition> importXML = ImportXML.importXML(fileXML.getAbsolutePath());
				if (RecordType.ALBUM.equals(importXML.get(0).getRecordType())) {
					compoFusionAlbum.addAll(importXML);
				} else {
					compoFusionSong.addAll(importXML);
				}
			}
		}
		// compoFusion =
		// compoFusion.stream().sorted(byRecord.thenComparing(byTitre).thenComparing(byArtist)).collect(Collectors.toList());
		List<Composition> compoFinal = new ArrayList<>();
		BigDecimal sizeBG = new BigDecimal(compoFusionAlbum.size() + compoFusionSong.size());
		int modulo = Math.round(sizeBG.divide(BigDecimal.valueOf(5000D)).floatValue());
		LOG.debug(modulo);
		LOG.debug(sizeBG.intValue());
		compoFinal = fusion(resultLabel, compoFusionAlbum, compoFinal, 0, modulo, sizeBG);
		compoFinal = fusion(resultLabel, compoFusionSong, compoFinal, compoFusionAlbum.size(), modulo, sizeBG);
		ExportXML.exportXML(compoFinal, Constant.FINAL_FILE);
		LOG.debug("End fusionFiles");
		return compoFinal;
	}

	private static List<Composition> fusion(final JTextArea resultLabel, List<Composition> compoFusion, List<Composition> compoFinal, int i, int modulo,
			BigDecimal sizeBG) {
		for (Composition compo : compoFusion) {
			Composition compoExist = CompositionUtils.compoExist(compoFinal, compo);
			if (compoExist == null) {
				compoFinal.add(compo);
			} else {
				compoExist.getFiles().addAll(compo.getFiles());
			}
			if (i % modulo == 0) {
				updateResultLabel(
						Arrays.asList("Fusion à "
								+ BigDecimal.valueOf(100D).setScale(2).multiply(new BigDecimal(i)).divide(sizeBG, RoundingMode.HALF_UP).doubleValue() + "%"),
						resultLabel);
			}
			i++;
		}
		return compoFinal;
	}

	private static void updateResultLabel(List<String> result2, final JTextArea resultLabel) {
		StringBuilder s = new StringBuilder();
		for (String string : result2) {
			s.append(string).append(Constant.NEW_LINE);
		}
		resultLabel.setText(s.toString());
		resultLabel.setForeground(new Color(243, 16, 16));
		Font labelFont = resultLabel.getFont();
		resultLabel.setFont(new Font(labelFont.getName(), labelFont.getStyle(), 20));
	}

}
