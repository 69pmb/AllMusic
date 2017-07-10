/**
 * 
 */
package pmb.music.AllMusic.utils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import pmb.music.AllMusic.model.Fichier;

/**
 * Classe utilitaire pour la gestion des {@link Fichier}.
 */
public class FichierUtils {

	private static final Logger LOG = Logger.getLogger(FichierUtils.class);

	private FichierUtils() {
	}

	/**
	 * Convertit une liste de {@link Fichier} en {@link Vector}.
	 * 
	 * @param fList {@code List<Fichier>} la liste à convertir
	 * @return Vector<Vector<Object>> la liste convertie
	 */
	@SuppressWarnings("rawtypes")
	public static Vector convertListForJTable(List<Fichier> fList) {
		LOG.debug("Start convertListForJTable");
		Vector<Vector<Object>> result = new Vector<Vector<Object>>();
		for (int i = 0; i < fList.size(); i++) {
			Fichier f = fList.get(i);
			Vector<Object> v = new Vector<>();
			v.addElement(f.getAuthor());
			v.addElement(f.getFileName());
			v.addElement(f.getPublishYear());
			v.addElement(f.getCategorie().toString());
			v.addElement(f.getRangeDateBegin() + " - " + f.getRangeDateEnd());
			v.addElement(new Constant().getSdfDttm().format(f.getCreationDate()));
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
	 * Supprime tous les fichiers historisés sauf le plus récent.
	 * 
	 * @throws ParseException
	 * @throws IOException
	 */
	public static void cleanHistory() throws IOException {
		LOG.debug("Start cleanHistory");
		// Création d'une map avec:
		// key nom du fichier sans date
		// value liste des dates du fichier
		List<File> files = new ArrayList<>();
		CompositionUtils.listFilesForFolder(new File(Constant.HISTORY_PATH), files, Constant.XML_EXTENSION, false);
		Map<String, List<Date>> list = new HashMap<String, List<Date>>();
		for (File file : files) {
			String nomFichier = StringUtils.substringBefore(file.getName(), Constant.SEPARATOR_DATE_HISTORY);
			String date = StringUtils.substringBetween(file.getName(), Constant.SEPARATOR_DATE_HISTORY, Constant.XML_EXTENSION);
			if (list.get(nomFichier) == null) {
				list.put(nomFichier, new ArrayList<Date>());
			}
			try {
				list.get(nomFichier).add(new Constant().getSdfHistory().parse(date));
			} catch (ParseException e) {
				LOG.error("Erreur lors du parsing d'une date", e);
			}
		}

		Set<String> keySet = list.keySet();
		for (String key : keySet) {
			// Tri des dates, la plus récente en 1er
			Collections.sort(list.get(key), Collections.reverseOrder());
			String path = Constant.HISTORY_PATH + key + Constant.SEPARATOR_DATE_HISTORY;
			// Suppression des fichiers sauf du 1er
			for (int i = 1; i < list.get(key).size(); i++) {
				String toDelete = path + new Constant().getSdfHistory().format(list.get(key).get(i)) + Constant.XML_EXTENSION;
				if (!new File(toDelete).delete()) {
					LOG.debug(toDelete + " n'a pas pu etre supprimé");
				}
			}
		}

		LOG.debug("End cleanHistory");
	}
}
