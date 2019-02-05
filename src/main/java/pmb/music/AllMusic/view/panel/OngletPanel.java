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
import pmb.music.AllMusic.utils.SearchUtils;
import pmb.music.AllMusic.view.BasicFrame;
import pmb.music.AllMusic.view.PanelUtils;

/**
 * Classe gérant les onglet de l'appli.
 * 
 */
public class OngletPanel extends JPanel {
	private static final long serialVersionUID = -7235352581168930316L;
	private static final Logger LOG = Logger.getLogger(OngletPanel.class);
	private JTabbedPane onglets;
	private static Score score;
	private static String[] artistList;
	private static String[] titleList;
	private static String[] authorList;

	/**
	 * Génère les onglets.
	 * 
	 * @param myFrame la fenetre principale
	 * @param withArtist if true the artist panel is displayed
	 */
	public OngletPanel(final BasicFrame myFrame, boolean withArtist) {
		LOG.debug("Start Onglet");
		onglets = new JTabbedPane(SwingConstants.TOP);
		final JPanel panel = new JPanel();
		final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		dim.height = 92 * dim.height / 100;
		dim.width = dim.width - 30;
		onglets.setPreferredSize(dim);

		initScore();
		List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
		setArtistList(importXML);
		setTitleList(importXML);
		setAuthorList(importXML);

		ArtistPanel artist = new ArtistPanel(withArtist);
		FichierPanel fichier = new FichierPanel(artist);
		ImportPanel importFile = new ImportPanel(artist, fichier);
		SearchPanel search = new SearchPanel(artist, fichier);
		BatchPanel batch = new BatchPanel(artist, fichier);

		onglets.addTab(Constant.ONGLET_SEARCH, search);
		if (withArtist) {
			onglets.addTab(Constant.ONGLET_ARTIST, artist);
		}
		onglets.addTab(Constant.ONGLET_FICHIER, fichier);
		onglets.addTab(Constant.ONGLET_IMPORT, importFile);
		onglets.addTab(Constant.ONGLET_BATCH, batch);
		fichier.initPanel();

		onglets.setOpaque(true);
		panel.add(onglets);
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
				PanelUtils.setColumnsWidth(search.getTableResult(), e.getComponent().getWidth(), "Search");
				PanelUtils.setColumnsWidth(fichier.getTableFiles(), e.getComponent().getWidth(), "Fichier files");
				PanelUtils.setColumnsWidth(fichier.getTableCompo(), e.getComponent().getWidth(), "Fichier compo");
				PanelUtils.setColumnsWidth(artist.getTable(), e.getComponent().getWidth(), "Artist");
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
		myFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		myFrame.pack();

		// Default button handling
		search.getRootPane().setDefaultButton(search.getSearch());
		onglets.addChangeListener((ChangeEvent e) -> {
			if (e.getSource() instanceof JTabbedPane) {
				// Modifies default button when changing current tab
				getSelectedDefaultButtonByTab(search, fichier, artist, importFile, batch);
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
		score.setLogMaxAlbum(CompositionUtils.getLogMax(RecordType.ALBUM, albums));
		score.setLogMaxSong(CompositionUtils.getLogMax(RecordType.SONG, songs));
		score.setDoubleMedianAlbum(CompositionUtils.getDoubleMedian(RecordType.ALBUM, albums));
		score.setDoubleMedianSong(CompositionUtils.getDoubleMedian(RecordType.SONG, songs));
		score.setDecileLimitSong(
				CompositionUtils.getDecileLimit(RecordType.SONG, getByType(importXML, RecordType.SONG, false)));
		score.setDecileLimitAlbum(
				CompositionUtils.getDecileLimit(RecordType.ALBUM, getByType(importXML, RecordType.ALBUM, false)));
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
	 * Extracts the artist from a list of compositions, (unique and sorted) and set
	 * to artistList.
	 * 
	 * @param importXML the list
	 */
	private static void setArtistList(List<Composition> importXML) {
		artistList = importXML.parallelStream().map(Composition::getArtist).map(WordUtils::capitalize).distinct()
				.sorted().toArray(String[]::new);
	}

	/**
	 * Extracts the title from a list of compositions, (unique and sorted) and set
	 * to titleList.
	 * 
	 * @param importXML the list
	 */
	private static void setTitleList(List<Composition> importXML) {
		titleList = importXML.parallelStream().map(Composition::getTitre).map(WordUtils::capitalize).distinct().sorted()
				.toArray(String[]::new);
	}

	/**
	 * Extracts the author from a list of compositions, (unique and sorted) and set
	 * to authorList.
	 * 
	 * @param importXML the list
	 */
	private static void setAuthorList(List<Composition> importXML) {
		authorList = importXML.parallelStream().map(Composition::getFiles).flatMap(List::stream).map(Fichier::getAuthor)
				.map(WordUtils::capitalize).distinct().sorted().toArray(String[]::new);
	}

	private String getSelectedDefaultButtonByTab(SearchPanel search, FichierPanel fichier, ArtistPanel artist,
			ImportPanel importP, BatchPanel batch) {
		int index = getOnglets().getSelectedIndex();
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

	public JTabbedPane getOnglets() {
		return onglets;
	}

	public void setOnglets(JTabbedPane onglets) {
		this.onglets = onglets;
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
}
