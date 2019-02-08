/**
 * 
 */
package pmb.music.AllMusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.TableBuilder;
import pmb.music.AllMusic.view.component.MyTable;
import pmb.music.AllMusic.view.model.CompoDialogModel;
import pmb.music.AllMusic.view.popup.CompositionPopupMenu;

/**
 * Une "pop-up" permettant d'afficher une liste de {@link Composition}.
 * 
 * @see {@link JDialog}
 * @author pmbroca
 */
public class DialogCompoTable {
	private static final Logger LOG = Logger.getLogger(DialogCompoTable.class);

	private static final int INDEX_ARTIST = 0;
	private static final int INDEX_TITLE = 1;
	private static final int INDEX_TYPE = 2;
	private static final int INDEX_RANK = 3;
	private static final int INDEX_DELETED = 4;

	private JDialog dialog;
	private List<Composition> compo = new ArrayList<>();
	private MyTable table;

	private static final String[] header = { "Artiste", "Titre", "Type", "Classement", "" };

	/**
	 * Constructeur.
	 * 
	 * @param parent {@link JFrame} la fenetre parente
	 * @param header {@link String} les entetes de la popup
	 * @param modal {@code boolean} si la popup bloque l'utilisateur
	 * @param compo {@code List<Composition>} la liste des fichier à afficher
	 * @param height la hauteur de la popup
	 */
	public DialogCompoTable(JFrame parent, String header, boolean modal, List<Composition> compo, int height) {
		LOG.debug("Start DialogFileTable");
		this.dialog = new JDialog(parent, header, modal);
		this.dialog.setSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width - 100, height));
		this.dialog.setLocationRelativeTo(null);
		this.dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.compo = compo;
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
					.withModelAndData(CompositionUtils.convertCompositionListToVector(compo, null, true, false, false,
							false, false), header, CompoDialogModel.class)
					.withDefaultRowSorterListener(null).withMouseClickAction(mouseAction)
					.withPopupMenu(new CompositionPopupMenu(null, INDEX_ARTIST, INDEX_TITLE)).withKeyListener().build();
			table.getRowSorter().toggleSortOrder(INDEX_RANK);
		} catch (MyException e1) {
			LOG.error("An error occured when init Dialog Compo table", e1);
			return;
		}

		this.dialog.getRootPane().registerKeyboardAction(e -> this.dialog.dispose(),
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		PanelUtils.colRenderer(table.getTable(), true, INDEX_DELETED, INDEX_TYPE, null, null, null, null, null);
		table.removeColumn(table.getColumnModel().getColumn(INDEX_DELETED));

		this.dialog.setLayout(new BorderLayout());
		this.dialog.add(new JScrollPane(table.getTable()), BorderLayout.CENTER);
		LOG.debug("End initComponent");
	}

	private Consumer<MouseEvent> mouseAction = e -> {
		if (SwingUtilities.isRightMouseButton(e)) {
			LOG.debug("Start right mouse");
			Optional<Vector<String>> row = PanelUtils.getSelectedRow((JTable) e.getSource(), e.getPoint());
			if (row.isPresent()) {
				table.getPopupMenu().initDataAndPosition(e, row.orElse(null));
				table.getPopupMenu().show(e);
			}
			LOG.debug("End right mouse");
		}
	};
}
