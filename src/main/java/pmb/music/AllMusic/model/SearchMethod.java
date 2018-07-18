package pmb.music.AllMusic.model;

/**
 * Enumération des différentes façon de faire des recherches.
 * @author pmbroca
 */
public enum SearchMethod {
    /**
     * Les critères de recherche seront utilisés comme des {@code '%like'}.
     */
    CONTAINS("CONTAINS"),
    /**
     * Le champ doit commencer comme le critère. 
     */
    BEGINS_WITH("BEGINS_WITH"),
    /**
     * Le champ doit être exactement égal au critère.  
     */
    WHOLE_WORD("WHOLE_WORD"),
    /**
     * Le critère de recherche comporte des {@code '*'} qui seront interprétés comme des caractères joker.
     */
    JOKER("JOKER");

	private final String value;

	SearchMethod(String value) {
		this.value = value;
	}

	public String getSearchMethod() {
		return value;
	}
}
