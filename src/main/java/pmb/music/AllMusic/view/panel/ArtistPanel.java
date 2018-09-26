/**
 * 
 */
package pmb.music.AllMusic.view.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.file.CsvFile;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.SearchMethod;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.SearchUtils;
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.component.JComboCheckBox;
import pmb.music.AllMusic.view.component.MyInputText;
import pmb.music.AllMusic.view.dialog.DialogFileTable;
import pmb.music.AllMusic.view.model.ArtistModel;

/**
 * L'onglet Artiste, classement des artistes les plus cités.
 * 
 */
public class ArtistPanel extends JPanel {

	private static final long serialVersionUID = 2593372709628283573L;

	private static final Logger LOG = Logger.getLogger(ArtistPanel.class);
	private static final int INDEX_ARTIST = 0;
	private static final int INDEX_NB_TOTAL = 1;

	private final MyInputText publi;
	private final MyInputText rangeB;
	private final MyInputText rangeE;
	private final MyInputText auteur;
	private final JComboCheckBox cat;
	private final JButton search;
	private final JButton reset;
	private Integer sortedColumn;
	private SortOrder sortOrder;
	private JCheckBox deleted;

	private final JTable table;

	private final ArtistModel model;

	private Map<String, List<Composition>> data;
	private Map<String, List<Composition>> searchResult;

	private static final String[] title = { "Artiste", "Nombre d'occurrences", "Album", "Chanson" };

	private int selectedRow = -1;

	private transient Thread updateArtistThread;
	private boolean withArtist;

