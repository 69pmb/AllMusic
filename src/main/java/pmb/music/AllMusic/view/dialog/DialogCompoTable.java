/**
 * 
 */
package pmb.music.AllMusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.TableBuilder;
import pmb.music.AllMusic.view.component.MyTable;
import pmb.music.AllMusic.view.model.CompoDialogModel;
import pmb.music.AllMusic.view.panel.OngletPanel;
import pmb.music.AllMusic.view.popup.CompositionPopupMenu;

/**
 * Une "pop-up" permettant d'afficher une liste de {@link Composition}.
 * 
 * @see {@link JDialog}
 */
public class DialogCompoTable {
	private static final Logger LOG = LogManager.getLogger(DialogCompoTable.class);

	public static final int INDEX_ARTIST = 0;
	public static final int INDEX_TITLE = 1;
	public static final int INDEX_TYPE = 2;
	public static final int INDEX_RANK = 3;
	public static final int INDEX_FILE_SIZE = 4;
	public static final int INDEX_SCORE = 5;
	public static final int INDEX_DECILE = 6;
	public static final int INDEX_DELETED = 7;
	public static final int INDEX_UUID = 8;

	private JDialog dialog;
	private List<Composition> compo = new ArrayList<>();
	private String fileName;
	private MyTable table;
	private JDialog parent;

	private static final String[] header = { "Artiste", "Titre", "Type", "Classement", "Nombre de fichiers", "Score", "", "", "" };

	/**
	 * Constructeur.
	 * @param parent {@link JDialog} parent dialog
	 * @param header {@link String} les entetes de la popup
	 * @param compo {@code List<Composition>} la liste des fichier à afficher
	 * @param fileName {@link String} file's name displayed in the dialog
	 * @param height la hauteur de la popup
	 */
	public DialogCompoTable(JDialog parent, String header, List<Composition> compo, String fileName, int height) {
		LOG.debug("Start DialogFileTable");
		this.dialog = new JDialog((JFrame) null, header, true);
		this.dialog.setSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width - 100, height));
		this.dialog.setLocationRelativeTo(null);
		this.dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.compo = compo;
		this.fileName = fileName;
		this.parent = parent;
		this.dialog.setResizable(true);
		this.initComponent();
		LOG.debug("End DialogFileTable");
	}

	/**
	 * Affiche une {@link DialogCompoTable}.
	 */
	public void showDialogFileTable() {
		this.dialog.setVisible(true);
	}

	private void initComponent() {
		LOG.debug("Start initComponent");

		try {
			table = new TableBuilder()
					.withModelAndData(CompositionUtils.convertCompositionListToVector(compo, fileName, true, true, false,
							true, false), header, CompoDialogModel.class)
					.withDefaultRowSorterListener(null).withMouseClickAction(e -> {
						Optional<Vector<String>> row = PanelUtils.getSelectedRow((JTable) e.getSource(),
								e.getPoint());
						if (SwingUtilities.isRightMouseButton(e)) {
							LOG.debug("Start right mouse");
							if (row.isPresent()) {
								table.getPopupMenu().initDataAndPosition(e, row.get());
								table.getPopupMenu().show(e);
							}
							LOG.debug("End right mouse");
						} else if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
							OngletPanel.getOnglets().setSelectedIndex(OngletPanel.getTabIndex(Constant.ONGLET_FICHIER));
							dialog.dispose();
							parent.dispose();
							OngletPanel.getFichier().searchProgrammatically(fileName, MiscUtils.stringToUuids(row.map(selected -> selected.get(INDEX_UUID)).orElse("")));
						}
					}).withPopupMenu(new CompositionPopupMenu(null, INDEX_ARTIST, INDEX_TITLE)).withKeyListener()
					.build();
			table.getRowSorter().toggleSortOrder(INDEX_RANK);
		} catch (MyException e1) {
			LOG.error("An error occured when init Dialog Compo table", e1);
			return;
		}

		this.dialog.getRootPane().registerKeyboardAction(e -> this.dialog.dispose(),
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		PanelUtils.colRenderer(table.getTable(), true, INDEX_DELETED, INDEX_TYPE, null, INDEX_DECILE, INDEX_SCORE, null, null);
		table.removeColumn(table.getColumnModel().getColumn(INDEX_DECILE));
		table.removeColumn(table.getColumnModel().getColumn(INDEX_DELETED - 1));
		table.removeColumn(table.getColumnModel().getColumn(INDEX_UUID - 2));

		this.dialog.setLayout(new BorderLayout());
		this.dialog.add(new JScrollPane(table.getTable()), BorderLayout.CENTER);
		LOG.debug("End initComponent");
	}

}
