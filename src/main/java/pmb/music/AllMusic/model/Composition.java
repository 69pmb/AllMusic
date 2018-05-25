/**
 * 
 */
package pmb.music.AllMusic.model;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

/**
 * Une composition est ensemble composé d'un artiste et d'une oeuvre musicale
 * qui est listé dans au moins un {@link Fichier}.
 */
public class Composition implements Serializable {
	private static final long serialVersionUID = 1L;
	private String artist;
	private List<Fichier> files;
	private String titre;
	private RecordType recordType;
	private boolean canBeMerged;

	/**
	 * Constructeur.
	 * @param artist un artiste (ou un groupe de musique)
	 * @param files les fichier contenant la composition
	 * @param titre le titre
	 * @param recordType si chanson ou album
	 */
	public Composition(String artist, List<Fichier> files, String titre, RecordType recordType) {
		super();
		this.artist = artist;
		this.files = files;
		this.titre = titre;
		this.recordType = recordType;
	}
	
	public Composition(Composition compo) {
		super();
		this.artist = compo.getArtist();
		this.files = compo.getFiles();
		this.titre = compo.getTitre();
		this.recordType = compo.getRecordType();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Composition [artist=" + this.artist + ", titre=" + this.titre + ", recordType=" + this.recordType + ", files=" + this.files + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.artist == null) ? 0 : this.artist.hashCode());
		result = prime * result + ((this.files == null) ? 0 : this.files.hashCode());
		result = prime * result + ((this.titre == null) ? 0 : this.titre.hashCode());
		result = prime * result + ((this.recordType == null) ? 0 : this.recordType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Composition other = (Composition) obj;
		if (this.artist == null) {
			if (other.artist != null)
				return false;
		} else if (!this.artist.equals(other.artist))
			return false;
		if (CollectionUtils.isEmpty(this.files)) {
			if (CollectionUtils.isEmpty(other.files))
				return false;
		} else if (this.files.size() != other.files.size()) {
			return false;
		} else {
			for (int i = 0; i < this.files.size(); i++) {
				if (!this.files.get(i).equals(other.files.get(i))) {
					return false;
				}
			}
		}
		if (this.titre == null) {
			if (other.titre != null)
				return false;
		} else if (!this.titre.equals(other.titre))
			return false;
		if (this.recordType != other.recordType)
			return false;
		return true;
	}
}
