package pmb.music.AllMusic.view.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	private static int index;
	private static Score score;
	private static List<String> artistList;
	private static List<String> titleList;
	private static List<String> authorList;

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
			}

			@Override
			public void componentHidden(ComponentEvent e) {
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
	private void initScore() {
		LOG.debug("Start initScore");
		OngletPanel.score = new Score();
		List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
		Map<String, String> criteria = new HashMap<>();
		criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, RecordType.SONG.toString());
		criteria.put(SearchUtils.CRITERIA_SORTED, Boolean.TRUE.toString());
		List<Composition> songs = SearchUtils.search(importXML, criteria, true, SearchMethod.CONTAINS, true, true);
		criteria = new HashMap<>();
		criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, RecordType.ALBUM.toString());
		criteria.put(SearchUtils.CRITERIA_SORTED, Boolean.TRUE.toString());
		List<Composition> albums = SearchUtils.search(importXML, criteria, true, SearchMethod.CONTAINS, true, true);
		score.setLogMaxAlbum(CompositionUtils.getLogMax(RecordType.ALBUM, albums));
		score.setLogMaxSong(CompositionUtils.getLogMax(RecordType.SONG, songs));
		score.setDoubleMedianAlbum(CompositionUtils.getDoubleMedian(RecordType.ALBUM, albums));
		score.setDoubleMedianSong(CompositionUtils.getDoubleMedian(RecordType.SONG, songs));
		LOG.debug("End initScore");
	}

	/**
	 * Extracts the artist from a list of compositions, (unique and sorted) and set
	 * to artistList.
	 * 
	 * @param importXML the list
	 */
	private static void setArtistList(List<Composition> importXML) {
		artistList = importXML.parallelStream().map(Composition::getArtist).map(WordUtils::capitalize).distinct()
				.sorted().collect(Collectors.toList());
	}

	/**
	 * Extracts the title from a list of compositions, (unique and sorted) and set
	 * to titleList.
	 * 
	 * @param importXML the list
	 */
	private static void setTitleList(List<Composition> importXML) {
		titleList = importXML.parallelStream().map(Composition::getTitre).map(WordUtils::capitalize).distinct().sorted()
				.collect(Collectors.toList());
	}

	/**
	 * Extracts the author from a list of compositions, (unique and sorted) and set
	 * to authorList.
	 * 
	 * @param importXML the list
	 */
	private static void setAuthorList(List<Composition> importXML) {
		authorList = importXML.parallelStream().map(Composition::getFiles).flatMap(List::stream).map(Fichier::getAuthor)
				.map(WordUtils::capitalize).distinct().sorted().collect(Collectors.toList());
	}

	/**
	 * Rafraichi un onglet.
	 * 
	 * @param title le titre de l'onglet à rafraichir
	 * @param onglets le conteneur de tous les onglets
	 * @param ongletToRedraw l'onglet à rafraichir
	 */
	public static void redrawTab(String title, JTabbedPane onglets, JPanel ongletToRedraw) {
		if (!isTabAlreadyExists(onglets, title)) {
			onglets.addTab(title, ongletToRedraw);
		} else {
			onglets.removeTabAt(index);
			onglets.insertTab(title, null, ongletToRedraw, null, index);
		}
	}

	private static boolean isTabAlreadyExists(JTabbedPane onglets, String title) {
		boolean isTabExists = false;
		for (int i = 0; i < onglets.getTabCount(); i++) {
			if (title.equals(onglets.getTitleAt(i))) {
				isTabExists = true;
				index = i;
				break;
			}
		}
		return isTabExists;
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

	public static List<String> getArtistList() {
		return artistList;
	}

	public static List<String> getTitleList() {
		return titleList;
	}

	public static List<String> getAuthorList() {
		return authorList;
	}
}
