/**
 * 
 */
package pmb.music.AllMusic.utils;

import java.util.List;
import java.util.Vector;

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
	 * @return Vector<Vector<Object>> la liste convertie
	 */
	@SuppressWarnings("rawtypes")
	public static Vector convertListForJTable(List<Fichier> fList, Composition compParente) {
		LOG.debug("Start convertListForJTable");
		Vector<Vector<Object>> result = new Vector<Vector<Object>>();
		for (int i = 0; i < fList.size(); i++) {
			Fichier f = fList.get(i);
			Vector<Object> v = new Vector<>();
			List<Composition> compo = ImportXML.importXML(Constant.XML_PATH + f.getFileName() + Constant.XML_EXTENSION);
			Composition c = null;
			try {
				c = CompositionUtils.findByRank(compo, f.getClassement(), compParente);
			} catch (MyException e) {
				LOG.error("Error when search in convertListForJTable", e);
			}
			if(c!=null) {
				v.addElement(c.getArtist() + " - " + c.getTitre());
			} else {
				v.addElement("");
			}
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
}
