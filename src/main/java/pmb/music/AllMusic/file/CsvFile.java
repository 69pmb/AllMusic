package pmb.music.AllMusic.file;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Comparator;
import java.util.List;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.opencsv.CSVWriter;

import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MiscUtils;

/**
 * Classe pour les fichiers csv.
 * 
 * @author pmbroca
 */
public class CsvFile {

	private static final Logger LOG = Logger.getLogger(CsvFile.class);

	private CsvFile() {
	}

	/**
	 * Save data into a csv file.
	 * 
	 * @param filename the name of the csv file
	 * @param csv the data to save
	 * @param sortKey {@link SortKey} list de tri du tableau si necessaire
	 * @param header the header of the file
	 * @return le full name of the saved file
	 */
	public static String exportCsv(String filename, List<List<String>> csv, List<SortKey> sortKeys, String[] header) {
		LOG.debug("Start exportCsv");
		if (sortKeys != null) {
			sortCsvList(csv, sortKeys);
		}
		String name = Constant.getOutputDir() + filename + Constant.CSV_EXTENSION;
		// Writing
		try (CSVWriter csvWriter = new CSVWriter(
				new OutputStreamWriter(new FileOutputStream(name), Constant.ANSI_ENCODING), ';',
				CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);) {
			csvWriter.writeNext(header);
			for (List<String> row : csv) {
				csvWriter.writeNext(row.toArray(new String[0]));
			}
		} catch (IOException e) {
			LOG.error("Erreur lors de la génération du csv", e);
		}
		LOG.debug("End exportCsv");
		return name;
	}

	private static void sortCsvList(List<List<String>> csv, List<SortKey> sortKeys) {
		LOG.debug("Start sortCsvList");
		if (!csv.isEmpty()) {
			LOG.debug("Sorting");
			Comparator<List<String>> comparator = null;
			for (SortKey sortKey : sortKeys) {
				int column = sortKey.getColumn();
				Comparator<List<String>> sort = null;
				if (NumberUtils.isCreatable(csv.get(0).get(column))) {
					sort = (c1, c2) -> MiscUtils.compareDouble.compare(c1.get(column), c2.get(column));
				} else if (StringUtils.contains(csv.get(0).get(column), "%")) {
					sort = (c1, c2) -> MiscUtils.comparePercentage.compare(c1.get(column), c2.get(column));
				} else if (column >= 0) {
					sort = (c1, c2) -> c1.get(column).compareToIgnoreCase(c2.get(column));
				}
				if (sort != null && SortOrder.DESCENDING.equals(sortKey.getSortOrder())) {
					sort = sort.reversed();
				}
				if (comparator == null) {
					comparator = sort;
				} else {
					comparator = comparator.thenComparing(sort);
				}
			}
			csv.sort(comparator);
		}
		LOG.debug("End sortCsvList");
	}
}
