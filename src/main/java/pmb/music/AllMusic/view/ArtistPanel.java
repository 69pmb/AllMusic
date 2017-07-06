/**
 * 
 */
package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;

/**
 * L'onglet Artiste, classement des artistes les plus cités.
 * @author i2113mj
 */
public class ArtistPanel extends JPanel {

	private static final long serialVersionUID = 2593372709628283573L;

	private static final Logger LOG = Logger.getLogger(ArtistPanel.class);

	private JTable table;

	private ArtistModel model;

	private List<Composition> list;

	private static final String[] title = { "Artiste", "Nombre d'occurrences", "Album", "Chanson" };
	
	private int selectedRow = -1;

	/**
	 * Génère le panel artiste.
	 */
	public ArtistPanel() {
		super();
		table = new JTable();
		table.setAutoCreateRowSorter(true);
		table.setRowHeight(30);
		table.setFillsViewportHeight(true);
		table.setBackground(UIManager.getColor("Label.background"));
		table.setFont(UIManager.getFont("Label.font"));
		table.setBorder(UIManager.getBorder("Label.border"));
		model = new ArtistModel(new Object[0][2], title);
		table.setModel(model);
		table.setRowSorter(new TableRowSorter<TableModel>(model));
		table.getRowSorter().toggleSortOrder(1);
		table.getRowSorter().toggleSortOrder(1);
		colRenderer();
		updateArtistPanel();
		table.addMouseListener(new MouseAdapter() {

			@SuppressWarnings("unchecked")
			@Override
			public void mouseClicked(MouseEvent e) {
				mouseClickAction(e);
			}
		});

		table.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// Nothing to do
			}

			@SuppressWarnings("unchecked")
			@Override
			public void keyReleased(KeyEvent e) {
				selectedRow = keyShortcutAction(e, selectedRow);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// Nothing to do
			}
		});

		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(table), BorderLayout.CENTER);
	}

	/**
	 *  Met à jour le panel artiste à l'aide du fichier final.
	 */
	public void updateArtistPanel() {
		LOG.debug("Start updateArtistPanel");
		model.setRowCount(0);
		list = ImportXML.importXML(Constant.FINAL_FILE_PATH);
		model.setDataVector(CompositionUtils.convertCompositionListToArtistVector(list), new Vector<>(Arrays.asList(title)));
		colRenderer();
		model.fireTableDataChanged();
		table.getRowSorter().toggleSortOrder(1);
		table.getRowSorter().toggleSortOrder(1);
		table.repaint();
		selectedRow = -1;
		LOG.debug("End updateArtistPanel");
	}

	private void colRenderer() {
		TableColumnModel modelecolonne = table.getColumnModel();
		int total = modelecolonne.getColumnCount();
		for (int i = 0; i < total; i++) {
			int taille = 0;
			int total2 = table.getRowCount();
			for (int j = 0; j < total2; j++) {
				int taille2 = table.getValueAt(j, i).toString().length() * 7; // determination
				// arbitraire
				if (taille2 > taille) {
					taille = taille2;
				}
			}
			modelecolonne.getColumn(i).setPreferredWidth(taille + 50);
		}

		DefaultTableCellRenderer renderer = new EvenOddRenderer();
		for (int i = 0; i < table.getColumnCount(); i++) {
			renderer.setHorizontalAlignment(JLabel.CENTER);
			table.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}
	}

	private void mouseClickAction(MouseEvent e) {
		JTable target = (JTable) e.getSource();
		if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			LOG.debug("Start artist mouse");
			// Affiche tous les fichiers de l'artiste double cliqué
			Vector<String> v = (Vector<String>) ((ArtistModel) target.getModel()).getDataVector()
					.get(target.getRowSorter().convertRowIndexToModel(target.getSelectedRow()));
			LOG.debug(v);
			List<Fichier> files = new ArrayList<>();
			List<Composition> findByArtist = CompositionUtils.findByArtist(list, v.get(0));
			for (Composition composition : findByArtist) {
				files.addAll(composition.getFiles());
			}
			DialogFileTable pop = new DialogFileTable(null, "Fichier", true, files, new Dimension(1500, 600));
			pop.showDialogFileTable();
			LOG.debug("End artist mouse");
		} else if (SwingUtilities.isRightMouseButton(e)) {
			LOG.debug("Start artist right mouse");
			// Copie dans le presse papier le nom de l'artiste
			int rowAtPoint = target.rowAtPoint(SwingUtilities.convertPoint(target, new Point(e.getX(), e.getY()), target));
			if (rowAtPoint > -1) {
				target.setRowSelectionInterval(rowAtPoint, rowAtPoint);
			}
			Vector<String> v = (Vector<String>) ((ArtistModel) target.getModel()).getDataVector()
					.get(target.getRowSorter().convertRowIndexToModel(rowAtPoint));
			StringSelection selection = new StringSelection(v.get(0));
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
			LOG.debug("End artist right mouse");
		}
	}

	private int keyShortcutAction(KeyEvent e, int selectedRow) {
		LOG.debug("Start keyShortcutAction");
		JTable target = (JTable) e.getSource();
		String keyChar = String.valueOf(e.getKeyChar());
		TableModel tableModel = target.getModel();
		int startRow = selectedRow;
		if (selectedRow == tableModel.getRowCount() - 1) {
			startRow = -1;// Go before start
		}
		// Check each cell to see if it starts with typed char.
		// if so set corresponding row selected and return.
		for (int row = startRow + 1; row < tableModel.getRowCount(); row++) {
			String value = ((Vector<String>) ((ArtistModel) target.getModel()).getDataVector().get(target.getRowSorter().convertRowIndexToModel(row)))
					.get(0);
			if (value != null && value.toLowerCase().startsWith(keyChar.toLowerCase())) {
				target.getSelectionModel().clearSelection();
				target.getColumnModel().getSelectionModel().clearSelection();
				target.changeSelection(row, 0, true, false);
				target.setRowSelectionInterval(row, row);
				LOG.debug("End keyShortcutAction");
				return row;
			}
		}
		LOG.debug("End keyShortcutAction, no result");
		return -1;
	}

}
