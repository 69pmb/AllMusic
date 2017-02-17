/**
 * 
 */
package pmb.music.AllMusic.model;

import java.util.Date;

/**
 * @author i2113mj
 *
 */
public class Fichier {

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

    public Fichier(String author, String fileName, int publishYear, Cat categorie, int rangeDateBegin, int rangeDateEnd, Boolean sorted, int classement, Date creationDate, int size) {
        super();
        this.author = author;
        this.fileName = fileName;
        this.publishYear = publishYear;
        this.categorie = categorie;
        this.rangeDateBegin = rangeDateBegin;
        this.rangeDateEnd = rangeDateEnd;
        this.sorted = sorted;
        this.classement = classement;
        this.creationDate = creationDate;
        this.size = size;
    }
    
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
    }

    /**
     * @return the publishYear
     */
    public int getPublishYear() {
        return this.publishYear;
    }

    /**
     * @param publishYear
     *            the publishYear to set
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
     * @param categorie
     *            the categorie to set
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
     * @param rangeDateBegin
     *            the rangeDateBegin to set
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
     * @param rangeDateEnd
     *            the rangeDateEnd to set
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
     * @param sorted
     *            the sorted to set
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
     * @param classement
     *            the classement to set
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
     * @param creationDate
     *            the creationDate to set
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
     * @param author
     *            the author to set
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
     * @param fileName
     *            the fileName to set
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Fichier [author=" + this.author + ", fileName=" + this.fileName + ", publishYear=" + this.publishYear + ", categorie=" + this.categorie + ", rangeDateBegin="
                + this.rangeDateBegin + ", rangeDateEnd=" + this.rangeDateEnd + ", sorted=" + this.sorted + ", classement=" + this.classement + ", creationDate="
                + this.creationDate + ", size=" + this.size + "]";
    }
}
