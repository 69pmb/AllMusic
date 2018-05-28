package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import pmb.music.AllMusic.utils.BatchUtils;
import pmb.music.AllMusic.utils.Constant;

/**
 * Onglet pour lancer des traitements.
 * Batchs List: 
 * - Find duplicate compositions: FDC
 * - Find duplicate files: FDF 
 * - Missing XML files: MXF
 * - Top year: top
 * - Find suspicious compositions: FSC
 * - Nettoyer dossier historique
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

	public BatchPanel() {
		super();
		LOG.debug("Start BatchPanel");
		this.setLayout(new GridLayout(10, 1));

		findDuplicateComposition();
		findDuplicateFiles();
		missingXmlFiles();
		lastLine();

		LOG.debug("End BatchPanel");
	}

	/**
	 * Initialise les composants pour trouver les compositions en double (FDC).
	 */
	private void findDuplicateComposition() {
		JPanel fdc = createBoxLayoutPanel();

		JLabel fdcLabel = new JLabel("Recherche les compositions en double: ");
		addComponent(fdc, fdcLabel, Component.LEFT_ALIGNMENT, 100);

		// Checkbox song
		JLabel fdcSongLabel = new JLabel("Chanson: ");
		JCheckBox fdcSong = new JCheckBox();
		fdcSong.setSelected(true);
		addComponent(fdc, fdcSongLabel, Component.LEFT_ALIGNMENT, 0);
		addComponent(fdc, fdcSong, Component.LEFT_ALIGNMENT, 100);

		// Checkbox album
		JLabel fdcAlbumLabel = new JLabel("Album: ");
		JCheckBox fdcAlbum = new JCheckBox();
		fdcAlbum.setSelected(true);
		addComponent(fdc, fdcAlbumLabel, Component.LEFT_ALIGNMENT, 0);
		addComponent(fdc, fdcAlbum, Component.LEFT_ALIGNMENT, 100);

		// Checkbox unmergeable
		JLabel fdcUnmergeableLabel = new JLabel("Ignorer les fichier non mergeables: ");
		JCheckBox fdcUnmergeable = new JCheckBox();
		fdcUnmergeable.setSelected(true);
		addComponent(fdc, fdcUnmergeableLabel, Component.LEFT_ALIGNMENT, 0);
		addComponent(fdc, fdcUnmergeable, Component.LEFT_ALIGNMENT, 100);

		JButton fdcBtn = new JButton("Go");
		fdcBtn.setToolTipText("Fusionne les compositions identiques mais non détectées à la fusion classique.");
		fdcBtn.addActionListener((ActionEvent arg0) -> {
			displayText("Start findDuplicateComposition: " + BatchUtils.getCurrentTime());
			new Thread(() -> {
				BatchUtils.detectsDuplicateFinal(fdcSong.isSelected(), fdcAlbum.isSelected(),
						fdcUnmergeable.isSelected());
				displayText("End findDuplicateComposition: " + BatchUtils.getCurrentTime());
			}).start();
		});
		addComponent(fdc, fdcBtn, Component.RIGHT_ALIGNMENT, 100);

		this.add(fdc);
	}

	/**
	 * Initialise les composants pour trouver les fichiers en double (FDF).
	 */
	private void findDuplicateFiles() {
		JPanel fdf = createBoxLayoutPanel();
		
		// Label
		JLabel fdfLabel = new JLabel("Recherche les compositions en double: ");
		addComponent(fdf, fdfLabel, Component.LEFT_ALIGNMENT, 700);

		// Bouton d'action
		JButton fdfBtn = new JButton("Go");
		fdfBtn.setToolTipText("Cherche les fichiers en double.");
		fdfBtn.addActionListener((ActionEvent arg0) -> {
			displayText("Start findDuplicateFiles: " + BatchUtils.getCurrentTime());
			new Thread(() -> {
				BatchUtils.findDuplicateFiles();
				displayText("End findDuplicateFiles: " + BatchUtils.getCurrentTime());
			}).start();
		});
		addComponent(fdf, fdfBtn, Component.RIGHT_ALIGNMENT, 100);

		this.add(fdf);
	}
	
	/**
	 * Initialise les composants pour trouver les fichiers txt non importés (MXF).
	 */
	private void missingXmlFiles() {
		JPanel mxf = createBoxLayoutPanel();
		
		// Label
		JLabel mxfLabel = new JLabel("Rechercher les fichiers XML manquant: ");
		addComponent(mxf, mxfLabel, Component.LEFT_ALIGNMENT, 700);
		
		// Bouton d'action
		JButton mxfBtn = new JButton("Go");
		mxfBtn.setToolTipText("Cherche si des fichiers txt n'ont pas d'équivalent XML.");
		mxfBtn.addActionListener((ActionEvent arg0) -> {
			displayText("Start missingXML: " + BatchUtils.getCurrentTime());
			new Thread(() -> {
				BatchUtils.missingXML();
				displayText("End missingXML: " + BatchUtils.getCurrentTime());
			}).start();
		});
		addComponent(mxf, mxfBtn, Component.RIGHT_ALIGNMENT, 100);
		
		this.add(mxf);
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
		if (StringUtils.isNotBlank(Constant.BATCH_FILE_PATH)) {
			try {
				if (FileUtils.fileExists(Constant.BATCH_FILE_PATH)) {
					Runtime.getRuntime().exec(Constant.NOTEPAD_PATH + Constant.BATCH_FILE_PATH);
				}
			} catch (IOException e) {
				LOG.error("Erreur lors de l'ouverture du fichier: " + Constant.BATCH_FILE_PATH, e);
				displayText(e.toString());
			}
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
	
	/**
	 * Crée un {@link JPanel} avec un layout de type {@link BoxLayout} aligné sur l'abscisse.
	 * @return le panel crée
	 */
	private JPanel createBoxLayoutPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(Box.createRigidArea(new Dimension(100,0)));		
		return panel;
	}

	/**
	 * Ajoute un composant au panel.
	 * @param panel
	 * @param component
	 * @param alignement
	 */
	private void addComponent(JPanel panel, JComponent component, float alignement, int rigidSize) {
		component.setAlignmentX(alignement);
		component.setAlignmentY(Component.CENTER_ALIGNMENT);
		panel.add(component);
		panel.add(Box.createRigidArea(new Dimension(rigidSize, 0)));
	}
}
