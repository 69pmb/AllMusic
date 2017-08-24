/**
 * 
 */
package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.file.CleanFile;
import pmb.music.AllMusic.file.ImportFile;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.MyException;

/**
 * Onglet d'import de fichiers txt.
 * 
 * @author pmbroca
 */
public class ImportPanel extends JPanel {

	private static final long serialVersionUID = 5796304304079887263L;

	private static final Logger LOG = Logger.getLogger(ImportPanel.class);

	private final JTextField author;
	private final JTextField publi;
	private final JTextField rangeB;
	private final JTextField rangeE;
	private final JTextField size;
	private final JTextField name;
	private final JTextField date;
	/**
	 * Random line.
	 */
	private final JTextField line;
	private final JTextField separator;
	private final JTextField firstL1;
	private final JTextField firstL2;
	private final JTextField firstL3;
	private final JTextField lastL1;
	private final JTextField lastL2;
	/**
	 * String to remove from import file.
	 */
	private final JTextField characterToRemove;

	private final JTextArea resultLabel;

	private File file;
	private File xmlFile;

	private final JComboBox<Cat> cat;

	private final JComboBox<RecordType> type;

	private Fichier fichier;

	private String absolutePathFileTxt;
	private String absolutePathFileXml;

	private final JCheckBox sorted;
	/**
	 * Si l'artist est en 1er, puis le titre.
	 */
	private final JCheckBox order;
	/**
	 * Si le nom et prénom de l'artist sont inversés. Ex: Young, Neil.
	 */
	private final JCheckBox reverseArtist;
	/**
	 * Doit on supprimer lors de l'import des parenthèses.
	 */
	private final JCheckBox removeParenthese;
	/**
	 * Pas de séparateur mais artist en majuscule ?
	 */
	private final JCheckBox upper;
	/**
	 * 2 séparateurs, suppression du dernier.
	 */
	private final JCheckBox removeAfter;
	/**
	 * Si le characterToRemove est à supprimer au debut ou à la fin de la ligne.
	 */
	private final JCheckBox isBefore;

	private RecordType determineType;

	private String explorePath;

	private List<String> result = new LinkedList<>();

