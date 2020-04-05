package pmb.music.AllMusic.view;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Set;

import pmb.music.AllMusic.exception.MinorException;

/**
 * Used to define indexes in tables of the application.
 */
public class ColumnIndex implements Serializable {
    private static final long serialVersionUID = 1L;
    private EnumMap<Index, Integer> mapper;

    /**
     * Column enum values of the tables.
     */
    public enum Index {
        LINE_NUMBER, ARTIST, TITLE, TYPE, FILE_SIZE, SCORE, DECILE, SELECTED, DELETED, UUID, AUTHOR, FILE_NAME, PUBLISH,
        CAT, RANGE, CREATE_DATE, PERCENT_DELETED, SORTED, RANK, NB_TOTAL, SCORE_TOTAL, SCORE_SONG, SCORE_ALBUM,
        SCORE_DELETED
    }

    /**
     * Constructor.
     */
    public ColumnIndex() {
        mapper = new EnumMap<>(Index.class);
    }

    /**
     * Puts an item.
     *
     * @param index column header
     * @param value index value
     * @return the new column index
     */
    public ColumnIndex put(Index index, Integer value) {
        mapper.put(index, value);
        return this;
    }

    /**
     * If the column index contains the given index.
     *
     * @param index to search
     * @return {@code true} if exist, {@code false} otherwise
     */
    public boolean has(Index index) {
        return mapper.containsKey(index);
    }

    /**
     * Gets the value for the given index.
     *
     * @param index to search
     * @return the value
     * @throws MinorException if not present
     */
    public Integer get(Index index) {
        Integer result = mapper.get(index);
        if (result == null) {
            throw new MinorException("Index: " + index + " is not present.");
        }
        return result;
    }

    /**
     * Gets the value for the given index or
     * {@code defaultValue} if the index contains no mapping for the index.
     *
     * @param index to search
     * @param defaultValue value returned if nothing found
     * @return the value
     */
    public Integer getOrDefault(Index index, Integer defaultValue) {
        Integer result = mapper.get(index);
        return result == null ? defaultValue : result;
    }

    /**
     * Gets all indexes.
     *
     * @return a {@link Set} of {@link Index}
     */
    public Set<Index> getAllIndex() {
        return mapper.keySet();
    }
}
