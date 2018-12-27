/**
 * 
 */
package pmb.music.AllMusic.view.panel;

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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

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
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.view.PanelUtils;

/**
 * Onglet d'import de fichiers txt.
 * 
 * @author pmbroca
 */
public class ImportPanel extends JPanel {

	private static final long serialVersionUID = 5796304304079887263L;

	private static final Logger LOG = Logger.getLogger(ImportPanel.class);

	private JTextField author;
	private JTextField publi;
	private JTextField rangeB;
	private JTextField rangeE;
	private JTextField size;
	private JTextField name;
	private JTextField date;
	/**
	 * Random line.
	 */
	private JTextField line;
	private JTextField separator;
	private JTextField firstL1;
	private JTextField firstL2;
	private JTextField firstL3;
	private JTextField lastL1;
	private JTextField lastL2;
	/**
	 * String to remove from import file.
	 */
	private JTextField characterToRemove;
	/**
	 * Maximum length of a line when cleaning.
	 */
	private JTextField maxLengthClean;

	private JTextArea resultLabel = new JTextArea();

	private File file;
	private File xmlFile;

	private JComboBox<Cat> cat;

	private JComboBox<RecordType> type;

	private Fichier fichier;

	private String absolutePathFileTxt;
	private String absolutePathFileXml;

	private JCheckBox sorted;
	/**
	 * Si l'artist est en 1er, puis le titre.
	 */
	private JCheckBox order;
	/**
	 * Si le nom et prénom de l'artist sont inversés. Ex: Young, Neil.
	 */
	private JCheckBox reverseArtist;
	/**
	 * Doit on supprimer lors de l'import des parenthèses.
	 */
	private JCheckBox removeParenthese;
	/**
	 * Pas de séparateur mais artist en majuscule ?
	 */
	private JCheckBox upper;
	/**
	 * 2 séparateurs, suppression du dernier.
	 */
	private JCheckBox removeAfter;
	/**
	 * Si le characterToRemove est à supprimer au debut ou à la fin de la ligne.
	 */
	private JCheckBox isBefore;
	/**
	 * Utilise-t-on le dossier du fichier à mettre en forme.
	 */
	private JCheckBox isCompleteDirectory;

	private RecordType determineType;

	/**
	 * Path of the last opened file.
	 */
	private String explorePath;

	/**
	 * Content for the result area.
	 */
	private List<String> result = new LinkedList<>();

	/**
	 * Button that launchs the import of the current loaded file.
	 */
	private JButton importFile;

	private FichierPanel fichierPanel;
	private ArtistPanel artistPanel;

	/**
	 * Import params constants.
	 */
	public static final String IMPORT_PARAM_SEPARATOR = "separator";
	public static final String IMPORT_PARAM_ARTIST_FIRST = "artistFirst";
	public static final String IMPORT_PARAM_REVERSE_ARTIST = "reverseArtist";
	public static final String IMPORT_PARAM_PARENTHESE = "parenthese";
	public static final String IMPORT_PARAM_UPPER = "upper";
	public static final String IMPORT_PARAM_REMOVE_AFTER = "removeAfter";
	public static final String IMPORT_PARAM_NAME = "name";
	public static final String IMPORT_PARAM_AUTEUR = "auteur";
	public static final String IMPORT_PARAM_CREATE = "create";
	public static final String IMPORT_PARAM_RECORD_TYPE = "type";
	public static final String IMPORT_PARAM_CATEGORIE = "cat";
	public static final String IMPORT_PARAM_RANGE_BEGIN = "rangeB";
	public static final String IMPORT_PARAM_RANGE_END = "rangeE";
	public static final String IMPORT_PARAM_SORTED = "sorted";
	public static final String IMPORT_PARAM_PUBLISH_YEAR = "publish";
	public static final String IMPORT_PARAM_SIZE = "size";

	/**
	 * Construit l'onglet import.
	 * 
	 * @param artistPanel l'onglet artiste
	 * @param fichierPanel
	 */
	public ImportPanel(ArtistPanel artistPanel, FichierPanel fichierPanel) {
		super();
		LOG.debug("Start ImportPanel");
		this.artistPanel = artistPanel;
		this.fichierPanel = fichierPanel;
		explorePath = Constant.getMusicAbsDirectory();
		this.setLayout(new GridLayout(6, 1));

		// Insert les boutons du hauts
		insertTopPanel();
		// Insert tous les inputs
		insertInputs();
		// Insert les boutons du bas
		insertBottomPanel();

		LOG.debug("End ImportPanel");
	}

