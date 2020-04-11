package pmb.music.AllMusic.view.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.model.Score;
import pmb.music.AllMusic.model.SearchMethod;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.ScoreUtils;
import pmb.music.AllMusic.utils.SearchUtils;
import pmb.music.AllMusic.view.BasicFrame;
import pmb.music.AllMusic.view.PanelUtils;

/**
 * Classe gérant les onglet de l'appli.
 *
 * @author pmbroca
 */
public class OngletPanel extends JPanel {
    private static final long serialVersionUID = -7235352581168930316L;
    private static final Logger LOG = LogManager.getLogger(OngletPanel.class);
    private static JTabbedPane onglets;
    private static Score score;
    private static String[] artistList;
    private static String[] titleList;
    private static String[] authorList;
    private static FichierPanel fichier;
    private static ArtistPanel artist;
    private static SearchPanel search;
    private static boolean withArtist;
    private static CompletableFuture<Void> asyncList;

    /**
     * Génère les onglets.
     *
     * @param myFrame la fenetre principale
     * @param withArtist if true the artist panel is displayed
     */
    public OngletPanel(final BasicFrame myFrame, boolean withArtist) {
        LOG.debug("Start Onglet");
        setOnglets(new JTabbedPane(SwingConstants.TOP));
        final JPanel panel = new JPanel();
        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        dim.height = 92 * dim.height / 100;
        dim.width = dim.width - 30;
        getOnglets().setPreferredSize(dim);
        setWithArtist(withArtist);

        asyncList = CompletableFuture.runAsync(new Thread(() -> {
            List<Composition> list = ImportXML.importXML(Constant.getFinalFilePath());
            initScore(list);
            setArtistList(list);
            setTitleList(list);
            setAuthorList(list);
        }));

        setArtist(new ArtistPanel(withArtist));
        setFichier(new FichierPanel());
        ImportPanel importFile = new ImportPanel();
        setSearch(new SearchPanel());
        BatchEditPanel batchEdit = new BatchEditPanel();
        BatchGeneratePanel batchGenerate = new BatchGeneratePanel();
        BatchCheckPanel batchCheck = new BatchCheckPanel();

        getOnglets().addTab(Constant.ONGLET_SEARCH, getSearch());
        if (withArtist) {
            getOnglets().addTab(Constant.ONGLET_ARTIST, getArtist());
        }
        getOnglets().addTab(Constant.ONGLET_FICHIER, getFichier());
        getOnglets().addTab(Constant.ONGLET_IMPORT, importFile);
        getOnglets().addTab(Constant.ONGLET_BATCH_EDIT, batchEdit.getBatchPanel());
        getOnglets().addTab(Constant.ONGLET_BATCH_GENERATE, batchGenerate.getBatchPanel());
        getOnglets().addTab(Constant.ONGLET_BATCH_CHECK, batchCheck.getBatchPanel());
        getFichier().initPanel();

        getOnglets().setOpaque(true);
        panel.add(getOnglets());
        panel.validate();

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.addComponentListener(new ComponentListener() {
            @Override
            public void componentShown(ComponentEvent e) {
                // Nothing to do
            }

            @Override
            public void componentResized(ComponentEvent e) {
                PanelUtils.setColumnsWidth(getSearch().getTableResult(), e.getComponent().getWidth(), "Search");
                PanelUtils.setColumnsWidth(getFichier().getTableFiles(), e.getComponent().getWidth(), "Fichier files");
                PanelUtils.setColumnsWidth(getFichier().getTableCompo(), e.getComponent().getWidth(), "Fichier compo");
                if (withArtist) {
                    PanelUtils.setColumnsWidth(getArtist().getTable(), e.getComponent().getWidth(), "Artist");
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
        myFrame.getFrame().getContentPane().add(scrollPane, BorderLayout.CENTER);
        myFrame.getFrame().pack();

        // Default button handling
        getSearch().getRootPane().setDefaultButton(getSearch().getSearch());
        getOnglets().addChangeListener((ChangeEvent e) -> {
            if (e.getSource() instanceof JTabbedPane) {
                // Modifies default button when changing current tab
                getSelectedDefaultButtonByTab(getSearch(), getFichier(), getArtist(), importFile, batchGenerate, batchEdit, batchCheck);
            }
        });
        LOG.debug("End Onglet");
    }

    /**
     * Calculates the constants of {@link Score}.
     */
    private static void initScore(List<Composition> list) {
        LOG.debug("Start initScore");
        OngletPanel.score = new Score();
        List<Composition> songs = getByType(list, RecordType.SONG, true);
        List<Composition> albums = getByType(list, RecordType.ALBUM, true);
        getScore().setLogMaxAlbum(ScoreUtils.getLogMax(albums));
        getScore().setLogMaxSong(ScoreUtils.getLogMax(songs));
        getScore().setDoubleMedianAlbum(ScoreUtils.getDoubleMedianRanking(albums));
        getScore().setDoubleMedianSong(ScoreUtils.getDoubleMedianRanking(songs));
        getScore().setDecileLimitSong(ScoreUtils.getDecileLimit(getByType(list, RecordType.SONG, false)));
        getScore().setDecileLimitAlbum(ScoreUtils.getDecileLimit(getByType(list, RecordType.ALBUM, false)));
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
    private static List<Composition> getByType(List<Composition> list, RecordType type, boolean sorted) {
        LOG.debug("Start getByType");
        Map<String, String> criteria = new HashMap<>();
        criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, type.toString());
        if (sorted) {
            criteria.put(SearchUtils.CRITERIA_SORTED, Boolean.TRUE.toString());
        }
        List<Composition> result = SearchUtils.search(list, criteria, true, SearchMethod.CONTAINS, true, true);
        LOG.debug("End getByType");
        return result;
    }

    /**
     * Extracts the artist from a list of compositions, (unique and sorted) and set
     * to artistList.
     *
     * @param list the list
     */
    private static void setArtistList(List<Composition> list) {
        OngletPanel.artistList = MiscUtils.distinctStreamToArray(
                CompositionUtils.groupByFieldAndSortByScore(list, Composition::getArtist).keySet().stream()
                .map(artist -> StringUtils.startsWithIgnoreCase(artist, "the")
                        ? StringUtils.substringAfter(StringUtils.lowerCase(artist), "the")
                                : artist)
                .map(StringUtils::trim).map(WordUtils::capitalize));
    }

    /**
     * Extracts the title from a list of compositions, (unique and sorted) and set
     * to titleList.
     *
     * @param list the list
     */
    private static void setTitleList(List<Composition> list) {
        OngletPanel.titleList = MiscUtils.distinctStreamToArray(
                CompositionUtils.groupByFieldAndSortByScore(list, Composition::getTitre).keySet().stream()
                .map(StringUtils::trim).map(WordUtils::capitalize));
    }

    /**
     * Extracts the author from a list of compositions, (unique and sorted) and set
     * to authorList.
     *
     * @param list the list
     */
    private static void setAuthorList(List<Composition> list) {
        OngletPanel.authorList = MiscUtils.distinctStreamToArray(list.parallelStream().map(Composition::getFiles)
                .flatMap(List::stream).map(Fichier::getAuthor).map(WordUtils::capitalize).distinct().sorted());
    }

    private static String getSelectedDefaultButtonByTab(SearchPanel search, FichierPanel fichier, ArtistPanel artist,
            ImportPanel importP, BatchGeneratePanel generate, BatchEditPanel edit, BatchCheckPanel check) {
        // TODO rewrite with stream
        int index = getOnglets().getSelectedIndex();
        index = !isWithArtist() && index != 0 ? index + 1 : index;
        String tab = "";
        switch (index) {
        case 0:
            search.getRootPane().setDefaultButton(search.getSearch());
            tab = Constant.ONGLET_SEARCH;
            break;
        case 1:
            artist.getRootPane().setDefaultButton(artist.getSearch());
            tab = Constant.ONGLET_ARTIST;
            break;
        case 2:
            fichier.getRootPane().setDefaultButton(fichier.getSearch());
            tab = Constant.ONGLET_FICHIER;
            break;
        case 3:
            importP.getRootPane().setDefaultButton(importP.getImportFile());
            tab = Constant.ONGLET_IMPORT;
            break;
        case 4:
            edit.getBatchPanel().getRootPane().setDefaultButton(edit.getBatchFileBtn());
            tab = Constant.ONGLET_BATCH_EDIT;
            break;
        case 5:
            generate.getBatchPanel().getRootPane().setDefaultButton(generate.getBatchFileBtn());
            tab = Constant.ONGLET_BATCH_GENERATE;
            break;
        case 6:
            check.getBatchPanel().getRootPane().setDefaultButton(check.getBatchFileBtn());
            tab = Constant.ONGLET_BATCH_CHECK;
            break;
        default:
            break;
        }
        LOG.debug(tab);
        return tab;
    }

    /**
     * For given tab name give its index.
     *
     * @param tab wanted tab
     * @return index
     */
    public static int getTabIndex(String tab) {
        // TODO rewrite with stream
        int result;
        switch (tab) {
        case Constant.ONGLET_SEARCH:
            result = 0;
            break;
        case Constant.ONGLET_ARTIST:
            result = 1;
            break;
        case Constant.ONGLET_FICHIER:
            result = isWithArtist() ? 2 : 1;
            break;
        case Constant.ONGLET_IMPORT:
            result = isWithArtist() ? 3 : 2;
            break;
        case Constant.ONGLET_BATCH_EDIT:
            result = isWithArtist() ? 4 : 5;
            break;
        case Constant.ONGLET_BATCH_GENERATE:
            result = isWithArtist() ? 5 : 6;
            break;
        case Constant.ONGLET_BATCH_CHECK:
            result = isWithArtist() ? 6 : 7;
            break;
        default:
            throw new IllegalArgumentException("Given tab doesn't exist: " + tab);
        }
        return result;
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

    public static JTabbedPane getOnglets() {
        return onglets;
    }

    private static void setOnglets(JTabbedPane onglets) {
        OngletPanel.onglets = onglets;
    }

    public static boolean isWithArtist() {
        return withArtist;
    }

    private static void setWithArtist(boolean withArtist) {
        OngletPanel.withArtist = withArtist;
    }

    public static CompletableFuture<Void> getAsyncList() {
        return asyncList;
    }
}
