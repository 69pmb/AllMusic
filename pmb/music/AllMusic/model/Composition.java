/**
 * 
 */
package pmb.music.AllMusic.model;

import java.util.List;

/**
 * @author i2113mj
 *
 */
public class Composition {
    private String artist;
    private List<Fichier> files;
    private String oeuvre;
    private RecordType recordType;
    
    public Composition(String artist, List<Fichier> files, String oeuvre, RecordType recordType) {
        super();
        this.artist = artist;
        this.files = files;
        this.oeuvre = oeuvre;
        this.recordType = recordType;
    }

    public Composition() {
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
     * @return the oeuvre
     */
    public String getOeuvre() {
        return this.oeuvre;
    }
    
    /**
     * @param oeuvre the oeuvre to set
     */
    public void setOeuvre(String oeuvre) {
        this.oeuvre = oeuvre;
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Composition [artist=" + this.artist + ", oeuvre=" + this.oeuvre + ", recordType=" + this.recordType + ", files=" + this.files + "]";
    }
}
