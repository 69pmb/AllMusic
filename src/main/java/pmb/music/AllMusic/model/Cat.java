/**
 * 
 */
package pmb.music.AllMusic.model;

/**
 * Enumération des différentes catégories de liste d'oeuvres musicales: bilan
 * annuel, décennale, par thème, par genre, sur une longue période de temps,
 * "all time" ou divers.
 * 
 * @author pmbroca
 */
public enum Cat {
	YEAR("YEAR"), 
	DECADE("DECADE"), 
	LONG_PERIOD("LONG PERIOD"), 
	THEME("THEME"), 
	GENRE("GENRE"), 
	ALL_TIME("ALL TIME"), 
	MISCELLANEOUS("MISCELLANEOUS");

	private final String value;

	Cat(String value) {
		this.value = value;
	}

	public String getCat() {
		return value;
	}

	public static Cat getByValue(String value) {
		for (Cat cat : values()) {
			if (cat.value == value) {
				return cat;
			}
		}
		return null;
	}
}
