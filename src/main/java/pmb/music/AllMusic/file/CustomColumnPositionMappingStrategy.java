package pmb.music.AllMusic.file;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.text.WordUtils;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.bean.AbstractCsvConverter;
import com.opencsv.bean.AbstractMappingStrategy;
import com.opencsv.bean.BeanField;
import com.opencsv.bean.BeanFieldJoinIntegerIndex;
import com.opencsv.bean.BeanFieldSingleValue;
import com.opencsv.bean.BeanFieldSplit;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvBindAndJoinByPosition;
import com.opencsv.bean.CsvBindAndSplitByPosition;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvConverter;
import com.opencsv.bean.CsvCustomBindByPosition;
import com.opencsv.bean.FieldMapByPosition;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

/**
 * Class describing how to parse csv file when using {@code OpenCsv}. This is a
 * custom class to mix Column position mapping and Header column name mapping.
 *
 * @see AbstractMappingStrategy
 * @see ColumnPositionMappingStrategy
 * @param <T> class model of the csv
 */
public class CustomColumnPositionMappingStrategy<T> extends ColumnPositionMappingStrategy<T> {

    /**
     * The map from column position to {@link BeanField}.
     */
    private FieldMapByPosition<T> fieldMap;

    /**
     * Used to store a mapping from presumed input column index to desired output
     * column index, as determined by applying {@link #writeOrder}.
     */
    private Integer[] columnIndexForWriting;

    public CustomColumnPositionMappingStrategy() {
        super();
    }

    public CustomColumnPositionMappingStrategy(Class<? extends T> type, String[] columnMapping) {
        super();
        this.type = type;
        if (columnMapping != null) {
            setColumnMapping(columnMapping);
        } else {
            this.setColumnMapping(FieldUtils.getFieldsListWithAnnotation(type, CsvBindByName.class).stream()
                    .map(Field::getName).collect(Collectors.toList()).toArray(String[]::new));
        }
    }

    /**
     * This method returns an empty array. The column position mapping strategy
     * assumes that there is no header, and thus it also does not write one,
     * accordingly.
     *
     * @return An empty array
     */
    // The rest of the Javadoc is inherited
    @Override
    public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {
        // Custom method
        final int numColumns = headerIndex.findMaxIndex();
        if (numColumns == -1) {
            return super.generateHeader(bean);
        }
        columnIndexForWriting = new Integer[numColumns + 1];
        Arrays.setAll(columnIndexForWriting, i -> i);
        String[] header = new String[numColumns + 1];
        List<Field> fields = FieldUtils.getFieldsListWithAnnotation(bean.getClass(), CsvBindByName.class);
        Arrays.setAll(header, i -> extractHeaderName(fields.get(i)));
        return header;
    }

    private String extractHeaderName(final Field field) {
        // Custom method
        if (field == null || field.getDeclaredAnnotationsByType(CsvBindByName.class).length == 0) {
            return StringUtils.EMPTY;
        }

        String column = field.getDeclaredAnnotationsByType(CsvBindByName.class)[0].column();
        String name = field.getName();
        return WordUtils.capitalize(StringUtils.isBlank(column) ? name : column);
    }

