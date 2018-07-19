/**
 * 
 */
package pmb.music.AllMusic.model;

/**
 * Enumération des différentes catégories de liste d'oeuvres musicales: bilan
 * annuel, décennale, par thème, par genre, "all time" ou divers.
 * 
 */
public enum Cat {
	YEAR("YEAR"), 
	DECADE("DECADE"), 
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
}
