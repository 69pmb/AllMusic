/**
 * 
 */
package pmb.music.AllMusic.model;

import org.apache.commons.lang3.StringUtils;

/**
 * Enumeration pour le type d'une composition: chanson ou album.
 * 
 */
public enum RecordType {
    SONG("SONG"),
    ALBUM("ALBUM"),
    UNKNOWN("UNKNOWN");

    private final String value;

    RecordType(String value) {
        this.value = value;
    }

    public String getRecordType() {
        return value;
    }

    /**
     * Finds a RecordType by its value.
     * 
     * @param value the value to find
     * @return a RecordType or null if not found
     */
    public static RecordType getByValue(String value) {
        for (RecordType type : values()) {
            if (StringUtils.equalsAnyIgnoreCase(type.value, value)) {
                return type;
            }
        }
        return null;
    }
}
