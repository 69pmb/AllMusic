/** */
package pmb.allmusic.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Représentation des listes contenant des compositions. Décrit ayant un auteur, une année de
 * parution, un nom, une catégorie, des dates d'application, un classment et une taille.
 */
public class Fichier implements Serializable {

  private static final long serialVersionUID = 1L;

  private String author;

  private String fileName;

  private int publishYear;

  private Cat categorie;

  private Integer rangeDateBegin;

  private Integer rangeDateEnd;

  private Boolean sorted;

  private Integer classement;

  private LocalDateTime creationDate;

  private Integer size;

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
  public Integer getRangeDateBegin() {
    return this.rangeDateBegin;
  }

  /**
   * @param rangeDateBegin the rangeDateBegin to set
   */
  public void setRangeDateBegin(Integer rangeDateBegin) {
    this.rangeDateBegin = rangeDateBegin;
  }

  /**
   * @return the rangeDateEnd
   */
  public Integer getRangeDateEnd() {
    return this.rangeDateEnd;
  }

  /**
   * @param rangeDateEnd the rangeDateEnd to set
   */
  public void setRangeDateEnd(Integer rangeDateEnd) {
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
  public Integer getClassement() {
    return this.classement;
  }

  /**
   * @param classement the classement to set
   */
  public void setClassement(Integer classement) {
    this.classement = classement;
  }

  /**
   * @return the creationDate
   */
  public LocalDateTime getCreationDate() {
    return this.creationDate;
  }

  /**
   * @param creationDate the creationDate to set
   */
  public void setCreationDate(LocalDateTime creationDate) {
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
  public Integer getSize() {
    return this.size;
  }

  /**
   * @param size the size to set
   */
  public void setSize(Integer size) {
    this.size = size;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Fichier [author="
        + this.author
        + ", fileName="
        + this.fileName
        + ", publishYear="
        + this.publishYear
        + ", categorie="
        + this.categorie
        + ", rangeDateBegin="
        + this.rangeDateBegin
        + ", rangeDateEnd="
        + this.rangeDateEnd
        + ", sorted="
        + this.sorted
        + ", classement="
        + this.classement
        + ", creationDate="
        + this.creationDate
        + ", size="
        + this.size
        + "]\t\r\n";
  }
}
