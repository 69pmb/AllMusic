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
	WHOLE_WORD("Egal"),
	/**
	 * Le critère de recherche comporte des {@code '*'} qui seront interprétés comme
	 * des caractères joker.
	 */
	JOKER("Joker");

	private final String value;

	SearchMethod(String value) {
		this.value = value;
	}

	public String getSearchMethod() {
		return value;
	}
}