	/**
	 * Ajoute les boutons en haut de l'écran.
	 */
	private void insertTopPanel() {
		JPanel top = new JPanel();
		JButton browse = PanelUtils.createJButton("Parcourir", 220, Constant.ICON_FOLDER);
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
		JButton cleanBtn = PanelUtils.createJButton("Reset", 220, Constant.ICON_ERASE);
		cleanBtn.setToolTipText("Remet à zéro tous les champs.");
		cleanBtn.addActionListener((ActionEvent arg0) -> resetAll());
		top.add(cleanBtn);

		// Reload
		JButton reloadBtn = PanelUtils.createJButton("Reload", 220, Constant.ICON_REFRESH);
		reloadBtn.setToolTipText(
				"Relance le chargement du fichier chargé précédemment. Utile si il a été modifié entre temps.");
		reloadBtn.addActionListener((ActionEvent arg0) -> loadFile());
		top.add(reloadBtn);

		// Open Xml file
		JButton open = PanelUtils.createJButton("Charger un fichier XML", 220, Constant.ICON_FILE);
		open.setToolTipText("Au lieu de charger un fichier texte, charge un xml.");
		open.addActionListener((ActionEvent arg0) -> {
			LOG.debug("Start open");
			xmlFile = addBrowsingFile("xml", Constant.getXmlPath());
			if (xmlFile != null) {
				absolutePathFileXml = xmlFile.getAbsolutePath();
			}
			LOG.debug("End open");
		});
		top.add(open);

		top.setBorder(BorderFactory.createTitledBorder(""));
		this.add(top);
	}

	private void insertInputs() {
		insertFirstLine();
		insertSecondLine();
		insertThirdLine();
		insertResultPanel();
	}

	private void insertFirstLine() {
		JPanel firstLine = new JPanel();

		// Nom du fichier
		JPanel namePanel = new JPanel();
		namePanel.setPreferredSize(new Dimension(360, PanelUtils.PANEL_HEIGHT));
		JLabel nameLabel = new JLabel("Nom du fichier : ");
		name = new JTextField();
		name.setPreferredSize(new Dimension(350, PanelUtils.COMPONENT_HEIGHT));
		namePanel.add(nameLabel);
		namePanel.add(name);
		firstLine.add(namePanel);

		name.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				absolutePathFileXml = Constant.getXmlPath() + name.getText() + Constant.XML_EXTENSION;
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
		authorPanel.setPreferredSize(new Dimension(160, PanelUtils.PANEL_HEIGHT));
		JLabel authorLabel = new JLabel("Auteur : ");
		author = new JTextField();
		author.setPreferredSize(new Dimension(150, PanelUtils.COMPONENT_HEIGHT));
		authorPanel.add(authorLabel);
		authorPanel.add(author);
		firstLine.add(authorPanel);

		// Date de creation
		JPanel datePanel = new JPanel();
		datePanel.setPreferredSize(new Dimension(160, PanelUtils.PANEL_HEIGHT));
		JLabel dateLabel = new JLabel("Date de création : ");
		date = new JTextField();
		date.setPreferredSize(new Dimension(150, PanelUtils.COMPONENT_HEIGHT));
		date.setEnabled(false);
		datePanel.add(dateLabel);
		datePanel.add(date);
		firstLine.add(datePanel);

		// Type
		JPanel typePanel = new JPanel();
		typePanel.setPreferredSize(new Dimension(110, PanelUtils.PANEL_HEIGHT));
		JLabel typeLabel = new JLabel("Type : ");
		type = new JComboBox<>();
		RecordType[] valuesType = RecordType.values();
		for (int i = 0; i < valuesType.length; i++) {
			type.addItem(valuesType[i]);
		}
		type.setPreferredSize(new Dimension(100, PanelUtils.COMPONENT_HEIGHT));
		typePanel.add(typeLabel);
		typePanel.add(type);
		firstLine.add(typePanel);

		// Categorie
		JPanel catPanel = new JPanel();
		catPanel.setPreferredSize(new Dimension(130, PanelUtils.PANEL_HEIGHT));
		JLabel catLabel = new JLabel("Catégorie : ");
		cat = new JComboBox<>();
		Cat[] values = Cat.values();
		for (int i = 0; i < values.length; i++) {
			cat.addItem(values[i]);
		}
		cat.setPreferredSize(new Dimension(120, PanelUtils.COMPONENT_HEIGHT));
		catPanel.add(catLabel);
		catPanel.add(cat);
		firstLine.add(catPanel);

		// Range
		JPanel rangePanel = new JPanel();
		PanelUtils.setSize(rangePanel, 300, PanelUtils.PANEL_HEIGHT);
		JLabel rangeLabel = PanelUtils.createJLabel("Année(s) du classement : ", 300);
		rangeB = new JTextField();
		rangeE = new JTextField();
		rangeB.setPreferredSize(new Dimension(100, PanelUtils.COMPONENT_HEIGHT));
		rangeE.setPreferredSize(new Dimension(100, PanelUtils.COMPONENT_HEIGHT));
		rangeB.addFocusListener(PanelUtils.selectAll);
		rangeE.addFocusListener(PanelUtils.selectAll);
		rangePanel.add(rangeLabel);
		rangePanel.add(rangeB);
		rangePanel.add(rangeE);
		firstLine.add(rangePanel);

		// Sort
		JPanel sortedPanel = new JPanel();
		sortedPanel.setPreferredSize(new Dimension(60, PanelUtils.PANEL_HEIGHT));
		JLabel sortedLabel = new JLabel("Classé : ");
		sorted = new JCheckBox();
		sorted.setPreferredSize(new Dimension(25, PanelUtils.COMPONENT_HEIGHT));
		sortedPanel.add(sortedLabel);
		sortedPanel.add(sorted);
		firstLine.add(sortedPanel);

		// Order/Artist
		JPanel orderPanel = new JPanel();
		orderPanel.setPreferredSize(new Dimension(100, PanelUtils.PANEL_HEIGHT));
		JLabel orderLabel = new JLabel("Artiste en premier: ");
		order = new JCheckBox();
		order.setSelected(true);
		order.setPreferredSize(new Dimension(25, PanelUtils.COMPONENT_HEIGHT));
		orderPanel.add(orderLabel);
		orderPanel.add(order);
		firstLine.add(orderPanel);

		this.add(firstLine);
	}

