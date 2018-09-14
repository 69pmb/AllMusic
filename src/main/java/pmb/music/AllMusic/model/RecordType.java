/**
 * 
 */
package pmb.music.AllMusic.model;

import org.apache.commons.lang3.StringUtils;

/**
 * Enumeration pour le type d'une composition: chanson ou album.
 * 
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

	public static RecordType getByValue(String value) {
		for (RecordType type : values()) {
			if (StringUtils.equalsAnyIgnoreCase(type.value, value)) {
				return type;
			}
		}
		return null;
	}
}
