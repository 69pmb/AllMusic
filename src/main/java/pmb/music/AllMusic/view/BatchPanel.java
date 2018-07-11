package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.model.Score;
import pmb.music.AllMusic.utils.BatchUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.MyException;

/**
 * Onglet pour lancer des traitements.
 * Batchs List: 
 * - Find duplicate compositions: FDC
 * - Find duplicate files: FDF 
 * - Missing XML files: MXF
 * - Top year: top
 * - Nettoyer dossier historique: clearHistory
 * - Find suspicious compositions: FSC
 * - Checks filenames (Author + name + publishYear): validateFileName
 * - Générer des statistiques: stats
 * @author PBR
 */
public class BatchPanel extends JPanel {
	private static final long serialVersionUID = -7659089306956006760L;
	private static final Logger LOG = Logger.getLogger(BatchPanel.class);
	
	/**
	 * Les messages.
	 */
	private JTextArea resultLabel;
	private List<String> resultLabelData;
	private String fileResult = Constant.BATCH_FILE;

	public BatchPanel(Score score) {
		super();
		LOG.debug("Start BatchPanel");
		this.setLayout(new GridLayout(11, 1));

		findDuplicateComposition();
		findDuplicateFiles();
		missingXmlFiles();
		topYear(score);
		clearHistory();
		suspicious();
		duplicateTitle();
		validateFileName();
		stats();
		lastLine();

		LOG.debug("End BatchPanel");
	}

	/**
	 * Initialise les composants pour trouver les compositions en double (FDC).
	 */
	private void findDuplicateComposition() {
		JPanel fdc = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);

		JLabel fdcLabel = new JLabel("Recherche les compositions en double: ");
		PanelUtils.addComponent(fdc, fdcLabel, Component.LEFT_ALIGNMENT, 100);

		// Checkbox song
		JLabel fdcSongLabel = new JLabel("Chanson: ");
		JCheckBox fdcSong = new JCheckBox();
		fdcSong.setSelected(true);
		PanelUtils.addComponent(fdc, fdcSongLabel, Component.LEFT_ALIGNMENT, 0);
		PanelUtils.addComponent(fdc, fdcSong, Component.LEFT_ALIGNMENT, 100);

		// Checkbox album
		JLabel fdcAlbumLabel = new JLabel("Album: ");
		JCheckBox fdcAlbum = new JCheckBox();
		fdcAlbum.setSelected(true);
		PanelUtils.addComponent(fdc, fdcAlbumLabel, Component.LEFT_ALIGNMENT, 0);
		PanelUtils.addComponent(fdc, fdcAlbum, Component.LEFT_ALIGNMENT, 100);

		// Checkbox unmergeable
		JLabel fdcUnmergeableLabel = new JLabel("Ignorer les fichier non mergeables: ");
		JCheckBox fdcUnmergeable = new JCheckBox();
		fdcUnmergeable.setSelected(true);
		PanelUtils.addComponent(fdc, fdcUnmergeableLabel, Component.LEFT_ALIGNMENT, 0);
		PanelUtils.addComponent(fdc, fdcUnmergeable, Component.LEFT_ALIGNMENT, 100);
		
		// Checkbox year
		JLabel fdcYearLabel = new JLabel("Uniquement les fichiers YEAR: ");
		JCheckBox fdcYear = new JCheckBox();
		PanelUtils.addComponent(fdc, fdcYearLabel, Component.LEFT_ALIGNMENT, 0);
		PanelUtils.addComponent(fdc, fdcYear, Component.LEFT_ALIGNMENT, 100);

