/**
 * 
 */
package pmb.music.AllMusic.model;

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
