package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.SearchUtils;

/**
 * Pour rechercher des fichiers et afficher/modifier/supprimer leurs
 * compositions. Created by PBR on 29 mai 2018.
 */
public class FichierPanel extends AbstractPanel {
	private static final long serialVersionUID = 8581952935884211032L;

	private static final Logger LOG = Logger.getLogger(FichierPanel.class);
	
	private static final int MIN_HEIGHT_TABLE = 42;
	private static final int MAX_HEIGHT_TABLE = 84;

	private JComboBox<String> auteur;
	private JTextField name;
	private JTextField publi;
	private JTextField rangeB;
	private JTextField rangeE;
	private JComboBox<String> cat;
	private JCheckBox sorted;
	private JButton search;
	private JButton reset;
	private boolean showFichierTable = true;
	private boolean showCompoTable = true;
	private JPanel fichierPanel;
	private JPanel compoPanel;

	private JTable tableFiles;
	private JTable tableCompo;
	private List<Fichier> fichiers;
	private FichierModel fichieModel;
	private CompoModel compoModel;
	private JButton hideFileList;
	private JButton hideCompoList;
	private Dimension parentSize;

	private static final String[] headerFiles = { "Auteur", "Nom du fichier", "Date de publication", "Categorie",
			"Dates", "Date de création", "Taille", "Classement", "Classé" };
	private static final String[] headerCompo = { "Artiste", "Titre", "Type", "Classement" };

	public FichierPanel() {
		super();
		LOG.debug("Start FichierPanel");
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		LOG.debug("End FichierPanel");
	}
	
	public void initPanel(List<String> authors) {

		parentSize = this.getParent().getPreferredSize();
		initSearchBtn(authors);
		initFichierTable();
		initCompoTable();
		
	}

