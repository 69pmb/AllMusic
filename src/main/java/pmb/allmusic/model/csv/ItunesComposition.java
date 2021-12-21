package pmb.allmusic.model.csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import java.util.Date;

/** Model class for Itunes csv file. */
public class ItunesComposition {

  @CsvBindByName(column = "Titre")
  private String titre;

  @CsvBindByName(column = "Artiste")
  private String artist;

  @CsvBindByName(column = "Album")
  private String album;

  @CsvBindByName(column = "Duree")
  private String duration;

  @CsvBindByName(column = "Debit")
  private String bitRate;

  @CsvBindByName(column = "Date d'ajout")
  @CsvDate("dd/MM/yyyy HH:mm")
  private Date added;

  @CsvBindByName(column = "Annee")
  private Integer year;

  @CsvBindByName(column = "Lectures")
  private Integer playCount;

  @CsvBindByName(column = "Classement")
  private Integer rank;

  @CsvBindByName(column = "Derniere lecture")
  @CsvDate("dd/MM/yyyy HH:mm")
  private Date lastPlay;

  @CsvBindByName(column = "Numero de piste")
  private String trackNumber;

  @CsvBindByName(column = "Numero de disque")
  private String cdNumber;

  @CsvBindByName(column = "Deleted Song")
  private String deletedSong;

  @CsvBindByName(column = "Deleted Album")
  private String deletedAlbum;

  public ItunesComposition() {
    // Nothing to do
  }

  public String getTitre() {
    return titre;
  }

  public void setTitre(String titre) {
    this.titre = titre;
  }

  public String getArtist() {
    return artist;
  }

  public void setArtist(String artist) {
    this.artist = artist;
  }

  public String getAlbum() {
    return album;
  }

  public void setAlbum(String album) {
    this.album = album;
  }

  public String getDuration() {
    return duration;
  }

  public void setDuration(String duration) {
    this.duration = duration;
  }

  public String getBitRate() {
    return bitRate;
  }

  public void setBitRate(String bitRate) {
    this.bitRate = bitRate;
  }

  public Date getAdded() {
    return added;
  }

  public void setAdded(Date added) {
    this.added = added;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public Integer getPlayCount() {
    return playCount;
  }

  public void setPlayCount(Integer playCount) {
    this.playCount = playCount;
  }

  public Integer getRank() {
    return rank;
  }

  public void setRank(Integer rank) {
    this.rank = rank;
  }

  public Date getLastPlay() {
    return lastPlay;
  }

  public void setLastPlay(Date lastPlay) {
    this.lastPlay = lastPlay;
  }

  public String getDeletedSong() {
    return deletedSong;
  }

  public void setDeletedSong(String deletedSong) {
    this.deletedSong = deletedSong;
  }

  public String getDeletedAlbum() {
    return deletedAlbum;
  }

  public void setDeletedAlbum(String deletedAlbum) {
    this.deletedAlbum = deletedAlbum;
  }

  public String getTrackNumber() {
    return trackNumber;
  }

  public void setTrackNumber(String trackNumber) {
    this.trackNumber = trackNumber;
  }

  public String getCdNumber() {
    return cdNumber;
  }

  public void setCdNumber(String cdNumber) {
    this.cdNumber = cdNumber;
  }

  @Override
  public String toString() {
    return "CsvComposition [titre="
        + titre
        + ", artist="
        + artist
        + ", album="
        + album
        + ", duration="
        + duration
        + ", bitRate="
        + bitRate
        + ", added="
        + added
        + ", year="
        + year
        + ", playCount="
        + playCount
        + ", rank="
        + rank
        + ", lastPlay="
        + lastPlay
        + ", trackNumber="
        + trackNumber
        + ", cdNumber="
        + cdNumber
        + ", deletedSong="
        + deletedSong
        + ", deletedAlbum="
        + deletedAlbum
        + "]";
  }
}