		// Launch Button
		JButton fdcBtn = new JButton("Go Compositions En Double");
		fdcBtn.setToolTipText("Fusionne les compositions identiques mais non détectées à la fusion classique.");
		fdcBtn.addActionListener((ActionEvent arg0) -> {
			displayText("Start findDuplicateComposition: " + MiscUtils.getCurrentTime());
			new Thread(() -> {
				fileResult = BatchUtils.detectsDuplicateFinal(fdcSong.isSelected(), fdcAlbum.isSelected(),
						fdcUnmergeable.isSelected(), fdcYear.isSelected());
				displayText("End findDuplicateComposition: " + MiscUtils.getCurrentTime());
			}).start();
		});
		PanelUtils.addComponent(fdc, fdcBtn, Component.RIGHT_ALIGNMENT, 100);

		this.add(fdc);
	}

	/**
	 * Initialise les composants pour trouver les fichiers en double (FDF).
	 */
	private void findDuplicateFiles() {
		JPanel fdf = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);
		
		// Label
		JLabel fdfLabel = new JLabel("Recherche les fichiers en double: ");
		PanelUtils.addComponent(fdf, fdfLabel, Component.LEFT_ALIGNMENT, 700);

		// Bouton d'action
		JButton fdfBtn = new JButton("Go Fichiers En Double");
		fdfBtn.setToolTipText("Cherche les fichiers en double.");
		fdfBtn.addActionListener((ActionEvent arg0) -> {
			displayText("Start findDuplicateFiles: " + MiscUtils.getCurrentTime());
			new Thread(() -> {
				fileResult = BatchUtils.findDuplicateFiles();
				displayText("End findDuplicateFiles: " + MiscUtils.getCurrentTime());
			}).start();
		});
		PanelUtils.addComponent(fdf, fdfBtn, Component.RIGHT_ALIGNMENT, 100);

		this.add(fdf);
	}
	
	/**
	 * Initialise les composants pour trouver les fichiers txt non importés (MXF).
	 */
	private void missingXmlFiles() {
		JPanel mxf = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);
		
		// Label
		JLabel mxfLabel = new JLabel("Rechercher les fichiers XML manquant: ");
		PanelUtils.addComponent(mxf, mxfLabel, Component.LEFT_ALIGNMENT, 700);
		
		// Bouton d'action
		JButton mxfBtn = new JButton("Go XML Manquant");
		mxfBtn.setToolTipText("Cherche si des fichiers txt n'ont pas d'équivalent XML.");
		mxfBtn.addActionListener((ActionEvent arg0) -> {
			displayText("Start missingXML: " + MiscUtils.getCurrentTime());
			new Thread(() -> {
				fileResult = BatchUtils.missingXML();
				displayText("End missingXML: " + MiscUtils.getCurrentTime());
			}).start();
		});
		PanelUtils.addComponent(mxf, mxfBtn, Component.RIGHT_ALIGNMENT, 100);
		
		this.add(mxf);
	}
	
	/**
	 * Generates csv reports for a specific year.
	 * @param score 
	 */
	private void topYear(Score score) {
		JPanel top = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);

		// Label
		JLabel topLabel = new JLabel("Générer les tops: ");
		PanelUtils.addComponent(top, topLabel, Component.LEFT_ALIGNMENT, 100);

		// Year Begin
		JTextField yearBeginTop = new JTextField(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - 1));
		PanelUtils.setSize(yearBeginTop, 100, 60);
		PanelUtils.addComponent(top, yearBeginTop, Component.LEFT_ALIGNMENT, 80);

		// Year End
		JTextField yearEndTop = new JTextField(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - 1));
		PanelUtils.setSize(yearEndTop, 100, 60);
		PanelUtils.addComponent(top, yearEndTop, Component.LEFT_ALIGNMENT, 80);

		// Album limit
		JTextField albumLimit = new JTextField("10");
		PanelUtils.setSize(albumLimit, 100, 60);
		PanelUtils.addComponent(top, albumLimit, Component.LEFT_ALIGNMENT, 80);

		// Song limit
		JTextField songLimit = new JTextField("4");
		PanelUtils.setSize(songLimit, 100, 60);
		PanelUtils.addComponent(top, songLimit, Component.LEFT_ALIGNMENT, 80);

		// Bouton d'action
		JButton topBtn = new JButton("Go Tops");
		topBtn.addActionListener((ActionEvent arg0) -> {
			displayText("Start topYear: " + MiscUtils.getCurrentTime());
			new Thread(() -> {
				fileResult = BatchUtils.topYear(Integer.parseInt(yearBeginTop.getText()), Integer.parseInt(yearEndTop.getText()),
						Integer.parseInt(albumLimit.getText()), Integer.parseInt(songLimit.getText()), score);
				displayText("End topYear: " + MiscUtils.getCurrentTime());
			}).start();
		});
		PanelUtils.addComponent(top, topBtn, Component.RIGHT_ALIGNMENT, 100);

		this.add(top);
	}

	private void clearHistory() {
		JPanel clear = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);

		// Label
		JLabel clearLabel = new JLabel("Nettoyer le dossier historique: ");
		PanelUtils.addComponent(clear, clearLabel, Component.LEFT_ALIGNMENT, 800);

		// Clean history
		JButton cleanHistory = new JButton("Go Clean Historique");
		cleanHistory.setToolTipText("Supprime tous les fichiers du dossier d'historique sauf le plus récent.");
		cleanHistory.addActionListener((ActionEvent arg0) -> {
			displayText("Start cleanHistory: " + MiscUtils.getCurrentTime());
			new Thread(() -> {
				fileResult = BatchUtils.cleanHistory();
				displayText("Start cleanHistory: " + MiscUtils.getCurrentTime());
			}).start();
		});
		PanelUtils.addComponent(clear, cleanHistory, Component.RIGHT_ALIGNMENT, 100);

		this.add(clear);
	}
	
	private void suspicious() {
		JPanel suspicious = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);
		
		// Label
		JLabel suspiciousLabel = new JLabel("Trouver des compositions suspectes: ");
		PanelUtils.addComponent(suspicious, suspiciousLabel, Component.LEFT_ALIGNMENT, 800);
		
		// suspicious Btn
		JButton suspiciousBtn = new JButton("Go Compositions Suspectes");
		suspiciousBtn.setToolTipText("Trouve des compositions bizarres");
		suspiciousBtn.addActionListener((ActionEvent arg0) -> {
			displayText("Start findSuspiciousComposition: " + MiscUtils.getCurrentTime());
			new Thread(() -> {
				fileResult = BatchUtils.findSuspiciousComposition();
				displayText("Start findSuspiciousComposition: " + MiscUtils.getCurrentTime());
			}).start();
		});
		PanelUtils.addComponent(suspicious, suspiciousBtn, Component.RIGHT_ALIGNMENT, 100);
		
		this.add(suspicious);
	}
	
	private void duplicateTitle() {
		JPanel duplicateTitle = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);
		
		// Label
		JLabel duplicateTitleLabel = new JLabel("Trouver les chansons aux titres identiques: ");
		PanelUtils.addComponent(duplicateTitle, duplicateTitleLabel, Component.LEFT_ALIGNMENT, 800);
		
		// duplicateTitle Btn
		JButton duplicateTitleBtn = new JButton("Go Same Title");
		duplicateTitleBtn.setToolTipText("Trouve les chansons avec le même titre mais avec l'artiste différent");
		duplicateTitleBtn.addActionListener((ActionEvent arg0) -> {
			displayText("Start findDuplicateTitleComposition: " + MiscUtils.getCurrentTime());
			new Thread(() -> {
				fileResult = BatchUtils.findDuplicateTitleComposition();
				displayText("Start findDuplicateTitleComposition: " + MiscUtils.getCurrentTime());
			}).start();
		});
		PanelUtils.addComponent(duplicateTitle, duplicateTitleBtn, Component.RIGHT_ALIGNMENT, 100);
		
		this.add(duplicateTitle);
	}

	private void validateFileName() {
		JPanel validate = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);

		// Label
		JLabel validateLabel = new JLabel("Trouver les noms de fichier incorrect: ");
		PanelUtils.addComponent(validate, validateLabel, Component.LEFT_ALIGNMENT, 800);

		// validate Btn
		JButton validateBtn = new JButton("Go Filename Incorrect");
		validateBtn.setToolTipText("Trouve les noms de fichier incorrect");
		validateBtn.addActionListener((ActionEvent arg0) -> {
			displayText("Start findIncorectFileNames: " + MiscUtils.getCurrentTime());
			new Thread(() -> {
				fileResult = BatchUtils.findIncorrectFileNames();
				displayText("Start findIncorectFileNames: " + MiscUtils.getCurrentTime());
			}).start();
		});
		PanelUtils.addComponent(validate, validateBtn, Component.RIGHT_ALIGNMENT, 100);

		this.add(validate);
	}
	
	private void stats() {
		JPanel stat = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);
		
		// Label
		JLabel statLabel = new JLabel("Statistiques: ");
		PanelUtils.addComponent(stat, statLabel, Component.LEFT_ALIGNMENT, 900);
		
		// Stat btn
		JButton statsBtn = new JButton("Go Statistiques");
		statsBtn.setToolTipText("Génère des statistiques.");
		statsBtn.addActionListener((ActionEvent arg0) -> {
			displayText("Start statistic: " + MiscUtils.getCurrentTime());
			new Thread(() -> {
				fileResult = BatchUtils.stat();
				displayText("Start statistic: " + MiscUtils.getCurrentTime());
			}).start();
		});
		PanelUtils.addComponent(stat, statsBtn, Component.RIGHT_ALIGNMENT, 100);
		
		this.add(stat);
	}

	/**
	 * Initialise la dernière ligne de composant.
	 */
	private void lastLine() {
		JPanel lastLine = new JPanel(new GridLayout(0, 2));
		
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
		lastLine.add(resultPanel);

		// Boutons
		JPanel btnPanel = new JPanel();
		// Clear
		JButton clearBtn = new JButton("Vider la zone de résultat");
		clearBtn.addActionListener((ActionEvent arg0) -> {
			resultLabelData = null;
			displayText("");
		});
		btnPanel.add(clearBtn);
		// Notepad
		JButton batchFileBtn = new JButton("Ouvrir le fichier de résultat");
		batchFileBtn.addActionListener((ActionEvent arg0) -> openResultFileInNotepad());
		btnPanel.add(batchFileBtn);
		
		lastLine.add(btnPanel);
		this.add(lastLine);
	}
	
	/**
	 * Ouvrir le fichier de resultat dans notepad.
	 */
	private void openResultFileInNotepad() {
		LOG.debug("Start openResultFileInNotepad");
		try {
			FichierUtils.openFileInNotepad(Optional.ofNullable(fileResult));
		} catch (MyException e) {
			displayText(e.toString());
			LOG.error("Erreur lors de l'ouverture du fichier: " + fileResult, e);
		}
		LOG.debug("End openResultFileInNotepad");
	}

	/**
	 * Ajoute un message dans la zone de texte resultLabel.
	 * 
	 * @param text un nouveau texte à afficher
	 */
	private void displayText(String text) {
		LOG.debug("Start displayText");
		if(resultLabelData == null) {
			resultLabelData = new ArrayList<>();
		}
		StringBuilder s = new StringBuilder();
		resultLabelData.add(text);
		for (String string : resultLabelData) {
			s.append(string).append(Constant.NEW_LINE);
		}
		resultLabel.setText(s.toString());
		resultLabel.setForeground(new Color(243, 16, 16));
		Font labelFont = resultLabel.getFont();
		resultLabel.setFont(new Font(labelFont.getName(), labelFont.getStyle(), 20));
		LOG.debug("End displayText");
	}
}
