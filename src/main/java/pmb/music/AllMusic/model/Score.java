package pmb.music.AllMusic.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Class to contains constants to calculate the score of Composition. Score
 * calculation: {@code max * Log10(max) * Log10((median*2)/currentRank + 3)}.
 * Created by PBR on 19 juin 2018.
 */
public class Score {

	/**
	 * Max is the biggest file size for songs files. LogMaxSong is equals to
	 * {@code Log10(max) * max}.
	 */
	private BigDecimal logMaxSong;

	/**
	 * Median is the ranking median of the song files. DoubleMedianSong is equals to
	 * the double of median.
	 */
	private BigDecimal doubleMedianSong;

	/**
	 * Max is the biggest file size for album files. LogMaxSong is equals to
	 * {@code Log10(max) * max}.
	 */
	private BigDecimal logMaxAlbum;

	/**
	 * Median is the ranking median of the album files. DoubleMedianSong is equals
	 * to the double of median.
	 */
	private BigDecimal doubleMedianAlbum;

	/**
	 * Decile score limit for songs.
	 */
	private List<Double> decileLimitSong;

	/**
	 * Decile score limit for albums.
	 */
	private List<Double> decileLimitAlbum;

	/**
	 * @see Score#logMaxSong
	 */
	public BigDecimal getLogMaxSong() {
		return logMaxSong;
	}

	/**
	 * @see Score#logMaxSong
	 */
	public void setLogMaxSong(BigDecimal logMaxSong) {
		this.logMaxSong = logMaxSong;
	}

	/**
	 * @see Score#doubleMedianSong
	 */
	public BigDecimal getDoubleMedianSong() {
		return doubleMedianSong;
	}

	/**
	 * @see Score#doubleMedianSong
	 */
	public void setDoubleMedianSong(BigDecimal doubleMedianSong) {
		this.doubleMedianSong = doubleMedianSong;
	}

	/**
	 * @see Score#logMaxAlbum
	 */
	public BigDecimal getLogMaxAlbum() {
		return logMaxAlbum;
	}

	/**
	 * @see Score#logMaxAlbum
	 */
	public void setLogMaxAlbum(BigDecimal logMaxAlbum) {
		this.logMaxAlbum = logMaxAlbum;
	}

	/**
	 * @see Score#doubleMedianAlbum
	 */
	public BigDecimal getDoubleMedianAlbum() {
		return doubleMedianAlbum;
	}

	/**
	 * @see Score#doubleMedianAlbum
	 */
	public void setDoubleMedianAlbum(BigDecimal doubleMedianAlbum) {
		this.doubleMedianAlbum = doubleMedianAlbum;
	}

	public List<Double> getDecileLimitSong() {
		return decileLimitSong;
	}

	public void setDecileLimitSong(List<Double> decileLimitSong) {
		this.decileLimitSong = decileLimitSong;
	}

	public List<Double> getDecileLimitAlbum() {
		return decileLimitAlbum;
	}

	public void setDecileLimitAlbum(List<Double> decileLimitAlbum) {
		this.decileLimitAlbum = decileLimitAlbum;
	}

	/**
	 * Select a log max constant depending of its type.
	 * 
	 * @param type the wanted type
	 * @return the log max
	 */
	public BigDecimal getLogMax(RecordType type) {
		if (type == RecordType.SONG) {
			return getLogMaxSong();
		} else {
			return getLogMaxAlbum();
		}
	}

	/**
	 * Select a double median constant depending of its type.
	 * 
	 * @param type the wanted type
	 * @return the double median
	 */
	public BigDecimal getDoubleMedian(RecordType type) {
		if (type == RecordType.SONG) {
			return getDoubleMedianSong();
		} else {
			return getDoubleMedianAlbum();
		}
	}

	public List<Double> getDecileLimit(RecordType type) {
		if (type == RecordType.SONG) {
			return getDecileLimitSong();
		} else {
			return getDecileLimitAlbum();
		}
	}
}
