package pmb.music.AllMusic.model;

import java.util.Date;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

import pmb.music.AllMusic.utils.Constant;

/**
 * Created by PBR on 27 déc. 2018.
 */
public class CsvComposition {

	@CsvBindByName(column = "Titre")
	private String titre;

	@CsvBindByName(column = "Artiste")
	private String artist;

	@CsvBindByName(column = "Album")
	private String album;

	@CsvBindByName(column = "Durée")
	private String duration;

	@CsvBindByName(column = "Débit")
	private String bitRate;

	@CsvBindByName(column = "Date d'ajout")
	@CsvDate("dd/MM/yyyy HH:mm")
	private Date added;

	@CsvBindByName(column = "Année")
	private Integer year;

	@CsvBindByName(column = "Lectures")
	private Integer playCount;

	@CsvBindByName(column = "Classement")
	private Integer rank;

	@CsvBindByName(column = "Dernière lecture")
	@CsvDate("dd/MM/yyyy HH:mm")
	private Date lastPlay;

	@CsvBindByName(column = "Deleted")
	private String deleted;

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

	public String getDeleted() {
		return deleted;
	}

	public void setDeleted(String deleted) {
		this.deleted = deleted;
	}

	public String prettyToString() {
		String lastPlayStr = "";
		if (lastPlay != null) {
			lastPlayStr = new Constant().getSdfDttm().format(lastPlay);
		}
		String addedStr = "";
		if (added != null) {
			addedStr = new Constant().getSdfDttm().format(added);
		}
		return artist + " - " + titre + Constant.NEW_LINE + "year: " + year + Constant.NEW_LINE + "album: " + album
				+ Constant.NEW_LINE + "duration: " + duration + Constant.NEW_LINE + "added: " + addedStr
				+ Constant.NEW_LINE + "playCount: " + playCount + Constant.NEW_LINE + "rank: " + rank
				+ Constant.NEW_LINE + "lastPlay: " + lastPlayStr + Constant.NEW_LINE + "bit rate: " + bitRate;
	}

	@Override
	public String toString() {
		return "CsvComposition [titre=" + titre + ", artist=" + artist + ", album=" + album + ", duration=" + duration
				+ ", bitRate=" + bitRate + ", added=" + added + ", year=" + year + ", playCount=" + playCount
				+ ", rank=" + rank + ", lastPlay=" + lastPlay + ", deleted=" + deleted + "]";
	}
}