	private void insertSecondLine() {
		JPanel secondLine = new JPanel();

		// Publi
		JPanel publiPanel = new JPanel();
		publiPanel.setPreferredSize(new Dimension(160, PanelUtils.PANEL_HEIGHT));
		JLabel publiLabel = new JLabel("Année de publication : ");
		publi = new JTextField();
		publi.setPreferredSize(new Dimension(150, PanelUtils.COMPONENT_HEIGHT));
		publi.addFocusListener(PanelUtils.selectAll);
		publiPanel.add(publiLabel);
		publiPanel.add(publi);
		secondLine.add(publiPanel);

		// Taille
		JPanel sizePanel = new JPanel();
		sizePanel.setPreferredSize(new Dimension(200, PanelUtils.PANEL_HEIGHT));
		JLabel sizeLabel = new JLabel("Taille : ");
		size = new JTextField();
		size.setPreferredSize(new Dimension(180, PanelUtils.COMPONENT_HEIGHT));
		size.addFocusListener(PanelUtils.selectAll);
		sizePanel.add(sizeLabel);
		sizePanel.add(size);
		secondLine.add(sizePanel);

		// Line
		JPanel linePanel = new JPanel();
		linePanel.setPreferredSize(new Dimension(400, PanelUtils.PANEL_HEIGHT));
		JLabel lineLabel = new JLabel("Ligne utilisée : ");
		line = new JTextField();
		line.setPreferredSize(new Dimension(350, PanelUtils.COMPONENT_HEIGHT));
		line.setEnabled(false);
		linePanel.add(lineLabel);
		linePanel.add(line);
		secondLine.add(linePanel);

		// separator
		JPanel separatorPanel = new JPanel();
		separatorPanel.setPreferredSize(new Dimension(100, PanelUtils.PANEL_HEIGHT));
		JLabel separatorLabel = new JLabel("Séparateur trouvé: ");
		separator = new JTextField();
		separator.setPreferredSize(new Dimension(20, PanelUtils.COMPONENT_HEIGHT));
		separator.addFocusListener(PanelUtils.selectAll);
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
	}

