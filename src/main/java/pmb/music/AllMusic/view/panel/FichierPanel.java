package pmb.music.AllMusic.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.log4j.Logger;
import org.kordamp.ikonli.swing.FontIcon;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.model.SearchMethod;
import pmb.music.AllMusic.model.SearchRange;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.utils.SearchUtils;
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.component.JComboCheckBox;
import pmb.music.AllMusic.view.component.MyInputText;
import pmb.music.AllMusic.view.dialog.ModifyCompositionDialog;
import pmb.music.AllMusic.view.dialog.ModifyFichierDialog;
import pmb.music.AllMusic.view.model.CompoFichierPanelModel;
import pmb.music.AllMusic.view.model.FichierPanelModel;

/**
 * Pour rechercher des fichiers et afficher/modifier/supprimer leurs
 * compositions. Created by PBR on 29 mai 2018.
 */
public class FichierPanel extends JPanel {
	private static final long serialVersionUID = 8581952935884211032L;

	private static final Logger LOG = Logger.getLogger(FichierPanel.class);

	private static final int MIN_HEIGHT_TABLE = 41;
	private static final int MAX_HEIGHT_TABLE = 82;

	public static final int INDEX_FILE_AUTHOR = 0;
	public static final int INDEX_FILE_FILE_NAME = 1;
	public static final int INDEX_FILE_PUBLISH = 2;
	public static final int INDEX_FILE_CAT = 3;
	public static final int INDEX_FILE_RANGE = 4;
	public static final int INDEX_PERCENT_DELETED = 5;
	public static final int INDEX_CREATE_DATE = 6;
	public static final int INDEX_FILE_SIZE = 7;
	public static final int INDEX_FILE_SORTED = 8;

	public static final int INDEX_COMPO_LINE_NUMBER = 0;
	public static final int INDEX_COMPO_ARTIST = 1;
	public static final int INDEX_COMPO_TITLE = 2;
	public static final int INDEX_COMPO_TYPE = 3;
	public static final int INDEX_COMPO_RANK = 4;
	public static final int INDEX_COMPO_FILE_SIZE = 5;
	public static final int INDEX_COMPO_SCORE = 6;
	public static final int INDEX_COMPO_SELECTED = 7;
	public static final int INDEX_COMPO_DELETED = 8;

	// Search components
	private MyInputText auteur;
	private MyInputText name;
	private JComboBox<String> searchRange;
	private MyInputText publi;
	private JTextField rangeB;
	private JTextField rangeE;
	private JComboCheckBox cat;
	private JComboCheckBox type;
	private JCheckBox sorted;
	private JCheckBox deleted;
	private JButton search;
	private JButton reset;
	private JLabel resultLabel;

	// Fichier componants
	private JPanel fichierPanel;
	private JTable tableFiles;
	private FichierPanelModel fichieModel;
	private List<Fichier> fichiers;
	private JButton hideFileList;
	private boolean showFichierTable = true;
	private Integer sortedFichierColumn;
	private SortOrder sortFichierOrder;
	private int selectedFichierRow = -1;
	private String selectedFichierName = "";

	// Composition componants
	private JPanel compoPanel;
	private JTable tableCompo;
	private CompoFichierPanelModel compoModel;
	private List<Composition> compositionList;
	private JButton hideCompoList;
	private boolean showCompoTable = true;
	private Integer sortedCompoColumn;
	private SortOrder sortCompoOrder;
	private SortOrder sortCompoDeletedOrder = SortOrder.ASCENDING;
	private int selectedCompoRow = -1;

	private Dimension parentSize;

