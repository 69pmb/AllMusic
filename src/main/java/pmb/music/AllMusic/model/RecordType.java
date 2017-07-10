/**
 * 
 */
package pmb.music.AllMusic.model;

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
}
