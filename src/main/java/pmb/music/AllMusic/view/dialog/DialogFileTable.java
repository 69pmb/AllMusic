/**
 * 
 */
package pmb.music.AllMusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.TableBuilder;
import pmb.music.AllMusic.view.component.MyTable;
import pmb.music.AllMusic.view.model.FichierDialogModel;
import pmb.music.AllMusic.view.popup.DialogFilePopupMenu;

/**
 * Une "pop-up" permettant d'afficher une liste de {@link Fichier}.
 * 
 * @see {@link JDialog}
 */
public class DialogFileTable {
	private static final Logger LOG = Logger.getLogger(DialogFileTable.class);
	private JDialog dialog;
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
	public static final int INDEX_SORTED = 12;

	private int defaultSort;
	private MyTable fichiers;

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
		LOG.debug("Start DialogFileTable");
		this.dialog = new JDialog(parent, header, modal);
		this.dialog.setSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width - 100, height));
		this.dialog.setLocationRelativeTo(null);
		this.dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.compoList = compoList;
		this.defaultSort = defaultSort;
		this.dialog.setResizable(true);
		this.initComponent();
		LOG.debug("End DialogFileTable");
	}

	/**
	 * Affiche une {@link DialogFileTable}.
	 */
	public void showDialogFileTable() {
		this.dialog.setVisible(true);
	}

	private void initComponent() {
		LOG.debug("Start initComponent");

		try {
			fichiers = new TableBuilder()
					.withModelAndData(FichierUtils.convertCompositionListToFichierVector(compoList, true, false),
							header, FichierDialogModel.class)
					.withDefaultRowSorterListener(null).withMouseClickAction(mouseAction)
					.withPopupMenu(new DialogFilePopupMenu(INDEX_ARTIST, INDEX_TITLE, INDEX_FILE_NAME, INDEX_AUTEUR,
							INDEX_RANK))
					.withKeyListener().build();
			fichiers.getRowSorter().toggleSortOrder(defaultSort);
			((TableRowSorter<?>) fichiers.getRowSorter()).setComparator(INDEX_PERCENT_DELETED,
					MiscUtils.comparePercentage);
			PanelUtils.colRenderer(fichiers.getTable(), true, INDEX_DELETED, INDEX_TYPE, INDEX_CAT, null, null,
					INDEX_SORTED, INDEX_RANK);
			fichiers.removeColumn(fichiers.getColumnModel().getColumn(INDEX_DELETED));

			this.dialog.getRootPane().registerKeyboardAction(e -> this.dialog.dispose(),
					KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
			this.dialog.setLayout(new BorderLayout());
			this.dialog.add(new JScrollPane(fichiers.getTable()), BorderLayout.CENTER);
		} catch (MyException e1) {
			LOG.error("An error occured when init Dialog File table", e1);
			return;
		}

		LOG.debug("End initComponent");
	}

	private Consumer<MouseEvent> mouseAction = e -> {
		Optional<Vector<String>> selectedRow = PanelUtils.getSelectedRow((JTable) e.getSource(), e.getPoint());
		fichiers.getPopupMenu().initDataAndPosition(e, selectedRow);
		if (!selectedRow.isPresent()) {
			return;
		}
		if (SwingUtilities.isRightMouseButton(e)) {
			fichiers.getPopupMenu().show(e);
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
	};
}
