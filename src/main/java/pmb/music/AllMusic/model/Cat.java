/**
 *
 */
package pmb.music.AllMusic.model;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

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

    /**
     * Name of the enum instance.
     *
     * @return value given in constructor
     */
    public String getValue() {
        return value;
    }

    /**
     * Finds a Cat by its value.
     *
     * @param value the value to find
     * @return a Cat or null if not found
     */
    public static Cat getByValue(String value) {
        return Arrays.stream(values()).filter(cat -> StringUtils.equalsAnyIgnoreCase(cat.toString(), value)).findFirst()
                .orElseGet(() -> Cat.valueOf(value));
    }

    @Override
    public String toString() {
        return value;
    }
}
