/**
 * 
 */
package pmb.music.allmusic.model;

/**
 * @author i2113mj
 *
 */
public enum RecordType {
    SONG("SONG"),
    ALBUM("ALBUM"),
    UNKNOWN("UNKNOWN");

    final private String value;

    RecordType(String value) {
        this.value = value;
    }

    public String getRecordType() {
        return value;
    }
}
