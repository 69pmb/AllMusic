package pmb.music.AllMusic.file;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.opencsv.CSVWriter;

import pmb.music.AllMusic.utils.Constant;

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
	 * @param sortKey {@link SortKey} le tri du tableau
	 * @param header the header of the file
	 * @return le full name of the saved file
	 */
	public static String exportCsv(String filename, List<List<String>> csv, SortKey sortKey, String[] header) {
		LOG.debug("Start exportCsv");
		if (sortKey != null && !csv.isEmpty()) {
			LOG.debug("Sorting");
			// Sorting
			List<String> list = csv.get(0);
			List<Integer> doubleColumn = new ArrayList<>();
			for (int i = 0; i < list.size(); i++) {
				String item = list.get(i);
				try {
					Double.parseDouble(StringUtils.replaceAll(item, ",", "."));
				} catch (NumberFormatException e) {
					continue;
				}
				doubleColumn.add(i);
			}
			int column = sortKey.getColumn();
			Comparator<List<String>> sort = null;
			if (doubleColumn.contains(column)) {
				sort = (c1, c2) -> Double.valueOf(StringUtils.replaceAll(c1.get(column), ",", "."))
						.compareTo(Double.valueOf(StringUtils.replaceAll(c2.get(column), ",", ".")));
			} else if (column >= 0) {
				sort = (c1, c2) -> c1.get(column).compareToIgnoreCase(c2.get(column));
			}
			if (sort != null) {
				if (SortOrder.DESCENDING.equals(sortKey.getSortOrder())) {
					csv.sort(sort.reversed());
				} else {
					csv.sort(sort);
				}
			}
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
}
