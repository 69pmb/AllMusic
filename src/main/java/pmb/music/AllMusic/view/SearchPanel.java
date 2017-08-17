package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.file.CsvFile;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.utils.SearchUtils;

/**
 * Gère le panel search.
 */
public class SearchPanel extends JPanel {

	private static final Logger LOG = Logger.getLogger(SearchPanel.class);

	private static final long serialVersionUID = 2593372709628283573L;
	private final JLabel countLabel;
	private final JLabel deleteLabel;

	private JCheckBox inFiles;

	private JButton search;

	private final JTextField publi;
	private final JTextField rangeB;
	private final JTextField rangeE;
	private final JTextField fileName;

	private final JTable result;

	private final JComboBox<Cat> cat;
	private final JComboBox<RecordType> type;
	private final JComboBox<String> titre;
	private final JComboBox<String> artist;
	private final JComboBox<String> author;

	private List<Composition> compoResult = new ArrayList<>();

	private static final String[] title = { "Artiste", "Titre", "Type", "Nombre de fichiers", "" };

	private int selectedRow = -1;

	private final CompoModel model;

	/**
	 * Génère le panel search
	 * @param artist2 le panel artiste
	 * @param artistList
	 * @param titleList
	 * @param authorList
	 */
	public SearchPanel(final ArtistPanel artist2, List<String> artistList, List<String> titleList, List<String> authorList) {
		super();
		LOG.debug("Start SearchPanel");
		this.setLayout(new GridLayout(2, 1));

		JPanel header = new JPanel();
		header.setLayout(new GridLayout(2, 1));

		insertTopPanel(artist2, header);

		JPanel firstLine = new JPanel();

		// Artiste
		JPanel artistPanel = new JPanel();
		artistPanel.setPreferredSize(new Dimension(200, 60));
		JLabel artistLabel = new JLabel("Artiste : ");
		artist = new JComboBox<>();
		AutoCompleteSupport.install(artist, GlazedLists.eventListOf(artistList.toArray()));
		artist.setPreferredSize(new Dimension(150, 30));
		artistPanel.add(artistLabel);
		artistPanel.add(artist);
		firstLine.add(artistPanel);

		// Titre
		JPanel titrePanel = new JPanel();
		titrePanel.setPreferredSize(new Dimension(180, 60));
		JLabel titreLabel = new JLabel("Titre : ");
		titre = new JComboBox<>();
		AutoCompleteSupport.install(titre, GlazedLists.eventListOf(titleList.toArray()));
		titre.setPreferredSize(new Dimension(150, 30));
		titrePanel.add(titreLabel);
		titrePanel.add(titre);
		firstLine.add(titrePanel);

		// Nom du fichier
		JPanel fileNamePanel = new JPanel();
		fileNamePanel.setPreferredSize(new Dimension(400, 60));
		JLabel fileNameLabel = new JLabel("Nom du fichier : ");
		fileName = new JTextField();
		fileName.setPreferredSize(new Dimension(350, 25));
		fileNamePanel.add(fileNameLabel);
		fileNamePanel.add(fileName);
		firstLine.add(fileNamePanel);

		// Auteur
		JPanel authorPanel = new JPanel();
		authorPanel.setPreferredSize(new Dimension(200, 60));
		JLabel authorLabel = new JLabel("Auteur : ");
		author = new JComboBox<>();
		AutoCompleteSupport.install(author, GlazedLists.eventListOf(authorList.toArray()));
		author.setPreferredSize(new Dimension(150, 25));
		authorPanel.add(authorLabel);
		authorPanel.add(author);
		firstLine.add(authorPanel);

		// Type
		JPanel typePanel = new JPanel();
		typePanel.setPreferredSize(new Dimension(180, 60));
		JLabel typeLabel = new JLabel("Type : ");
		type = new JComboBox<>();
		type.addItem(null);
		RecordType[] valuesType = RecordType.values();
		for (int i = 0; i < valuesType.length; i++) {
			type.addItem(valuesType[i]);
		}
		type.setPreferredSize(new Dimension(150, 25));
		typePanel.add(typeLabel);
		typePanel.add(type);
		firstLine.add(typePanel);

		// Range
		JPanel rangePanel = new JPanel();
		rangePanel.setPreferredSize(new Dimension(310, 60));
		JLabel rangeLabel = new JLabel("Année(s) du classement :                ");
		rangeB = new JTextField();
		rangeE = new JTextField();
		rangeB.setPreferredSize(new Dimension(150, 25));
		rangeE.setPreferredSize(new Dimension(150, 25));
		rangePanel.add(rangeLabel);
		rangePanel.add(rangeB);
		rangePanel.add(rangeE);
		firstLine.add(rangePanel);

		// Categorie
		JPanel catPanel = new JPanel();
		catPanel.setPreferredSize(new Dimension(200, 60));
		JLabel catLabel = new JLabel("Catégorie : ");
		cat = new JComboBox<>();
		cat.addItem(null);
		Cat[] values = Cat.values();
		for (int i = 0; i < values.length; i++) {
			cat.addItem(values[i]);
		}
		cat.setPreferredSize(new Dimension(150, 25));
		catPanel.add(catLabel);
		catPanel.add(cat);
		firstLine.add(catPanel);

		// Publi
		JPanel publiPanel = new JPanel();
		publiPanel.setPreferredSize(new Dimension(200, 60));
		JLabel publiLabel = new JLabel("Année de publication : ");
		publi = new JTextField();
		publi.setPreferredSize(new Dimension(150, 25));
		publiPanel.add(publiLabel);
		publiPanel.add(publi);
		firstLine.add(publiPanel);

		// Nombre de résultat
		JPanel countPanel = new JPanel();
		countPanel.setPreferredSize(new Dimension(200, 60));
		countLabel = new JLabel("");
		countLabel.setForeground(new Color(8, 187, 81));
		Font labelFont = countLabel.getFont();
		countLabel.setFont(new Font(labelFont.getName(), labelFont.getStyle(), 30));
		countPanel.add(countLabel);
		firstLine.add(countPanel);

		// Nombre de suppression
		JPanel deletePanel = new JPanel();
		deletePanel.setPreferredSize(new Dimension(400, 60));
		deleteLabel = new JLabel("");
		deleteLabel.setForeground(new Color(8, 187, 81));
		Font labelFont2 = deleteLabel.getFont();
		deleteLabel.setFont(new Font(labelFont2.getName(), labelFont2.getStyle(), 30));
		deletePanel.add(deleteLabel);
		firstLine.add(deletePanel);

		header.add(firstLine);
		this.add(header);

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());

