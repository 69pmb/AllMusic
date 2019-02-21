/**
 * 
 */
package pmb.music.AllMusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.TableBuilder;
import pmb.music.AllMusic.view.component.MyTable;
import pmb.music.AllMusic.view.model.FichierDialogModel;
import pmb.music.AllMusic.view.panel.OngletPanel;
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
					.withDefaultRowSorterListener(null).withMouseClickAction(e -> {
						Optional<Vector<String>> selectedRow = PanelUtils.getSelectedRow((JTable) e.getSource(),
								e.getPoint());
						fichiers.getPopupMenu().initDataAndPosition(e, selectedRow.orElse(null));
						if (!selectedRow.isPresent()) {
							return;
						}
						if (SwingUtilities.isRightMouseButton(e)) {
							fichiers.getPopupMenu().show(e);
						} else if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
							LOG.debug("Start double right mouse");
							// Double click gauche -> Ouvre une popup pour afficher les compositions du
							// fichier sélectionné
							List<Composition> compo = ImportXML.importXML(Constant.getXmlPath()
									+ selectedRow.get().get(INDEX_FILE_NAME) + Constant.XML_EXTENSION);
							List<String> title = new ArrayList<>();
							if (!compo.isEmpty()) {
								Fichier file = compo.get(0).getFiles().get(0);
								title = Arrays.asList("FileName:", file.getFileName(), "PublishYear:",
										String.valueOf(file.getPublishYear()), "Categorie:",
										file.getCategorie().getCat(), "RangeDateBegin:",
										String.valueOf(file.getRangeDateBegin()), "RangeDateEnd:",
										String.valueOf(file.getRangeDateEnd()), "Sorted:",
										String.valueOf(file.getSorted()), "Size:", String.valueOf(file.getSize()));
							} else {
								LOG.warn(selectedRow.get().get(INDEX_FILE_NAME) + " empty !");
							}
							DialogCompoTable pop = new DialogCompoTable(null, StringUtils.join(title, " / "), true,
									compo, 800);
							pop.showDialogFileTable();
							LOG.debug("End double right mouse");
						}
					}).withPopupMenu(new DialogFilePopupMenu(this, INDEX_ARTIST, INDEX_TITLE, INDEX_FILE_NAME,
							INDEX_AUTEUR, INDEX_RANK))
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

	/**
	 * Launchs a dialog to modify the selected composition.
	 * 
	 * @param selected the selected row representing a composition
	 * @throws MyException if something went wrong
	 */
	public void modifyCompositionAction(Vector<Object> selected) throws MyException {
		LOG.debug("Start modifyCompositionAction");
		OngletPanel.getArtist().interruptUpdateArtist(true);
		List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
		// On récupère la composition à modifier
		String fileName = (String) selected.get(INDEX_FILE_NAME);
		String artist = (String) selected.get(INDEX_ARTIST);
		String titre = (String) selected.get(INDEX_TITLE);
		List<Composition> xmlFile = ImportXML.importXML(Constant.getXmlPath() + fileName + Constant.XML_EXTENSION);
		Composition edited = xmlFile.stream()
				.filter(c -> c.getFiles().get(0).getClassement().equals((Integer) selected.get(INDEX_RANK))
						&& StringUtils.equals(c.getTitre(), titre)
						&& c.getFiles().get(0).getSize().equals((Integer) selected.get(INDEX_FILE_SIZE))
						&& StringUtils.equals(c.getArtist(), artist))
				.findFirst().orElseThrow(() -> new MyException("Can't find edited file"));
		Fichier editedFile = edited.getFiles().get(0);
		// Lancement de la popup de modification
		ModifyCompositionDialog md = new ModifyCompositionDialog(
				selected.stream().map(Object::toString).collect(Collectors.toCollection(Vector::new)), INDEX_ARTIST,
				INDEX_TITLE, INDEX_TYPE, INDEX_DELETED);
		md.showModifyCompositionDialog();
		Vector<String> editedRow;
		if (md.isSendData()) {
			// On recupère la compo si elle a bien été modifiée
			LOG.debug("Composition modifiée");
			editedRow = md.getCompo();
		} else {
			LOG.debug("Aucune modification");
			return;
		}

		// Update composition from dialog
		edited.setArtist(editedRow.get(INDEX_ARTIST));
		edited.setTitre(editedRow.get(INDEX_TITLE));
		edited.setRecordType(RecordType.valueOf(editedRow.get(INDEX_TYPE)));
		edited.setDeleted(Boolean.valueOf(editedRow.get(INDEX_DELETED)));

		Predicate<Fichier> filterFile = f -> !(f.getClassement().equals((Integer) selected.get(INDEX_RANK))
				&& StringUtils.equals(f.getFileName(), (String) selected.get(INDEX_FILE_NAME))
				&& f.getSize().equals((Integer) selected.get(INDEX_FILE_SIZE)));

		int indexFromFinal = importXML.indexOf(CompositionUtils.findByFile(importXML, editedFile, artist, titre)
				.orElseThrow(() -> new MyException("Can't find composition in final file")));
		importXML.get(indexFromFinal).setFiles(
				importXML.get(indexFromFinal).getFiles().stream().filter(filterFile).collect(Collectors.toList()));
		int indexCompoList = compoList.indexOf(CompositionUtils.findByFile(compoList, editedFile, artist, titre)
				.orElseThrow(() -> new MyException("Can't find composition in dialog data")));
		compoList.get(indexCompoList).setFiles(
				compoList.get(indexCompoList).getFiles().stream().filter(filterFile).collect(Collectors.toList()));
		Composition compoExist = CompositionUtils.compoExist(importXML, edited);
		boolean isDeleted = false;
		if (compoExist == null) {
			LOG.debug("Pas de regroupement");
			importXML.add(edited);
		} else {
			LOG.debug("La compo existe déjà, on regroupe");
			// regroupement avec une autre composition
			isDeleted = compoExist.isDeleted() || edited.isDeleted();
			compoExist.getFiles().addAll(edited.getFiles());
			compoExist.setDeleted(isDeleted);
			edited.setDeleted(isDeleted);
			// Liste des compositions affichées
			Composition compoExistResult = CompositionUtils.compoExist(compoList, edited);
			if (compoExistResult != null) {
				// La compo apparait bien dans les resultats de recherche
				compoExistResult.getFiles().addAll(edited.getFiles());
				compoExistResult.setDeleted(isDeleted);
			}
		}

		if (OngletPanel.getOnglets().getSelectedIndex() == 0) {
			LOG.debug("Updates search panel data");
			List<Composition> searchPanelCompo = OngletPanel.getSearch().getCompoResult();
			int indexOfSearchPanel = searchPanelCompo
					.indexOf(CompositionUtils.findByFile(searchPanelCompo, editedFile, artist, titre)
							.orElseThrow(() -> new MyException("Can't find edited composition in search parnel data")));
			searchPanelCompo.get(indexOfSearchPanel).setFiles(searchPanelCompo.get(indexOfSearchPanel).getFiles()
					.stream().filter(filterFile).collect(Collectors.toList()));
			compoExist = CompositionUtils.compoExist(searchPanelCompo, edited);
			if (compoExist == null) {
				searchPanelCompo.add(indexOfSearchPanel, edited);
			} else {
				compoExist.getFiles().addAll(edited.getFiles());
				compoExist.setDeleted(isDeleted);
			}
			OngletPanel.getSearch().updateTable();
		}

		// Updates fichier panel data
		PanelUtils.updateFichierPanelData(selected, INDEX_ARTIST, INDEX_TITLE, INDEX_TYPE, new JLabel(), edited);

		try {
			ExportXML.exportXML(importXML, Constant.getFinalFile());
			OngletPanel.getArtist().updateArtistPanel();
		} catch (IOException e1) {
			LOG.error("Erreur lors de l'export du fichier final !!", e1);
		}

		try {
			ExportXML.exportXML(xmlFile, fileName);
		} catch (IOException e) {
			LOG.error("Erreur lors de la modification d'une composition dans le fichier: " + fileName, e);
		}

		// Update dialog
		fichiers.getModel().setRowCount(0);
		fichiers.getModel().setDataVector(FichierUtils.convertCompositionListToFichierVector(compoList, true, false),
				new Vector<>(Arrays.asList(header)));
		PanelUtils.colRenderer(fichiers.getTable(), true, INDEX_DELETED, INDEX_TYPE, INDEX_CAT, null, null,
				INDEX_SORTED, INDEX_RANK);
		fichiers.removeColumn(fichiers.getColumnModel().getColumn(INDEX_DELETED));
		LOG.debug("End modifyCompositionAction");
	}
}
