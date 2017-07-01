/**
 * 
 */
package pmb.music.AllMusic.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.codehaus.plexus.util.FileUtils;

import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;

/**
 * @author i2113mj
 * 
 */
public class FichierUtils {

	@SuppressWarnings("rawtypes")
	public static Vector convertListForJTable(List<Fichier> fList) {
		System.out.println("Start convertListForJTable");
		Vector<Vector<Object>> result = new Vector<Vector<Object>>();
		for (int i = 0; i < fList.size(); i++) {
			Fichier f = fList.get(i);
			Vector<Object> v = new Vector<>();
			v.addElement(f.getAuthor());
			v.addElement(f.getFileName());
			v.addElement(f.getPublishYear());
			v.addElement(f.getCategorie().toString());
			v.addElement(f.getRangeDateBegin() + " - " + f.getRangeDateEnd());
			v.addElement(Constant.SDF_DTTM.format(f.getCreationDate()));
			v.addElement(f.getSize());
			v.addElement(f.getClassement());
			v.addElement(f.getSorted().toString().toUpperCase());
			result.add(v);
		}
		System.out.println("End convertListForJTable");
		return result;
	}

	public static void findDuplicateFichierInCompo(List<Composition> allCompo) {
		for (Composition composition : allCompo) {
			List<Fichier> files = composition.getFiles();
			List<String> names = new ArrayList<>();
			for (Fichier fichier : files) {
				names.add(fichier.getFileName());
			}
			Set<String> set = new HashSet<String>(names);
			if (set.size() < names.size()) {
				System.out.println("Duplicates for: " + composition);
				System.out.println("");
			}
		}
	}

	/**
	 * Crée le dossier si il n'existe pas.
	 * @param nomDir le chemin du dossier
	 */
	public static void createFolderIfNotExists(String nomDir) {
		if (!FileUtils.fileExists(nomDir)) {
			FileUtils.mkdir(nomDir);
		}
	}
}
