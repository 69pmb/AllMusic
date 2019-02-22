package pmb.music.AllMusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JTextPane;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.TableBuilder;
import pmb.music.AllMusic.view.component.MyTable;
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
			"Date de publication", "Categorie", "Dates", "Supprimés", "Taille", "Score", "Classement", "", "Classé" };

	public static final int INDEX_ARTIST = 0;
	public static final int INDEX_TITLE = 1;
	public static final int INDEX_TYPE = 2;
	public static final int INDEX_AUTEUR = 3;
	public static final int INDEX_FILE_NAME = 4;
	public static final int INDEX_PUBLISH_YEAR = 5;
	public static final int INDEX_CAT = 6;
	public static final int INDEX_PERCENT_DELETED = 8;
	public static final int INDEX_FILE_SIZE = 9;
	public static final int INDEX_SCORE = 10;
	public static final int INDEX_RANK = 11;
	public static final int INDEX_DELETED = 12;
	public static final int INDEX_SORTED = 13;

	private JDialog dialog;
	// Data
	private Boolean sendData;
	private int size;

	// Components
	private JTextPane compoCsv;
	private JTextPane warning;
	private MyTable filesFound;

	/**
	 * Constructeur de {@link DeleteCompoDialog}.
	 * 
	 * @param parent {@link JFrame} la fenetre parente
	 * @param size amount of compositions to delete
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
		compoCsv = ComponentBuilder.initJTextPaneComponent(new Color(21, 77, 153), 20);
		compoCsvPanel.add(compoCsv);

		// Warning
		warning = ComponentBuilder.initJTextPaneComponent(new Color(255, 67, 67), 30);
		compoCsvPanel.add(warning);
		panel.add(new JScrollPane(compoCsvPanel));

		// Files found
		try {
			filesFound = new TableBuilder().withModelAndData(null, header, FichierDialogModel.class)
					.withDefaultRowSorterListener(null).withKeyListener().build();
			PanelUtils.colRenderer(filesFound.getTable(), true, INDEX_DELETED, INDEX_TYPE, INDEX_CAT, null, null,
					INDEX_SORTED, INDEX_RANK);
			panel.add(new JScrollPane(filesFound.getTable()), BorderLayout.CENTER);
		} catch (MyException e1) {
			LOG.error("An error occured when init Delete Compo Dialog table", e1);
			warning.setText(e1.getMessage());
			return;
		}

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

		filesFound.getModel().setRowCount(0);
		filesFound.getModel().setDataVector(
				FichierUtils.convertCompositionListToFichierVector(Arrays.asList(found), true, false),
				new Vector<>(Arrays.asList(header)));
		PanelUtils.colRenderer(filesFound.getTable(), true, INDEX_DELETED, INDEX_TYPE, INDEX_CAT, null, null,
				INDEX_SORTED, INDEX_RANK);
		filesFound.removeColumn(filesFound.getColumnModel().getColumn(INDEX_DELETED));
		filesFound.getModel().fireTableDataChanged();
		filesFound.getTable().repaint();
	}

	public Boolean getSendData() {
		return sendData;
	}

	public void setVisible(boolean b) {
		dialog.setVisible(b);
	}
}
