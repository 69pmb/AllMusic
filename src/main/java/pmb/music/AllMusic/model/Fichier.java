/**
 * 
 */
package pmb.music.AllMusic.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Représentation des listes contenant des compositions. Décrit ayant un auteur,
 * une année de parution, un nom, une catégorie, des dates d'application, un
 * classment et une taille.
 */
public class Fichier implements Serializable {

	private static final long serialVersionUID = 1L;

	private String author;

	private String fileName;

	private int publishYear;

	private Cat categorie;

	private int rangeDateBegin;

	private int rangeDateEnd;

	private Boolean sorted;

	private int classement;

	private Date creationDate;

	private int size;

	/**
	 * Clone un Fichier.
	 * 
	 * @param fichier le fichier à cloner
	 */
	public Fichier(Fichier fichier) {
		super();
		this.author = fichier.getAuthor();
		this.fileName = fichier.getFileName();
		this.publishYear = fichier.getPublishYear();
		this.categorie = fichier.getCategorie();
		this.rangeDateBegin = fichier.getRangeDateBegin();
		this.rangeDateEnd = fichier.getRangeDateEnd();
		this.sorted = fichier.getSorted();
		this.classement = fichier.getClassement();
		this.creationDate = fichier.getCreationDate();
		this.size = fichier.getSize();
	}

	public Fichier() {
		// Nothing to do
	}

	/**
	 * @return the publishYear
	 */
	public int getPublishYear() {
		return this.publishYear;
	}

	/**
	 * @param publishYear the publishYear to set
	 */
	public void setPublishYear(int publishYear) {
		this.publishYear = publishYear;
	}

	/**
	 * @return the categorie
	 */
	public Cat getCategorie() {
		return this.categorie;
	}

	/**
	 * @param categorie the categorie to set
	 */
	public void setCategorie(Cat categorie) {
		this.categorie = categorie;
	}

	/**
	 * @return the rangeDateBegin
	 */
	public int getRangeDateBegin() {
		return this.rangeDateBegin;
	}

	/**
	 * @param rangeDateBegin the rangeDateBegin to set
	 */
	public void setRangeDateBegin(int rangeDateBegin) {
		this.rangeDateBegin = rangeDateBegin;
	}

	/**
	 * @return the rangeDateEnd
	 */
	public int getRangeDateEnd() {
		return this.rangeDateEnd;
	}

	/**
	 * @param rangeDateEnd the rangeDateEnd to set
	 */
	public void setRangeDateEnd(int rangeDateEnd) {
		this.rangeDateEnd = rangeDateEnd;
	}

	/**
	 * @return the sorted
	 */
	public Boolean getSorted() {
		return this.sorted;
	}

	/**
	 * @param sorted the sorted to set
	 */
	public void setSorted(Boolean sorted) {
		this.sorted = sorted;
	}

	/**
	 * @return the classement
	 */
	public int getClassement() {
		return this.classement;
	}

	/**
	 * @param classement the classement to set
	 */
	public void setClassement(int classement) {
		this.classement = classement;
	}

	/**
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return this.creationDate;
	}

	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return this.author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return this.fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return this.size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Fichier [author=" + this.author + ", fileName=" + this.fileName + ", publishYear=" + this.publishYear + ", categorie=" + this.categorie
				+ ", rangeDateBegin=" + this.rangeDateBegin + ", rangeDateEnd=" + this.rangeDateEnd + ", sorted=" + this.sorted + ", classement="
				+ this.classement + ", creationDate=" + this.creationDate + ", size=" + this.size + "]\t\r\n";
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
		result = prime * result + ((this.author == null) ? 0 : this.author.hashCode());
		result = prime * result + ((this.categorie == null) ? 0 : this.categorie.hashCode());
		result = prime * result + this.classement;
		result = prime * result + ((this.creationDate == null) ? 0 : this.creationDate.hashCode());
		result = prime * result + ((this.fileName == null) ? 0 : this.fileName.hashCode());
		result = prime * result + this.publishYear;
		result = prime * result + this.rangeDateBegin;
		result = prime * result + this.rangeDateEnd;
		result = prime * result + this.size;
		result = prime * result + ((this.sorted == null) ? 0 : this.sorted.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Fichier other = (Fichier) obj;
		if (this.author == null) {
			if (other.author != null)
				return false;
		} else if (!this.author.equals(other.author))
			return false;
		if (this.categorie != other.categorie)
			return false;
		if (this.classement != other.classement)
			return false;
		if (this.creationDate == null) {
			if (other.creationDate != null)
				return false;
		} else if (!this.creationDate.equals(other.creationDate))
			return false;
		if (this.fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!this.fileName.equals(other.fileName))
			return false;
		if (this.publishYear != other.publishYear)
			return false;
		if (this.rangeDateBegin != other.rangeDateBegin)
			return false;
		if (this.rangeDateEnd != other.rangeDateEnd)
			return false;
		if (this.size != other.size)
			return false;
		if (this.sorted == null) {
			if (other.sorted != null)
				return false;
		} else if (!this.sorted.equals(other.sorted))
			return false;
		return true;
	}
}
