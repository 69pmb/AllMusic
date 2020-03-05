package pmb.music.AllMusic.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.MappingStrategy;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import pmb.music.AllMusic.exception.MinorException;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MiscUtils;

/**
 * Classe pour les fichiers csv.
 *
 * @author pmbroca
 */
public final class CsvFile {

    private static final Logger LOG = LogManager.getLogger(CsvFile.class);

    private CsvFile() {
        throw new AssertionError("Must not be used");
    }

    /**
     * Save data into a csv file. Only keeps the columns whose headers are not
     * empty.
     *
     * @param filename the name of the csv file
     * @param csv the data to save
     * @param sortKeys {@link SortKey} list de tri du tableau si necessaire
     * @param header the header of the file
     * @return le full name of the saved file
     */
    public static String exportCsv(String filename, List<List<String>> csv, List<SortKey> sortKeys, String[] header) {
        LOG.debug("Start exportCsv: {}", filename);
        if (sortKeys != null) {
            sortCsvList(csv, sortKeys);
        }
        String name = Constant.getOutputDir() + filename + Constant.CSV_EXTENSION;
        List<String> headerList = Arrays.asList(header);
        List<Boolean> matrixHeaders = headerList.stream().map(StringUtils::isNotBlank).collect(Collectors.toList());
        // Writing
        try (CSVWriter csvWriter = new CSVWriter(
                new OutputStreamWriter(new FileOutputStream(name), Constant.ANSI_ENCODING), Constant.getCsvSeparator(),
                ICSVWriter.DEFAULT_QUOTE_CHARACTER, ICSVWriter.DEFAULT_ESCAPE_CHARACTER, ICSVWriter.DEFAULT_LINE_END)) {
            csvWriter.writeNext(filterRow(matrixHeaders, headerList));
            for (List<String> row : csv) {
                csvWriter.writeNext(filterRow(matrixHeaders, row));
            }
        } catch (IOException e) {
            LOG.error("Erreur lors de la génération du csv", e);
        }
        LOG.debug("End exportCsv");
        return name;
    }

    /**
     * Filters given row regarding if corresponding matrix entry is true.
     *
     * @param matrix list of boolean values to keep or trash
     * @param row values filter
     * @return a list which index in matrix are true
     */
    private static String[] filterRow(List<Boolean> matrix, List<String> row) {
        List<String> result = new ArrayList<>();
        for (int i = 0 ; i < row.size() ; i++) {
            if (BooleanUtils.isTrue(matrix.get(i))) {
                result.add(row.get(i));
            }
        }
        return result.toArray(new String[0]);
    }

    /**
     * Sorts given csv data with sorting keys.
     *
     * @param csv data to sort
     * @param sortKeys sorting keys
     */
    private static void sortCsvList(List<List<String>> csv, List<SortKey> sortKeys) {
        LOG.debug("Start sortCsvList");
        if (csv.isEmpty()) {
            return;
        }
        LOG.debug("Sorting");
        Comparator<List<String>> comparator = null;
        for (SortKey sortKey : sortKeys) {
            int column = sortKey.getColumn();
            Comparator<List<String>> sort = null;
            // Finds comparator depending on the sorted column of the first line
            if (NumberUtils.isCreatable(csv.get(0).get(column))) {
                sort = (c1, c2) -> MiscUtils.compareDouble.compare(c1.get(column), c2.get(column));
            } else if (StringUtils.contains(csv.get(0).get(column), "%")) {
                sort = (c1, c2) -> MiscUtils.comparePercentage.compare(c1.get(column), c2.get(column));
            } else if (column >= 0) {
                sort = (c1, c2) -> c1.get(column).compareToIgnoreCase(c2.get(column));
            }
            if (sort != null && SortOrder.DESCENDING == sortKey.getSortOrder()) {
                sort = sort.reversed();
            }
            if (comparator == null) {
                comparator = sort;
            } else {
                comparator = comparator.thenComparing(sort);
            }
        }
        csv.sort(comparator);
        LOG.debug("End sortCsvList");
    }

    /**
     * Read data from a csv file.
     *
     * @param csvFile the csv file
     * @param returnType the {@link Class} of data read
     * @param <T> the type of data parsed
     * @return a {@code List<T>}
     */
    public static <T> List<T> importCsv(File csvFile, Class<T> returnType) {
        LOG.debug("Start importCsv: {}", csvFile.getAbsolutePath());
        List<T> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(csvFile), Constant.ANSI_ENCODING))) {
            result = new CsvToBeanBuilder<T>(br).withType(returnType).withSeparator(Constant.getCsvSeparator()).build()
                    .parse();
        } catch (IOException e) {
            LOG.error("Erreur lors de la lecture du csv", e);
        }
        LOG.debug("End importCsv");
        return result;
    }

    /**
     * Export a list of bean in a csv file.
     *
     * @param csvFile the csv file
     * @param beans the {@code List<T>} the beans to export
     * @param mappingStrategy {@link MappingStrategy} columns order and name
     * @param <T> the type of data exported
     */
    public static <T> void exportBeanList(File csvFile, List<T> beans, MappingStrategy<T> mappingStrategy,
            boolean append) {
        LOG.debug("Start exportBeanList: {}", csvFile.getAbsolutePath());
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(csvFile, append),
                Constant.ANSI_ENCODING)) {
            new StatefulBeanToCsvBuilder<T>(writer).withSeparator(Constant.getCsvSeparator())
                    .withQuotechar(Character.MIN_VALUE).withMappingStrategy(mappingStrategy).build().write(beans);
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new MinorException("Error when exporting csv: " + csvFile.getName(), e);
        }
        LOG.debug("End exportBeanList");
    }
}