	private void initSearchBtn(List<String> authors) {
		JPanel header = new JPanel();
		// Auteur
		JPanel auteurPanel = new JPanel();
		auteurPanel.setPreferredSize(new Dimension(200, 60));
		JLabel auteurLabel = new JLabel("Auteur : ");
		auteur = new JComboBox<>();
		auteur.addItem("");
		for (String author : authors) {
			auteur.addItem(author);
		}
		auteur.setPreferredSize(new Dimension(150, 25));
		auteurPanel.add(auteurLabel);
		auteurPanel.add(auteur);
		header.add(auteurPanel);
		// Name
		JPanel namePanel = new JPanel();
		namePanel.setPreferredSize(new Dimension(250, 60));
		JLabel nameLabel = new JLabel("Nom du fichier : ");
		name = new JTextField();
		name.setPreferredSize(new Dimension(250, 25));
		namePanel.add(nameLabel);
		namePanel.add(name);
		header.add(namePanel);
		// Publi
		JPanel publiPanel = new JPanel();
		publiPanel.setPreferredSize(new Dimension(150, 60));
		JLabel publiLabel = new JLabel("Année de publication : ");
		publi = new JTextField();
		publi.setPreferredSize(new Dimension(75, 25));
		publiPanel.add(publiLabel);
		publiPanel.add(publi);
		header.add(publiPanel);
		// Range
		JPanel rangePanel = new JPanel();
		rangePanel.setPreferredSize(new Dimension(200, 60));
		JLabel rangeLabel = new JLabel("Année(s) du classement :                ");
		rangeB = new JTextField();
		rangeE = new JTextField();
		rangeB.setPreferredSize(new Dimension(90, 25));
		rangeE.setPreferredSize(new Dimension(90, 25));
		rangePanel.add(rangeLabel);
		rangePanel.add(rangeB);
		rangePanel.add(rangeE);
		header.add(rangePanel);
		// Categorie
		JPanel catPanel = new JPanel();
		catPanel.setPreferredSize(new Dimension(180, 60));
		JLabel catLabel = new JLabel("Catégorie : ");
		cat = new JComboBox<>();
		cat.addItem("");
		Cat[] values = Cat.values();
		for (int i = 0; i < values.length; i++) {
			cat.addItem(values[i].toString());
		}
		cat.setPreferredSize(new Dimension(120, 25));
		catPanel.add(catLabel);
		catPanel.add(cat);
		header.add(catPanel);
		// Sorted
		JPanel sortedPanel = new JPanel();
		sortedPanel.setPreferredSize(new Dimension(50, 60));
		JLabel sortedLabel = new JLabel("Classé: ");
		sorted = new JCheckBox();
		sorted.setPreferredSize(new Dimension(50, 25));
		sortedPanel.add(sortedLabel);
		sortedPanel.add(sorted);
		header.add(sortedPanel);

		// SEARCH
		search = new JButton("Chercher");
		search.setBackground(Color.white);
		search.setPreferredSize(new Dimension(150, 60));
		search.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				searchAction();
			}
		});
		header.add(search);
		// RESET
		reset = new JButton("Réinitialiser");
		reset.setBackground(Color.white);
		reset.setPreferredSize(new Dimension(150, 60));
		reset.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				resetAction();
			}
		});
		header.add(reset);
		// hideFileList
		hideFileList = new JButton("Cacher la liste des fichiers");
		hideFileList.setBackground(Color.white);
		hideFileList.setPreferredSize(new Dimension(200, 60));
		hideFileList.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				showFichierTable = !showFichierTable;
				fichierPanel.setVisible(showFichierTable);
				if (showFichierTable) {
					hideFileList.setText("Cacher la liste des fichiers");
					setTableSize(compoPanel, MIN_HEIGHT_TABLE);
				} else {
					hideFileList.setText("Afficher la liste des fichiers");
					setTableSize(compoPanel, MAX_HEIGHT_TABLE);
				}
			}
		});
		header.add(hideFileList);
		// hideCompoList
		hideCompoList = new JButton("Cacher la liste des compositions");
		hideCompoList.setBackground(Color.white);
		hideCompoList.setPreferredSize(new Dimension(200, 60));
		hideCompoList.addActionListener(new AbstractAction() {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showCompoTable = !showCompoTable;
				compoPanel.setVisible(showCompoTable);
				if (showCompoTable) {
					hideCompoList.setText("Cacher la liste des compositions");
					setTableSize(fichierPanel, MIN_HEIGHT_TABLE);
				} else {
					hideCompoList.setText("Afficher la liste des compositions");
					setTableSize(fichierPanel, MAX_HEIGHT_TABLE);
				}
			}
		});
		header.add(hideCompoList);

		setSize(header, (int) parentSize.getWidth(), Math.floorDiv(10 * (int) parentSize.getHeight(), 100));
		this.add(header);
	}

	private void initFichierTable() {
		fichierPanel = new JPanel(new BorderLayout());

		tableFiles = new JTable();
		tableFiles.setAutoCreateRowSorter(true);
		tableFiles.setRowHeight(30);
		tableFiles.setFillsViewportHeight(true);
		tableFiles.setBackground(UIManager.getColor("Label.background"));
		tableFiles.setFont(UIManager.getFont("Label.font"));
		tableFiles.setBorder(UIManager.getBorder("Label.border"));
		fichieModel = new FichierModel(new Object[0][9], headerFiles);
		tableFiles.setModel(fichieModel);
		tableFiles.setRowSorter(new TableRowSorter<TableModel>(fichieModel));
		tableFiles.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				mouseAction(e);
			}
		});

		fichierPanel.add(new JScrollPane(tableFiles), BorderLayout.CENTER);
		this.add(fichierPanel);
		setTableSize(fichierPanel, MIN_HEIGHT_TABLE);
	}
	
	private void initCompoTable() {
		compoPanel = new JPanel(new BorderLayout());
		
		tableCompo = new JTable();
		tableCompo.setAutoCreateRowSorter(true);
		tableCompo.setRowHeight(30);
		tableCompo.setFillsViewportHeight(true);
		tableCompo.setBackground(UIManager.getColor("Label.background"));
		tableCompo.setFont(UIManager.getFont("Label.font"));
		tableCompo.setBorder(UIManager.getBorder("Label.border"));
		compoModel = new CompoModel(new Object[0][5], headerCompo);
		tableCompo.setModel(compoModel);
		tableCompo.setRowSorter(new TableRowSorter<TableModel>(compoModel));
		tableCompo.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				mouseAction(e);
			}
		});

		compoPanel.add(new JScrollPane(tableCompo), BorderLayout.CENTER);
		this.add(compoPanel);
		setTableSize(compoPanel, MIN_HEIGHT_TABLE);
	}

	@SuppressWarnings("unchecked")
	private void mouseAction(MouseEvent e) {
		if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			LOG.debug("Start result mouse");
			// Ouvre une popup pour afficher les compositions du fichier sélectionné
			JTable target = (JTable) e.getSource();
			Vector<String> v = (Vector<String>) ((FichierModel) target.getModel()).getDataVector()
					.get(target.getRowSorter().convertRowIndexToModel(target.getSelectedRow()));
			List<Composition> compo = ImportXML.importXML(Constant.XML_PATH + v.get(1) + Constant.XML_EXTENSION);
			updateCompoTable(compo);
			LOG.debug("End result mouse");
		} else if (SwingUtilities.isRightMouseButton(e)) {
			LOG.debug("Start right mouse");
			// Ouvre le fichier xml
			JTable target = (JTable) e.getSource();
			int rowAtPoint = target.rowAtPoint(SwingUtilities.convertPoint(target,
					new Point(e.getX(), e.getY()), target));
			if (rowAtPoint > -1) {
				target.setRowSelectionInterval(rowAtPoint, rowAtPoint);
			}
			Vector<String> v = (Vector<String>) ((FichierModel) target.getModel()).getDataVector().get(
					target.getRowSorter().convertRowIndexToModel(rowAtPoint));
			String absFile = Constant.XML_PATH + v.get(1) + Constant.XML_EXTENSION;
			try {
				Runtime.getRuntime().exec(Constant.NOTEPAD_PATH + absFile);
			} catch (IOException e1) {
				LOG.error("Erreur lors de l'ouverture de Notepad++ " + Constant.NOTEPAD_PATH, e1);
			}
			LOG.debug("End right mouse");
		}
	}

	private void searchAction() {
		LOG.debug("Start searchAction");
		fichiers = new ArrayList<Fichier>(
				ImportXML.importXML(Constant.FINAL_FILE_PATH).stream().map(Composition::getFiles).flatMap(List::stream)
						.collect(Collectors.toMap(Fichier::getFileName, f -> f, (p, q) -> p)).values());
		if (CollectionUtils.isNotEmpty(fichiers)) {
			CollectionUtils.filter(fichiers,
					(Object f) -> SearchUtils.evaluateFichierStrictly(publi.getText(), name.getText(),
							auteur.getSelectedItem().toString(), cat.getSelectedItem().toString(), rangeB.getText(),
							rangeE.getText(), f));
			updateFileTable();
		}
		LOG.debug("End searchAction");
	}

	private void updateFileTable() {
		LOG.debug("Start updateFileTable");
		fichieModel.setRowCount(0);
		fichieModel.setDataVector(FichierUtils.convertListForJTable(fichiers, null),
				new Vector<>(Arrays.asList(headerFiles)));
		colRenderer(tableFiles);
		fichieModel.fireTableDataChanged();
		tableFiles.getRowSorter().toggleSortOrder(1);
		tableFiles.repaint();
		LOG.debug("Start updateFileTable");
	}
	
	private void updateCompoTable(List<Composition> compo) {
		LOG.debug("Start updateCompoTable");
		compoModel.setRowCount(0);
		compoModel.setDataVector(CompositionUtils.convertCompositionListToVector(compo, true, false),
				new Vector<>(Arrays.asList(headerCompo)));
		colRenderer(tableCompo);
		compoModel.fireTableDataChanged();
		tableCompo.getRowSorter().toggleSortOrder(3);
		tableCompo.repaint();
		LOG.debug("Start updateCompoTable");
	}

	private void resetAction() {
		LOG.debug("Start resetAction");
		auteur.setSelectedItem("");
		name.setText("");
		publi.setText("");
		rangeB.setText("");
		rangeE.setText("");
		cat.setSelectedItem("");
		LOG.debug("End resetAction");
	}

	private void setTableSize(JPanel panel, int height) {
		setSize(panel, (int) parentSize.getWidth(), Math.floorDiv(height * (int) parentSize.getHeight(), 100));
	}
}
