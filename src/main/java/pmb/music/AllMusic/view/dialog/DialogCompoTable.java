/**
 * 
 */
package pmb.music.AllMusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.RowSorterEvent;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.view.PanelUtils;
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

	private static final String[] header = { "Artiste", "Titre", "Type", "Classement", "" };

	private Integer sortedColumn;
	private int selectedRow = -1;
	private CompositionPopupMenu popup;

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
		JTable table = new JTable();
		table.setAutoCreateRowSorter(true);
		table.setRowHeight(30);
		table.getTableHeader().setResizingAllowed(true);
		table.setFillsViewportHeight(true);
		table.setBackground(UIManager.getColor("Label.background"));
		table.setFont(UIManager.getFont("Label.font"));
		table.setBorder(UIManager.getBorder("Label.border"));
		table.setModel(new CompoDialogModel(
				CompositionUtils.convertCompositionListToVector(compo, null, true, false, false, false, false),
				new Vector<>(Arrays.asList(header))));
		table.getRowSorter().toggleSortOrder(INDEX_RANK);
		table.getRowSorter().addRowSorterListener((RowSorterEvent e) -> {
			if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
				List<? extends SortKey> sortKeys = ((RowSorter<?>) e.getSource()).getSortKeys();
				if (!sortKeys.isEmpty()) {
					sortedColumn = sortKeys.get(0).getColumn();
				}
			}
		});
		popup = new CompositionPopupMenu(table, null, INDEX_ARTIST, INDEX_TITLE);
		table.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// Nothing to do
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
					popup.show(e);
				} else {
					selectedRow = PanelUtils.keyShortcutAction(e, selectedRow, sortedColumn);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// Nothing to do
			}
		});
		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				mouseAction(e);
			}
		});
		this.dialog.getRootPane().registerKeyboardAction(e -> this.dialog.dispose(),
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		PanelUtils.colRenderer(table, true, INDEX_DELETED, INDEX_TYPE, null, null, null, null, null);
		table.removeColumn(table.getColumnModel().getColumn(INDEX_DELETED));

		this.dialog.setLayout(new BorderLayout());
		this.dialog.add(new JScrollPane(table), BorderLayout.CENTER);
		LOG.debug("End initComponent");
	}

	private void mouseAction(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			LOG.debug("Start right mouse");
			Optional<Vector<String>> row = PanelUtils.getSelectedRow((JTable) e.getSource(), e.getPoint());
			if (row.isPresent()) {
				popup.initDataAndPosition(e, row);
				popup.show(e);
			}
			LOG.debug("End right mouse");
		}
	}
}