	/**
	 * Génère le panel artiste.
	 * 
	 * @param withArtist if true the artist panel is displayed and the data is
	 *            calculated
	 */
	@SuppressWarnings("unchecked")
	public ArtistPanel(boolean withArtist) {
		super();
		LOG.debug("Start ArtistPanel");
		this.withArtist = withArtist;
		this.setLayout(new BorderLayout());

		JPanel header = new JPanel();
		// Publi
		JPanel publiPanel = new JPanel();
		publiPanel.setPreferredSize(new Dimension(150, PanelUtils.PANEL_HEIGHT));
		JLabel publiLabel = new JLabel("Année de publication : ");
		publi = new MyInputText(JTextField.class, 75);
		publiPanel.add(publiLabel);
		publiPanel.add(publi);
		header.add(publiPanel);
		// Range
		JPanel rangePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		PanelUtils.setSize(rangePanel, 300, PanelUtils.PANEL_HEIGHT);
		JLabel rangeLabel = new JLabel("Année(s) du classement : ");
		PanelUtils.setSize(rangeLabel, 180, PanelUtils.COMPONENT_HEIGHT);
		rangeB = new MyInputText(JTextField.class, 90);
		rangeE = new MyInputText(JTextField.class, 90);
		rangePanel.add(rangeLabel);
		rangePanel.add(rangeB);
		rangePanel.add(rangeE);
		header.add(rangePanel);
		// Auteur
		JPanel fileNamePanel = new JPanel();
		fileNamePanel.setPreferredSize(new Dimension(200, PanelUtils.PANEL_HEIGHT));
		JLabel fileNameLabel = new JLabel("Auteur : ");
		auteur = new MyInputText(JTextField.class, 150);
		fileNamePanel.add(fileNameLabel);
		fileNamePanel.add(auteur);
		header.add(fileNamePanel);
		// Categorie
		JPanel catPanel = new JPanel();
		catPanel.setPreferredSize(new Dimension(180, PanelUtils.PANEL_HEIGHT));
		JLabel catLabel = new JLabel("Catégorie : ");
		cat = new JComboCheckBox(
				Arrays.asList(Cat.values()).stream().map(c -> c.getCat()).collect(Collectors.toList()));
		cat.setPreferredSize(new Dimension(120, PanelUtils.COMPONENT_HEIGHT));
		catPanel.add(catLabel);
		catPanel.add(cat);
		header.add(catPanel);
		// Deleted
		JPanel deletedPanel = new JPanel();
		deletedPanel.setPreferredSize(new Dimension(90, PanelUtils.PANEL_HEIGHT));
		JLabel deletedLabel = new JLabel("Supprimés: ");
		deleted = new JCheckBox();
		deleted.setPreferredSize(new Dimension(50, PanelUtils.COMPONENT_HEIGHT));
		deleted.setHorizontalAlignment(SwingConstants.CENTER);
		deletedPanel.add(deletedLabel);
		deletedPanel.add(deleted);
		header.add(deletedPanel);
		// SEARCH
		search = PanelUtils.createJButton("Rechercher", 150, Constant.ICON_SEARCH);
		search.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				searchAction();
			}
		});
		header.add(search);
		// RESET
		reset = PanelUtils.createJButton("Réinitialiser", 150, Constant.ICON_ERASE);
		reset.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				resetAction();
			}
		});
		header.add(reset);

		// ----- DEBUT TABLE ----------
		table = new JTable();
		table.setAutoCreateRowSorter(true);
		table.setRowHeight(30);
		table.setFillsViewportHeight(true);
		table.getTableHeader().setResizingAllowed(true);
		table.setBackground(UIManager.getColor("Label.background"));
		table.setFont(UIManager.getFont("Label.font"));
		table.setBorder(UIManager.getBorder("Label.border"));
		model = new ArtistModel(new Object[0][4], title);
		table.setModel(model);
		table.setRowSorter(new TableRowSorter<TableModel>(model));
		table.getRowSorter().addRowSorterListener(new RowSorterListener() {
			@Override
			public void sorterChanged(RowSorterEvent e) {
				if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
					List<SortKey> sortKeys = e.getSource().getSortKeys();
					if (!sortKeys.isEmpty()) {
						sortedColumn = ((SortKey) sortKeys.get(0)).getColumn();
						sortOrder = ((SortKey) sortKeys.get(0)).getSortOrder();
					}
				}
			}
		});
		PanelUtils.colRenderer(table, false, null);

		updateArtistPanel();

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				mouseClickAction(e);
			}
		});
		table.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// Nothing to do
			}

			@Override
			public void keyReleased(KeyEvent e) {
				selectedRow = PanelUtils.keyShortcutAction(e, selectedRow, sortedColumn);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// Nothing to do
			}
		});

		this.add(new JScrollPane(table), BorderLayout.CENTER);
		// ----- FIN TABLE ----------

		// CSV
		JButton csv = PanelUtils.createJButton("Télécharger la recherche en CSV", 220, Constant.ICON_DOWNLOAD);
		csv.addActionListener((ActionEvent e) -> {
			LOG.debug("Start Csv");
			List<String> c = Arrays
					.asList(publi.getText(), rangeB.getText(), rangeE.getText(), auteur.getText(),
							cat.getSelectedItems())
					.stream().filter(s -> !"".equals(s)).collect(Collectors.toList());
			String criteres = StringUtils.join(c, " ");
			String[] csvHeader = { "Artiste", "Nombre d'occurences totales", "Albums", "Chansons",
					"Critères: " + criteres };
			String name = CsvFile.exportCsv("artist", MiscUtils.convertVectorToList(model.getDataVector()),
					table.getRowSorter().getSortKeys().get(0), csvHeader);
			try {
				Runtime.getRuntime().exec(Constant.getExcelPath() + name);
			} catch (IOException e1) {
				LOG.error("Impossible d'ouvrir excel: " + Constant.getExcelPath(), e1);
			}
			LOG.debug("End Csv");
		});
		header.add(csv);
		this.add(header, BorderLayout.PAGE_START);

		LOG.debug("End ArtistPanel");
	}

	/**
	 * Met à jour le panel artiste à l'aide du fichier final.
	 */
	public void updateArtistPanel() {
		LOG.debug("Start updateArtistPanel");
		if (!withArtist) {
			return;
		}
		if (updateArtistThread == null || !updateArtistThread.isAlive()) {
			startUpdateArtistThread();
		} else {
			interruptUpdateArtist();
			try {
				updateArtistThread.join();
			} catch (InterruptedException e) {
				LOG.error("", e);
				Thread.currentThread().interrupt();
			}
			startUpdateArtistThread();
		}
		LOG.debug("End updateArtistPanel");
	}

	public void interruptUpdateArtist() {
		if (!withArtist) {
			return;
		}
		updateArtistThread.interrupt();
	}

	private void startUpdateArtistThread() {
		updateArtistThread = new Thread(() -> {
			LOG.debug("Start ThreadUpdateArtist");
			data = CompositionUtils.groupCompositionByArtist(ImportXML.importXML(Constant.getFinalFilePath()));

			SwingUtilities.invokeLater(() -> {
				resetAction();
				searchResult = new HashMap<>();
				for (Map.Entry<String, List<Composition>> entry : data.entrySet()) {
					for (Composition c : entry.getValue()) {
						if (deleted.isSelected() || !c.isDeleted()) {
							Composition newCompo = new Composition(c);
							newCompo.setFiles(c.getFiles());
							if (!searchResult.containsKey(entry.getKey())) {
								searchResult.put(entry.getKey(), new ArrayList<>(Arrays.asList(newCompo)));
							} else {
								searchResult.get(entry.getKey()).add(newCompo);
							}
						}
					}
				}
				updateTable(searchResult);
			});
			LOG.debug("End ThreadUpdateArtist");
		});
		updateArtistThread.setDaemon(true);
		updateArtistThread.setPriority(Thread.MIN_PRIORITY);
		updateArtistThread.start();
	}

	private void updateTable(Map<String, List<Composition>> donnee) {
		model.setRowCount(0);
		model.setDataVector(CompositionUtils.convertArtistPanelResultToVector(donnee),
				new Vector<>(Arrays.asList(title)));
		PanelUtils.colRenderer(table, true, null);
		model.fireTableDataChanged();
		if (sortedColumn == null) {
			sortedColumn = INDEX_NB_TOTAL;
			sortOrder = SortOrder.DESCENDING;
		}
		table.getRowSorter().setSortKeys(Collections.singletonList(new RowSorter.SortKey(sortedColumn, sortOrder)));
		table.repaint();
		selectedRow = -1;
	}

	private void mouseClickAction(MouseEvent e) {
		Optional<Vector<String>> selectedRow = PanelUtils.getSelectedRow(e);
		if (!selectedRow.isPresent()) {
			return;
		}
		LOG.debug(selectedRow);
		if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			LOG.debug("Start artist mouse");
			// Affiche tous les fichiers de l'artiste double cliqué
			Optional<String> key = CompositionUtils.findArtistKey(searchResult, new JaroWinklerDistance(),
					selectedRow.get().get(INDEX_ARTIST));
			if (!key.isPresent()) {
				LOG.error("Error when searching: " + selectedRow.get().get(INDEX_ARTIST) + " in data table");
			} else {
				DialogFileTable pop = new DialogFileTable(null, "Fichier", true, searchResult.get(key.get()),
						new Dimension(1500, 600), DialogFileTable.INDEX_TITLE);
				pop.showDialogFileTable();
			}
			LOG.debug("End artist mouse");
		} else if (SwingUtilities.isRightMouseButton(e)) {
			LOG.debug("Start artist right mouse");
			// Copie dans le presse papier le nom de l'artiste
			StringSelection selection = new StringSelection(selectedRow.get().get(INDEX_ARTIST));
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
			LOG.debug("End artist right mouse");
		}
	}

	private void searchAction() {
		LOG.debug("Start search");
		if (data != null && !data.isEmpty()) {
			searchResult = new HashMap<>();
			for (Map.Entry<String, List<Composition>> entry : data.entrySet()) {
				for (Composition c : entry.getValue()) {
					if (!deleted.isSelected() && c.isDeleted()) {
						continue;
					}
					List<Fichier> files = c.getFiles().stream()
							.filter(f -> SearchUtils.filterFichier(SearchMethod.WHOLE_WORD, new JaroWinklerDistance(),
									publi.getText(), null, auteur.getText(),
									cat.getSelectedItems(),
									rangeB.getText(), rangeE.getText(), null, null, f))
							.collect(Collectors.toList());
					if (!files.isEmpty()) {
						Composition newCompo = new Composition(c);
						newCompo.setFiles(files);
						if (!searchResult.containsKey(entry.getKey())) {
							searchResult.put(entry.getKey(), new ArrayList<>(Arrays.asList(newCompo)));
						} else {
							searchResult.get(entry.getKey()).add(newCompo);
						}
					}
				}
			}
			updateTable(searchResult);
		}
		LOG.debug("End search");
	}

	private void resetAction() {
		LOG.debug("Start resetAction");
		rangeB.setText("");
		rangeE.setText("");
		auteur.setText("");
		publi.setText("");
		cat.clearSelection();
		deleted.setSelected(false);
		LOG.debug("End resetAction");
	}

	public JTable getTable() {
		return table;
	}

}
