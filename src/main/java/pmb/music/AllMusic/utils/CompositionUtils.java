package pmb.music.AllMusic.utils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
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
		allCompo.stream().forEach(LOG::debug);
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
		JaroWinklerDistance jaro = new JaroWinklerDistance();
		for (Composition composition : compos) {
			if (c.getRecordType().equals(composition.getRecordType())) {
				String compoTitre = Constant.PATTERN_PUNCTUATION.matcher(composition.getTitre()).replaceAll("").toLowerCase();
				if (StringUtils.isBlank(compoTitre)) {
					compoTitre = composition.getTitre().toLowerCase();
				}
				String cTitre = Constant.PATTERN_PUNCTUATION.matcher(c.getTitre()).replaceAll("").toLowerCase();
				if (StringUtils.isBlank(cTitre)) {
					cTitre = c.getTitre().toLowerCase();
				}
				if (new BigDecimal(jaro.apply(compoTitre, cTitre)).compareTo(Constant.SCORE_LIMIT_TITLE_FUSION) > 0
						&& artistJaroEquals(composition.getArtist(), c.getArtist(), jaro) != null) {
					res = composition;
					break;
				}
			}
		}
		return res;
	}
	
	/**
	 * Détermine si 2 artistes sont égaux en utilisant la distance de JaroWinkler.
	 * 
	 * @param artist un artist
	 * @param a un autre artist
	 * @param jaro une instance de {@link JaroWinklerDistance} 
	 * @return {@code null} rien trouvé, le 1er artiste sinon
	 */
	public static String artistJaroEquals(String artist, String a, JaroWinklerDistance jaro) {
		String compoArtist = Constant.PATTERN_PUNCTUATION.matcher(artist).replaceAll("").toLowerCase();
		if (StringUtils.startsWith(compoArtist, "the")) {
			compoArtist = StringUtils.substringAfter(compoArtist, "the");
		}
		if (StringUtils.isBlank(compoArtist)) {
			compoArtist = artist.toLowerCase();
		}
		String cArtist = Constant.PATTERN_PUNCTUATION.matcher(a).replaceAll("").toLowerCase();
		if (StringUtils.startsWith(cArtist, "the")) {
			cArtist = StringUtils.substringAfter(cArtist, "the");
		}
		if (StringUtils.isBlank(cArtist)) {
			cArtist = a.toLowerCase();
		}
		if (new BigDecimal(jaro.apply(compoArtist, cArtist)).compareTo(Constant.SCORE_LIMIT_ARTIST_FUSION) > 0) {
			return artist;
		}
		return null;
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
		Vector<Vector<Object>> result = new Vector<Vector<Object>>();
		for (int i = 0; i < compoList.size(); i++) {
			if(Thread.currentThread().isInterrupted()) {
				LOG.debug("Thread interrupted, End convertCompositionListToArtistVector");
				return result;
			}
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
		JaroWinklerDistance jaro = new JaroWinklerDistance();
		for (Vector<Object> v : temp) {
			boolean res = false;
			for (int i = 0; i < result.size(); i++) {
				if(Thread.currentThread().isInterrupted()) {
					LOG.debug("Thread interrupted, End convertCompositionListToArtistVector");
					return result;
				}
				if (artistJaroEquals((String) v.get(0),(String) result.get(i).get(0), jaro) != null) {
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
			LOG.debug("Critères: " + artist + " " + titre + " " + type);
			return new Composition();
		}
	}

	/**
	 * Cherche une {@link Composition} dans une liste donnée en fonction de
	 * l'artiste. Recherche jaro.
	 * @param compoList {@link List<Composition>} une liste de compo
	 * @param artist {@link String} un artiste
	 * @return la composition trouvée
	 */
	public static List<Composition> findByArtist(List<Composition> compoList, String artist) {
		LOG.debug("Start findByArtistTitreAndType");
		Map<String, String> criteria = new HashMap<>();
		criteria.put("artist", artist);

		List<Composition> search = SearchUtils.searchJaro(compoList, criteria, false);
		if (!search.isEmpty()) {
			LOG.debug("End findByArtistTitreAndType");
			return search;
		} else {
			LOG.debug("End findByArtistTitreAndType, no result");
			return new ArrayList<>();
		}
	}
	
	/**
	 * Convertit une liste de {@link Composition} en {@link Vector}.
	 * 
	 * @param cList {@code List<Composition>} la liste à convertir
	 * @return Vector<Vector<Object>> la liste convertie
	 */
	@SuppressWarnings("rawtypes")
	public static Vector convertCompoListForJTable(List<Composition> cList) {
		LOG.debug("Start convertCompoListForJTable");
		Vector<Vector<Object>> result = new Vector<Vector<Object>>();
		for (int i = 0; i < cList.size(); i++) {
			Composition f = cList.get(i);
			Vector<Object> v = new Vector<>();
			v.addElement(f.getArtist());
			v.addElement(f.getTitre());
			v.addElement(f.getFiles().get(0).getClassement());
			v.addElement(f.getRecordType().toString());
			result.add(v);
		}
		LOG.debug("End convertCompoListForJTable");
		return result;
	}

	/**
	 * Supprime dans les fichiers XML, la composition donnée.
	 * @param toRemove la {@link Composition} à supprimer des fichiers
	 * @throws MyException 
	 */
	public static void removeCompositionsInFiles(Composition toRemove) throws MyException {
		LOG.debug("Start removeCompositionsInFiles");
		for (Fichier file : toRemove.getFiles()) {
			// Récupération des compositions du fichier XML
			String filename = Constant.XML_PATH + file.getFileName() + Constant.XML_EXTENSION;
			List<Composition> importXML = ImportXML.importXML(filename);
			if(importXML.isEmpty()) {
				LOG.error("Fichier vide ! " + filename);
			}
			// Suppresion de la liste de la composition à enlever
			Composition toRemoveFromFile = CompositionUtils.compoExist(importXML, toRemove);
			if (toRemoveFromFile != null) {
				importXML.remove(toRemoveFromFile);
			} else {
				LOG.error(filename + Constant.NEW_LINE);
				new MyException("compoExist null: " + toRemove.getArtist() + " " + toRemove.getTitre() + " " + toRemove.getRecordType());
			}
			try {
				// Sauvegarde des modifications
				ExportXML.exportXML(importXML, file.getFileName());
			} catch (IOException e) {
				new MyException("Erreur lors de la suppresion d'une composition dans le fichier: " + file.getFileName(), e);
			}
		}
		LOG.debug("End removeCompositionsInFiles");
	}
	
	/**
	 * Modifie dans les fichiers XML, la composition donnée.
	 * @param toModif la {@link Composition} à modifier des fichiers
	 * @param v 
	 * @throws MyException 
	 */
	public static void modifyCompositionsInFiles(Composition toModif, Vector<String> v) throws MyException {
		LOG.debug("Start modifyCompositionsInFiles");
		for (Fichier file : toModif.getFiles()) {
			// Récupération des compositions du fichier XML
			String filename = Constant.XML_PATH + file.getFileName() + Constant.XML_EXTENSION;
			List<Composition> importXML = ImportXML.importXML(filename);
			if(importXML.isEmpty()) {
				LOG.error("Fichier vide ! " + filename);
			}
			// Modificaton de la liste de la composition à enlever
			Composition toModifFromFile = CompositionUtils.compoExist(importXML, toModif);
			if (toModifFromFile != null) {
				int indexOf = SearchUtils.indexOf(importXML, toModifFromFile);
				Composition composition = importXML.get(indexOf);
				composition.setArtist(v.get(0));
				composition.setTitre(v.get(1));
				importXML.set(indexOf, composition);
				try {
					// Sauvegarde des modifications
					ExportXML.exportXML(importXML, file.getFileName());
				} catch (IOException e) {
					new MyException("Erreur lors de la modification d'une composition dans le fichier: " + file.getFileName(), e);
				}
			} else {
				LOG.error(filename + Constant.NEW_LINE);
				new MyException("compoExist null: " + toModif.getArtist() + " " + toModif.getFiles() + " " + toModif.getTitre() + " " + toModif.getRecordType());
			}
		}
		LOG.debug("End modifyCompositionsInFiles");
	}
}