	/**
	 * Construit l'onglet import.
	 * 
	 * @param artist l'onglet artiste
	 */
	public ImportPanel(final ArtistPanel artist) {
		super();
		LOG.debug("Start ImportPanel");
		explorePath = Constant.MUSIC_ABS_DIRECTORY;
		this.setLayout(new GridLayout(6, 1));

		// Insert les boutons du hauts
		insertTopPanel();

		JPanel firstLine = new JPanel();

		// Nom du fichier
		JPanel namePanel = new JPanel();
		namePanel.setPreferredSize(new Dimension(400, 60));
		JLabel nameLabel = new JLabel("Nom du fichier : ");
		name = new JTextField();
		name.setPreferredSize(new Dimension(350, 25));
		namePanel.add(nameLabel);
		namePanel.add(name);
		firstLine.add(namePanel);

		name.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				absolutePathFileXml = Constant.XML_PATH + name.getText() + Constant.XML_EXTENSION;
				if (FileUtils.fileExists(absolutePathFileXml)) {
					miseEnFormeResultLabel(new LinkedList<>(Arrays.asList(name.getText() + " existe déjà")));
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				// Nothing to do
			}
		});

		// Auteur
		JPanel authorPanel = new JPanel();
		authorPanel.setPreferredSize(new Dimension(200, 60));
		JLabel authorLabel = new JLabel("Auteur : ");
		author = new JTextField();
		author.setPreferredSize(new Dimension(150, 25));
		authorPanel.add(authorLabel);
		authorPanel.add(author);
		firstLine.add(authorPanel);

		// Date de creation
		JPanel datePanel = new JPanel();
		datePanel.setPreferredSize(new Dimension(200, 60));
		JLabel dateLabel = new JLabel("Date de création : ");
		date = new JTextField();
		date.setPreferredSize(new Dimension(150, 25));
		date.setEnabled(false);
		datePanel.add(dateLabel);
		datePanel.add(date);
		firstLine.add(datePanel);

		// Type
		JPanel typePanel = new JPanel();
		typePanel.setPreferredSize(new Dimension(180, 60));
		JLabel typeLabel = new JLabel("Type : ");
		type = new JComboBox<>();
		RecordType[] valuesType = RecordType.values();
		for (int i = 0; i < valuesType.length; i++) {
			type.addItem(valuesType[i]);
		}
		type.setPreferredSize(new Dimension(150, 25));
		typePanel.add(typeLabel);
		typePanel.add(type);
		firstLine.add(typePanel);

		// Categorie
		JPanel catPanel = new JPanel();
		catPanel.setPreferredSize(new Dimension(200, 60));
		JLabel catLabel = new JLabel("Catégorie : ");
		cat = new JComboBox<>();
		Cat[] values = Cat.values();
		for (int i = 0; i < values.length; i++) {
			cat.addItem(values[i]);
		}
		cat.setPreferredSize(new Dimension(150, 25));
		catPanel.add(catLabel);
		catPanel.add(cat);
		firstLine.add(catPanel);

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

		// Sort
		JPanel sortedPanel = new JPanel();
		sortedPanel.setPreferredSize(new Dimension(60, 60));
		JLabel sortedLabel = new JLabel("Classé : ");
		sorted = new JCheckBox();
		sorted.setPreferredSize(new Dimension(25, 25));
		sortedPanel.add(sortedLabel);
		sortedPanel.add(sorted);
		firstLine.add(sortedPanel);

		// Order/Artist
		JPanel orderPanel = new JPanel();
		orderPanel.setPreferredSize(new Dimension(100, 60));
		JLabel orderLabel = new JLabel("Artiste en premier: ");
		order = new JCheckBox();
		order.setSelected(true);
		order.setPreferredSize(new Dimension(25, 25));
		orderPanel.add(orderLabel);
		orderPanel.add(order);
		firstLine.add(orderPanel);

		this.add(firstLine);
		JPanel secondLine = new JPanel();

		// Publi
		JPanel publiPanel = new JPanel();
		publiPanel.setPreferredSize(new Dimension(200, 60));
		JLabel publiLabel = new JLabel("Année de publication : ");
		publi = new JTextField();
		publi.setPreferredSize(new Dimension(150, 25));
		publiPanel.add(publiLabel);
		publiPanel.add(publi);
		secondLine.add(publiPanel);

		// Taille
		JPanel sizePanel = new JPanel();
		sizePanel.setPreferredSize(new Dimension(200, 60));
		JLabel sizeLabel = new JLabel("Taille : ");
		size = new JTextField();
		size.setPreferredSize(new Dimension(180, 25));
		sizePanel.add(sizeLabel);
		sizePanel.add(size);
		secondLine.add(sizePanel);

		// Line
		JPanel linePanel = new JPanel();
		linePanel.setPreferredSize(new Dimension(400, 60));
		JLabel lineLabel = new JLabel("Ligne utilisée : ");
		line = new JTextField();
		line.setPreferredSize(new Dimension(350, 25));
		line.setEnabled(false);
		linePanel.add(lineLabel);
		linePanel.add(line);
		secondLine.add(linePanel);

		// separator
		JPanel separatorPanel = new JPanel();
		separatorPanel.setPreferredSize(new Dimension(100, 60));
		JLabel separatorLabel = new JLabel("Séparateur trouvé: ");
		separator = new JTextField();
		separator.setPreferredSize(new Dimension(20, 25));
		separatorPanel.add(separatorLabel);
		separatorPanel.add(separator);
		secondLine.add(separatorPanel);

		// firstLines
		JPanel firstLinesPanel = new JPanel();
		firstLinesPanel.setPreferredSize(new Dimension(340, 100));
		JLabel firstLinesLabel = new JLabel("3 premières lignes du fichier: ");
		firstL1 = new JTextField();
		firstL1.setPreferredSize(new Dimension(340, 20));
		firstL2 = new JTextField();
		firstL2.setPreferredSize(new Dimension(340, 20));
		firstL3 = new JTextField();
		firstL3.setPreferredSize(new Dimension(340, 20));
		firstLinesPanel.add(firstLinesLabel);
		firstLinesPanel.add(firstL1);
		firstLinesPanel.add(firstL2);
		firstLinesPanel.add(firstL3);
		secondLine.add(firstLinesPanel);

		this.add(secondLine);
		JPanel thirdLine = new JPanel();

		// lastLines
		JPanel lastLinesPanel = new JPanel();
		lastLinesPanel.setPreferredSize(new Dimension(340, 80));
		JLabel lastLinesLabel = new JLabel("2 dernières lignes du fichier: ");
		lastL1 = new JTextField();
		lastL1.setPreferredSize(new Dimension(340, 20));
		lastL2 = new JTextField();
		lastL2.setPreferredSize(new Dimension(340, 20));
		lastLinesPanel.add(lastLinesLabel);
		lastLinesPanel.add(lastL1);
		lastLinesPanel.add(lastL2);
		thirdLine.add(lastLinesPanel);

		// characterToRemove
		JPanel characterToRemovePanel = new JPanel();
		characterToRemovePanel.setPreferredSize(new Dimension(120, 60));
		JLabel characterToRemoveLabel = new JLabel("Caractères à supprimer: ");
		characterToRemove = new JTextField();
		characterToRemove.setPreferredSize(new Dimension(40, 20));
		characterToRemovePanel.add(characterToRemoveLabel);
		characterToRemovePanel.add(characterToRemove);
		thirdLine.add(characterToRemovePanel);
		
		// isBeforePanel
		JPanel isBeforePanel = new JPanel();
		isBeforePanel.setPreferredSize(new Dimension(100, 60));
		JLabel isBeforeLabel = new JLabel("Supprimer au début: ");
		isBefore = new JCheckBox();
		isBefore.setSelected(true);
		isBefore.setPreferredSize(new Dimension(20, 20));
		isBeforePanel.add(isBeforeLabel);
		isBeforePanel.add(isBefore);
		thirdLine.add(isBeforePanel);

		// reverseArtist
		JPanel reverseArtistPanel = new JPanel();
		reverseArtistPanel.setPreferredSize(new Dimension(100, 60));
		JLabel reverseArtistLabel = new JLabel("Retourner l'artiste: ");
		reverseArtist = new JCheckBox();
		reverseArtist.setPreferredSize(new Dimension(20, 20));
		reverseArtistPanel.add(reverseArtistLabel);
		reverseArtistPanel.add(reverseArtist);
		thirdLine.add(reverseArtistPanel);

		// removeParenthese
		JPanel removeParenthesePanel = new JPanel();
		removeParenthesePanel.setPreferredSize(new Dimension(220, 60));
		JLabel removeParentheseLabel = new JLabel("Supprimer le texte entre parenthèse du titre: ");
		removeParenthese = new JCheckBox();
		removeParenthese.setPreferredSize(new Dimension(20, 20));
		removeParenthesePanel.add(removeParentheseLabel);
		removeParenthesePanel.add(removeParenthese);
		thirdLine.add(removeParenthesePanel);

		// upper
		JPanel upperPanel = new JPanel();
		upperPanel.setPreferredSize(new Dimension(200, 60));
		JLabel upperLabel = new JLabel("Pas de séparateur, artiste en capitale: ");
		upper = new JCheckBox();
		upper.setPreferredSize(new Dimension(20, 20));
		upperPanel.add(upperLabel);
		upperPanel.add(upper);
		thirdLine.add(upperPanel);

		// removeAfter
		JPanel removeAfterPanel = new JPanel();
		removeAfterPanel.setPreferredSize(new Dimension(200, 60));
		JLabel removeAfterLabel = new JLabel("Supprime après le dernier séparateur: ");
		removeAfter = new JCheckBox();
		removeAfter.setPreferredSize(new Dimension(20, 20));
		removeAfterPanel.add(removeAfterLabel);
		removeAfterPanel.add(removeAfter);
		thirdLine.add(removeAfterPanel);

		this.add(thirdLine);
		JPanel fourthLine = new JPanel(new GridLayout(0, 1));

		FocusListener selectAll = new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				// Nothing to do
			}

			@Override
			public void focusGained(FocusEvent e) {
				JTextField source = (JTextField) e.getSource();
				source.selectAll();
			}
		};
		rangeB.addFocusListener(selectAll);
		rangeE.addFocusListener(selectAll);
		publi.addFocusListener(selectAll);
		size.addFocusListener(selectAll);
		separator.addFocusListener(selectAll);

		// result
		JPanel resultPanel = new JPanel(new BorderLayout());
		resultLabel = new JTextArea();
		resultLabel.setWrapStyleWord(true);
		resultLabel.setLineWrap(true);
		resultLabel.setOpaque(false);
		resultLabel.setEditable(false);
		resultLabel.setBackground(UIManager.getColor("Label.background"));
		resultLabel.setFont(UIManager.getFont("Label.font"));
		resultLabel.setBorder(UIManager.getBorder("Label.border"));
		resultPanel.add(new JScrollPane(resultLabel), BorderLayout.CENTER);
		fourthLine.add(resultPanel);

		this.add(fourthLine);

		// Insert les boutons du bas
		insertBottomPanel(artist);

		LOG.debug("End ImportPanel");
	}

	/**
	 * Ajoute les boutons du bas de l'écran.
	 * 
	 * @param artist l'onglet artist
	 */
	private void insertBottomPanel(final ArtistPanel artist) {
		JPanel bottom = new JPanel();
		JButton importFile = new JButton("Importer le fichier");
		importFile.setToolTipText("Importe au format XML le fichier chargé précédemment avec les critères renseignés.");
		importFile.addActionListener((ActionEvent arg0) -> importFileAction());
		bottom.add(importFile);

		JButton cleanFile = new JButton("Nettoyer le fichier");
		cleanFile.setToolTipText("Supprime les lignes qui ne contiennent pas le séparateur. Supprime également les charactères à supprimer.");
		cleanFile.addActionListener((ActionEvent arg0) -> cleanFileAction());
		bottom.add(cleanFile);

		JButton fusionFile = new JButton("Fusionner tous les fichiers");
		fusionFile.setToolTipText("Aggrège tous les fichiers XML importés dans le fichier final.");
		fusionFile.addActionListener((ActionEvent arg0) -> {
			try {
				fusionFilesAction(artist);
			} catch (InterruptedException e) {
				LOG.error("Erreur lors de la fusion des fichiers XML", e);
				Thread.currentThread().interrupt();
			}
		});
		bottom.add(fusionFile);

		// Ouvre le fichier d'entrée dans notepad
		JButton openFile = new JButton("Éditer le fichier source");
		openFile.setToolTipText("Ouvre le fichier chargé dans Notepad++");
		openFile.addActionListener((ActionEvent arg0) -> openFileNotepadAction());
		bottom.add(openFile);

		// Ouvre le fichier xml dans notepad
		JButton openXml = new JButton("Éditer le fichier xml");
		openXml.setToolTipText("Ouvre le fichier XML généré dans Notepad++");
		openXml.addActionListener((ActionEvent arg0) -> openXmlNotepadAction());
		bottom.add(openXml);

		// Clean history
		JButton cleanHistory = new JButton("Nettoyer le dossier d'historique");
		cleanHistory.setToolTipText("Supprime tous les fichiers du dossier d'historique sauf le plus récent.");
		cleanHistory.addActionListener((ActionEvent arg0) -> {
			result = new LinkedList<>(Arrays.asList("Dossier historique nettoyé"));
			try {
				FichierUtils.cleanHistory();
			} catch (IOException e) {
				LOG.error("Erreur lors de cleanHistory", e);
				result = new LinkedList<>(Arrays.asList(e.toString()));
			}
			miseEnFormeResultLabel(result);
		});
		bottom.add(cleanHistory);

		bottom.setBorder(BorderFactory.createTitledBorder(""));
		this.add(bottom);
	}

	/**
	 * Ajoute les boutons en haut de l'écran.
	 */
	private void insertTopPanel() {
		JPanel top = new JPanel();
		JButton browse = new JButton("Parcourir");
		browse.setBackground(Color.white);
		browse.setPreferredSize(new Dimension(220, 60));
		browse.setToolTipText("Charge un fichier texte contenant des musiques.");
		browse.addActionListener((ActionEvent arg0) -> {
			LOG.debug("Start browse");
			file = addBrowsingFile("txt", explorePath);
			if (file != null) {
				loadFile();
			}
			LOG.debug("End browse");
		});
		top.add(browse);

		// Reset
		JButton cleanBtn = new JButton("Reset");
		cleanBtn.setBackground(Color.white);
		cleanBtn.setPreferredSize(new Dimension(220, 60));
		cleanBtn.setToolTipText("Remet à zéro tous les champs.");
		cleanBtn.addActionListener((ActionEvent arg0) -> resetAll());
		top.add(cleanBtn);

		// Reload
		JButton reloadBtn = new JButton("Reload");
		reloadBtn.setBackground(Color.white);
		reloadBtn.setPreferredSize(new Dimension(220, 60));
		reloadBtn.setToolTipText("Relance le chargement du fichier chargé précédemment. Utile si il a été modifié entre temps.");
		reloadBtn.addActionListener((ActionEvent arg0) -> loadFile());
		top.add(reloadBtn);

		JButton open = new JButton("Charger un fichier XML");
		open.setBackground(Color.white);
		open.setPreferredSize(new Dimension(220, 60));
		open.setToolTipText("Au lieu de charger un fichier texte, charge un xml.");
		open.addActionListener((ActionEvent arg0) -> {
			LOG.debug("Start open");
			xmlFile = addBrowsingFile("xml", Constant.XML_PATH);
			if (xmlFile != null) {
				absolutePathFileXml = xmlFile.getAbsolutePath();
			}
			LOG.debug("End open");
		});
		top.add(open);

		top.setBorder(BorderFactory.createTitledBorder(""));
		this.add(top);
	}

	/**
	 * Le traitement lorsqu'on importe un fichier txt.
	 */
	private void importFileAction() {
		LOG.debug("Start importFile");
		result = new LinkedList<>(Arrays.asList("Sélectionnez un fichier"));
		if (fichier != null) {
			fichier.setSorted(sorted.isSelected());
			fichier.setFileName(name.getText());
			fichier.setAuthor(author.getText());
			fichier.setCategorie((Cat) cat.getSelectedItem());
			fichier.setPublishYear(Integer.parseInt(publi.getText()));
			fichier.setRangeDateBegin(Integer.parseInt(rangeB.getText()));
			fichier.setRangeDateEnd(Integer.parseInt(rangeE.getText()));
			fichier.setSize(Integer.parseInt(size.getText()));
			result = new LinkedList<>(Arrays.asList(name.getText() + " OK !"));

			try {
				List<Composition> compoList = ImportFile.getCompositionsFromFile(new File(absolutePathFileTxt), fichier, (RecordType) type.getSelectedItem(),
						separator.getText(), result, order.isSelected(), reverseArtist.isSelected(), removeParenthese.isSelected(), upper.isSelected(),
						removeAfter.isSelected());
				ExportXML.exportXML(compoList, name.getText());
				absolutePathFileXml = Constant.XML_PATH + name.getText() + Constant.XML_EXTENSION;
			} catch (IOException | MyException e) {
				LOG.error("Erreur lors de l'import du fichier: " + absolutePathFileTxt, e);
				result = new LinkedList<>(Arrays.asList(e.toString()));
			}
		}
		miseEnFormeResultLabel(result);
		LOG.debug("End importFile");
	}

	/**
	 * Mise en forme des messages pour l'afficher dans la zone texte result.
	 * 
	 * @param result2 liste de texte à afficher
	 */
	private void miseEnFormeResultLabel(List<String> result2) {
		LOG.debug("Start miseEnFormeResultLabel");
		StringBuilder s = new StringBuilder();
		for (String string : result2) {
			s.append(string).append(Constant.NEW_LINE);
		}
		resultLabel.setText(s.toString());
		resultLabel.setForeground(new Color(243, 16, 16));
		Font labelFont = resultLabel.getFont();
		resultLabel.setFont(new Font(labelFont.getName(), labelFont.getStyle(), 20));
		LOG.debug("End miseEnFormeResultLabel");
	}

	/**
	 * Crée un file chooser pour sélectionner un fichier selon l'extension
	 * donnée et à l'endroit donnée.
	 * 
	 * @param extension le filtre sur les extensions
	 * @param dir à quel endroit le file chooser s'ouvre
	 * @return le fichier choisit
	 */
	private File addBrowsingFile(String extension, String dir) {
		LOG.debug("Start addBrowsingFile");
		LOG.debug(dir);
		JFileChooser jfile = new JFileChooser(dir);
		jfile.setApproveButtonText("Ouvrir");
		FileNameExtensionFilter filter = new FileNameExtensionFilter(extension, extension);
		jfile.setFileFilter(filter);
		if (jfile.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			resetAll();
			LOG.debug("End addBrowsingFile");
			return jfile.getSelectedFile();
		} else {
			LOG.debug("End addBrowsingFile, no file choose");
			return null;
		}
	}

	/**
	 * Remet à zéro tous les champs de l'écran.
	 */
	private void resetAll() {
		LOG.debug("Start resetAll");
		explorePath = Constant.MUSIC_ABS_DIRECTORY;
		absolutePathFileTxt = "";
		fichier = null;
		absolutePathFileXml = "";
		file = null;
		name.setText("");
		author.setText("");
		date.setText("");
		type.setSelectedItem(null);
		cat.setSelectedItem(null);
		rangeB.setText("");
		rangeE.setText("");
		sorted.setSelected(false);
		order.setSelected(true);
		publi.setText("");
		size.setText("");
		line.setText("");
		firstL1.setText("");
		firstL2.setText("");
		firstL3.setText("");
		lastL1.setText("");
		lastL2.setText("");
		separator.setText("");
		characterToRemove.setText("");
		isBefore.setSelected(true);
		reverseArtist.setSelected(false);
		removeParenthese.setSelected(false);
		upper.setSelected(false);
		removeAfter.setSelected(false);
		miseEnFormeResultLabel(new ArrayList<String>());
		LOG.debug("End resetAll");
	}

	/**
	 * Traitement lorsqu'on séléctionne un fichier. Les paramètres d'import sont
	 * déduit du nom du fichier et du contenu.
	 */
	private void loadFile() {
		LOG.debug("Start loadFile");
		explorePath = StringUtils.substring(file.getAbsolutePath(), 0, file.getAbsolutePath().lastIndexOf(File.separator));
		absolutePathFileTxt = file.getAbsolutePath();
		fichier = ImportFile.convertOneFile(file);
		List<String> randomLineAndLastLines = ImportFile.randomLineAndLastLines(file);
		fichier.setSorted(ImportFile.isSorted(randomLineAndLastLines.get(3)));
		fichier.setSize(ImportFile.determineSize(fichier, randomLineAndLastLines, file.getAbsolutePath()));
		absolutePathFileXml = Constant.XML_PATH + fichier.getFileName() + Constant.XML_EXTENSION;
		if (FileUtils.fileExists(absolutePathFileXml)) {
			miseEnFormeResultLabel(new LinkedList<>(Arrays.asList(fichier.getFileName() + " a déjà été importé")));
		}
		determineType = ImportFile.determineType(file.getName());
		boolean rangeDatesZero = fichier.getRangeDateBegin() == 0 && fichier.getRangeDateEnd() == 0;
		if (Cat.MISCELLANEOUS.equals(fichier.getCategorie()) && !RecordType.UNKNOWN.equals(determineType) && fichier.getPublishYear() != 0 && rangeDatesZero) {
			fichier.setCategorie(Cat.YEAR);
			fichier.setRangeDateBegin(fichier.getPublishYear());
			fichier.setRangeDateEnd(fichier.getPublishYear());
		}
		name.setText(fichier.getFileName());
		author.setText(fichier.getAuthor());
		date.setText(new Constant().getSdfDttm().format(fichier.getCreationDate()));
		cat.setSelectedItem(fichier.getCategorie());
		publi.setText(String.valueOf(fichier.getPublishYear()));
		type.setSelectedItem(determineType);
		rangeB.setText(String.valueOf(fichier.getRangeDateBegin()));
		rangeE.setText(String.valueOf(fichier.getRangeDateEnd()));
		sorted.setSelected(fichier.getSorted());
		size.setText(String.valueOf(fichier.getSize()));
		firstL1.setText(randomLineAndLastLines.get(0));
		firstL2.setText(randomLineAndLastLines.get(1));
		firstL3.setText(randomLineAndLastLines.get(2));
		line.setText(randomLineAndLastLines.get(3));
		lastL1.setText(randomLineAndLastLines.get(4));
		lastL2.setText(randomLineAndLastLines.get(5));
		separator.setText(ImportFile.getSeparator(randomLineAndLastLines.get(3)));
		LOG.debug("End loadFile");
	}

	/**
	 * Traitement lorsqu'on fusionne tous les fichiers xml.
	 * 
	 * @param artist l'onglet artist
	 * @throws InterruptedException
	 */
	private void fusionFilesAction(final ArtistPanel artist) throws InterruptedException {
		new Thread(() -> {
			LOG.debug("Start fusionFilesAction");
			result = new LinkedList<>(Arrays.asList("Fichiers fusionnés"));
			try {
				ImportXML.fusionFiles(Constant.XML_PATH, resultLabel);
			} catch (IOException e) {
				LOG.error("Erreur lors de la fusion de tous les fichiers xml", e);
				result = new LinkedList<>(Arrays.asList(e.toString()));
			}

			SwingUtilities.invokeLater(() -> {
				artist.updateArtistPanel();
				miseEnFormeResultLabel(result);
			});
			LOG.debug("End fusionFilesAction");
		}).start();
	}

	/**
	 * Pour ouvrir le fichier txt séléctionné dans notepad.
	 */
	private void openFileNotepadAction() {
		LOG.debug("Start openFile");
		if (StringUtils.isNotBlank(absolutePathFileTxt)) {
			try {
				if (FileUtils.fileExists(absolutePathFileTxt)) {
					Runtime.getRuntime().exec(Constant.NOTEPAD_PATH + absolutePathFileTxt);
				}
			} catch (IOException e) {
				LOG.error("Erreur lors de l'ouverture du fichier txt: " + absolutePathFileTxt, e);
				result = new LinkedList<>(Arrays.asList(e.toString()));
				miseEnFormeResultLabel(result);
			}
		}
		LOG.debug("End openFile");
	}

	/**
	 * Pour ouvrir le fichier xml nouvellement crée dans notepad.
	 */
	private void openXmlNotepadAction() {
		LOG.debug("Start openXml");
		if (StringUtils.isNotBlank(absolutePathFileXml)) {
			try {
				LOG.debug(absolutePathFileXml);
				if (FileUtils.fileExists(absolutePathFileXml)) {
					Runtime.getRuntime().exec(Constant.NOTEPAD_PATH + absolutePathFileXml);
				}
			} catch (IOException e) {
				LOG.error("Erreur lors de l'ouverture du fichier xml: " + absolutePathFileXml, e);
				result = new LinkedList<>(Arrays.asList(e.toString()));
				miseEnFormeResultLabel(result);
			}
		}
		LOG.debug("End openXml");
	}

	/**
	 * Traitment lorsqu'on veut nettoyer un fichier txt.
	 */
	private void cleanFileAction() {
		LOG.debug("Start cleanFile");
		result = new LinkedList<>(Arrays.asList("Sélectionnez un fichier"));
		if (file != null) {
			result = new LinkedList<>(Arrays.asList(file.getName() + " nettoyé !"));
			try {
				CleanFile.clearFile(file, sorted.isSelected(), separator.getText(), characterToRemove.getText(), isBefore.isSelected());
			} catch (IOException e) {
				LOG.error("Erreur lors du nettoyage du fichier: " + file.getAbsolutePath(), e);
				result = new LinkedList<>(Arrays.asList(e.toString()));
			}
		}
		miseEnFormeResultLabel(result);
		LOG.debug("End cleanFile");
	}

}
