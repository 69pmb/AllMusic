/**
 * 
 */
package pmb.music.AllMusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.event.RowSorterListener;

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
public class DialogCompoTable extends JDialog {

	private static final long serialVersionUID = 1304786661370052913L;

	private static final Logger LOG = Logger.getLogger(DialogCompoTable.class);
	private static final int INDEX_ARTIST = 0;
	private static final int INDEX_TITLE = 1;
	private static final int INDEX_TYPE = 2;
	private static final int INDEX_RANK = 3;
	private static final int INDEX_DELETED = 4;

	private List<Composition> compo = new ArrayList<>();

	private static final String[] header = { "Artiste", "Titre", "Type", "Classement", "" };

	private JTable table;
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
	 * @param dim {@link Dimension} les dimension de la popup
	 */
	public DialogCompoTable(JFrame parent, String header, boolean modal, List<Composition> compo, Dimension dim) {
		super(parent, header, modal);
		LOG.debug("Start DialogFileTable");
		this.setSize(dim);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.compo = compo;
		this.setResizable(true);
		this.initComponent();
		LOG.debug("End DialogFileTable");
	}

	/**
	 * Affiche une {@link DialogCompoTable}.
	 */
	public void showDialogFileTable() {
		this.setVisible(true);
	}

	private void initComponent() {
		LOG.debug("Start initComponent");
		table = new JTable();
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
		table.getRowSorter().addRowSorterListener(new RowSorterListener() {
			@Override
			public void sorterChanged(RowSorterEvent e) {
				if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
					List<? extends SortKey> sortKeys = ((RowSorter<?>) e.getSource()).getSortKeys();
					if (!sortKeys.isEmpty()) {
						sortedColumn = sortKeys.get(0).getColumn();
					}
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
		this.getRootPane().registerKeyboardAction(e -> {
			this.dispose();
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		PanelUtils.colRenderer(table, true, INDEX_DELETED, INDEX_TYPE);
		table.removeColumn(table.getColumnModel().getColumn(INDEX_DELETED));

		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(table), BorderLayout.CENTER);
		LOG.debug("End initComponent");
	}

	private void mouseAction(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			LOG.debug("Start right mouse");
			Optional<Vector<String>> selectedRow = PanelUtils.getSelectedRow((JTable) e.getSource(), e.getPoint());
			if (selectedRow.isPresent()) {
				popup.initDataAndPosition(e, selectedRow);
				popup.show(e);
			}
			LOG.debug("End right mouse");
		}
	}
}
