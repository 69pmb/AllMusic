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
import org.apache.log4j.Logger;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;

/**
 * Classe utilitaire pour les {@link Composition}.
 */
public class CompositionUtils {

	private static final Logger LOG = Logger.getLogger(CompositionUtils.class);

	private CompositionUtils() {
	}

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

	/**
	 * Print la liste de compo.
	 * @param allCompo la liste à afficher
	 */
	public static void printCompoList(List<Composition> allCompo) {
		LOG.debug("Start printCompoList");
		LOG.debug("Compo: " + allCompo.size());
		for (Composition composition : allCompo) {
			LOG.debug(composition);
		}
		LOG.debug("End printCompoList");
	}

	/**
	 * Détermine si la compo existe dans la liste donnée.
	 * 
	 * @param compos la liste
	 * @param c la compo à chercher
	 * @return {@code null} rien trouvé, la 1ère {@link Composition} trouvée sinon
	 */
	public static Composition compoExist(List<Composition> compos, Composition c) {
		Composition res = null;
		for (Composition composition : compos) {
			if (c.getRecordType().equals(composition.getRecordType())) {
				String compoTitre = Constant.PATTERN_PUNCTUATION.matcher(composition.getTitre()).replaceAll("");
				String cTitre = Constant.PATTERN_PUNCTUATION.matcher(c.getTitre()).replaceAll("");
				if (cTitre.equalsIgnoreCase(compoTitre)) {
					String compoArtist = Constant.PATTERN_PUNCTUATION.matcher(composition.getArtist()).replaceAll("");
					String cArtist = Constant.PATTERN_PUNCTUATION.matcher(c.getArtist()).replaceAll("");
					if (cArtist.equalsIgnoreCase(compoArtist)) {
						res = composition;
						break;
					}
				}
			}
		}
		return res;
	}

	/**
	 * Convertit une liste de {@link Composition} en {@link Vector}.
	 * @param compoList {@code List<Composition>} la liste de compo
	 * @return {@code Vector<Vector<Object>>} la liste de vecteur
	 */
	public static Vector<Vector<Object>> convertCompositionListToVector(List<Composition> compoList) {
		LOG.debug("Start convertCompositionListToVector");
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
		LOG.debug("End convertCompositionListToVector");
		return result;
	}

	/**
	 * Convertit une liste de compositions en vecteur pour l'onglet artist. On
	 * compte pour chaque artiste le nombre de chanson et d'album enregistrés
	 * @param compoList {@code List<Composition>} la liste de compo
	 * @return {@code Vector<Vector<Object>>} le resultat
	 */
	public static Vector<Vector<Object>> convertCompositionListToArtistVector(List<Composition> compoList) {
		LOG.debug("Start convertCompositionListToArtistVector");
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
		LOG.debug("End convertCompositionListToArtistVector");
		return result;
	}

	/**
	 * Cherche une {@link Composition} dans une liste donnée en fonction de
	 * l'artiste, du titre et de son type. Recherche stricte.
	 * @param compoList {@link List<Composition>} une liste de compo
	 * @param artist {@link String} un artiste
	 * @param titre {@link String} un titre de chanson ou d'album
	 * @param type {@link String} album ou chanson
	 * @return une seule {@link Composition}
	 * @throws MyException si plusieurs résultat
	 */
	public static Composition findByArtistTitreAndType(List<Composition> compoList, String artist, String titre, String type) throws MyException {
		LOG.debug("Start findByArtistTitreAndType");
		Map<String, String> criteria = new HashMap<>();
		criteria.put("artist", artist);
		criteria.put("titre", titre);
		criteria.put("type", type);

		List<Composition> search = SearchUtils.searchStrictly(compoList, criteria);
		if (search.size() > 1) {
			CompositionUtils.printCompoList(search);
			throw new MyException("Trop de résultat dans findByArtistTitreAndType: " + artist + " " + titre + " " + type);
		}
		if (!search.isEmpty()) {
			LOG.debug("End findByArtistTitreAndType");
			return search.get(0);
		} else {
			LOG.debug("End findByArtistTitreAndType, no result");
			return new Composition();
		}
	}

	/**
	 * Cherche une {@link Composition} dans une liste donnée en fonction de
	 * l'artiste. Recherche stricte.
	 * @param compoList {@link List<Composition>} une liste de compo
	 * @param artist {@link String} un artiste
	 * @return la composition trouvée
	 */
	public static List<Composition> findByArtist(List<Composition> compoList, String artist) {
		LOG.debug("Start findByArtistTitreAndType");
		Map<String, String> criteria = new HashMap<>();
		criteria.put("artist", artist);

		List<Composition> search = SearchUtils.searchStrictly(compoList, criteria);
		if (!search.isEmpty()) {
			LOG.debug("End findByArtistTitreAndType");
			return search;
		} else {
			LOG.debug("End findByArtistTitreAndType, no result");
			return new ArrayList<>();
		}
	}

	/**
	 * Supprime dans les fichiers XML, la composition donnée.
	 * @param toRemove la {@link Composition} à supprimer des fichiers
	 */
	public static void removeCompositionsInFiles(Composition toRemove) {
		LOG.debug("Start removeCompositionsInFiles");
		for (Fichier file : toRemove.getFiles()) {
			// Récupération des compositions du fichier XML
			List<Composition> importXML = ImportXML.importXML(Constant.XML_PATH + file.getFileName() + Constant.XML_EXTENSION);
			// Suppresion de la liste de la composition à enlever
			int indexOf = importXML.indexOf(new Composition(toRemove.getArtist(), Arrays.asList(file), toRemove.getTitre(), toRemove.getRecordType()));
			if (indexOf != -1) {
				importXML.remove(indexOf);
			} else {
				LOG.error("indexOf -1: " + toRemove.getArtist() + " " + toRemove.getFiles() + " " + toRemove.getTitre() + " " + toRemove.getRecordType());
			}
			try {
				// Sauvegarde des modifications
				ExportXML.exportXML(importXML, file.getFileName());
			} catch (IOException e) {
				LOG.error("Erreur lors de la suppresion d'une composition dans le fichier: " + file.getFileName(), e);
			}
		}
		LOG.debug("End removeCompositionsInFiles");
	}
}