		// result
		result = new JTable();
		result.setAutoCreateRowSorter(true);
		result.setRowHeight(30);
		result.setFillsViewportHeight(true);
		result.setBackground(UIManager.getColor("Label.background"));
		result.setFont(UIManager.getFont("Label.font"));
		result.setBorder(UIManager.getBorder("Label.border"));
		model = new CompoModel(new Object[0][5], title);
		result.setModel(model);
		result.setRowSorter(new TableRowSorter<TableModel>(model));
		result.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// Nothing to do
			}

			@Override
			public void keyReleased(KeyEvent e) {
				selectedRow = shortcutKeyAction(e);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// Nothing to do
			}

		});
		result.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				mouseAction(e);
			}
		});
		bottom.add(new JScrollPane(result), BorderLayout.CENTER);

		this.add(bottom);
		LOG.debug("End SearchPanel");
	}

	/**
	 * Insert les boutons du panel search en haut.
	 * @param artist2 le panel artist
	 * @param header le header de l'onglet
	 */
	private void insertTopPanel(final ArtistPanel artist2, JPanel header) {
		JPanel top = new JPanel();
		AbstractAction searchAction = new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				searchAction();
			}
		};

		search = new JButton("Chercher");
		search.setBackground(Color.white);
		search.setPreferredSize(new Dimension(220, 60));
		search.addActionListener(searchAction);
		search.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0),
				"Enter_pressed");
		search.getActionMap().put("Enter_pressed", searchAction);
		top.add(search);

		// inFiles
		JPanel inFilesPanel = new JPanel();
		inFilesPanel.setPreferredSize(new Dimension(180, 60));
		JLabel inFilesLabel = new JLabel("Rechercher dans les fichiers : ");
		inFiles = new JCheckBox();
		inFiles.setPreferredSize(new Dimension(25, 25));
		inFiles.setSelected(true);
		inFilesPanel.add(inFilesLabel);
		inFilesPanel.add(inFiles);
		top.add(inFilesPanel);

		// Clear Btn
		JButton clear = new JButton("Réinitialiser recherche");
		clear.setBackground(Color.white);
		clear.setPreferredSize(new Dimension(200, 60));
		AbstractAction cleanAction = new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				cleanAction();
			}
		};
		clear.addActionListener(cleanAction);
		top.add(clear);

		// Delete Btn
		JButton delete = new JButton("Supprimer les compositions sélectionnées");
		delete.setBackground(Color.white);
		delete.setPreferredSize(new Dimension(300, 60));
		delete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				deleteAction(artist2);
			}
		});
		top.add(delete);
		
		// Modif Btn
		JButton modif = new JButton("Modifier la composition sélectionnée");
		modif.setBackground(Color.white);
		modif.setPreferredSize(new Dimension(300, 60));
		modif.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				modifAction(artist2);
			}
		});
		top.add(modif);

		// CSV
		JButton csv = new JButton("Télécharger le résultat de la recherche en CSV");
		csv.setBackground(Color.white);
		csv.setPreferredSize(new Dimension(300, 60));
		csv.addActionListener(new ActionListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {
				List<String> c = Arrays
						.asList(publi.getText(), rangeB.getText(), rangeE.getText(), fileName.getText(),
								cat.getSelectedItem() == null ? "" : cat.getSelectedItem().toString(),
								type.getSelectedItem() == null ? "" : type.getSelectedItem().toString(),
								titre.getSelectedItem() == null ? "" : titre.getSelectedItem().toString(),
								artist.getSelectedItem() == null ? "" : artist.getSelectedItem().toString(),
								author.getSelectedItem() == null ? "" : author.getSelectedItem().toString())
						.stream().filter(s -> !"".equals(s)).collect(Collectors.toList());
				String criteres = StringUtils.join(c, " ");
				String name = CsvFile.writeCsvFromSearchResult(model.getDataVector(), "search", criteres);
				try {
					Runtime.getRuntime().exec(Constant.EXCEL_PATH + name);
				} catch (IOException e1) {
					LOG.error("Impossible d'ouvrir excel: " + Constant.EXCEL_PATH, e1);
				}
			}

		});
		top.add(csv);
		header.add(top);
	}

	private void searchAction() {
		LOG.debug("Start search");
		deleteLabel.setText("");
		List<Composition> allCompo = ImportXML.importXML(Constant.FINAL_FILE_PATH);
		if (CollectionUtils.isNotEmpty(allCompo)) {
			Map<String, String> criteria = new HashMap<>();
			if (artist.getSelectedItem() != null) {
				criteria.put("artist", artist.getSelectedItem().toString());
			}
			if (titre.getSelectedItem() != null) {
				criteria.put("titre", titre.getSelectedItem().toString());
			}
			if (type.getSelectedItem() != null) {
				criteria.put("type", type.getSelectedItem().toString());
			}
			criteria.put("publish", publi.getText());
			criteria.put("fileName", fileName.getText());
			if (author.getSelectedItem() != null) {
				criteria.put("auteur", author.getSelectedItem().toString());
			}
			if (cat.getSelectedItem() != null) {
				criteria.put("cat", cat.getSelectedItem().toString());
			}
			criteria.put("dateB", rangeB.getText());
			criteria.put("dateE", rangeE.getText());

			compoResult = new ArrayList<>();
			compoResult.addAll(SearchUtils.searchJaro(allCompo, criteria, inFiles.isSelected()));
			updateTable();
		}
		LOG.debug("End search");
	}

	private void colRenderer() {
		TableColumnModel modelecolonne = result.getColumnModel();
		int total = modelecolonne.getColumnCount();
		for (int i = 0; i < total; i++) {
			int taille = 0;
			int total2 = result.getRowCount();
			for (int j = 0; j < total2; j++) {
				int taille2 = result.getValueAt(j, i).toString().length() * 7; // determination
				// arbitraire
				if (taille2 > taille) {
					taille = taille2;
				}
			}
			modelecolonne.getColumn(i).setPreferredWidth(taille + 50);
		}

		DefaultTableCellRenderer renderer = new EvenOddRenderer();
		for (int i = 0; i < result.getColumnCount() - 1; i++) {
			renderer.setHorizontalAlignment(JLabel.CENTER);
			result.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}
	}

	private void updateTable() {
		LOG.debug("Start updateTable");
		model.setRowCount(0);
		model.setDataVector(CompositionUtils.convertCompositionListToVector(compoResult), new Vector<>(Arrays.asList(title)));
		colRenderer();
		countLabel.setText(compoResult.size() + " résultats");
		model.fireTableDataChanged();
		result.getRowSorter().toggleSortOrder(3);
		result.getRowSorter().toggleSortOrder(3);
		result.repaint();
		selectedRow = -1;
		LOG.debug("Start updateTable");
	}

	private void cleanAction() {
		LOG.debug("Start cleanAction");
		artist.setSelectedItem(null);
		titre.setSelectedItem(null);
		type.setSelectedItem(null);
		publi.setText("");
		fileName.setText("");
		author.setSelectedItem(null);
		cat.setSelectedItem(null);
		rangeB.setText("");
		rangeE.setText("");
		deleteLabel.setText("");
		countLabel.setText("");
		LOG.debug("End cleanAction");
	}

	@SuppressWarnings("unchecked")
	private void deleteAction(final ArtistPanel artist2) {
		LOG.debug("Start delete");
		List<Object> selected = model.getSelected();
		deleteLabel.setText(selected.size() + " élément(s) supprimée(s)");
		List<Composition> importXML = ImportXML.importXML(Constant.FINAL_FILE_PATH);
		for (Object o : selected) {
			Vector<String> v = (Vector<String>) o;
			try {
				Composition toRemove = CompositionUtils.findByArtistTitreAndType(importXML, v.get(0), v.get(1), v.get(2));
				compoResult.remove(compoResult.indexOf(toRemove));
				importXML.remove(importXML.indexOf(toRemove));
				CompositionUtils.removeCompositionsInFiles(toRemove);
			} catch (MyException e1) {
				LOG.error("Erreur lors de la suppression d'une composition", e1);
			}
		}
		try {
			ExportXML.exportXML(importXML, "final");
			artist2.updateArtistPanel();
		} catch (IOException e1) {
			LOG.error("Erreur lors de l'export du fichier final", e1);
			deleteLabel.setText("Erreur lors de l'export du fichier final !!" + e1);
		}
		updateTable();
		LOG.debug("End delete");
	}
	
	@SuppressWarnings("unchecked")
	private void modifAction(final ArtistPanel artist2) {
		LOG.debug("Start modif");
		deleteLabel.setText("Élément modifié");
		Object selected;
		Composition toModif;
		Vector<String> v;
		List<Composition> importXML;
		if (model.getSelected().size() > 1) {
			return;
		} else {
			selected = model.getSelected().get(0);
			v = (Vector<String>) selected;
			importXML = ImportXML.importXML(Constant.FINAL_FILE_PATH);
			try {
				toModif = CompositionUtils.findByArtistTitreAndType(importXML, v.get(0), v.get(1), v.get(2));
			} catch (MyException e1) {
				String log = "Erreur lors de la modifiation d'une composition";
				LOG.error(log, e1);
				deleteLabel.setText(log + e1);
				return;
			}
		}
		ModifyDialog md = new ModifyDialog(null, "Modifier une composition", true, new Dimension(600, 150), v);
		md.showDialogFileTable();
		if (md.isSendData()) {
			v = md.getCompo();
		} else {
			return;
		}
		int indexOfXml = importXML.indexOf(toModif);
		int indexOfResult = compoResult.indexOf(toModif);
		CompositionUtils.modifyCompositionsInFiles(toModif, v);
		toModif.setArtist(v.get(0));
		toModif.setTitre(v.get(1));
		
		importXML.remove(indexOfXml);
		compoResult.remove(indexOfResult);
		Composition compoExist = CompositionUtils.compoExist(importXML, toModif);
		if (compoExist == null) {
			importXML.add(toModif);
			compoResult.add(toModif);
		} else {
			compoExist.getFiles().addAll(toModif.getFiles());
			
			Composition compoExistResult = CompositionUtils.compoExist(compoResult, toModif);
			compoExistResult.getFiles().addAll(toModif.getFiles());
		}
		try {
			ExportXML.exportXML(importXML, "final");
			artist2.updateArtistPanel();
		} catch (IOException e1) {
			LOG.error("Erreur lors de l'export du fichier final", e1);
			deleteLabel.setText("Erreur lors de l'export du fichier final !!");
		}
		updateTable();
		LOG.debug("End modif");
	}

	@SuppressWarnings("unchecked")
	private int shortcutKeyAction(KeyEvent e) {
		LOG.debug("Start shortcutKeyAction");
		JTable target = (JTable) e.getSource();
		String keyChar = String.valueOf(e.getKeyChar());
		TableModel modelTable = target.getModel();
		int startRow = selectedRow;
		if (selectedRow == modelTable.getRowCount() - 1) {
			startRow = -1;// Go before start
		}
		// Check each cell to see if it starts with typed char.
		// if so set corresponding row selected and return.
		for (int row = startRow + 1; row < modelTable.getRowCount(); row++) {
			String value = ((Vector<String>) ((CompoModel) target.getModel()).getDataVector().get(target.getRowSorter().convertRowIndexToModel(row))).get(0);
			if (value != null && value.toLowerCase().startsWith(keyChar.toLowerCase())) {
				target.getSelectionModel().clearSelection();
				target.getColumnModel().getSelectionModel().clearSelection();
				target.changeSelection(row, 0, true, false);
				target.setRowSelectionInterval(row, row);
				LOG.debug("End shortcutKeyAction");
				return row;
			}
		}
		LOG.debug("End shortcutKeyAction, no result");
		return -1;
	}

	@SuppressWarnings("unchecked")
	private void mouseAction(MouseEvent e) {
		if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			LOG.debug("Start result mouse");
			// Ouvre une popup pour afficher les fichiers de la
			// composition sélectionnée
			JTable target = (JTable) e.getSource();
			Vector<String> v = (Vector<String>) ((CompoModel) target.getModel()).getDataVector()
					.get(target.getRowSorter().convertRowIndexToModel(target.getSelectedRow()));
			List<Fichier> files;
			try {
				files = CompositionUtils.findByArtistTitreAndType(compoResult, v.get(0), v.get(1), v.get(2)).getFiles();
				DialogFileTable pop = new DialogFileTable(null, "Fichier", true, files, new Dimension(1500, 400));
				pop.showDialogFileTable();
			} catch (MyException e1) {
				LOG.error("Ereur lors de l'affichage des fichier d'une compo", e1);
			}
			LOG.debug("End result mouse");
		} else if (SwingUtilities.isRightMouseButton(e)) {
			LOG.debug("Start right mouse");
			// Copie dans le clipboard l'artist et l'oeuvre
			JTable target = (JTable) e.getSource();
			int rowAtPoint = target.rowAtPoint(SwingUtilities.convertPoint(target, new Point(e.getX(), e.getY()), target));
			if (rowAtPoint > -1) {
				target.setRowSelectionInterval(rowAtPoint, rowAtPoint);
			}
			Vector<String> v = (Vector<String>) ((CompoModel) target.getModel()).getDataVector().get(target.getRowSorter().convertRowIndexToModel(rowAtPoint));
			StringSelection selection = new StringSelection(v.get(0) + " " + v.get(1));
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
			LOG.debug("End right mouse");
		}
	}

	public JButton getSearch() {
		return search;
	}

	public void setSearch(JButton search) {
		this.search = search;
	}
}
