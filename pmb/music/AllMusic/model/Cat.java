/**
 * 
 */
package pmb.music.AllMusic.model;

/**
 * @author i2113mj
 *
 */
public enum Cat {
    YEAR("YEAR"),
    DECADE("DECADE"),
    THEME("THEME"),
    GENRE("GENRE"),
    ALL_TIME("ALL TIME"),
    MISCELLANEOUS("MISCELLANEOUS");

    final private String value;

    Cat(String value) {
        this.value = value;
    }

    public String getCat() {
        return value;
    }
}
