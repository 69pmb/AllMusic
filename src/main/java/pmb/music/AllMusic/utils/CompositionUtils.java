package pmb.music.AllMusic.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;

public class CompositionUtils {

	/**
	 * Récupère la liste des fichiers d'un dossier
	 * @param folder le dossier où chercher
	 * @param files la liste qui contiendra les résultats
	 * @param extension l'extension des fichiers à chercher
	 * @param recursive si la recherche doit etre récursive ou non
	 */
	public static void listFilesForFolder(final File folder, List<File> files, String extension, boolean recursive) {
		if (!folder.isDirectory()) {
			files.add(folder);
			return;
		}
		for (final File fileEntry : folder.listFiles()) {
			if (recursive && fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry, files, extension, recursive);
			} else if (StringUtils.endsWith(fileEntry.getName(), extension)) {
				files.add(fileEntry);
			}
		}
	}

	public static void printCompoList(List<Composition> allCompo) {
		System.out.println("Start printCompoList");
		System.out.println("Compo: " + allCompo.size());
		for (Composition composition : allCompo) {
			System.out.println(composition);
			// List<Fichier> files3 = composition.getFiles();
			// for (Fichier fichier : files3) {
			// System.out.println(fichier);
			// }
		}
		System.out.println("End printCompoList");
	}

	public static Composition compoExist(List<Composition> compos, Composition c) {
		Composition res = null;
		for (Composition composition : compos) {
			if (c.getRecordType().equals(composition.getRecordType())
					&& c.getArtist().equalsIgnoreCase(composition.getArtist())
					&& c.getTitre().equalsIgnoreCase(composition.getTitre())) {
				res = composition;
				break;
			}
		}
		return res;
	}

	public static Vector<Vector<Object>> convertCompositionListToVector(List<Composition> compoList) {
		Vector<Vector<Object>> result = new Vector<Vector<Object>>();
		for (int i = 0; i < compoList.size(); i++) {
			Composition composition = compoList.get(i);
			Vector<Object> v = new Vector<>();
			v.addElement(composition.getArtist());
			v.addElement(composition.getTitre());
			v.addElement(composition.getRecordType().toString());
			v.addElement(composition.getFiles().size());
			v.addElement(new Boolean(false));
			result.addElement(v);
		}
		return result;
	}

	public static Vector<Vector<Object>> convertCompositionListToArtistVector(List<Composition> compoList) {
		Vector<Vector<Object>> temp = new Vector<Vector<Object>>();
		for (int i = 0; i < compoList.size(); i++) {
			Composition composition = compoList.get(i);
			Vector<Object> v = new Vector<>();
			v.addElement(composition.getArtist());
			v.addElement(composition.getFiles().size());
			if (composition.getRecordType().equals(RecordType.ALBUM)) {
				v.addElement(composition.getFiles().size());
			} else {
				v.addElement(0);
			}
			if (composition.getRecordType().equals(RecordType.SONG)) {
				v.addElement(composition.getFiles().size());
			} else {
				v.addElement(0);
			}
			temp.addElement(v);
		}
		Vector<Vector<Object>> result = new Vector<Vector<Object>>();
		for (Vector<Object> v : temp) {
			boolean res = false;
			for (int i = 0; i < result.size(); i++) {
				if (((String) v.get(0)).equalsIgnoreCase((String) result.get(i).get(0))) {
					result.get(i).set(1, (int) result.get(i).get(1) + (int) v.get(1));
					result.get(i).set(2, (int) result.get(i).get(2) + (int) v.get(2));
					result.get(i).set(3, (int) result.get(i).get(3) + (int) v.get(3));
					res = true;
					break;
				}
			}
			if (!res) {
				result.add(v);
			}
		}
		return result;
	}

	public static Composition findByArtistTitreAndType(List<Composition> compoList, String artist, String titre,
			String type) throws MyException {
		System.out.println("Start findByArtistTitreAndType");
		Map<String, String> criteria = new HashMap<>();
		criteria.put("artist", artist);
		criteria.put("titre", titre);
		criteria.put("type", type);

		List<Composition> search = SearchUtils.searchStrictly(compoList, criteria);
		if (search.size() > 1) {
			CompositionUtils.printCompoList(search);
			throw new MyException("Trop de résultat dans findByArtistTitreAndType: " + artist + " " + titre + " "
					+ type);
		}
		if (!search.isEmpty()) {
			System.out.println("End findByArtistTitreAndType");
			return search.get(0);
		} else {
			System.out.println("End findByArtistTitreAndType, no result");
			return new Composition();
		}
	}

	public static List<Composition> findByArtist(List<Composition> compoList, String artist) throws MyException {
		System.out.println("Start findByArtistTitreAndType");
		Map<String, String> criteria = new HashMap<>();
		criteria.put("artist", artist);

		List<Composition> search = SearchUtils.searchStrictly(compoList, criteria);
		if (!search.isEmpty()) {
			System.out.println("End findByArtistTitreAndType");
			return search;
		} else {
			System.out.println("End findByArtistTitreAndType, no result");
			return new ArrayList<Composition>();
		}
	}

	public static void removeCompositionsInFiles(Composition toRemove) {
		System.out.println("Start removeCompositionsInFiles");
		for (Fichier file : toRemove.getFiles()) {
			List<Composition> importXML = ImportXML.importXML(Constant.RESOURCES_ABS_DIRECTORY + file.getFileName()
					+ Constant.XML_EXTENSION);
			importXML.remove(importXML.indexOf(new Composition(toRemove.getArtist(), Arrays.asList(file), toRemove
					.getTitre(), toRemove.getRecordType())));
			try {
				ExportXML.exportXML(importXML, file.getFileName());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("End removeCompositionsInFiles");
	}
}
