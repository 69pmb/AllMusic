package pmb.music.AllMusic.view.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.model.Score;
import pmb.music.AllMusic.model.SearchMethod;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MiscUtils;
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
	private static final Logger LOG = Logger.getLogger(OngletPanel.class);
	private static JTabbedPane onglets;
	private static Score score;
	private static String[] artistList;
	private static String[] titleList;
	private static String[] authorList;
	private static FichierPanel fichier;
	private static ArtistPanel artist;
	private static SearchPanel search;

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

		initScore();
		List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
		setArtistList(importXML);
		setTitleList(importXML);
		setAuthorList(importXML);

		setArtist(new ArtistPanel(withArtist));
		setFichier(new FichierPanel());
		ImportPanel importFile = new ImportPanel();
		setSearch(new SearchPanel());
		BatchPanel batch = new BatchPanel();

		getOnglets().addTab(Constant.ONGLET_SEARCH, getSearch());
		if (withArtist) {
			getOnglets().addTab(Constant.ONGLET_ARTIST, getArtist());
		}
		getOnglets().addTab(Constant.ONGLET_FICHIER, getFichier());
		getOnglets().addTab(Constant.ONGLET_IMPORT, importFile);
		getOnglets().addTab(Constant.ONGLET_BATCH, batch);
		getFichier().initPanel();

		getOnglets().setOpaque(true);
		panel.add(getOnglets());
		panel.validate();

		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
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
				PanelUtils.setColumnsWidth(getArtist().getTable(), e.getComponent().getWidth(), "Artist");
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
				getSelectedDefaultButtonByTab(getSearch(), getFichier(), getArtist(), importFile, batch, withArtist);
			}
		});
		LOG.debug("End Onglet");
	}

	/**
	 * Calculates the constants of {@link Score}.
	 * 
	 */
	private static void initScore() {
		LOG.debug("Start initScore");
		OngletPanel.score = new Score();
		List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
		List<Composition> songs = getByType(importXML, RecordType.SONG, true);
		List<Composition> albums = getByType(importXML, RecordType.ALBUM, true);
		getScore().setLogMaxAlbum(CompositionUtils.getLogMax(albums));
		getScore().setLogMaxSong(CompositionUtils.getLogMax(songs));
		getScore().setDoubleMedianAlbum(CompositionUtils.getDoubleMedian(albums));
		getScore().setDoubleMedianSong(CompositionUtils.getDoubleMedian(songs));
		getScore().setDecileLimitSong(CompositionUtils.getDecileLimit(getByType(importXML, RecordType.SONG, false)));
		getScore().setDecileLimitAlbum(CompositionUtils.getDecileLimit(getByType(importXML, RecordType.ALBUM, false)));
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
	private static List<Composition> getByType(List<Composition> importXML, RecordType type, boolean sorted) {
		Map<String, String> criteria = new HashMap<>();
		criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, type.toString());
		if (sorted) {
			criteria.put(SearchUtils.CRITERIA_SORTED, Boolean.TRUE.toString());
		}
		return SearchUtils.search(importXML, criteria, true, SearchMethod.CONTAINS, true, true);
	}

	/**
	 * Extracts the artist from a list of compositions, (unique and sorted) and set to artistList.
	 * 
	 * @param importXML the list
	 */
	private static void setArtistList(List<Composition> importXML) {
		OngletPanel.artistList = MiscUtils.distinctSortToArray(MiscUtils.projectAndCapitalize(importXML,
				Composition::getArtist, artist -> StringUtils.startsWithIgnoreCase(artist, "the") ? StringUtils.substringAfter(artist, "the") : artist));
	}

	/**
	 * Extracts the title from a list of compositions, (unique and sorted) and set to titleList.
	 * 
	 * @param importXML the list
	 */
	private static void setTitleList(List<Composition> importXML) {
		OngletPanel.titleList = MiscUtils.distinctSortToArray(MiscUtils.projectAndCapitalize(importXML, Composition::getTitre, null));
	}

	/**
	 * Extracts the author from a list of compositions, (unique and sorted) and set to authorList.
	 * 
	 * @param importXML the list
	 */
	private static void setAuthorList(List<Composition> importXML) {
		OngletPanel.authorList = MiscUtils.distinctSortToArray(() -> importXML.parallelStream().map(Composition::getFiles).flatMap(List::stream)
				.map(Fichier::getAuthor).map(WordUtils::capitalize));
	}

	private static String getSelectedDefaultButtonByTab(SearchPanel search, FichierPanel fichier, ArtistPanel artist,
			ImportPanel importP, BatchPanel batch, boolean withArtist) {
		int index = getOnglets().getSelectedIndex();
		index = !withArtist && index != 0 ? index + 1 : index;
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
			batch.getRootPane().setDefaultButton(batch.getBatchFileBtn());
			tab = Constant.ONGLET_BATCH;
			break;
		default:
			break;
		}
		LOG.debug(tab);
		return tab;
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

	public static void setFichier(FichierPanel fichier) {
		OngletPanel.fichier = fichier;
	}

	public static void setArtist(ArtistPanel artist) {
		OngletPanel.artist = artist;
	}

	public static SearchPanel getSearch() {
		return search;
	}

	public static void setSearch(SearchPanel search) {
		OngletPanel.search = search;
	}

	public static JTabbedPane getOnglets() {
		return onglets;
	}

	public static void setOnglets(JTabbedPane onglets) {
		OngletPanel.onglets = onglets;
	}
}
