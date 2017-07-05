/**
 * 
 */
package pmb.music.AllMusic.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;

import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;

/**
 * Contient les méthodes de recherche dans une liste de {@link Composition} avec
 * des critères de recherche.
 * 
 * @author i2113mj
 */
public class SearchUtils {

	private SearchUtils() {
	}

	/**
	 * Les critères de recherche sont utilisés comme des {@code '%like'}.
	 * @param compoList la liste de compo dans laquelle rechercher
	 * @param criteria les critères
	 * @param searchInFiles si on doit filtrer ou non les fichiers des compos
	 * @return la liste de compo filtrée selon les critères
	 */
	public static List<Composition> searchContains(List<Composition> compoList, Map<String, String> criteria, final boolean searchInFiles) {
		List<Composition> arrayList = new ArrayList<>(compoList);
		// Critères compositions
		final String artist = criteria.get("artist");
		final String titre = criteria.get("titre");
		final String type = criteria.get("type");
		// Critères fichiers
		final String publish = criteria.get("publish");
		final String fileName = criteria.get("fileName");
		final String auteur = criteria.get("auteur");
		final String cat = criteria.get("cat");
		final String dateB = criteria.get("dateB");
		final String dateE = criteria.get("dateE");

		final boolean searchCompo = StringUtils.isNotBlank(artist) || StringUtils.isNotBlank(titre) || StringUtils.isNotBlank(type);

		final boolean searchFile = StringUtils.isNotBlank(publish) || StringUtils.isNotBlank(fileName) || StringUtils.isNotBlank(auteur)
				|| StringUtils.isNotBlank(cat) || StringUtils.isNotBlank(dateB) || StringUtils.isNotBlank(dateE);

		if (searchCompo || searchFile) {
			CollectionUtils.filter(arrayList, new Predicate() {

				@Override
				public boolean evaluate(Object c) {
					Composition co = (Composition) c;

					boolean result = true;
					if (StringUtils.isNotBlank(artist)) {
						result = result && StringUtils.containsIgnoreCase(co.getArtist(), artist);
					}
					if (StringUtils.isNotBlank(titre)) {
						result = result && StringUtils.containsIgnoreCase(co.getTitre(), titre);
					}
					if (type != null) {
						result = result && co.getRecordType() == RecordType.valueOf(type);
					}
					
					List<Fichier> files = new ArrayList<>(co.getFiles());
					if (searchFile && CollectionUtils.isNotEmpty(files) && result) {
						CollectionUtils.filter(files, new Predicate() {

							@Override
							public boolean evaluate(Object f) {
								return evaluateFichierStrictly(publish, fileName, auteur, cat, dateB, dateE, f);
							}
						});
					}
					if (searchInFiles) {
						co.setFiles(files);
					}
					return result && CollectionUtils.isNotEmpty(files);
				}
			});
		}

		return arrayList;
	}

	/**
	 * Les critères de recherche sont strict, le champ doit être exactement égal
	 * au critère.
	 * 
	 * @param compoList la liste de compo dans laquelle rechercher
	 * @param criteria les critères
	 * @return la liste de compo filtrée selon les critères
	 */
	public static List<Composition> searchStrictly(List<Composition> compoList, Map<String, String> criteria) {
		List<Composition> arrayList = new ArrayList<>(compoList);
		final String artist = criteria.get("artist");
		final String titre = criteria.get("titre");
		final String type = criteria.get("type");
		final String publish = criteria.get("publish");
		final String fileName = criteria.get("fileName");
		final String auteur = criteria.get("auteur");
		final String cat = criteria.get("cat");
		final String dateB = criteria.get("dateB");
		final String dateE = criteria.get("dateE");

		// Si on doit chercher dans les compos
		final boolean searchCompo = StringUtils.isNotBlank(artist) || StringUtils.isNotBlank(titre) || StringUtils.isNotBlank(type);
		// Si on doit chercher dans les fichiers
		final boolean searchFile = StringUtils.isNotBlank(publish) || StringUtils.isNotBlank(fileName) || StringUtils.isNotBlank(auteur)
				|| StringUtils.isNotBlank(cat) || StringUtils.isNotBlank(dateB) || StringUtils.isNotBlank(dateE);

		if (searchCompo || searchFile) {
			CollectionUtils.filter(arrayList, new Predicate() {

				@Override
				public boolean evaluate(Object c) {
					Composition co = (Composition) c;

					boolean result = true;
					if (StringUtils.isNotBlank(artist)) {
						boolean equalsIgnoreCase = StringUtils.equalsIgnoreCase(co.getArtist(), artist);
						result = result && equalsIgnoreCase;
					}
					if (StringUtils.isNotBlank(titre)) {
						result = result && StringUtils.equalsIgnoreCase(co.getTitre(), titre);
					}
					if (type != null) {
						result = result && co.getRecordType() == RecordType.valueOf(type);
					}
					
					List<Fichier> files = new ArrayList<>(co.getFiles());
					if (searchFile && CollectionUtils.isNotEmpty(files)) {
						CollectionUtils.filter(files, new Predicate() {

							@Override
							public boolean evaluate(Object f) {
								return evaluateFichierContains(publish, fileName, auteur, cat, dateB, dateE, f);
							}
						});
					}
					result = result && CollectionUtils.isNotEmpty(files);
					return result;
				}
			});
		}

		return arrayList;
	}

	private static boolean evaluateFichierContains(final String publish, final String fileName, final String auteur, final String cat, final String dateB,
			final String dateE, Object f) {
		Fichier fi = (Fichier) f;
		boolean result = true;

		if (StringUtils.isNotBlank(publish)) {
			result = result && fi.getPublishYear() == Integer.parseInt(publish);
		}
		if (StringUtils.isNotBlank(fileName)) {
			result = result && StringUtils.equalsIgnoreCase(fi.getFileName(), fileName);
		}
		if (StringUtils.isNotBlank(auteur)) {
			result = result && StringUtils.equalsIgnoreCase(fi.getAuthor(), auteur);
		}
		if (StringUtils.isNotBlank(cat)) {
			result = result && fi.getCategorie() == Cat.valueOf(cat);
		}
		if (StringUtils.isNotBlank(dateB)) {
			result = result && fi.getRangeDateBegin() == Integer.parseInt(dateB);
		}
		if (StringUtils.isNotBlank(dateE)) {
			result = result && fi.getRangeDateEnd() == Integer.parseInt(dateE);
		}
		return result;
	}

	private static boolean evaluateFichierStrictly(final String publish, final String fileName, final String auteur, final String cat, final String dateB,
			final String dateE, Object f) {
		Fichier fi = (Fichier) f;
		boolean result = true;

		if (StringUtils.isNotBlank(publish)) {
			result = result && fi.getPublishYear() == Integer.parseInt(publish);
		}
		if (StringUtils.isNotBlank(fileName)) {
			result = result && StringUtils.containsIgnoreCase(fi.getFileName(), fileName);
		}
		if (StringUtils.isNotBlank(auteur)) {
			result = result && StringUtils.containsIgnoreCase(fi.getAuthor(), auteur);
		}
		if (StringUtils.isNotBlank(cat)) {
			result = result && fi.getCategorie() == Cat.valueOf(cat);
		}
		if (StringUtils.isNotBlank(dateB)) {
			result = result && fi.getRangeDateBegin() == Integer.parseInt(dateB);
		}
		if (StringUtils.isNotBlank(dateE)) {
			result = result && fi.getRangeDateEnd() == Integer.parseInt(dateE);
		}
		return result;
	}

}
