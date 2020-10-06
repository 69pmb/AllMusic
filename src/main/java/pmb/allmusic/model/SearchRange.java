package pmb.allmusic.model;

/**
 * Enumération des différentes façon de comparer des chiffres.
 * 
 * @author pmbroca
 */
public enum SearchRange {
    EQUAL("=="), GREATER(">="), LESS("<=");

    private final String value;

    SearchRange(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Finds a SearchRange by its value.
     * 
     * @param value the value to find
     * @return a SearchRange or null if not found
     */
    public static SearchRange getByValue(String value) {
        for (SearchRange sm : values()) {
            if (sm.value.equals(value)) {
                return sm;
            }
        }
        return null;
    }
}
