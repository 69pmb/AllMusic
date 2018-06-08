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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.MyException;

/**
 * Une "pop-up" permettant d'afficher une liste de {@link Fichier}.
 * @see {@link JDialog}
 */
public class DialogFileTable extends JDialog {

	private static final long serialVersionUID = 1304786661370052913L;

	private static final Logger LOG = Logger.getLogger(DialogFileTable.class);

	private List<Fichier> files = new ArrayList<>();

	private static final String[] header = { "Artiste", "Oeuvre", "Type", "Auteur", "Nom du fichier",
			"Date de publication", "Categorie", "Dates", "Taille", "Classement", "Classé" };

	private static final int VECTOR_INDEX_OEUVRE = 1;
	private static final int VECTOR_INDEX_FILE_NAME = 4;

	private JTable fichiers;

	/**
	 * Constructeur de {@link DialogFileTable}.
	 * @param parent {@link JFrame} la fenetre parente
	 * @param header {@link String} les entetes de la popup
	 * @param modal {@code boolean} si la popup bloque l'utilisateur
	 * @param files {@code List<Fichier>} la liste des fichier à afficher
	 * @param dim {@link Dimension} les dimension de la popup
	 */
	public DialogFileTable(JFrame parent, String header, boolean modal, List<Fichier> files, Dimension dim) {
		super(parent, header, modal);
		LOG.debug("Start DialogFileTable");
		this.setSize(dim);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.files = files;
		this.setResizable(true);
		this.initComponent();
		LOG.debug("End DialogFileTable");
	}

	/**
	 * Affiche une {@link DialogFileTable}.
	 */
	public void showDialogFileTable() {
		this.setVisible(true);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initComponent() {
		LOG.debug("Start initComponent");
		fichiers = new JTable();
		fichiers.setAutoCreateRowSorter(true);
		fichiers.setRowHeight(30);
		fichiers.setFillsViewportHeight(true);
		fichiers.setBackground(UIManager.getColor("Label.background"));
		fichiers.setFont(UIManager.getFont("Label.font"));
		fichiers.setBorder(UIManager.getBorder("Label.border"));
		fichiers.setModel(
				new FichierModel(FichierUtils.convertListForJTable(files, true), new Vector(Arrays.asList(header))));
		fichiers.getRowSorter().toggleSortOrder(VECTOR_INDEX_OEUVRE);

		fichiers.addMouseListener(pasteFichierListener());

		PanelUtils.colRenderer(fichiers, true);

		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(fichiers), BorderLayout.CENTER);
		LOG.debug("End initComponent");
	}

	private MouseAdapter pasteFichierListener() {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				mouseAction(e);
			}
		};
	}

	@SuppressWarnings("unchecked")
	private void mouseAction(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e) && e.getClickCount() == 2) {
			LOG.debug("Start fichier mouse");
			// Double click droit -> ouvre le fichier XML
			JTable target = (JTable) e.getSource();
			Vector<String> v = (Vector<String>) ((FichierModel) target.getModel()).getDataVector()
					.get(target.getRowSorter().convertRowIndexToModel(target.getSelectedRow()));
			String absFile = Constant.XML_PATH + v.get(VECTOR_INDEX_FILE_NAME) + Constant.XML_EXTENSION;
			try {
				FichierUtils.openFileInNotepad(Optional.of(absFile));
			} catch (MyException e1) {
				LOG.error("Erreur lors de l'ouverture du fichier: " + absFile, e1);
			}
			LOG.debug("End fichier mouse");
		} else if (SwingUtilities.isRightMouseButton(e) && e.getClickCount() == 1) {
			LOG.debug("Start right mouse");
			// simple click droit -> copie le nom du fichier dans le presse papier
			JTable target = (JTable) e.getSource();
			int rowAtPoint = target
					.rowAtPoint(SwingUtilities.convertPoint(target, new Point(e.getX(), e.getY()), target));
			if (rowAtPoint > -1) {
				target.setRowSelectionInterval(rowAtPoint, rowAtPoint);
			}
			Vector<String> v = (Vector<String>) ((FichierModel) target.getModel()).getDataVector()
					.get(target.getRowSorter().convertRowIndexToModel(rowAtPoint));
			StringSelection selection = new StringSelection(v.get(VECTOR_INDEX_FILE_NAME));
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
			LOG.debug("End right mouse");
		} else if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			LOG.debug("Start double right mouse");
			// Double click gauche -> Ouvre une popup pour afficher les compositions du
			// fichier sélectionné
			JTable target = (JTable) e.getSource();
			Vector<String> v = (Vector<String>) ((FichierModel) target.getModel()).getDataVector()
					.get(target.getRowSorter().convertRowIndexToModel(target.getSelectedRow()));
			List<Composition> compo = ImportXML
					.importXML(Constant.XML_PATH + v.get(VECTOR_INDEX_FILE_NAME) + Constant.XML_EXTENSION);
			List<String> title = new ArrayList<>();
			if (!compo.isEmpty()) {
				Fichier file = compo.get(0).getFiles().get(0);
				title = Arrays.asList("FileName:", file.getFileName(), "PublishYear:",
						String.valueOf(file.getPublishYear()), "Categorie:", file.getCategorie().toString(),
						"RangeDateBegin:", String.valueOf(file.getRangeDateBegin()), "RangeDateEnd:",
						String.valueOf(file.getRangeDateEnd()), "Sorted:", String.valueOf(file.getSorted()), "Size:",
						String.valueOf(file.getSize()));
			} else {
				LOG.warn(v.get(VECTOR_INDEX_FILE_NAME) + " empty !");
			}
			DialogCompoTable pop = new DialogCompoTable(null, StringUtils.join(title, " "), true, compo,
					new Dimension(1500, 400));
			pop.showDialogFileTable();
			LOG.debug("End double right mouse");
		}
	}
}