    /**
     * Creates a map of annotated fields in the bean to be processed.
     * <p>
     * This method is called by {@link #loadFieldMap()} when at least one relevant
     * annotation is found on a member variable.
     * </p>
     */
    @Override
    protected void loadAnnotatedFieldMap(ListValuedMap<Class<?>, Field> fields) {
        boolean required;
        int i = 0;
        for (Map.Entry<Class<?>, Field> classAndField : fields.entries()) {
            Class<?> localType = classAndField.getKey();
            Field localField = classAndField.getValue();
            String fieldLocale, fieldWriteLocale, capture, format;

            // Custom converters always have precedence.
            if (localField.isAnnotationPresent(CsvCustomBindByPosition.class)) {
                CsvCustomBindByPosition annotation = localField.getAnnotation(CsvCustomBindByPosition.class);
                @SuppressWarnings("unchecked")
                Class<? extends AbstractBeanField<T, Integer>> converter = (Class<? extends AbstractBeanField<T, Integer>>) annotation
                        .converter();
                BeanField<T, Integer> bean = instantiateCustomConverter(converter);
                bean.setType(localType);
                bean.setField(localField);
                required = annotation.required();
                bean.setRequired(required);
                fieldMap.put(annotation.position(), bean);
            }

            // Then check for a collection
            else if (localField.isAnnotationPresent(CsvBindAndSplitByPosition.class)) {
                CsvBindAndSplitByPosition annotation = localField.getAnnotation(CsvBindAndSplitByPosition.class);
                required = annotation.required();
                fieldLocale = annotation.locale();
                fieldWriteLocale = annotation.writeLocaleEqualsReadLocale() ? fieldLocale : annotation.writeLocale();
                String splitOn = annotation.splitOn();
                String writeDelimiter = annotation.writeDelimiter();
                Class<? extends Collection> collectionType = annotation.collectionType();
                Class<?> elementType = annotation.elementType();
                Class<? extends AbstractCsvConverter> splitConverter = annotation.converter();
                capture = annotation.capture();
                format = annotation.format();

                CsvConverter converter = determineConverter(localField, elementType, fieldLocale, fieldWriteLocale,
                        splitConverter);
                fieldMap.put(annotation.position(), new BeanFieldSplit<>(localType, localField, required, errorLocale,
                        converter, splitOn, writeDelimiter, collectionType, capture, format));
            }

            // Then check for a multi-column annotation
            else if (localField.isAnnotationPresent(CsvBindAndJoinByPosition.class)) {
                CsvBindAndJoinByPosition annotation = localField.getAnnotation(CsvBindAndJoinByPosition.class);
                required = annotation.required();
                fieldLocale = annotation.locale();
                fieldWriteLocale = annotation.writeLocaleEqualsReadLocale() ? fieldLocale : annotation.writeLocale();
                Class<?> elementType = annotation.elementType();
                Class<? extends MultiValuedMap> mapType = annotation.mapType();
                Class<? extends AbstractCsvConverter> joinConverter = annotation.converter();
                capture = annotation.capture();
                format = annotation.format();

                CsvConverter converter = determineConverter(localField, elementType, fieldLocale, fieldWriteLocale,
                        joinConverter);
                fieldMap.putComplex(annotation.position(), new BeanFieldJoinIntegerIndex<>(localType, localField,
                        required, errorLocale, converter, mapType, capture, format));
            }

            // By name annotation.
            // Custom method
            else if (localField.isAnnotationPresent(CsvBindByName.class)) {
                CsvBindByName annotation = localField.getAnnotation(CsvBindByName.class);
                required = annotation.required();
                fieldLocale = annotation.locale();
                fieldWriteLocale = annotation.writeLocaleEqualsReadLocale() ? fieldLocale : annotation.writeLocale();
                capture = annotation.capture();
                format = annotation.format();

                CsvConverter converter = determineConverter(localField, localField.getType(), fieldLocale,
                        fieldWriteLocale, null);
                fieldMap.put(i, new BeanFieldSingleValue<>(localField.getType(), localField, required, errorLocale,
                        converter, capture, format));
            }

            // Then it must be a bind by position.
            else {
                CsvBindByPosition annotation = localField.getAnnotation(CsvBindByPosition.class);
                required = annotation.required();
                fieldLocale = annotation.locale();
                fieldWriteLocale = annotation.writeLocaleEqualsReadLocale() ? fieldLocale : annotation.writeLocale();
                capture = annotation.capture();
                format = annotation.format();
                CsvConverter converter = determineConverter(localField, localField.getType(), fieldLocale,
                        fieldWriteLocale, null);

                fieldMap.put(annotation.position(), new BeanFieldSingleValue<>(localType, localField, required,
                        errorLocale, converter, capture, format));
            }
            i++;
        }
    }
}
