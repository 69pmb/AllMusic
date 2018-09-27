/**
 * 
 */
package pmb.music.AllMusic.view.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.apache.commons.text.WordUtils;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.model.Score;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
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
		initStats();

		ArtistPanel artist = new ArtistPanel(withArtist);
		ImportPanel importFile = new ImportPanel(artist);
		List<String> authorList = getAuthorList();
		SearchPanel search = new SearchPanel(artist, getArtistList(), getTitleList(), authorList);
		FichierPanel fichier = new FichierPanel();
		BatchPanel batch = new BatchPanel();

		onglets.addTab(Constant.ONGLET_SEARCH, search);
		if (withArtist) {
			onglets.addTab(Constant.ONGLET_ARTIST, artist);
		}
		onglets.addTab(Constant.ONGLET_FICHIER, fichier);
		onglets.addTab(Constant.ONGLET_IMPORT, importFile);
		onglets.addTab(Constant.ONGLET_BATCH, batch);
		fichier.initPanel(artist, authorList);

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
		search.getRootPane().setDefaultButton(search.getSearch());
		LOG.debug("End Onglet");
	}

	/**
	 * Calculates the constants of {@link Score}.
	 * 
	 */
	public void initStats() {
		Score stats = new Score();
		stats.setLogMaxAlbum(CompositionUtils.getLogMax(RecordType.ALBUM));
		stats.setLogMaxSong(CompositionUtils.getLogMax(RecordType.SONG));
		stats.setDoubleMedianAlbum(CompositionUtils.getDoubleMedian(RecordType.ALBUM));
		stats.setDoubleMedianSong(CompositionUtils.getDoubleMedian(RecordType.SONG));
		OngletPanel.score = stats;
	}

	public static List<String> getArtistList() {
		return ImportXML.importXML(Constant.getFinalFilePath()).parallelStream().map(Composition::getArtist)
				.map(WordUtils::capitalize).distinct().sorted().collect(Collectors.toList());
	}

	public static List<String> getTitleList() {
		return ImportXML.importXML(Constant.getFinalFilePath()).parallelStream().map(Composition::getTitre)
				.map(WordUtils::capitalize).distinct().sorted().collect(Collectors.toList());
	}

	public static List<String> getAuthorList() {
		return ImportXML.importXML(Constant.getFinalFilePath()).parallelStream().map(Composition::getFiles)
				.flatMap(List::stream).map(Fichier::getAuthor).map(WordUtils::capitalize).distinct().sorted()
				.collect(Collectors.toList());
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

	public JTabbedPane getOnglets() {
		return onglets;
	}

	public void setOnglets(JTabbedPane onglets) {
		this.onglets = onglets;
	}

	public static Score getScore() {
		return score;
	}
}