	private void insertThirdLine() {
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

		// reverseArtist
		JPanel reverseArtistPanel = new JPanel();
		reverseArtistPanel.setPreferredSize(new Dimension(100, PanelUtils.PANEL_HEIGHT));
		JLabel reverseArtistLabel = PanelUtils.createJLabel("<html>Retourner l'artiste: </html>", 100);
		reverseArtist = new JCheckBox();
		reverseArtist.setPreferredSize(new Dimension(20, 20));
		reverseArtistPanel.add(reverseArtistLabel);
		reverseArtistPanel.add(reverseArtist);
		thirdLine.add(reverseArtistPanel);

		// removeParenthese
		JPanel removeParenthesePanel = new JPanel();
		removeParenthesePanel.setPreferredSize(new Dimension(150, PanelUtils.PANEL_HEIGHT));
		JLabel removeParentheseLabel = PanelUtils
				.createJLabel("<html>Supprimer le texte entre parenthèse du titre: </html>", 150);
		removeParenthese = new JCheckBox();
		removeParenthese.setPreferredSize(new Dimension(20, 20));
		removeParenthesePanel.add(removeParentheseLabel);
		removeParenthesePanel.add(removeParenthese);
		thirdLine.add(removeParenthesePanel);

		// upper
		JPanel upperPanel = new JPanel();
		upperPanel.setPreferredSize(new Dimension(100, PanelUtils.PANEL_HEIGHT));
		JLabel upperLabel = PanelUtils.createJLabel("<html>Pas de séparateur, artiste en capitale: </html>", 100);
		upper = new JCheckBox();
		upper.setPreferredSize(new Dimension(20, 20));
		upperPanel.add(upperLabel);
		upperPanel.add(upper);
		thirdLine.add(upperPanel);

		// removeAfter
		JPanel removeAfterPanel = new JPanel();
		removeAfterPanel.setPreferredSize(new Dimension(100, PanelUtils.PANEL_HEIGHT));
		JLabel removeAfterLabel = PanelUtils.createJLabel("<html>Supprime après le dernier séparateur: </html>", 100);
		removeAfter = new JCheckBox();
		removeAfter.setPreferredSize(new Dimension(20, 20));
		removeAfterPanel.add(removeAfterLabel);
		removeAfterPanel.add(removeAfter);
		thirdLine.add(removeAfterPanel);

		// isCompleteDirectory
		JPanel isCompleteDirectoryPanel = new JPanel();
		isCompleteDirectoryPanel.setPreferredSize(new Dimension(150, PanelUtils.PANEL_HEIGHT));
		JLabel isCompleteDirectoryLabel = PanelUtils
				.createJLabel("<html>Utiliser le dossier du fichier pour la mise en forme: </html>", 150);
		isCompleteDirectory = new JCheckBox();
		isCompleteDirectory.setPreferredSize(new Dimension(20, 20));
		isCompleteDirectoryPanel.add(isCompleteDirectoryLabel);
		isCompleteDirectoryPanel.add(isCompleteDirectory);
		thirdLine.add(isCompleteDirectoryPanel);

		// Clean Params
		JPanel cleanBtnPanel = new JPanel();
		cleanBtnPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				"Paramètres de nettoyage: ", TitledBorder.LEFT, TitledBorder.ABOVE_TOP));
		// characterToRemove
		JPanel characterToRemovePanel = new JPanel();
		characterToRemovePanel.setPreferredSize(new Dimension(120, PanelUtils.PANEL_HEIGHT));
		JLabel characterToRemoveLabel = new JLabel("Caractères à supprimer: ");
		characterToRemove = new JTextField();
		characterToRemove.setPreferredSize(new Dimension(40, 20));
		characterToRemove.addFocusListener(PanelUtils.selectAll);
		characterToRemovePanel.add(characterToRemoveLabel);
		characterToRemovePanel.add(characterToRemove);
		cleanBtnPanel.add(characterToRemovePanel);
		// maxLengthClean
		JPanel maxLengthCleanPanel = new JPanel();
		maxLengthCleanPanel.setPreferredSize(new Dimension(120, PanelUtils.PANEL_HEIGHT));
		JLabel maxLengthCleanLabel = PanelUtils.createJLabel(
				"<html><body style='width: 100%'>Longueur maximale d'une ligne valide: </body></html>", 100);
		maxLengthClean = new JTextField("120");
		maxLengthClean.setPreferredSize(new Dimension(40, 20));
		maxLengthClean.addFocusListener(PanelUtils.selectAll);
		maxLengthCleanPanel.add(maxLengthCleanLabel);
		maxLengthCleanPanel.add(maxLengthClean);
		cleanBtnPanel.add(maxLengthCleanPanel);
		// isBefore
		JPanel isBeforePanel = new JPanel();
		isBeforePanel.setPreferredSize(new Dimension(100, PanelUtils.PANEL_HEIGHT));
		JLabel isBeforeLabel = new JLabel("Supprimer au début: ");
		isBefore = new JCheckBox();
		isBefore.setSelected(true);
		isBefore.setPreferredSize(new Dimension(20, 20));
		isBeforePanel.add(isBeforeLabel);
		isBeforePanel.add(isBefore);
		cleanBtnPanel.add(isBeforePanel);
		thirdLine.add(cleanBtnPanel);

		this.add(thirdLine);
	}

	private void insertResultPanel() {
		JPanel fourthLine = new JPanel(new GridLayout(0, 1));

		// result
		JPanel resultPanel = new JPanel(new BorderLayout());
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
	}

	/**
	 * Ajoute les boutons du bas de l'écran.
	 */
	private void insertBottomPanel() {
		JPanel bottom = new JPanel();

		// Import
		importFile = PanelUtils.createJButton("Importer le fichier", 200, Constant.ICON_UPLOAD);
		importFile.setToolTipText("Importe au format XML le fichier chargé précédemment avec les critères renseignés.");
		importFile.addActionListener((ActionEvent arg0) -> importFileAction());
		bottom.add(importFile);

		// Clean
		JButton cleanFile = PanelUtils.createJButton("Nettoyer le fichier", 200, Constant.ICON_CLEAN);
		cleanFile.setToolTipText(
				"Supprime les lignes qui ne contiennent pas le séparateur. Supprime également les charactères à supprimer.");
		cleanFile.addActionListener((ActionEvent arg0) -> cleanFileAction());
		bottom.add(cleanFile);

		// Mise en forme
		JButton mef = PanelUtils.createJButton("Mettre en forme un fichier ou dossier", 250, Constant.ICON_ALIGN);
		mef.setToolTipText("Pour supprimer les diacritiques et remplacer des charactères spéciaux.");
		mef.addActionListener((ActionEvent arg0) -> {
			result = new LinkedList<>(
					Arrays.asList((isCompleteDirectory.isSelected() ? "Dossier" : "Fichier") + " mis en forme:"));
			CleanFile.miseEnForme(file, isCompleteDirectory.isSelected(), result);
			if (result.size() > 1) {
				miseEnFormeResultLabel(result);
			} else {
				miseEnFormeResultLabel(new LinkedList<>(Arrays.asList("Rien à mettre en forme")));
			}
		});
		bottom.add(mef);

		// Fusion
		JButton fusionFile = PanelUtils.createJButton("Fusionner tous les fichiers", 200, Constant.ICON_FUSION);
		fusionFile.setToolTipText("Aggrège tous les fichiers XML importés dans le fichier final.");
		fusionFile.addActionListener((ActionEvent arg0) -> {
			try {
				fusionFilesAction();
			} catch (InterruptedException e) {
				LOG.error("Erreur lors de la fusion des fichiers XML", e);
				Thread.currentThread().interrupt();
			}
		});
		bottom.add(fusionFile);

		// Ouvre le fichier d'entrée dans notepad
		JButton openFile = PanelUtils.createJButton("Ouvrir le fichier source", 200, Constant.ICON_TXT_FILE);
		openFile.setToolTipText("Ouvre le fichier chargé dans Notepad++");
		openFile.addActionListener((ActionEvent arg0) -> openFileNotepad(absolutePathFileTxt));
		bottom.add(openFile);

		// Ouvre le fichier xml dans notepad
		JButton openXml = PanelUtils.createJButton("Ouvrir le fichier xml", 200, Constant.ICON_XML_FILE);
		openXml.setToolTipText("Ouvre le fichier XML généré dans Notepad++");
		openXml.addActionListener((ActionEvent arg0) -> openFileNotepad(absolutePathFileXml));
		bottom.add(openXml);

		// Ouvre le fichier de log
		JButton log = PanelUtils.createJButton("Logs", 200, Constant.ICON_TXT_FILE);
		log.setToolTipText("Ouvre le fichier de logs dans Notepad++");
		log.addActionListener((ActionEvent arg0) -> openFileNotepad(Constant.FILE_LOG_PATH));
		bottom.add(log);

		bottom.setBorder(BorderFactory.createTitledBorder(""));
		this.add(bottom);
	}

	/**
	 * Le traitement lorsqu'on importe un fichier txt.
	 */
	private void importFileAction() {
		LOG.debug("Start importFileAction");
		result = new LinkedList<>(Arrays.asList("Sélectionnez un fichier"));
		if (fichier != null) {
			fichier.setSorted(sorted.isSelected());
			String xmlFileName = name.getText();
			fichier.setFileName(xmlFileName);
			fichier.setAuthor(author.getText());
			fichier.setCategorie((Cat) cat.getSelectedItem());
			fichier.setPublishYear(Integer.parseInt(publi.getText()));
			fichier.setRangeDateBegin(Integer.parseInt(rangeB.getText()));
			fichier.setRangeDateEnd(Integer.parseInt(rangeE.getText()));
			fichier.setSize(Integer.parseInt(size.getText()));
			result = new LinkedList<>(Arrays.asList(xmlFileName + " Import OK !"));

			try {
				// Parse Txt file to Compositions List
				List<Composition> compoList = ImportFile.getCompositionsFromFile(new File(absolutePathFileTxt), fichier,
						(RecordType) type.getSelectedItem(), separator.getText(), result, order.isSelected(),
						reverseArtist.isSelected(), removeParenthese.isSelected(), upper.isSelected(),
						removeAfter.isSelected());
				// Export Compositions to XML file
				ExportXML.exportXML(compoList, xmlFileName);
				// Change xml file path
				absolutePathFileXml = Constant.getXmlPath() + xmlFileName + Constant.XML_EXTENSION;
				// Write in Txt file import params
				FichierUtils.writeMapInTxtFile(new File(absolutePathFileTxt),
						convertParamsToMap(separator.getText(), order.isSelected(), reverseArtist.isSelected(),
								removeParenthese.isSelected(), upper.isSelected(), removeAfter.isSelected()));
			} catch (IOException | MyException e) {
				LOG.error("Erreur lors de l'import du fichier: " + absolutePathFileTxt, e);
				result = new LinkedList<>(Arrays.asList(e.toString()));
			}
		}
		miseEnFormeResultLabel(result);
		LOG.debug("End importFileAction");
	}

	private Map<String, String> convertParamsToMap(String separator, boolean artistFirst, boolean reverseArtist,
			boolean parenthese, boolean upper, boolean removeAfter) throws JsonProcessingException {
		Map<String, String> map = new HashMap<>();
		map.put(IMPORT_PARAM_SEPARATOR, separator);
		map.put(IMPORT_PARAM_ARTIST_FIRST, Boolean.toString(artistFirst));
		map.put(IMPORT_PARAM_REVERSE_ARTIST, Boolean.toString(reverseArtist));
		map.put(IMPORT_PARAM_PARENTHESE, Boolean.toString(parenthese));
		map.put(IMPORT_PARAM_UPPER, Boolean.toString(upper));
		map.put(IMPORT_PARAM_REMOVE_AFTER, Boolean.toString(removeAfter));
		map.put(IMPORT_PARAM_NAME, fichier.getFileName());
		map.put(IMPORT_PARAM_AUTEUR, fichier.getAuthor());
		map.put(IMPORT_PARAM_CREATE, Constant.getSdfDttm().format(fichier.getCreationDate()));
		map.put(IMPORT_PARAM_RECORD_TYPE, type.getSelectedItem().toString());
		map.put(IMPORT_PARAM_CATEGORIE, fichier.getCategorie().getCat());
		map.put(IMPORT_PARAM_RANGE_BEGIN, String.valueOf(fichier.getRangeDateBegin()));
		map.put(IMPORT_PARAM_RANGE_END, String.valueOf(fichier.getRangeDateEnd()));
		map.put(IMPORT_PARAM_SORTED, String.valueOf(fichier.getSorted()));
		map.put(IMPORT_PARAM_PUBLISH_YEAR, String.valueOf(fichier.getPublishYear()));
		map.put(IMPORT_PARAM_SIZE, String.valueOf(fichier.getSize()));
		return map;
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
	 * Crée un file chooser pour sélectionner un fichier selon l'extension donnée et
	 * à l'endroit donnée.
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
		jfile.setPreferredSize(new Dimension(1200, 600));
		jfile.setFileFilter(new FileNameExtensionFilter(extension, extension));
		if (jfile.showOpenDialog(new JDialog()) == JFileChooser.APPROVE_OPTION) {
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
		explorePath = Constant.getMusicAbsDirectory();
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
		maxLengthClean.setText("120");
		isBefore.setSelected(true);
		reverseArtist.setSelected(false);
		removeParenthese.setSelected(false);
		upper.setSelected(false);
		removeAfter.setSelected(false);
		isCompleteDirectory.setSelected(false);
		miseEnFormeResultLabel(new ArrayList<String>());
		LOG.debug("End resetAll");
	}

	/**
	 * Traitement lorsqu'on séléctionne un fichier. Les paramètres d'import sont
	 * déduit du nom du fichier et du contenu.
	 */
	private void loadFile() {
		LOG.debug("Start loadFile");
		List<String> label = new LinkedList<>();
		explorePath = StringUtils.substring(file.getAbsolutePath(), 0,
				file.getAbsolutePath().lastIndexOf(File.separator));
		absolutePathFileTxt = file.getAbsolutePath();
		List<String> randomLineAndLastLines = ImportFile.randomLineAndLastLines(file);
		String firstLine = FichierUtils.readFirstLine(file.getAbsolutePath()).get();
		if (StringUtils.startsWith(firstLine, Constant.IMPORT_PARAMS_PREFIX)) {
			Map<String, String> value = new HashMap<>();
			try {
				value = MiscUtils
						.<String>readValueAsMap(StringUtils.substringAfter(firstLine, Constant.IMPORT_PARAMS_PREFIX));
			} catch (IOException e) {
				LOG.error("Error while decoding import params:" + firstLine + " in file " + absolutePathFileTxt, e);
			}
			LOG.debug("value: " + value.entrySet().stream().map(entry -> entry.getKey() + " - " + entry.getValue())
					.collect(Collectors.joining(", ")));
			LOG.debug("Init with stored params");
			name.setText(value.get(IMPORT_PARAM_NAME));
			absolutePathFileXml = Constant.getXmlPath() + value.get(IMPORT_PARAM_NAME) + Constant.XML_EXTENSION;
			author.setText(value.get(IMPORT_PARAM_AUTEUR));
			date.setText(value.get(IMPORT_PARAM_CREATE));
			cat.setSelectedItem(Cat.valueOf(value.get(IMPORT_PARAM_CATEGORIE)));
			publi.setText(value.get(IMPORT_PARAM_PUBLISH_YEAR));
			type.setSelectedItem(RecordType.valueOf(value.get(IMPORT_PARAM_RECORD_TYPE)));
			rangeB.setText(value.get(IMPORT_PARAM_RANGE_BEGIN));
			rangeE.setText(value.get(IMPORT_PARAM_RANGE_END));
			sorted.setSelected(Boolean.parseBoolean(value.get(IMPORT_PARAM_SORTED)));
			size.setText(value.get(IMPORT_PARAM_SIZE));
			separator.setText(value.get(IMPORT_PARAM_SEPARATOR));
			upper.setSelected(Boolean.parseBoolean(value.get(IMPORT_PARAM_UPPER)));
			removeParenthese.setSelected(Boolean.parseBoolean(value.get(IMPORT_PARAM_PARENTHESE)));
			removeAfter.setSelected(Boolean.parseBoolean(value.get(IMPORT_PARAM_REMOVE_AFTER)));
			reverseArtist.setSelected(Boolean.parseBoolean(value.get(IMPORT_PARAM_REVERSE_ARTIST)));
			order.setSelected(Boolean.parseBoolean(value.get(IMPORT_PARAM_ARTIST_FIRST)));
			fichier = new Fichier();
			try {
				fichier.setCreationDate(Constant.getSdfDttm().parse(value.get(IMPORT_PARAM_CREATE)));
			} catch (ParseException e) {
				LOG.warn("Error when parsing creation date", e);
			}
			label.add("Paramètres importés");
		} else {
			LOG.debug("Guessing params");
			fichier = ImportFile.convertOneFile(file);
			if (randomLineAndLastLines.size() == 6) {
				fichier.setSorted(ImportFile.isSorted(randomLineAndLastLines.get(3)));
				fichier.setSize(ImportFile.determineSize(fichier, randomLineAndLastLines, file.getAbsolutePath()));
				separator.setText(ImportFile.getSeparator(randomLineAndLastLines.get(3)));
				sorted.setSelected(fichier.getSorted());
			} else if (!randomLineAndLastLines.isEmpty()) {
				label.add("Fichier trop petit, paramètres devinés sur la 1ère ligne du fichier.");
				fichier.setSorted(ImportFile.isSorted(randomLineAndLastLines.get(0)));
				fichier.setSize(ImportFile.determineSize(fichier, randomLineAndLastLines, file.getAbsolutePath()));
				separator.setText(ImportFile.getSeparator(randomLineAndLastLines.get(0)));
				sorted.setSelected(fichier.getSorted());
			}
			absolutePathFileXml = Constant.getXmlPath() + fichier.getFileName() + Constant.XML_EXTENSION;
			determineType = ImportFile.determineType(file.getName());
			boolean rangeDatesZero = fichier.getRangeDateBegin() == 0 && fichier.getRangeDateEnd() == 0;
			if (Cat.MISCELLANEOUS.equals(fichier.getCategorie()) && !RecordType.UNKNOWN.equals(determineType)
					&& fichier.getPublishYear() != 0 && rangeDatesZero) {
				fichier.setCategorie(Cat.YEAR);
				fichier.setRangeDateBegin(fichier.getPublishYear());
				fichier.setRangeDateEnd(fichier.getPublishYear());
			}
			reverseArtist.setSelected(ImportFile.countComma(file) > fichier.getSize() / 2);
			name.setText(fichier.getFileName());
			author.setText(fichier.getAuthor());
			date.setText(Constant.getSdfDttm().format(fichier.getCreationDate()));
			cat.setSelectedItem(fichier.getCategorie());
			publi.setText(String.valueOf(fichier.getPublishYear()));
			type.setSelectedItem(determineType);
			rangeB.setText(String.valueOf(fichier.getRangeDateBegin()));
			rangeE.setText(String.valueOf(fichier.getRangeDateEnd()));
			size.setText(String.valueOf(fichier.getSize()));
			label.add("Paramètres devinés");
		}
		if (FileUtils.fileExists(absolutePathFileXml)) {
			label.add(name.getText() + " a déjà été importé");
			miseEnFormeResultLabel(label);
		}
		for (int i = 0; i < 7; i++) {
			if (randomLineAndLastLines.size() > i) {
				switch (i) {
				case 0:
					firstL1.setText(randomLineAndLastLines.get(0));
					break;
				case 1:
					firstL2.setText(randomLineAndLastLines.get(1));
					break;
				case 2:
					firstL3.setText(randomLineAndLastLines.get(2));
					break;
				case 3:
					line.setText(randomLineAndLastLines.get(3));
					break;
				case 4:
					lastL1.setText(randomLineAndLastLines.get(4));
					break;
				case 5:
					lastL2.setText(randomLineAndLastLines.get(5));
					break;
				default:
					break;
				}
			}
		}
		LOG.debug("End loadFile");
	}

	/**
	 * Traitement lorsqu'on fusionne tous les fichiers xml.
	 * @throws InterruptedException
	 */
	private void fusionFilesAction() throws InterruptedException {
		new Thread(() -> {
			LOG.debug("Start fusionFilesAction");
			artistPanel.interruptUpdateArtist(true);
			result = new LinkedList<>(Arrays.asList("Fichiers fusionnés"));
			try {
				ImportXML.fusionFiles(Constant.getXmlPath(), resultLabel);
			} catch (IOException e) {
				LOG.error("Erreur lors de la fusion de tous les fichiers xml", e);
				result = new LinkedList<>(Arrays.asList(e.toString()));
			}
			try {
				ImportXML.synchroDeletedWithFinal();
			} catch (MyException e) {
				LOG.error("Erreur lors de la détection de composition supprimées", e);
				result = new LinkedList<>(Arrays.asList(e.toString()));
			}
			SwingUtilities.invokeLater(() -> {
				fichierPanel.updateData();
				artistPanel.updateArtistPanel();
				miseEnFormeResultLabel(result);
			});
			LOG.debug("End fusionFilesAction");
		}).start();
	}

	/**
	 * Pour ouvrir un fichier dans notepad.
	 */
	private void openFileNotepad(String path) {
		LOG.debug("Start openFileNotepad");
		try {
			FichierUtils.openFileInNotepad(Optional.ofNullable(path), Optional.empty());
		} catch (MyException e) {
			result = new LinkedList<>(Arrays.asList(e.toString()));
			miseEnFormeResultLabel(result);
			LOG.error("Erreur lors de l'ouverture du fichier: " + path, e);
		}
		LOG.debug("End openFileNotepad");
	}

	/**
	 * Traitment lorsqu'on veut nettoyer un fichier txt.
	 */
	private void cleanFileAction() {
		LOG.debug("Start cleanFileAction");
		String txt = "";
		if (file == null) {
			txt = "Sélectionnez un fichier !";
		} else {
			String text = maxLengthClean.getText();
			if (StringUtils.isBlank(text)) {
				txt = "Pas de valeur pour la taille maximale !";
			} else if (!StringUtils.isNumeric(text)) {
				txt = "La valeur entrée pour la taille maximale n'est pas un nombre !";
			} else if (Integer.parseInt(text) < 0) {
				txt = "La valeur entrée pour la taille maximale est négative !";
			} else {
				try {
					CleanFile.clearFile(file, sorted.isSelected(), separator.getText(), characterToRemove.getText(),
							Integer.parseInt(text), isBefore.isSelected());
					txt = file.getName() + " nettoyé !";
				} catch (IOException e) {
					LOG.error("Erreur lors du nettoyage du fichier: " + file.getAbsolutePath(), e);
					txt = e.toString();
				}
			}
		}
		result = new LinkedList<>(Arrays.asList(txt));
		miseEnFormeResultLabel(result);
		LOG.debug("End cleanFileAction");
	}

	public JButton getImportFile() {
		return importFile;
	}

}
