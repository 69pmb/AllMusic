package pmb.music.AllMusic.file;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.opencsv.CSVWriter;

import pmb.music.AllMusic.utils.Constant;

/**
 * Classe pour les fichiers csv.
 * @author pmbroca
 */
public class CsvFile {

	private static final Logger LOG = Logger.getLogger(CsvFile.class);
	
	private CsvFile() {}

	/**
	 * Crée un fichier {@code CSV} à partir des résultats de recherche.
	 * @param dataVector les données issues d'une recherche
	 * @param filename le nom du fichier csv
	 * @param criteres 
	 * @return  le nom du fichier
	 */
	public static String writeCsvFromSearchResult(Vector<Vector<Object>> dataVector, String filename, String criteres) {
		LOG.debug("Start writeCsv");
		String name = filename + Constant.CSV_EXTENSION;
		try (CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(name), Constant.ANSI_ENCODING),
				';');) {
			String[] header = {"Artiste","Titre", "Type", "Nombre de fichiers", "Critères: " + criteres};
			csvWriter.writeNext(header);
			for (int i = 0; i < dataVector.size(); i++) {
				Vector<Object> vector = dataVector.get(i);
				String[] row = new String[4];
				row[0] = (String) vector.get(0);
				row[1] = (String) vector.get(1);
				row[2] = (String) vector.get(2);
				row[3] = String.valueOf((Integer) vector.get(3));
				csvWriter.writeNext(row);
			}
		} catch (IOException e) {
			LOG.error("Erreur lors de la génération du csv", e);
		}
		LOG.debug("End writeCsv");
		return name;
	}
	
	/**
	 * Crée un fichier {@code CSV} à partir des résultats de recherche de l'écran artiste.
	 * @param dataVector les données issues d'une recherche
	 * @param filename le nom du fichier csv
	 * @return  le nom du fichier
	 */
	public static String writeCsvFromArtistPanel(Vector<Vector<Object>> dataVector, String filename, String criteres) {
		LOG.debug("Start writeCsv");
		String name = filename + Constant.CSV_EXTENSION;
		try (CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(name), Constant.ANSI_ENCODING),
				';');) {
			String[] header = {"Artiste","Nombre d'occurences totales", "Albums", "Chansons", "Critères: " + criteres};
			csvWriter.writeNext(header);
			for (int i = 0; i < dataVector.size(); i++) {
				Vector<Object> vector = dataVector.get(i);
				String[] row = new String[4];
				row[0] = (String) vector.get(0);
				row[1] = String.valueOf((Integer) vector.get(1));
				row[2] = String.valueOf((Integer) vector.get(2));
				row[3] = String.valueOf((Integer) vector.get(3));
				csvWriter.writeNext(row);
			}
		} catch (IOException e) {
			LOG.error("Erreur lors de la génération du csv", e);
		}
		LOG.debug("End writeCsv");
		return name;
	}
}
