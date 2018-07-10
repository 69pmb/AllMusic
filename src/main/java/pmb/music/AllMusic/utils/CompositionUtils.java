package pmb.music.AllMusic.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.model.Score;

/**
 * Classe utilitaire pour les {@link Composition}.
 * @author pmbroca
 */
public class CompositionUtils {

	private static final Logger LOG = Logger.getLogger(CompositionUtils.class);

	private CompositionUtils() {
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
	 * C'est à dire, si le {@link RecordType} est le même et 
	 * si le titre et l'artiste sont similaires en utilisant leur score de JaroWinkler.
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
				// Suppression de la ponctuation
				String compoTitre = SearchUtils.removePunctuation(composition.getTitre());
				if (StringUtils.isBlank(compoTitre)) {
					// Si le titre n'est constitué que de ponctuation
					compoTitre = composition.getTitre().toLowerCase();
				}
				// Suppression de la ponctuation
				String cTitre = SearchUtils.removePunctuation(c.getTitre());
				if (StringUtils.isBlank(cTitre)) {
					// Si le titre n'est constitué que de ponctuation
					cTitre = c.getTitre().toLowerCase();
				}
				// Si le titre et l'artist sont similaires, on sort
				if (SearchUtils.isEqualsJaro(jaro, compoTitre, cTitre, Constant.SCORE_LIMIT_TITLE_FUSION)
						&& artistJaroEquals(composition.getArtist(), c.getArtist(), jaro,
								Constant.SCORE_LIMIT_ARTIST_FUSION) != null) {
					res = composition;
					break;
				} 
			}
		}
		return res;
	}

	public static boolean isCompositionEquals(Composition c1, Composition c2, String type) {
		if (!c1.getRecordType().toString().equals(type) || !c2.getRecordType().toString().equals(type)) {
			return false;
		}
		final JaroWinklerDistance jaro = new JaroWinklerDistance();
		String artist1 = c1.getArtist();
		String artist2 = c2.getArtist();
		boolean similarArtist = StringUtils.startsWithIgnoreCase(artist1, artist2)
				|| StringUtils.startsWithIgnoreCase(artist2, artist1);
		if (similarArtist) {
			String titre1 = c1.getTitre().toLowerCase();
			String titre2 = c2.getTitre().toLowerCase();
			String remParTitre1 = SearchUtils.removeParentheses(titre1);
			String parTitre1 = SearchUtils.removePunctuation2(remParTitre1);
			String remParTitre2 = SearchUtils.removeParentheses(titre2);
			String parTitre2 = SearchUtils.removePunctuation2(remParTitre2);
			boolean parTitreEqu = StringUtils.startsWithIgnoreCase(parTitre1, parTitre2)
					|| StringUtils.startsWithIgnoreCase(parTitre2, parTitre1);
			if (parTitreEqu
					&& (StringUtils.containsIgnoreCase(remParTitre1, " and ")
							|| StringUtils.containsIgnoreCase(remParTitre2, " and "))
					&& !StringUtils.containsIgnoreCase(remParTitre1, "/")
					&& !StringUtils.containsIgnoreCase(remParTitre2, "/")) {
				String andTitre1 = SearchUtils.removePunctuation2(StringUtils.substringBefore(remParTitre1, " and "));
				String andTitre2 = SearchUtils.removePunctuation2(StringUtils.substringBefore(remParTitre2, " and "));
				parTitre1 = andTitre1;
				parTitre2 = andTitre2;
				parTitreEqu = false;
			}
			return SearchUtils.isEqualsJaro(jaro, parTitre1, parTitre2,
					Constant.SCORE_LIMIT_TITLE_FUSION);
		}
		return false;
	}
	
	/**
	 * Détermine si 2 artistes sont égaux en utilisant la distance de JaroWinkler.
	 * 
	 * @param artist un artist
	 * @param a un autre artist
	 * @param jaro une instance de {@link JaroWinklerDistance} 
	 * @return {@code null} rien trouvé, le 1er artiste sinon
	 */
	public static String artistJaroEquals(String artist, String a, JaroWinklerDistance jaro, BigDecimal scoreLimit) {
		// Suppression de la ponctuation
		String compoArtist = SearchUtils.removePunctuation(artist);
		if (StringUtils.startsWith(compoArtist, "the")) {
			// Si l'artist commence par The, on supprime le The
			compoArtist = StringUtils.substringAfter(compoArtist, "the");
		}
		if (StringUtils.isBlank(compoArtist)) {
			// Si l'artiste est constitué que de ponctuation ou de The
			compoArtist = artist.toLowerCase();
		}
		// Suppression de la ponctuation
		String cArtist = SearchUtils.removePunctuation(a);
		if (StringUtils.startsWith(cArtist, "the")) {
			// Si l'artist commence par The, on supprime le The
			cArtist = StringUtils.substringAfter(cArtist, "the");
		}
		if (StringUtils.isBlank(cArtist)) {
			// Si l'artiste est constitué que de ponctuation ou de The
			cArtist = a.toLowerCase();
		}
		if (SearchUtils.isEqualsJaro(jaro, compoArtist, cArtist, scoreLimit)) {
			// Si les artistes sont similaires on le retourne
			return artist;
		}
		return null;
	}

	/**
	 * Convertit une liste de {@link Composition} en {@link Vector}.
	 * @param compoList {@code List<Composition>} la liste de composition à convertir
	 * @param displayClassement si on affiche le classement de la composition ou son nombre de fichiers
	 * @param addBoolean si on ajoute une colonne de boolean remplie à false
	 * @param score {@link Score} constants to calculate the composition score
	 * @return {@code Vector<Vector<Object>>} la liste de vecteur convertie
	 */
	public static Vector<Vector<Object>> convertCompositionListToVector(List<Composition> compoList,
			boolean displayClassement, boolean addBoolean, Score score) {
		LOG.debug("Start convertCompositionListToVector");
		Vector<Vector<Object>> result = new Vector<Vector<Object>>();
		if (compoList == null || compoList.isEmpty()) {
			LOG.debug("End convertCompositionListToVector, empty list");
			return result;
		}
		for (int i = 0; i < compoList.size(); i++) {
			Composition composition = compoList.get(i);
			Vector<Object> v = new Vector<>();
			v.addElement(composition.getArtist());
			v.addElement(composition.getTitre());
			v.addElement(composition.getRecordType().toString());
			if (displayClassement) {
				v.addElement(composition.getFiles().get(0).getClassement());
			} else {
				v.addElement(composition.getFiles().size());
			}
			if (score != null) {
				v.addElement(calculateCompositionScore(score.getLogMax(composition.getRecordType()),
						score.getDoubleMedian(composition.getRecordType()), composition));
			}
			if (addBoolean) {
				v.addElement(new Boolean(false));
			}
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
				if (artistJaroEquals((String) v.get(0),(String) result.get(i).get(0), jaro, Constant.SCORE_LIMIT_ARTIST_FUSION) != null) {
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
	public static Composition findByArtistTitreAndType(List<Composition> compoList, String artist, String titre, String type, boolean isStrictly) throws MyException {
		LOG.debug("Start findByArtistTitreAndType");
		Map<String, String> criteria = new HashMap<>();
		criteria.put(SearchUtils.CRITERIA_ARTIST, artist);
		criteria.put(SearchUtils.CRITERIA_TITRE, titre);
		criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, type);
		
		List<Composition> search = new ArrayList<>();
		if(isStrictly) {
			search = SearchUtils.searchStrictly(compoList, criteria);
		} else {
			search = SearchUtils.searchJaro(compoList, criteria, false);
		}
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
	 * Cherche une composition dans une liste par son classement.
	 * @param compoList la liste de composition
	 * @param rank le classement de la composition recherchée
	 * @param compoParente la composition à chercher
	 * @return la composition trouvée
	 * @throws MyException
	 */
	public static Composition findByRank(List<Composition> compoList, int rank, Composition compoParente)
			throws MyException {
		LOG.debug("Start findByRank");
		List<Composition> filtered = compoList.stream().filter(f -> f.getFiles().get(0).getClassement() == rank)
				.collect(Collectors.toList());
		// LOG.debug("End findByRank, no result");
		return compoParente != null && !filtered.isEmpty() ? compoExist(filtered, compoParente) : filtered.get(0);
	}

	/**
	 * Recherche dans une liste de composition la 1ère composition qui a le même fichier que celui donné.
	 * (Même nom, même auteur et même classement par exemple)
	 * @param compoList la liste de composition
	 * @param fichier le fichier
	 * @return un {@link Optional} de composition
	 */
	public static Optional<Composition> findByFile(List<Composition> compoList, Fichier fichier) {
		LOG.debug("Start findByFileAndRank");
		Optional<Composition> filtered = compoList.stream()
				.filter(c -> c.getFiles().stream().anyMatch(f -> f.equals(fichier))).findFirst();
		LOG.debug("End findByFileAndRank");
		return filtered;
	}
	
	/**
	 * Cherche une {@link Composition} dans une liste donnée en fonction de
	 * l'artiste.
	 * @see SearchUtils#searchJaro(List, Map, boolean) 
	 * @param compoList {@link List<Composition>} une liste de compo
	 * @param artist {@link String} un artiste
	 * @return la composition trouvée
	 */
	public static List<Composition> findByArtist(List<Composition> compoList, String artist) {
		LOG.debug("Start findByArtistTitreAndType");
		Map<String, String> criteria = new HashMap<>();
		criteria.put(SearchUtils.CRITERIA_ARTIST, artist);

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
				throw new MyException("compoExist null: " + toRemove.getArtist() + " " + toRemove.getTitre() + " " + toRemove.getRecordType());
			}
			try {
				// Sauvegarde des modifications
				ExportXML.exportXML(importXML, file.getFileName());
			} catch (IOException e) {
				throw new MyException("Erreur lors de la suppresion d'une composition dans le fichier: " + file.getFileName(), e);
			}
		}
		LOG.debug("End removeCompositionsInFiles");
	}
	
	/**
	 * Modifie dans les fichiers XML, la composition donnée.
	 * @param toModif la {@link Composition} à modifier des fichiers
	 * @param newArtist {@link String} le nouvel artiste
	 * @param newTitre {@link String} le nouveau titre
	 * @param newType {@link String} le nouveau type
	 * @throws MyException 
	 */
	public static void modifyCompositionsInFiles(Composition toModif, String newArtist, String newTitre, String newType) throws MyException {
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
			if (toModifFromFile == null) {
				Optional<Composition> findByFile = CompositionUtils.findByFile(importXML, file);
				if (findByFile.isPresent()) {
					toModifFromFile = findByFile.get();
				}
			}
			if (toModifFromFile != null) {
				int indexOf = SearchUtils.indexOf(importXML, toModifFromFile);
				Composition composition = importXML.get(indexOf);
				composition.setArtist(newArtist);
				composition.setTitre(newTitre);
				composition.setRecordType(RecordType.valueOf(newType));
				importXML.set(indexOf, composition);
				try {
					// Sauvegarde des modifications
					ExportXML.exportXML(importXML, file.getFileName());
				} catch (IOException e) {
					throw new MyException("Erreur lors de la modification d'une composition dans le fichier: " + file.getFileName(), e);
				}
			} else {
				LOG.error(filename + Constant.NEW_LINE);
				String message = "Impossible de trouver la composition à modifier: " + toModif.getArtist() + " " + file + " " + toModif.getTitre() + " " + toModif.getRecordType();
				LOG.error(message);
				throw new MyException(message);
			}
		}
		LOG.debug("End modifyCompositionsInFiles");
	}
	
	/**
	 * Calculates the {@link Score} doubleMedian for the given type.
	 * The median of all ranking multiply by 2. 
	 * @param type {@link RecordType}
	 * @return {@link BigDecimal}
	 */
	public static BigDecimal getDoubleMedian(RecordType type) {
		BigDecimal median = getMedian(type);
		BigDecimal doubleMedian = median.multiply(BigDecimal.valueOf(2));
		return doubleMedian;
	}

	/**
	 * Calculates the {@link Score} logMax for the given type.
	 * Max is the biggest size of all files. LogMax is {@code Log10(max) * max}.
	 * @param type {@link RecordType}
	 * @return {@link BigDecimal}
	 */
	public static BigDecimal getLogMax(RecordType type) {
		BigDecimal max = getMax(type);
		BigDecimal logMax = BigDecimal.valueOf(Math.log10(max.doubleValue())).multiply(max);
		return logMax;
	}

	/**
	 * Calculates the score for a composition. 
	 * It's the sum of the score of its files.
	 * @param logMax @see {@link Score#getLogMax(RecordType)}
	 * @param doubleMedian @see {@link Score#getDoubleMedian(RecordType)}
	 * @param composition the composition
	 * @return {@link BigDecimal} the score
	 */
	public static long calculateCompositionScore(BigDecimal logMax, BigDecimal doubleMedian,
			Composition composition) {
		BigDecimal sumPts = BigDecimal.ZERO;
		for (Fichier fichier : composition.getFiles()) {
			sumPts = sumPts.add(calculateFileScore(logMax, doubleMedian, fichier));
		}
		return Math.round(sumPts.doubleValue());
	}

	/**
	 * Calculates the score for a file.
	 * @param logMax @see {@link Score#getLogMax(RecordType)}
	 * @param doubleMedian @see {@link Score#getDoubleMedian(RecordType)}
	 * @param fichier the file
	 * @return {@link BigDecimal} the score
	 */
	public static BigDecimal calculateFileScore(BigDecimal logMax, BigDecimal doubleMedian, Fichier fichier) {
		BigDecimal points = BigDecimal.ZERO;
		if (fichier.getSorted() && fichier.getClassement() != 0) {
			// Log10(doubleMedian/rank + 3) * logMax
			points = BigDecimal.valueOf(Math
					.log10(doubleMedian.divide(BigDecimal.valueOf(fichier.getClassement()), 10, RoundingMode.HALF_UP)
							.add(BigDecimal.valueOf(3)).doubleValue()))
					.multiply(logMax);
		} else {
			// Log10(5) * logMax
			points = BigDecimal.valueOf(Math.log10(5)).multiply(logMax);
		}
		if (fichier.getCategorie().equals(Cat.ALL_TIME)) {
			points = points.multiply(BigDecimal.valueOf(1.5));
		} else if (fichier.getCategorie().equals(Cat.DECADE)) {
			points = points.multiply(BigDecimal.valueOf(1.3));
		} else if (fichier.getCategorie().equals(Cat.YEAR)) {
			points = points.divide(BigDecimal.valueOf(2));
		}
		return points;
	}

	/**
	 * La medianne de tous les classements des fichiers du type donné. 
	 * @param type {@link RecordType}
	 * @return {@link BigDecimal}
	 */
	public static BigDecimal getMedian(RecordType type) {
		List<Composition> importXML = ImportXML.importXML(Constant.FINAL_FILE_PATH);
		Map<String, String> criteria = new HashMap<>();
		criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, type.toString());
		criteria.put(SearchUtils.CRITERIA_SORTED, Boolean.TRUE.toString());
		List<Composition> yearList = SearchUtils.searchJaro(importXML, criteria, true);
		List<Integer> rankList = yearList.stream().map(Composition::getFiles).flatMap(List::stream)
				.map(Fichier::getClassement).collect(Collectors.toList());
		return BigDecimal.valueOf(MiscUtils.median(rankList));
	}
	
	/**
	 * La taille maximum des fichiers du type donné.
	 * @param type {@link RecordType}
	 * @return {@link BigDecimal}
	 */
	public static BigDecimal getMax(RecordType type) {
		List<Composition> importXML = ImportXML.importXML(Constant.FINAL_FILE_PATH);
		Map<String, String> criteria = new HashMap<>();
		criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, type.toString());
		criteria.put(SearchUtils.CRITERIA_SORTED, Boolean.TRUE.toString());
		List<Composition> yearList = SearchUtils.searchJaro(importXML, criteria, true);
		List<Integer> rankList = yearList.stream().map(Composition::getFiles).flatMap(List::stream)
				.map(Fichier::getClassement).collect(Collectors.toList());
		return BigDecimal.valueOf(rankList.stream().mapToInt(Integer::intValue).max().getAsInt());
	}
}
