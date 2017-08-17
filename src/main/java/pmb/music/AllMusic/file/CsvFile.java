package pmb.music.AllMusic.file;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.apache.log4j.Logger;

import com.opencsv.CSVWriter;

import pmb.music.AllMusic.utils.Constant;

/**
 * Classe pour les fichiers csv.
 * @author pmbroca
 */
public class CsvFile {

	private static final Logger LOG = Logger.getLogger(CsvFile.class);
	
	private static final int SEARCH_INDEX_ARTIST = 0;
	private static final int SEARCH_INDEX_TITRE = 1;
	private static final int SEARCH_INDEX_TYPE = 2;
	private static final int SEARCH_INDEX_NB_FILE = 3;
	
	private static final int ARTIST_INDEX_ARTIST = 0;
	private static final int ARTIST_INDEX_NB_TOTAL = 1;
	private static final int ARTIST_INDEX_NB_ALBUM = 2;
	private static final int ARTIST_INDEX_NB_SONG = 3;

	private CsvFile() {}
	
	/**
	 * Crée un fichier {@code CSV} à partir des résultats de recherche.
	 * @param dataVector les données issues d'une recherche
	 * @param filename le nom du fichier csv
	 * @param criteres les critères de recherche
	 * @param sortKey {@link SortKey} le tri du tableau
	 * @return  le nom du fichier
	 */
	public static String writeCsvFromSearchResult(Vector<Vector<Object>> dataVector, String filename, String criteres, SortKey sortKey) {
		LOG.debug("Start writeCsvFromSearchResult");
		// Convertion en liste
		List<String []> csv = new ArrayList<>();
		for (int i = 0; i < dataVector.size(); i++) {
			Vector<Object> vector = dataVector.get(i);
			String[] row = new String[4];
			row[SEARCH_INDEX_ARTIST] = (String) vector.get(SEARCH_INDEX_ARTIST);
			row[SEARCH_INDEX_TITRE] = (String) vector.get(SEARCH_INDEX_TITRE);
			row[SEARCH_INDEX_TYPE] = (String) vector.get(SEARCH_INDEX_TYPE);
			row[SEARCH_INDEX_NB_FILE] = String.valueOf((Integer) vector.get(SEARCH_INDEX_NB_FILE));
			csv.add(row);
		}
		
		// Tri
		int column = sortKey.getColumn();
		Comparator<String []> sort = null;
		if(column == SEARCH_INDEX_NB_FILE) {
			sort = (c1, c2) -> Integer.valueOf(c1[SEARCH_INDEX_NB_FILE]).compareTo(Integer.valueOf(c2[SEARCH_INDEX_NB_FILE]));
		} else if(column < SEARCH_INDEX_NB_FILE && column >= 0) {
			sort = (c1, c2) -> c1[column].compareToIgnoreCase(c2[column]);
		}
		if(sort!=null){
			if(SortOrder.DESCENDING.equals(sortKey.getSortOrder())) {
				csv.sort(sort.reversed());
			} else {
				csv.sort(sort);
			}
		}

		String[] header = {"Artiste","Titre", "Type", "Nombre de fichiers", "Critères: " + criteres};
		LOG.debug("End writeCsvFromSearchResult");
		return exportCsv(filename, csv, header);
	}
	
	/**
	 * Crée un fichier {@code CSV} à partir des résultats de recherche de l'écran artiste.
	 * @param dataVector les données issues d'une recherche
	 * @param filename le nom du fichier csv
	 * @param criteres les critères de recherche
	 * @param sortKey {@link SortKey} le tri du tableau
	 * @return  le nom du fichier
	 */
	public static String writeCsvFromArtistPanel(Vector<Vector<Object>> dataVector, String filename, String criteres, SortKey sortKey) {
		LOG.debug("Start writeCsvFromArtistPanel");
		// Convertion en liste
		List<String []> csv = new ArrayList<>();
		for (int i = 0; i < dataVector.size(); i++) {
			Vector<Object> vector = dataVector.get(i);
			String[] row = new String[4];
			row[ARTIST_INDEX_ARTIST] = (String) vector.get(ARTIST_INDEX_ARTIST);
			row[ARTIST_INDEX_NB_TOTAL] = String.valueOf((Integer) vector.get(ARTIST_INDEX_NB_TOTAL));
			row[ARTIST_INDEX_NB_ALBUM] = String.valueOf((Integer) vector.get(ARTIST_INDEX_NB_ALBUM));
			row[ARTIST_INDEX_NB_SONG] = String.valueOf((Integer) vector.get(ARTIST_INDEX_NB_SONG));
			csv.add(row);
		}
		
		// Tri
		int column = sortKey.getColumn();
		Comparator<String []> sort = null;
		if(column == ARTIST_INDEX_ARTIST) {
			sort = (c1, c2) -> c1[column].compareToIgnoreCase(c2[column]);
		} else if(column <= ARTIST_INDEX_NB_SONG && column > ARTIST_INDEX_ARTIST) {
			sort = (c1, c2) -> Integer.valueOf(c1[column]).compareTo(Integer.valueOf(c2[column]));
		}
		if(sort!=null){
			if(SortOrder.DESCENDING.equals(sortKey.getSortOrder())) {
				csv.sort(sort.reversed());
			} else {
				csv.sort(sort);
			}
		}
		
		String[] header = {"Artiste","Nombre d'occurences totales", "Albums", "Chansons", "Critères: " + criteres};
		LOG.debug("End writeCsvFromArtistPanel");
		return exportCsv(filename, csv, header);
	}

	private static String exportCsv(String filename, List<String[]> csv, String[] header) {
		LOG.debug("Start exportCsv");
		String name = filename + Constant.CSV_EXTENSION;
		try (CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(name), Constant.ANSI_ENCODING),
				';');) {
			csvWriter.writeNext(header);
			for (String[] row : csv) {
				csvWriter.writeNext(row);	
			}
		} catch (IOException e) {
			LOG.error("Erreur lors de la génération du csv", e);
		}
		LOG.debug("End exportCsv");
		return name;
	}
}
