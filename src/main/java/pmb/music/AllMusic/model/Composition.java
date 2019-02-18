/**
 * 
 */
package pmb.music.AllMusic.model;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Une composition est ensemble composé d'un artiste et d'une oeuvre musicale
 * qui est listé dans au moins un {@link Fichier}.
 * 
 */
public class Composition implements Serializable {
	private static final long serialVersionUID = 1L;
	private String artist;
	private List<Fichier> files;
	private String titre;
	private RecordType recordType;
	private boolean canBeMerged;
	private boolean deleted;

	/**
	 * Constructeur.
	 * 
	 * @param artist un artiste (ou un groupe de musique)
	 * @param files les fichier contenant la composition
	 * @param titre le titre
	 * @param recordType si chanson ou album
	 * @param deleted si supprimé
	 * @param canBeMerged si peut etre mergé pendant le batch duplicate composition
	 */
	public Composition(String artist, List<Fichier> files, String titre, RecordType recordType, boolean deleted,
			boolean canBeMerged) {
		super();
		this.artist = artist;
		this.files = files;
		this.titre = titre;
		this.recordType = recordType;
		this.canBeMerged = canBeMerged;
		this.deleted = deleted;
	}

	/**
	 * Constructor with a given composition.
	 * 
	 * @param compo composition to clone
	 */
	public Composition(Composition compo) {
		super();
		this.artist = compo.getArtist();
		this.files = compo.getFiles().stream().map(Fichier::new).collect(Collectors.toList());
		this.titre = compo.getTitre();
		this.recordType = compo.getRecordType();
		this.canBeMerged = compo.isCanBeMerged();
		this.deleted = compo.isDeleted();
	}

	public Composition() {
		// Nothing to do
	}

	/**
	 * @return the artist
	 */
	public String getArtist() {
		return this.artist;
	}

	/**
	 * @param artist the artist to set
	 */
	public void setArtist(String artist) {
		this.artist = artist;
	}

	/**
	 * @return the files
	 */
	public List<Fichier> getFiles() {
		return this.files;
	}

	/**
	 * @param files the files to set
	 */
	public void setFiles(List<Fichier> files) {
		this.files = files;
	}

	/**
	 * @return the titre
	 */
	public String getTitre() {
		return this.titre;
	}

	/**
	 * @param titre the titre to set
	 */
	public void setTitre(String titre) {
		this.titre = titre;
	}

	/**
	 * @return the recordType
	 */
	public RecordType getRecordType() {
		return this.recordType;
	}

	/**
	 * @param recordType the recordType to set
	 */
	public void setRecordType(RecordType recordType) {
		this.recordType = recordType;
	}

	public boolean isCanBeMerged() {
		return canBeMerged;
	}

	public void setCanBeMerged(boolean canBeMerged) {
		this.canBeMerged = canBeMerged;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public String toString() {
		return "Composition [artist=" + artist + ", files=" + files + ", titre=" + titre + ", recordType=" + recordType
				+ ", canBeMerged=" + canBeMerged + ", deleted=" + deleted + "]";
	}
}
