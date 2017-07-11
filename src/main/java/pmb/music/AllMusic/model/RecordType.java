/**
 * 
 */
package pmb.music.AllMusic.model;

/**
 * Enumeration pour le type d'une composition: chanson ou album.
 * @author pmbroca
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
}
