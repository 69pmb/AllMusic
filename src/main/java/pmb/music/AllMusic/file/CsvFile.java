package pmb.music.AllMusic.file;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.opencsv.CSVWriter;

import pmb.music.AllMusic.utils.Constant;

public class CsvFile {

	private static final Logger LOG = Logger.getLogger(CsvFile.class);

	public static String writeCsv(Vector<Vector<Object>> dataVector, String filename) {
		LOG.debug("Start writeCsv");
		String name = filename + Constant.CSV_EXTENSION;
		try (CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(name), Constant.ANSI_ENCODING),
				';');) {
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
}
