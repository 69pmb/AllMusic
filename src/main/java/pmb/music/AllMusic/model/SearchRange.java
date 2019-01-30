package pmb.music.AllMusic.model;

/**
 * Enumération des différentes façon de comparer des chiffres.
 * 
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

	public static SearchRange getByValue(String value) {
		for (SearchRange sm : values()) {
			if (sm.value.equals(value)) {
				return sm;
			}
		}
		return null;
	}
}