	private static final String[] headerFiles = { "Auteur", "Nom du fichier", "Date de publication", "Categorie",
	private static final String[] headerCompo = { "#", "Artiste", "Titre", "Type", "Classement", "Nombre de fichiers",
			"Score", "", "" };

	/**
	 * Constructeur de {@link FichierPanel}.
	 * 
	 */
	public FichierPanel() {
		super();
		LOG.debug("Start FichierPanel");
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		LOG.debug("End FichierPanel");
	}

	/**
	 * Initialise tous les composants du {@link FichierPanel}.
	 * 
	 * @param artistPanel le panel artiste
	 * @param authors la liste des auteurs
	 */
	public void initPanel(ArtistPanel artistPanel, List<String> authors) {
		LOG.debug("Start initPanel");

		parentSize = this.getParent().getPreferredSize();
		initSearchBtn(artistPanel, authors);
		initFichierTable();
		initCompoTable(artistPanel);

		LOG.debug("End initPanel");
	}

	/**
	 * Initialise les composants de recherche.
	 * 
	 * @param artistPanel le panel artiste
	 * @param authors la liste des auteurs
	 */
	private void initSearchBtn(ArtistPanel artistPanel, List<String> authors) {
		JPanel header = new JPanel(new GridLayout(2, 6));
		// Auteur
		JPanel auteurPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		JLabel auteurLabel = PanelUtils.createJLabel("Auteur : ", 150);
		auteur = new MyInputText(JComboBox.class, 150);
		AutoCompleteSupport.install((JComboBox<?>) auteur.getInput(), GlazedLists.eventListOf(authors.toArray()));
		auteurPanel.add(auteurLabel);
		auteurPanel.add(auteur);
		header.add(auteurPanel);
		// Nom du fichier
		JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		JLabel nameLabel = PanelUtils.createJLabel("Nom du fichier : ", 200);
		name = new MyInputText(JTextField.class, 180);
		namePanel.add(nameLabel);
		namePanel.add(name);
		header.add(namePanel);
		// Publi
		JPanel publiPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		JLabel publiLabel = PanelUtils.createJLabel("Année de publication : ", 230);
		publi = new MyInputText(JTextField.class, 100);
		searchRange = new JComboBox<String>(
				Arrays.asList(SearchRange.values()).stream().map(v -> v.getValue()).toArray(String[]::new));
		PanelUtils.setSize(searchRange, 45, PanelUtils.COMPONENT_HEIGHT);
		publiPanel.add(publiLabel);
		publiPanel.add(searchRange);
		publiPanel.add(publi);
		header.add(publiPanel);
		// Range
		JPanel rangePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
		JLabel rangeLabel = PanelUtils.createJLabel("Année(s) du classement : ", 180);
		rangeB = new JTextField();
		rangeE = new JTextField();
		rangeE.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
			}

			@Override
			public void focusGained(FocusEvent e) {
				JTextField source = (JTextField) e.getSource();
				if (StringUtils.isBlank(source.getText()) && StringUtils.isNotBlank(rangeB.getText())) {
					source.setText(rangeB.getText());
					source.selectAll();
				}
			}
		});
		PanelUtils.setSize(rangeB, 85, PanelUtils.COMPONENT_HEIGHT);
		PanelUtils.setSize(rangeE, 85, PanelUtils.COMPONENT_HEIGHT);
		rangePanel.add(rangeLabel);
		rangePanel.add(rangeB);
		rangePanel.add(rangeE);
		header.add(rangePanel);
		// Categorie
		JPanel catPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		JLabel catLabel = PanelUtils.createJLabel("Catégorie : ", 180);
		cat = new JComboCheckBox(
				Arrays.asList(Cat.values()).stream().map(c -> c.getCat()).collect(Collectors.toList()));
		cat.setPreferredSize(new Dimension(120, PanelUtils.COMPONENT_HEIGHT));
		catPanel.add(catLabel);
		catPanel.add(cat);
		header.add(catPanel);
		// Type
		JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		JLabel typeLabel = PanelUtils.createJLabel("Type : ", 180);
		type = new JComboCheckBox(
				Arrays.asList(RecordType.values()).stream().map(t -> t.getRecordType()).collect(Collectors.toList()));
		type.setPreferredSize(new Dimension(150, PanelUtils.COMPONENT_HEIGHT));
		typePanel.add(typeLabel);
		typePanel.add(type);
		header.add(typePanel);
		// Sorted
		JPanel sortedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		JLabel sortedLabel = PanelUtils.createJLabel("Classé: ", 150);
		sorted = new JCheckBox();
		sorted.setPreferredSize(new Dimension(150, PanelUtils.COMPONENT_HEIGHT));
		sorted.setHorizontalAlignment(SwingConstants.CENTER);
		sortedPanel.add(sortedLabel);
		sortedPanel.add(sorted);
		header.add(sortedPanel);
		// Deleted
		JPanel deletedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		JLabel deletedLabel = PanelUtils.createJLabel("Supprimées: ", 150);
		deleted = new JCheckBox();
		deleted.setPreferredSize(new Dimension(150, PanelUtils.COMPONENT_HEIGHT));
		deleted.setHorizontalAlignment(SwingConstants.CENTER);
		deletedPanel.add(deletedLabel);
		deletedPanel.add(deleted);
		header.add(deletedPanel);

		// SEARCH
		search = PanelUtils.createJButton("Rechercher", 120, Constant.ICON_SEARCH);
		search.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				searchAction();
			}
		});
		header.add(search);
		// RESET
		reset = PanelUtils.createJButton("Réinitialiser", 120, Constant.ICON_ERASE);
		reset.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				resetAction();
			}
		});
		header.add(reset);
		// hideFileList
		hideFileList = PanelUtils.createJButton("Cacher la liste des fichiers", 180, Constant.ICON_HIDE);
		hideFileList.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				showFichierTable = !showFichierTable;
				fichierPanel.setVisible(showFichierTable);
				if (showFichierTable) {
					hideFileList.setText("Cacher la liste des fichiers");
					hideFileList.setIcon(FontIcon.of(Constant.ICON_HIDE));
					setTableSize(compoPanel, MIN_HEIGHT_TABLE);
				} else {
					hideFileList.setText("Afficher la liste des fichiers");
					hideFileList.setIcon(FontIcon.of(Constant.ICON_SHOW));
					setTableSize(compoPanel, MAX_HEIGHT_TABLE);
				}
			}
		});
		header.add(hideFileList);
		// hideCompoList
		hideCompoList = PanelUtils.createJButton("Cacher la liste des compositions", 200, Constant.ICON_HIDE);
		hideCompoList.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				showCompoTable = !showCompoTable;
				compoPanel.setVisible(showCompoTable);
				if (showCompoTable) {
					hideCompoList.setText("Cacher la liste des compositions");
					hideCompoList.setIcon(FontIcon.of(Constant.ICON_HIDE));
					setTableSize(fichierPanel, MIN_HEIGHT_TABLE);
				} else {
					hideCompoList.setText("Afficher la liste des compositions");
					hideCompoList.setIcon(FontIcon.of(Constant.ICON_SHOW));
					setTableSize(fichierPanel, MAX_HEIGHT_TABLE);
				}
			}
		});
		header.add(hideCompoList);
		// Delete Btn
		JButton delete = PanelUtils.createJButton("<html>Supprimer les compositions sélectionnées</html>", 200,
				Constant.ICON_DELETE);
		delete.addActionListener((ActionEvent e) -> {
			PanelUtils.deleteCompositionAction(artistPanel, compositionList, compoModel.getSelected(), resultLabel);
			updateCompoTable(compositionList, selectedFichierName);
		});
		header.add(delete);
		// Label pour afficher les resultats
		JPanel resultPanel = new JPanel();
		resultLabel = PanelUtils.createJLabel("", 400);
		resultLabel.setForeground(new Color(8, 187, 81));
		Font labelFont2 = resultLabel.getFont();
		resultLabel.setFont(new Font(labelFont2.getName(), labelFont2.getStyle(), 20));
		resultPanel.add(resultLabel);
		header.add(resultPanel);

		PanelUtils.setSize(header, (int) parentSize.getWidth(), Math.floorDiv(15 * (int) parentSize.getHeight(), 100));
		this.add(header);
	}

	@SuppressWarnings({ "unchecked" })
	private void initFichierTable() {
		fichierPanel = new JPanel(new BorderLayout());

		tableFiles = new JTable();
		tableFiles.setAutoCreateRowSorter(true);
		tableFiles.setRowHeight(30);
		tableFiles.setFillsViewportHeight(true);
		tableFiles.getTableHeader().setResizingAllowed(true);
		tableFiles.setBackground(UIManager.getColor("Label.background"));
		tableFiles.setFont(UIManager.getFont("Label.font"));
		tableFiles.setBorder(UIManager.getBorder("Label.border"));
		fichieModel = new FichierPanelModel(new Object[0][9], headerFiles);
		tableFiles.setModel(fichieModel);
		tableFiles.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				mouseActionForFileTable(e);
			}
		});
		tableFiles.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// Nothing to do
			}

			@Override
			public void keyReleased(KeyEvent e) {
				selectedFichierRow = PanelUtils.keyShortcutAction(e, selectedFichierRow, sortedFichierColumn);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// Nothing to do
			}
		});
		tableFiles.getRowSorter().addRowSorterListener(new RowSorterListener() {
			@Override
			public void sorterChanged(RowSorterEvent e) {
				if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
					List<SortKey> sortKeys = e.getSource().getSortKeys();
					if (!sortKeys.isEmpty()) {
						sortedFichierColumn = sortKeys.get(0).getColumn();
						sortFichierOrder = sortKeys.get(0).getSortOrder();
					}
				}
			}
		});

		fichierPanel.add(new JScrollPane(tableFiles), BorderLayout.CENTER);
		this.add(fichierPanel);
		setTableSize(fichierPanel, MIN_HEIGHT_TABLE);
	}

	private void initCompoTable(ArtistPanel artistPanel) {
		compoPanel = new JPanel(new BorderLayout());

		tableCompo = new JTable();
		tableCompo.setAutoCreateRowSorter(true);
		tableCompo.setRowHeight(30);
		tableCompo.setFillsViewportHeight(true);
		tableCompo.getTableHeader().setResizingAllowed(true);
		tableCompo.setBackground(UIManager.getColor("Label.background"));
		tableCompo.setFont(UIManager.getFont("Label.font"));
		tableCompo.setBorder(UIManager.getBorder("Label.border"));
		compoModel = new CompoFichierPanelModel(new Object[0][headerCompo.length - 1], headerCompo);
		tableCompo.setModel(compoModel);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(compoModel) {
			@Override
			public boolean isSortable(int column) {
				if (column != INDEX_COMPO_LINE_NUMBER)
					return true;
				else
					return false;
			};
		};
		tableCompo.setRowSorter(sorter);
		tableCompo.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				mouseActionForCompoTable(e, artistPanel);
			}
		});
		tableCompo.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// Nothing to do
			}

			@Override
			public void keyReleased(KeyEvent e) {
				selectedCompoRow = PanelUtils.keyShortcutAction(e, selectedCompoRow, sortedCompoColumn);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// Nothing to do
			}
		});
		tableCompo.getRowSorter().addRowSorterListener(new RowSorterListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void sorterChanged(RowSorterEvent e) {
					List<SortKey> sortKeys = e.getSource().getSortKeys();
					if (!sortKeys.isEmpty()) {
					if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
						// Sort of deleted column and store sorted column and order
						int column = sortKeys.get(0).getColumn();
						if (column == INDEX_COMPO_SELECTED) {
							sortedCompoColumn = INDEX_COMPO_DELETED;
							sortCompoDeletedOrder = sortCompoDeletedOrder == SortOrder.ASCENDING ? SortOrder.DESCENDING
									: SortOrder.ASCENDING;
							sortCompoOrder = sortCompoDeletedOrder;
							List<SortKey> list = new LinkedList<>(
									Arrays.asList(new RowSorter.SortKey(sortedCompoColumn, sortCompoDeletedOrder)));
							tableCompo.getRowSorter().getSortKeys().stream().forEach(s -> list.add(s));
							tableCompo.getRowSorter().setSortKeys(list);
						} else {
							sortCompoOrder = sortKeys.get(0).getSortOrder();
							sortedCompoColumn = column;
						}
					}
					// Handling of line numbers
					for (int i = 0; i < tableCompo.getRowCount(); i++) {
						tableCompo.setValueAt(i + 1, i, INDEX_COMPO_LINE_NUMBER);
					}
				}
			}
		});

		compoPanel.add(new JScrollPane(tableCompo), BorderLayout.CENTER);
		this.add(compoPanel);
		setTableSize(compoPanel, MIN_HEIGHT_TABLE);
	}

	private void mouseActionForFileTable(MouseEvent e) {
		Optional<Vector<String>> selectedRow = PanelUtils.getSelectedRow(e);
		if (!selectedRow.isPresent()) {
			return;
		}
		selectedFichierName = selectedRow.get().get(INDEX_FILE_FILE_NAME);
		if (e.getClickCount() == 1 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			LOG.debug("Start left mouse, open");
			// Double click avec le bouton gauche
			// Affiche les compositions du fichier sélectionné
			compositionList = ImportXML.importXML(
					Constant.getXmlPath() + selectedRow.get().get(INDEX_FILE_FILE_NAME) + Constant.XML_EXTENSION);
			if (!deleted.isSelected()) {
				compositionList = compositionList.stream().filter(c -> !c.isDeleted()).collect(Collectors.toList());
			}
			updateCompoTable(compositionList, selectedFichierName);
			LOG.debug("End left mouse, open");
		} else if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			LOG.debug("End left mouse, modify");
			// Popup pour modifier le fichier
			modifyFichierAction(selectedRow.get());
			LOG.debug("End left mouse, modify");
		} else if (SwingUtilities.isRightMouseButton(e)) {
			LOG.debug("Start right mouse");
			// Ouvre le fichier txt
			Optional<String> filePath = FichierUtils.buildTxtFilePath(selectedRow.get().get(INDEX_FILE_FILE_NAME),
					selectedRow.get().get(INDEX_FILE_AUTHOR));
			try {
				FichierUtils.openFileInNotepad(filePath);
			} catch (MyException e1) {
				LOG.error("Erreur lors de l'ouverture du fichier: " + filePath, e1);
			}
			LOG.debug("End right mouse");
		}
	}

	private void mouseActionForCompoTable(MouseEvent e, ArtistPanel artistPanel) {
		Optional<Vector<String>> selectedRow = PanelUtils.getSelectedRow(e);
		if (!selectedRow.isPresent()) {
			return;
		}
		if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			LOG.debug("Start left mouse");
			// Popup pour modifier la composition
			modifyCompositionAction(artistPanel, selectedRow.get());
			LOG.debug("End left mouse");
		} else if (SwingUtilities.isRightMouseButton(e)) {
			LOG.debug("Start right mouse");
			// Copie dans le clipboard l'artist et l'oeuvre
			StringSelection selection = new StringSelection(
					selectedRow.get().get(INDEX_COMPO_ARTIST) + " " + selectedRow.get().get(INDEX_COMPO_TITLE));
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
			LOG.debug("End right mouse");
		}
	}

	private void modifyFichierAction(Vector<String> selected) {
		LOG.debug("Start modifyFichierAction");
		resultLabel.setText("");
		String fileName = selected.get(INDEX_FILE_FILE_NAME);
		// Index du fichier dans le tableau
		int indexOf = fichiers.indexOf(
				fichiers.stream().filter(f -> StringUtils.equals(f.getFileName(), fileName)).findFirst().get());
		// Lancement de la popup de modification
		ModifyFichierDialog md = new ModifyFichierDialog(null, "Modifier un fichier", true, selected);
		md.showDialogFileTable();
		Vector<String> newFichier;
		if (md.isSendData()) {
			// On recupère le fichier si il a bien été modifié
			LOG.debug("Fichier modifiée");
			newFichier = md.getFichier();
		} else {
			LOG.debug("Aucune modification");
			return;
		}
		// Retire les caractères interdits pour windows
		if (Arrays.asList(Constant.getForbiddenCharactersFilename()).stream()
				.anyMatch(s -> newFichier.get(INDEX_FILE_FILE_NAME).contains(s))) {
			Arrays.asList(Constant.getForbiddenCharactersFilename()).stream().forEach(
					s -> newFichier.set(INDEX_FILE_FILE_NAME, newFichier.get(INDEX_FILE_FILE_NAME).replaceAll(s, "")));
		}
		Fichier modifiedFichier = null;
		try {
			// Modification du fichier
			modifiedFichier = FichierUtils.modifyFichier(fileName, newFichier.get(INDEX_FILE_FILE_NAME),
					newFichier.get(INDEX_FILE_PUBLISH), newFichier.get(INDEX_FILE_RANGE),
					newFichier.get(INDEX_FILE_CAT), newFichier.get(INDEX_FILE_SIZE), newFichier.get(INDEX_FILE_SORTED));
		} catch (MyException e) {
			String log = "Erreur pendant FichierUtils.modifyFichier";
			LOG.error(log, e);
			resultLabel.setText(log + e);
			return;
		}
		// Mise à jour du JTable
		fichiers.set(indexOf, modifiedFichier);
		updateFileTable();
		resultLabel.setText("Fichier modifié");
		LOG.debug("End modifyFichierAction");
	}

	@SuppressWarnings("unchecked")
	private void modifyCompositionAction(final ArtistPanel artistPanel, Vector<String> selected) {
		LOG.debug("Start modif");
		resultLabel.setText("");
		artistPanel.interruptUpdateArtist();
		String label = "Élément modifié";
		Composition compoToModifInFinal;
		Composition compoToModifInTable;
		// On récupère la ligne selectionnée
		Vector<String> v = selected;
		List<Composition> importXML;
		importXML = ImportXML.importXML(Constant.getFinalFilePath());
		Fichier currentFile = null;
		try {
			// On récupère la composition à modifier
			compoToModifInTable = CompositionUtils.findByArtistTitreAndType(compositionList, v.get(INDEX_COMPO_ARTIST),
					v.get(INDEX_COMPO_TITLE), v.get(INDEX_COMPO_TYPE), true);
			currentFile = compoToModifInTable.getFiles().get(0);
			Optional<Composition> findByFileAndRank = CompositionUtils.findByFile(importXML, currentFile,
					Optional.of(v.get(INDEX_COMPO_ARTIST)), Optional.of(v.get(INDEX_COMPO_TITLE)));
			if (!findByFileAndRank.isPresent()) {
				resultLabel.setText("Impossible de trouver la composition dans Final.xml");
				return;
			}
			compoToModifInFinal = findByFileAndRank.get();
		} catch (MyException e1) {
			String log = "Erreur dans modifAction, impossible de trouver la compo à modifier";
			LOG.error(log, e1);
			resultLabel.setText(log + e1);
			return;
		}
		int indexOfXml = importXML.indexOf(compoToModifInFinal);
		int indexOfResult = compositionList.indexOf(compoToModifInTable);
		// Lancement de la popup de modification
		ModifyCompositionDialog md = new ModifyCompositionDialog(null, "Modifier une composition", true,
				new Dimension(800, 150), v);
		md.showDialogFileTable();
		if (md.isSendData()) {
			// On recupère la compo si elle a bien été modifiée
			LOG.debug("Compo modifiée");
			v = md.getCompo();
		} else {
			LOG.debug("Aucune modification");
			return;
		}

		boolean isDeleted = false;
		// On modifie la composition
		compoToModifInTable.setArtist(v.get(INDEX_COMPO_ARTIST));
		compoToModifInTable.setTitre(v.get(INDEX_COMPO_TITLE));
		compoToModifInTable.setRecordType(RecordType.valueOf(v.get(INDEX_COMPO_TYPE)));

		// Gestion du fichier final.xml
		if (importXML.get(indexOfXml).getFiles().size() > 1) {
			LOG.debug("Plusieurs fichiers, suppression du fichier en cours");
			importXML.get(indexOfXml).getFiles().remove(currentFile);
		} else {
			LOG.debug("Un seul fichier, suppression de la composition");
			importXML.remove(indexOfXml);
		}
		Composition compoExist = CompositionUtils.compoExist(importXML, compoToModifInTable);
		if (compoExist == null) {
			LOG.debug("Pas de regroupement");
			importXML.add(compoToModifInTable);
		} else {
			LOG.debug("La compo existe déjà, on regroupe");
			isDeleted = compoToModifInTable.isDeleted() || compoExist.isDeleted();
			compoExist.getFiles().addAll(compoToModifInTable.getFiles());
			compoExist.setDeleted(isDeleted);
			compoToModifInTable.setDeleted(isDeleted);
		}
		try {
			ExportXML.exportXML(importXML, Constant.getFinalFile());
			artistPanel.updateArtistPanel();
		} catch (IOException e1) {
			String log = "Erreur lors de l'export du fichier final !!";
			LOG.error(log, e1);
			label = log;
		}

		// On remplace par la nouvelle composition dans le tableau
		compositionList.remove(indexOfResult);
		compositionList.add(compoToModifInTable);

		// On modifier les fichier xml en conséquence
		try {
			CompositionUtils.modifyCompositionsInFiles(compoToModifInTable, v.get(INDEX_COMPO_ARTIST),
					v.get(INDEX_COMPO_TITLE), v.get(INDEX_COMPO_TYPE), isDeleted);
		} catch (MyException e1) {
			String log = "Erreur lors de la modification d'une composition";
			LOG.error(log, e1);
			resultLabel.setText(log + e1);
			return;
		}

		resultLabel.setText(label);
		updateCompoTable(compositionList, currentFile.getFileName());
		LOG.debug("End modif");
	}

	private void searchAction() {
		LOG.debug("Start searchAction");
		resultLabel.setText("");
		fichiers = new ArrayList<Fichier>(
				ImportXML.importXML(Constant.getFinalFilePath()).parallelStream().filter(c -> {
					if (StringUtils.isNotBlank(type.getSelectedItems())) {
						return Arrays.asList(StringUtils.split(type.getSelectedItems(), ";")).stream()
								.anyMatch((t -> c.getRecordType() == RecordType.getByValue(t)));
					} else {
						return true;
					}
				}).map(Composition::getFiles).flatMap(List::stream)
						.collect(Collectors.toMap(Fichier::getFileName, f -> f, (p, q) -> p)).values());
		if (CollectionUtils.isNotEmpty(fichiers)) {
			CollectionUtils.filter(fichiers,
					(Object f) -> SearchUtils.filterFichier(SearchMethod.CONTAINS, new JaroWinklerDistance(),
							publi.getText(), (String) searchRange.getSelectedItem(), name.getText(), auteur.getText(),
							cat.getSelectedItems(), rangeB.getText(), rangeE.getText(),
							sorted.isSelected() ? Boolean.TRUE.toString() : "", null, f));
			updateFileTable();
		}
		resultLabel.setText(fichiers.size() + " fichiers trouvé(s) ");
		LOG.debug("End searchAction");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void updateFileTable() {
		LOG.debug("Start updateFileTable");
		Composition c = new Composition();
		c.setFiles(fichiers);
		fichieModel.setRowCount(0);
		fichieModel.setDataVector(FichierUtils.convertCompositionListToFichierVector(Arrays.asList(c), false),
				new Vector<>(Arrays.asList(headerFiles)));
		PanelUtils.colRenderer(tableFiles, true, null, null);
		fichieModel.fireTableDataChanged();
		if (sortedFichierColumn == null) {
			sortedFichierColumn = INDEX_FILE_FILE_NAME;
			sortFichierOrder = SortOrder.ASCENDING;
		}
		tableFiles.getRowSorter()
				.setSortKeys(Collections.singletonList(new RowSorter.SortKey(sortedFichierColumn, sortFichierOrder)));
		((TableRowSorter) tableFiles.getRowSorter()).setComparator(INDEX_PERCENT_DELETED, MiscUtils.comparePercentage);
		selectedFichierRow = -1;
		tableFiles.repaint();
		updateCompoTable(new ArrayList<>(), null);
		LOG.debug("Start updateFileTable");
	}

	/**
	 * Met à jour le tableau des compositions.
	 * 
	 * @param compo la liste des compositions à afficher
	 * @param selectedFile le fichier selectionné
	 */
	private void updateCompoTable(List<Composition> compo, String selectedFile) {
		LOG.debug("Start updateCompoTable");
		compoModel.setRowCount(0);
		if (selectedFile != null && !compo.isEmpty()) {
			List<Fichier> fichier = compo.stream()
					.map(c -> c.getFiles().stream()
							.filter(f -> StringUtils.equalsIgnoreCase(selectedFile, f.getFileName())).findFirst().get())
					.collect(Collectors.toList());
			List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
			// recover of all the files of the compositions
			compo.parallelStream().forEach(c -> {
				Optional<Composition> findByFile = CompositionUtils.findByFile(importXML, c.getFiles().get(0),
						Optional.of(c.getArtist()), Optional.of(c.getTitre()));
				if (findByFile.isPresent()) {
					c.setFiles(findByFile.get().getFiles());
				} else {
					LOG.warn("Could not find files for: " + c);
				}
			});
			compoModel.setDataVector(
					CompositionUtils.convertCompositionListToVector(compo, fichier, true, true, true, true, true),
					new Vector<>(Arrays.asList(headerCompo)));
		} else {
			compoModel.setDataVector(new Vector<Vector<Object>>(), new Vector<>(Arrays.asList(headerCompo)));
		}
		PanelUtils.colRenderer(tableCompo, false, INDEX_COMPO_DELETED, INDEX_COMPO_TYPE);
		compoModel.fireTableDataChanged();
		if (sortedCompoColumn == null) {
			sortedCompoColumn = INDEX_COMPO_RANK;
			sortCompoOrder = SortOrder.ASCENDING;
		}
		tableCompo.getRowSorter()
				.setSortKeys(Collections.singletonList(new RowSorter.SortKey(sortedCompoColumn, sortCompoOrder)));
		selectedCompoRow = -1;
		tableCompo.getColumnModel().getColumn(INDEX_COMPO_LINE_NUMBER).setMinWidth(30);
		tableCompo.getColumnModel().getColumn(INDEX_COMPO_LINE_NUMBER).setMaxWidth(30);
		tableCompo.removeColumn(tableCompo.getColumnModel().getColumn(INDEX_COMPO_DELETED));
		tableCompo.repaint();
		((JScrollPane) tableCompo.getParent().getParent()).getVerticalScrollBar().setValue(0);
		LOG.debug("End updateCompoTable");
	}

	private void resetAction() {
		LOG.debug("Start resetAction");
		auteur.setText(null);
		name.setText("");
		publi.setText("");
		rangeB.setText("");
		rangeE.setText("");
		cat.clearSelection();
		type.clearSelection();
		sorted.setSelected(false);
		deleted.setSelected(false);
		LOG.debug("End resetAction");
	}

	private void setTableSize(JPanel panel, int height) {
		PanelUtils.setSize(panel, (int) parentSize.getWidth(),
				Math.floorDiv(height * (int) parentSize.getHeight(), 100));
	}

	public JTable getTableFiles() {
		return tableFiles;
	}

	public JTable getTableCompo() {
		return tableCompo;
	}
}
