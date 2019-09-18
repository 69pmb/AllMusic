package pmb.music.AllMusic.view;

import java.util.HashMap;
import java.util.Map;

public class ColumnIndex {
    private Map<Index, Integer> mapper;

    public enum Index {
        LINE_NUMBER,
        ARTIST,
        TITLE,
        TYPE,
        FILE_SIZE,
        SCORE,
        DECILE,
        SELECTED,
        DELETED,
        UUID,
        AUTHOR,
        FILE_NAME,
        PUBLISH,
        CAT,
        RANGE,
        CREATE_DATE,
        PERCENT_DELETED,
        SORTED,
        RANK,
        NB_TOTAL,
        SCORE_TOTAL,
        SCORE_SONG,
        SCORE_ALBUM,
        SCORE_DELETED
    }

    public ColumnIndex() {
        mapper = new HashMap<>();
    }

    public ColumnIndex put(Index index, Integer value) {
        mapper.put(index, value);
        return this;
    }

    public Integer get(Index index) {
        return mapper.get(index);
    }
}
