package pmb.allmusic.model.csv;

import com.opencsv.bean.CsvBindByName;

/** Bean for End of Year statistics. */
public class Stats {
  @CsvBindByName private Integer year;
  @CsvBindByName private Integer songsFiles;
  @CsvBindByName private Long songsCount;
  @CsvBindByName private Integer albumsFiles;
  @CsvBindByName private Long albumsCount;
  @CsvBindByName private Integer totalFiles;
  @CsvBindByName private Long totalCount;

  /**
   * Constructor.
   *
   * @param year year
   * @param songsFiles song files count
   * @param songsCount songs count
   * @param albumsFiles album files count
   * @param albumsCount albums count
   * @param totalFiles song and album files count
   * @param totalCount songs and albums count
   */
  public Stats(
      Integer year,
      Integer songsFiles,
      Long songsCount,
      Integer albumsFiles,
      Long albumsCount,
      Integer totalFiles,
      Long totalCount) {
    super();
    this.year = year;
    this.songsFiles = songsFiles;
    this.songsCount = songsCount;
    this.albumsFiles = albumsFiles;
    this.albumsCount = albumsCount;
    this.totalFiles = totalFiles;
    this.totalCount = totalCount;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public Integer getSongsFiles() {
    return songsFiles;
  }

  public void setSongsFiles(Integer songsFiles) {
    this.songsFiles = songsFiles;
  }

  public Long getSongsCount() {
    return songsCount;
  }

  public void setSongsCount(Long songsCount) {
    this.songsCount = songsCount;
  }

  public Integer getAlbumsFiles() {
    return albumsFiles;
  }

  public void setAlbumsFiles(Integer albumsFiles) {
    this.albumsFiles = albumsFiles;
  }

  public Long getAlbumsCount() {
    return albumsCount;
  }

  public void setAlbumsCount(Long albumsCount) {
    this.albumsCount = albumsCount;
  }

  public Integer getTotalFiles() {
    return totalFiles;
  }

  public void setTotalFiles(Integer totalFiles) {
    this.totalFiles = totalFiles;
  }

  public Long getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(Long totalCount) {
    this.totalCount = totalCount;
  }
}
