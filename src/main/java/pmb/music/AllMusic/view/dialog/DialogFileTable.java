/**
 * 
 */
package pmb.music.AllMusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.model.FichierDialogModel;
import pmb.music.AllMusic.view.popup.DialogFilePopupMenu;

/**
 * Une "pop-up" permettant d'afficher une liste de {@link Fichier}.
 * 
 * @see {@link JDialog}
 * @author pmbroca
 */
public class DialogFileTable extends JDialog {

	private static final long serialVersionUID = 1304786661370052913L;

	private static final Logger LOG = Logger.getLogger(DialogFileTable.class);

	private List<Composition> compoList = new ArrayList<>();

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

	private JTable fichiers;
	private int defaultSort;
	private DialogFilePopupMenu popup;

	/**
	 * Constructeur de {@link DialogFileTable}.
	 * 
	 * @param parent {@link JFrame} la fenetre parente
	 * @param header {@link String} les entetes de la popup
	 * @param modal {@code boolean} si la popup bloque l'utilisateur
	 * @param compoList {@code List<Composition>} la liste des compositions dont les
	 *            fichiers seront affichés
	 * @param height la hauteur de la popup
	 * @param defaultSort the index of the sorted column at start
	 */
	public DialogFileTable(JFrame parent, String header, boolean modal, List<Composition> compoList, int height,
			int defaultSort) {
		super(parent, header, modal);
		LOG.debug("Start DialogFileTable");
		this.setSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width - 100, height));
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.compoList = compoList;
		this.defaultSort = defaultSort;
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

	private void initComponent() {
		LOG.debug("Start initComponent");
		fichiers = new JTable();
		fichiers.setAutoCreateRowSorter(true);
		fichiers.setRowHeight(30);
		fichiers.getTableHeader().setResizingAllowed(true);
		fichiers.setFillsViewportHeight(true);
		fichiers.setBackground(UIManager.getColor("Label.background"));
		fichiers.setFont(UIManager.getFont("Label.font"));
		fichiers.setBorder(UIManager.getBorder("Label.border"));
		fichiers.setModel(
				new FichierDialogModel(FichierUtils.convertCompositionListToFichierVector(compoList, true, false),
						new Vector<>(Arrays.asList(header))));
		fichiers.getRowSorter().toggleSortOrder(defaultSort);
		((TableRowSorter<?>) fichiers.getRowSorter()).setComparator(INDEX_PERCENT_DELETED, MiscUtils.comparePercentage);

		popup = new DialogFilePopupMenu(fichiers, INDEX_ARTIST, INDEX_TITLE, INDEX_FILE_NAME, INDEX_AUTEUR, INDEX_RANK);
		fichiers.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// Nothing to do
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
					popup.show(e);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// Nothing to do
			}
		});
		fichiers.addMouseListener(pasteFichierListener());
		this.getRootPane().registerKeyboardAction(e -> {
			this.dispose();
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		PanelUtils.colRenderer(fichiers, true, INDEX_DELETED, INDEX_TYPE, INDEX_CAT);
		fichiers.removeColumn(fichiers.getColumnModel().getColumn(INDEX_DELETED));

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

	private void mouseAction(MouseEvent e) {
		Optional<Vector<String>> selectedRow = PanelUtils.getSelectedRow((JTable) e.getSource(), e.getPoint());
		popup.initDataAndPosition(e, selectedRow);
		if (!selectedRow.isPresent()) {
			return;
		}
		if (SwingUtilities.isRightMouseButton(e)) {
			popup.show(e);
		} else if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			LOG.debug("Start double right mouse");
			// Double click gauche -> Ouvre une popup pour afficher les compositions du
			// fichier sélectionné
			List<Composition> compo = ImportXML
					.importXML(Constant.getXmlPath() + selectedRow.get().get(INDEX_FILE_NAME) + Constant.XML_EXTENSION);
			List<String> title = new ArrayList<>();
			if (!compo.isEmpty()) {
				Fichier file = compo.get(0).getFiles().get(0);
				title = Arrays.asList("FileName:", file.getFileName(), "PublishYear:",
						String.valueOf(file.getPublishYear()), "Categorie:", file.getCategorie().getCat(),
						"RangeDateBegin:", String.valueOf(file.getRangeDateBegin()), "RangeDateEnd:",
						String.valueOf(file.getRangeDateEnd()), "Sorted:", String.valueOf(file.getSorted()), "Size:",
						String.valueOf(file.getSize()));
			} else {
				LOG.warn(selectedRow.get().get(INDEX_FILE_NAME) + " empty !");
			}
			DialogCompoTable pop = new DialogCompoTable(null, StringUtils.join(title, " / "), true, compo, 800);
			pop.showDialogFileTable();
			LOG.debug("End double right mouse");
		}
	}
}
