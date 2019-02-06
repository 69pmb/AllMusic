package pmb.music.AllMusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.CsvComposition;
import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.model.FichierDialogModel;

/**
 * Une "pop-up" permettant de confirmer ou non la suppression d'une
 * {@link Composition}.
 * 
 * @see {@link JDialog}
 */
public class DeleteCompoDialog {
	private static final Logger LOG = Logger.getLogger(DeleteCompoDialog.class);

	private static final String[] header = { "Artiste", "Oeuvre", "Type", "Auteur", "Nom du fichier",
			"Date de publication", "Categorie", "Dates", "Supprimés", "Taille", "Classement", "", "Classé" };

	public static final int INDEX_ARTIST = 0;
	public static final int INDEX_TITLE = 1;
	public static final int INDEX_TYPE = 2;
	public static final int INDEX_AUTEUR = 3;
	public static final int INDEX_FILE_NAME = 4;
	public static final int INDEX_PUBLISH_YEAR = 5;
	public static final int INDEX_CAT = 6;
	public static final int INDEX_PERCENT_DELETED = 8;
	public static final int INDEX_FILE_SIZE = 9;
	public static final int INDEX_RANK = 10;
	public static final int INDEX_DELETED = 11;
	public static final int INDEX_SORTED = 12;

	private JDialog dialog;
	// Data
	private Boolean sendData;
	private int size;

	// Components
	private JTextPane compoCsv;
	private JTextPane warning;
	private JTable filesFound;

	/**
	 * Constructeur de {@link DeleteCompoDialog}.
	 * 
	 * @param parent {@link JFrame} la fenetre parente
	 * @param compoCsv {@link CsvComposition} la composition dont la suppression
	 *            doit être confirmée
	 */
	public DeleteCompoDialog(JFrame parent, int size) {
		LOG.debug("Start DeleteCompoDialog");
		this.dialog = new JDialog(parent, "", true);
		this.dialog.setSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width - 100,
				Toolkit.getDefaultToolkit().getScreenSize().height - 50));
		this.dialog.setLocationRelativeTo(null);
		this.size = size;
		this.dialog.setResizable(true);
		this.initComponent();
		LOG.debug("End DeleteCompoDialog");
	}

	private void initComponent() {
		LOG.debug("Start initComponent");
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		// Csv compo
		JPanel compoCsvPanel = new JPanel();
		compoCsvPanel.setLayout(new BoxLayout(compoCsvPanel, BoxLayout.Y_AXIS));
		compoCsv = initJTextPaneComponent(new Color(21, 77, 153), 20);
		compoCsvPanel.add(compoCsv);

		// Warning
		warning = initJTextPaneComponent(new Color(255, 67, 67), 30);
		compoCsvPanel.add(warning);
		panel.add(new JScrollPane(compoCsvPanel));

		// Files found
		filesFound = new JTable();
		filesFound.setAutoCreateRowSorter(true);
		filesFound.setRowHeight(30);
		filesFound.getTableHeader().setResizingAllowed(true);
		filesFound.setFillsViewportHeight(true);
		filesFound.setBackground(UIManager.getColor("Label.background"));
		filesFound.setFont(UIManager.getFont("Label.font"));
		filesFound.setBorder(UIManager.getBorder("Label.border"));
		filesFound.setModel(new FichierDialogModel(new Object[0][header.length - 1], header));
		PanelUtils.colRenderer(filesFound, true, INDEX_DELETED, INDEX_TYPE, INDEX_CAT, null, null, INDEX_SORTED,
				INDEX_RANK);
		panel.add(new JScrollPane(filesFound), BorderLayout.CENTER);

		JPanel btnPanel = new JPanel();

		// Yes
		JButton yes = new JButton("Oui");
		yes.addActionListener((ActionEvent a) -> {
			dialog.setVisible(false);
			sendData = true;
		});
		btnPanel.add(yes);

		// No
		JButton no = new JButton("Non");
		no.addActionListener((ActionEvent a) -> {
			dialog.setVisible(false);
			sendData = false;
		});
		btnPanel.add(no);
		panel.add(btnPanel);

		this.dialog.setLayout(new BorderLayout());
		this.dialog.add(panel, BorderLayout.CENTER);
		LOG.debug("End initComponent");
	}

	private JTextPane initJTextPaneComponent(Color color, int fontSize) {
		JTextPane textPane = new JTextPane();
		textPane.setOpaque(false);
		textPane.setEditable(false);
		textPane.setBackground(UIManager.getColor("Label.background"));
		textPane.setFont(UIManager.getFont("Label.font"));
		textPane.setBorder(UIManager.getBorder("Label.border"));
		textPane.setForeground(color);
		Font labelFont = textPane.getFont();
		textPane.setFont(new Font(labelFont.getName(), labelFont.getStyle(), fontSize));
		StyledDocument doc = textPane.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);
		return textPane;
	}

	/**
	 * Update delete dialog infos.
	 * 
	 * @param csv composition infos from csv file
	 * @param found composition found from xml final file
	 * @param index index in csv file
	 * @param warning warning on csv composition like play count or rank
	 */
	public void updateDialog(String csv, Composition found, int index, String warning) {
		sendData = null;
		this.dialog.setTitle(
				index + "/" + size + " - " + BigDecimal.valueOf(100D).setScale(2).multiply(new BigDecimal(index))
						.divide(new BigDecimal(size), RoundingMode.HALF_UP).doubleValue() + "%");
		compoCsv.setText(csv);

		this.warning.setText(warning);

		((DefaultTableModel) filesFound.getModel()).setRowCount(0);
		((DefaultTableModel) filesFound.getModel()).setDataVector(
				FichierUtils.convertCompositionListToFichierVector(Arrays.asList(found), true, false),
				new Vector<>(Arrays.asList(header)));
		PanelUtils.colRenderer(filesFound, true, INDEX_DELETED, INDEX_TYPE, INDEX_CAT, null, null, INDEX_SORTED,
				INDEX_RANK);
		filesFound.removeColumn(filesFound.getColumnModel().getColumn(INDEX_DELETED));
		((AbstractTableModel) filesFound.getModel()).fireTableDataChanged();
		filesFound.repaint();
	}

	public Boolean getSendData() {
		return sendData;
	}

	public void setVisible(boolean b) {
		dialog.setVisible(b);
	}
}
