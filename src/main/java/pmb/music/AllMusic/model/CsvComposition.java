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

	@CsvBindByName(column = "Année")
	private int year;

	@CsvBindByName(column = "Album")
	private String album;

	@CsvBindByName(column = "Durée")
	private String duration;

	@CsvBindByName(column = "Date d'ajout")
	@CsvDate("dd/MM/yyyy HH:mm")
	private Date added;

	@CsvBindByName(column = "Lectures")
	private int playCount;

	@CsvBindByName(column = "Classement")
	private int rank;

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

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
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

	public Date getAdded() {
		return added;
	}

	public void setAdded(Date added) {
		this.added = added;
	}

	public int getPlayCount() {
		return playCount;
	}

	public void setPlayCount(int playCount) {
		this.playCount = playCount;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
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

	@Override
	public String toString() {
		return "CsvComposition [titre=" + titre + ", artist=" + artist + ", year=" + year + ", album=" + album
				+ ", duration=" + duration + ", added=" + added + ", playCount=" + playCount + ", rank=" + rank
				+ ", lastPlay=" + lastPlay + "]";
	}

	public String prettyToString() {
		return artist + " - " + titre + Constant.NEW_LINE + "year: " + year + Constant.NEW_LINE + "album: " + album
				+ Constant.NEW_LINE + "duration: " + duration + Constant.NEW_LINE + "added: "
				+ Constant.getSdfDttm().format(added) + Constant.NEW_LINE + "playCount: " + playCount
				+ Constant.NEW_LINE + "rank: " + rank + Constant.NEW_LINE + "lastPlay: "
				+ Constant.getSdfDttm().format(lastPlay);
	}
}
