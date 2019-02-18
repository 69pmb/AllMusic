package pmb.music.AllMusic.model;

/**
 * Enumération des différentes façon de faire des recherches.
 * 
 * @author pmbroca
 */
public enum SearchMethod {
	/**
	 * Les critères de recherche seront utilisés comme des {@code '%like'}.
	 */
	CONTAINS("Contient"),
	/**
	 * Le champ doit commencer comme le critère.
	 */
	BEGINS_WITH("Commence Par"),
	/**
	 * Le champ doit être exactement égal au critère.
	 */
	WHOLE_WORD("Strictement Egal"),
	/**
	 * Le critère de recherche comporte des {@code '*'} qui seront interprétés comme
	 * des caractères joker.
	 */
	JOKER("Joker");

	private final String value;

	SearchMethod(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	/**
	 * Finds a SearchMethod by its value.
	 * 
	 * @param value the value to find
	 * @return a SearchMethod or null if not found
	 */
	public static SearchMethod getByValue(String value) {
		for (SearchMethod sm : values()) {
			if (sm.value.equals(value)) {
				return sm;
			}
		}
		return null;
	}
}
