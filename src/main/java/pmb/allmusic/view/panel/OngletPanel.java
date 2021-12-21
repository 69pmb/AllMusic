package pmb.allmusic.view.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pmb.allmusic.model.Composition;
import pmb.allmusic.model.Fichier;
import pmb.allmusic.model.RecordType;
import pmb.allmusic.model.Score;
import pmb.allmusic.model.SearchMethod;
import pmb.allmusic.utils.CompositionUtils;
import pmb.allmusic.utils.Constant;
import pmb.allmusic.utils.ScoreUtils;
import pmb.allmusic.utils.SearchUtils;
import pmb.allmusic.view.ActionPanel;
import pmb.allmusic.view.PanelUtils;
import pmb.allmusic.xml.ImportXML;
import pmb.my.starter.exception.MinorException;
import pmb.my.starter.utils.VariousUtils;

/** Handling application tabs. */
public final class OngletPanel {
  private static final Logger LOG = LogManager.getLogger(OngletPanel.class);
  private static JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP);
  private static Score score;
  private static String[] artistList;
  private static String[] titleList;
  private static String[] authorList;
  private static FichierPanel fichier;
  private static ArtistPanel artist;
  private static SearchPanel search;
  private static CompletableFuture<Void> asyncList;
  private static final Map<String, JPanel> TITLE_PANEL = new HashMap<>();

  private OngletPanel() {
    throw new AssertionError("Must not be used");
  }

  /**
   * Génère les onglets.
   *
   * @param frame la fenetre principale
   * @param withArtist if true the artist panel is displayed
   */
  public static void buildTabs(final JFrame frame, boolean withArtist) {
    LOG.debug("Start Onglet");
    tabs.setOpaque(true);
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    dim.height = 92 * dim.height / 100;
    dim.width -= 5;
    tabs.setPreferredSize(new Dimension(dim.width, dim.height));
    JScrollPane scrollPane = new JScrollPane(tabs);
    frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
    frame.pack();
    asyncList =
        CompletableFuture.runAsync(
            () -> {
              List<Composition> list = ImportXML.importXML(Constant.getFinalFilePath());
              initScore(list);
              setArtistList(list);
              setTitleList(list);
              setAuthorList(list);
            });

    setArtist(new ArtistPanel(withArtist));
    setFichier(new FichierPanel());
    ImportPanel importFile = new ImportPanel();
    setSearch(new SearchPanel());
    BatchEditPanel batchEdit = new BatchEditPanel();
    BatchGeneratePanel batchGenerate = new BatchGeneratePanel();
    BatchCheckPanel batchCheck = new BatchCheckPanel();

    addTab(Constant.ONGLET_SEARCH, getSearch());
    if (withArtist) {
      addTab(Constant.ONGLET_ARTIST, getArtist());
    }
    addTab(Constant.ONGLET_FICHIER, getFichier());
    addTab(Constant.ONGLET_IMPORT, importFile);
    addTab(Constant.ONGLET_BATCH_EDIT, batchEdit.getBatchPanel());
    addTab(Constant.ONGLET_BATCH_GENERATE, batchGenerate.getBatchPanel());
    addTab(Constant.ONGLET_BATCH_CHECK, batchCheck.getBatchPanel());
    getFichier().initPanel();

    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    scrollPane.addComponentListener(
        new ComponentListener() {
          @Override
          public void componentShown(ComponentEvent e) {
            // Nothing to do
          }

          @Override
          public void componentResized(ComponentEvent e) {
            PanelUtils.setColumnsWidth(
                getSearch().getTableResult(), e.getComponent().getWidth(), "Search");
            PanelUtils.setColumnsWidth(
                getFichier().getTableFiles(), e.getComponent().getWidth(), "Fichier files");
            PanelUtils.setColumnsWidth(
                getFichier().getTableCompo(), e.getComponent().getWidth(), "Fichier compo");
            if (withArtist) {
              PanelUtils.setColumnsWidth(
                  getArtist().getTable(), e.getComponent().getWidth(), "Artist");
            }
          }

          @Override
          public void componentMoved(ComponentEvent e) {
            // Nothing to do
          }

          @Override
          public void componentHidden(ComponentEvent e) {
            // Nothing to do
          }
        });

    // Default button handling
    getSearch().getRootPane().setDefaultButton(getSearch().getActionButton());
    tabs.addChangeListener(
        (ChangeEvent e) -> {
          if (e.getSource() instanceof JTabbedPane && tabs.getSelectedComponent() != null) {
            // Modifies default button when changing current tab
            ((JPanel) tabs.getSelectedComponent())
                .getRootPane()
                .setDefaultButton(((ActionPanel) tabs.getSelectedComponent()).getActionButton());
          }
        });
    LOG.debug("End Onglet");
  }

  /** Calculates the constants of {@link Score}. */
  private static void initScore(List<Composition> list) {
    LOG.debug("Start initScore");
    OngletPanel.score = new Score();
    List<Composition> songs = getByType(list, RecordType.SONG, true);
    List<Composition> albums = getByType(list, RecordType.ALBUM, true);
    getScore().setLogMaxAlbum(ScoreUtils.getLogMax(albums));
    getScore().setLogMaxSong(ScoreUtils.getLogMax(songs));
    getScore().setDoubleMedianAlbum(ScoreUtils.getDoubleMedianRanking(albums));
    getScore().setDoubleMedianSong(ScoreUtils.getDoubleMedianRanking(songs));
    getScore()
        .setDecileLimitSong(ScoreUtils.getDecileLimit(getByType(list, RecordType.SONG, false)));
    getScore()
        .setDecileLimitAlbum(ScoreUtils.getDecileLimit(getByType(list, RecordType.ALBUM, false)));
    LOG.debug("End initScore");
  }

  /**
   * Filters given compositions list by type and sorting.
   *
   * @param importXML the list to filter
   * @param type the record type
   * @param sorted if true only sorted will be returned
   * @return a list of composition
   */
  private static List<Composition> getByType(
      List<Composition> list, RecordType type, boolean sorted) {
    LOG.debug("Start getByType");
    Map<String, String> criteria = new HashMap<>();
    criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, type.toString());
    if (sorted) {
      criteria.put(SearchUtils.CRITERIA_SORTED, Boolean.TRUE.toString());
    }
    List<Composition> result =
        SearchUtils.search(list, criteria, true, SearchMethod.CONTAINS, true, true);
    LOG.debug("End getByType");
    return result;
  }

  /**
   * Extracts the artist from a list of compositions, (unique and sorted) and set to artistList.
   *
   * @param list the list
   */
  private static void setArtistList(List<Composition> list) {
    OngletPanel.artistList =
        VariousUtils.distinctStreamToArray(
            CompositionUtils.groupByFieldAndSortByScore(list, Composition::getArtist)
                .keySet()
                .stream()
                .map(
                    artist ->
                        StringUtils.startsWithIgnoreCase(artist, "the")
                            ? StringUtils.substringAfter(StringUtils.lowerCase(artist), "the")
                            : artist)
                .map(StringUtils::trim)
                .map(WordUtils::capitalizeFully));
  }

  /**
   * Extracts the title from a list of compositions, (unique and sorted) and set to titleList.
   *
   * @param list the list
   */
  private static void setTitleList(List<Composition> list) {
    OngletPanel.titleList =
        VariousUtils.distinctStreamToArray(
            CompositionUtils.groupByFieldAndSortByScore(list, Composition::getTitre)
                .keySet()
                .stream()
                .map(StringUtils::trim)
                .map(WordUtils::capitalizeFully));
  }

  /**
   * Extracts the author from a list of compositions, (unique and sorted) and set to authorList.
   *
   * @param list the list
   */
  private static void setAuthorList(List<Composition> list) {
    OngletPanel.authorList =
        VariousUtils.distinctStreamToArray(
            list.parallelStream()
                .map(Composition::getFiles)
                .flatMap(List::stream)
                .map(Fichier::getAuthor)
                .map(WordUtils::capitalizeFully)
                .distinct()
                .sorted());
  }

  /**
   * Gets a tab by its title.
   *
   * @param title wanted tab's title
   * @return the {@link JPanel}
   */
  private static JPanel getTabByTitle(String title) {
    return Optional.ofNullable(TITLE_PANEL.get(title))
        .orElseThrow(() -> new MinorException("Given tab doesn't exist: " + title));
  }

  /**
   * Gets selected tab title.
   *
   * @return the title
   */
  public static String getSelectTabTitle() {
    return tabs.getTitleAt(tabs.getSelectedIndex());
  }

  /**
   * Selects a tab by its title, it must exist.
   *
   * @param title of the wanted tab
   */
  public static void setSelectTab(String title) {
    tabs.setSelectedComponent(getTabByTitle(title));
  }

  private static void addTab(String title, JPanel panel) {
    tabs.addTab(title, panel);
    TITLE_PANEL.putIfAbsent(title, panel);
  }

  public static Score getScore() {
    return score;
  }

  public static String[] getArtistList() {
    return artistList;
  }

  public static String[] getTitleList() {
    return titleList;
  }

  public static String[] getAuthorList() {
    return authorList;
  }

  public static FichierPanel getFichier() {
    return fichier;
  }

  public static ArtistPanel getArtist() {
    return artist;
  }

  private static void setFichier(FichierPanel fichier) {
    OngletPanel.fichier = fichier;
  }

  private static void setArtist(ArtistPanel artist) {
    OngletPanel.artist = artist;
  }

  public static SearchPanel getSearch() {
    return search;
  }

  private static void setSearch(SearchPanel search) {
    OngletPanel.search = search;
  }

  public static CompletableFuture<Void> getAsyncList() {
    return asyncList;
  }
}
