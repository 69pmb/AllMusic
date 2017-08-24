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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.utils.CompositionUtils;

/**
 * Une "pop-up" permettant d'afficher une liste de {@link Composition}.
 * @see {@link JDialog}
 */
public class DialogCompoTable extends JDialog {

	private static final long serialVersionUID = 1304786661370052913L;

	private static final Logger LOG = Logger.getLogger(DialogCompoTable.class);

	private List<Composition> compo = new ArrayList<>();

	private static final String[] header = { "Artiste", "Titre", "Classement", "Type" };

	private JTable table;

	/**
	 * Constructeur.
	 * @param parent {@link JFrame} la fenetre parente
	 * @param header {@link String} les entetes de la popup
	 * @param modal {@code boolean} si la popup bloque l'utilisateur
	 * @param files {@code List<Fichier>} la liste des fichier à afficher
	 * @param dim {@link Dimension} les dimension de la popup
	 */
	public DialogCompoTable(JFrame parent, String header, boolean modal, List<Composition> compo, Dimension dim) {
		super(parent, header, modal);
		LOG.debug("Start DialogFileTable");
		this.setSize(dim);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initComponent() {
		LOG.debug("Start initComponent");
		table = new JTable();
		table.setAutoCreateRowSorter(true);
		table.setRowHeight(30);
		table.setFillsViewportHeight(true);
		table.setBackground(UIManager.getColor("Label.background"));
		table.setFont(UIManager.getFont("Label.font"));
		table.setBorder(UIManager.getBorder("Label.border"));
		table.setModel(new CompoDialogModel(CompositionUtils.convertCompoListForJTable(compo), new Vector(Arrays.asList(header))));
		table.getRowSorter().toggleSortOrder(2);
		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				mouseAction(e);
			}
		});

		TableColumnModel modelecolonne = table.getColumnModel();
		int total = modelecolonne.getColumnCount();
		for (int i = 0; i < total; i++) {
			int taille = 0;
			int total2 = table.getRowCount();
			for (int j = 0; j < total2; j++) {
				int taille2 = table.getValueAt(j, i).toString().length() * 7;
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

		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(table), BorderLayout.CENTER);
		LOG.debug("End initComponent");
	}

	@SuppressWarnings("unchecked")
	private void mouseAction(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			LOG.debug("Start right mouse");
			// Copie dans le clipboard l'artist et l'oeuvre
			JTable target = (JTable) e.getSource();
			int rowAtPoint = target.rowAtPoint(SwingUtilities.convertPoint(target, new Point(e.getX(), e.getY()), target));
			if (rowAtPoint > -1) {
				target.setRowSelectionInterval(rowAtPoint, rowAtPoint);
			}
			Vector<String> v = (Vector<String>) ((CompoDialogModel) target.getModel()).getDataVector().get(target.getRowSorter().convertRowIndexToModel(rowAtPoint));
			StringSelection selection = new StringSelection(v.get(0) + " " + v.get(1));
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
			LOG.debug("End right mouse");
		}
	}
}
