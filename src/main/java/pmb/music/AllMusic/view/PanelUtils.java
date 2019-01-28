package pmb.music.AllMusic.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.swing.FontIcon;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.utils.SearchUtils;
import pmb.music.AllMusic.view.dialog.ModifyCompositionDialog;
import pmb.music.AllMusic.view.model.AbstractModel;
import pmb.music.AllMusic.view.panel.ArtistPanel;
import pmb.music.AllMusic.view.panel.FichierPanel;

public class PanelUtils {

	private static final Logger LOG = Logger.getLogger(PanelUtils.class);
	public static final int PANEL_HEIGHT = 70;
	public static final int COMPONENT_HEIGHT = 25;
	public static final FocusListener selectAll = new FocusListener() {
		@Override
		public void focusLost(FocusEvent e) {
			// Nothing to do
		}

		@Override
		public void focusGained(FocusEvent e) {
			JTextField source = (JTextField) e.getSource();
			source.selectAll();
		}
	};

	/**
	 * Dimensionne les colonnes du tableau et ajoute des couleurs aux lignes.
	 * 
	 * @see {@link EvenOddRenderer}
	 * @param table le tableau
	 * @param lastColumn si la dernière colonne doit être colorisée ou non
	 * @param deletedIndex index of the deleted column, use it to draw deleted row
	 *            with specific color
	 * @param typeIndex index of the record type column, use it to draw type cell
	 *            with a specific color
	 * @param catIndex index of the category column, use it to draw type cell with a
	 *            specific color
	 * @param decileIndex TODO
	 * @param scoreIndex TODO
	 */
	public static void colRenderer(JTable table, boolean lastColumn, Integer deletedIndex, Integer typeIndex,
			Integer catIndex, Integer decileIndex, Integer scoreIndex) {
		LOG.debug("Start colRenderer");
		Component topComponent = (Component) SwingUtilities.getWindowAncestor(table);
		setColumnsWidth(table, topComponent != null ? topComponent.getWidth() : table.getWidth(), "Init");

		DefaultTableCellRenderer renderer = new EvenOddRenderer(deletedIndex, typeIndex, catIndex, decileIndex, scoreIndex);
		int columnCount = table.getColumnCount();
		if (!lastColumn) {
			columnCount--;
			if (deletedIndex != null) {
				columnCount--;
			}
		}
		for (int i = 0; i < columnCount; i++) {
			renderer.setHorizontalAlignment(JLabel.CENTER);
			table.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}
		LOG.debug("End colRenderer");
	}

	/**
	 * Fixe la largeur de chaques colonnes à la longueur maximum pour de chaques
	 * colonnes.
	 * 
	 * @param table le tableau
	 * @param width la largeur du composant parent quand il est redimensionné
	 * @param name name of the table
	 */
	public static void setColumnsWidth(JTable table, int width, String name) {
		LOG.trace("Start setColumnsWidth: " + name);
		boolean isTableWiderThanScreen = table.getWidth() != 0 && table.getWidth() > (width + 10);
		TableColumnModel columnModel = table.getColumnModel();
		Map<Integer, Integer> colWidth = new HashMap<>();
		Double widthDouble = new Double(width);
		int columnCount = columnModel.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			int maximum = 0;
			Object currentValue = null;
			for (int j = 0; j < table.getRowCount(); j++) {
				currentValue = table.getValueAt(j, i);
				if (currentValue == null) {
					continue;
				}
				int longueurCourante = currentValue.toString().length();
				if (longueurCourante > maximum) {
					maximum = longueurCourante;
				}
			}
			Double columnWidth = 0D;
			if (currentValue != null) {
				@SuppressWarnings("deprecation")
				FontMetrics fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(table.getFont());
				int widthFactor = fontMetrics.stringWidth(new JLabel(currentValue.toString()).getText());
				columnModel.getColumn(i).setPreferredWidth(widthFactor + 2); // valeur arbitraire
				columnWidth = new Double(widthFactor + 2);
			} else {
				columnModel.getColumn(i).setPreferredWidth(maximum * 7); // valeur arbitraire
				columnWidth = new Double(maximum) * 7D;
			}

			if (isTableWiderThanScreen) {
				// If table wider than given width, calculates a column width
				Double tableWidthDouble = new Double(table.getWidth());
				Double ratio = widthDouble / tableWidthDouble;
				// Calcule le ratio entre la largeur donnée et la largeur du tableau
				int round = (int) Math.round(columnWidth * ratio);
				colWidth.put(i, round);
			}
		}
		if (isTableWiderThanScreen) {
			LOG.debug("Table is wider than Screen");
			// Sum of the calculates columns width
			Integer sum = colWidth.values().stream().mapToInt(Integer::intValue).sum();
			Double sumDouble = new Double(sum);
			Double columnCountDouble = new Double(columnCount);
			if (sum.compareTo(width) > 0) {
				LOG.debug("Too large");
				// If the calculates column are too large
				Double offset = (sumDouble - widthDouble) / columnCountDouble;
				colWidth.entrySet().stream().forEach(e -> e.setValue(e.getValue() - (int) Math.round(offset)));
			} else {
				LOG.debug("Too thin");
				// If the calculates column are too thin
				Double offset = (widthDouble - sumDouble) / columnCountDouble;
				colWidth.entrySet().stream().forEach(e -> e.setValue(e.getValue() + (int) Math.round(offset)));
			}
			sum = colWidth.values().stream().mapToInt(Integer::intValue).sum();
			// Set of the columns max/min width for each column
			colWidth.entrySet().stream().forEach(e -> columnModel.getColumn(e.getKey()).setMaxWidth(e.getValue()));
			colWidth.entrySet().stream().forEach(e -> columnModel.getColumn(e.getKey()).setMinWidth(e.getValue()));
		}
		LOG.trace("End setColumnsWidth: " + name);
	}

	/**
	 * Finds in the data table model the selected row.
	 * 
	 * @param <T> the model of the table
	 * @param target the table selected
	 * @param point the spot clicked
	 * @return the selected row
	 */
	@SuppressWarnings("unchecked")
	public static <T extends AbstractModel> Optional<Vector<String>> getSelectedRow(JTable target, Point point) {
		LOG.debug("Start getSelectedRow");
		int rowAtPoint = target.rowAtPoint(SwingUtilities.convertPoint(target, point, target));
		Vector<String> selectedRow = null;
		if (rowAtPoint > -1) {
			LOG.debug("Found selectedRow");
			target.setRowSelectionInterval(rowAtPoint, rowAtPoint);
			selectedRow = (Vector<String>) ((T) target.getModel()).getDataVector()
					.get(target.getRowSorter().convertRowIndexToModel(rowAtPoint));
		}
		LOG.debug("End getSelectedRow");
		return Optional.ofNullable(selectedRow);
	}

	@SuppressWarnings("unchecked")
	public static <T extends AbstractModel> List<List<String>> convertDataVectorToList(JTable table) {
		List<List<String>> result = new ArrayList<>();
		TableModel model = table.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			Vector<String> selectedRow = (Vector<String>) ((T) model).getDataVector()
					.get(table.getRowSorter().convertRowIndexToModel(i));
			List<String> row = new ArrayList<>();
			for (int j = 0; j < model.getColumnCount(); j++) {
				if (model.getColumnClass(j) == Boolean.class) {
					row.add(String.valueOf(selectedRow.get(j)));
				} else if (model.getColumnClass(j) == Integer.class || model.getColumnClass(j) == Double.class
						|| model.getColumnClass(j) == Long.class) {
					row.add(String.valueOf(selectedRow.get(j)));
				} else if (model.getColumnClass(j) == Date.class) {
					row.add(new Constant().getSdfDate().format(selectedRow.get(j)));
				} else {
					row.add(selectedRow.get(j));
				}
			}
			result.add(row);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static int keyShortcutAction(KeyEvent e, int selectedRow, Integer sortedColumn) {
		if (!Character.isLetter(e.getKeyChar()) && !Character.isDigit(e.getKeyChar())) {
			return selectedRow;
		}
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
			Vector<String> vector = (Vector<String>) ((AbstractModel) target.getModel()).getDataVector()
					.get(target.getRowSorter().convertRowIndexToModel(row));
			Object value = vector.get(sortedColumn != null ? sortedColumn : 0);
			if (value != null
					&& ((value instanceof String && ((String) value).toLowerCase().startsWith(keyChar.toLowerCase()))
							|| ((value instanceof Integer || value instanceof Long)
									&& String.valueOf(value).toLowerCase().startsWith(keyChar.toLowerCase())))) {
				target.getSelectionModel().clearSelection();
				target.getColumnModel().getSelectionModel().clearSelection();
				target.changeSelection(row, 0, true, false);
				target.setRowSelectionInterval(row, row);
				LOG.debug("End keyShortcutAction");
				return row;
			}
		}
		LOG.debug("End keyShortcutAction, no result");
		return selectedRow != -1 ? keyShortcutAction(e, -1, sortedColumn) : -1;
	}

	/**
	 * Supprime les compositions sélectionnées du tableau et des fichiers XML.
	 * 
	 * @param artistPanel le panel artiste
	 * @param fichierPanel le panel des fichiers
	 * @param compoList la liste de composition
	 * @param selected les lignes sélectionnées
	 * @param label le label de résultat
	 * @param artistIndex index de l'artist
	 * @param titleIndex index du titre
	 * @param typeIndex index du type
	 */
	@SuppressWarnings("unchecked")
	public static void deleteCompositionAction(final ArtistPanel artistPanel, FichierPanel fichierPanel,
			List<Composition> compoList, List<Object> selected, JLabel label, int artistIndex, int titleIndex,
			int typeIndex) {
		LOG.debug("Start deleteCompositionAction");
		if (selected.isEmpty()) {
			label.setText("Aucune composition sélectionnée !");
			LOG.debug("End deleteCompositionAction, no selected composition");
			return;
		}
		label.setText("");
		artistPanel.interruptUpdateArtist(true);
		List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
		for (Object o : selected) {
			Vector<String> v = (Vector<String>) o;
			try {
				Composition toRemoveToFinal = CompositionUtils.findByArtistTitreAndType(importXML, v.get(artistIndex),
						v.get(titleIndex), v.get(typeIndex), true);
				Composition toRemoveToTable = CompositionUtils.findByArtistTitreAndType(compoList, v.get(artistIndex),
						v.get(titleIndex), v.get(typeIndex), true);
				compoList.get(SearchUtils.indexOf(compoList, toRemoveToTable)).setDeleted(true);
				importXML.get(SearchUtils.indexOf(importXML, toRemoveToFinal)).setDeleted(true);
				CompositionUtils.removeCompositionsInFiles(toRemoveToFinal);
				// Update fichier panel data
				try {
					for (Fichier file : toRemoveToFinal.getFiles()) {
						List<Composition> compoListFromData = fichierPanel.getCompoListFromData(file);
						compoListFromData.get(SearchUtils.indexOf(compoListFromData, toRemoveToFinal)).setDeleted(true);
					}
				} catch (Exception e) {
					LOG.warn("Erreur lors de la mise à jour du panel", e);
				}
			} catch (MyException e1) {
				LOG.error("Erreur lors de la suppression d'une composition", e1);
				return;
			}
		}
		try {
			ExportXML.exportXML(importXML, Constant.getFinalFile());
			artistPanel.updateArtistPanel();
		} catch (IOException e1) {
			LOG.error("Erreur lors de l'export du fichier final", e1);
			label.setText("Erreur lors de l'export du fichier final !!" + e1);
		}
		label.setText(selected.size() + " élément(s) supprimé(s)");
		LOG.debug("End deleteCompositionAction");
	}

	/**
	 * Launchs a dialog to modify the selected composition.
	 * 
	 * @param artistPanel to stop calculation
	 * @param fichierPanel to modify its data
	 * @param selectedRow the selected composition
	 * @param compositionList the composition list displayed
	 * @param indexArtist index of the artist in the vector
	 * @param indexTitre index of the title in the vector
	 * @param indexType index of the record type in the vector
	 * @param indexDeleted index of the deleted boolean in the vector
	 * @param resultLabel result of the process
	 */
	public static void modificationCompositionAction(final ArtistPanel artistPanel, FichierPanel fichierPanel,
			Vector<String> selectedRow, List<Composition> compositionList, int indexArtist, int indexTitre,
			int indexType, int indexDeleted, JLabel resultLabel) {
		LOG.debug("Start modificationCompositionAction");
		artistPanel.interruptUpdateArtist(true);
		String label = "Élément modifié";
		Composition toModif;
		List<Composition> importXML;
		importXML = ImportXML.importXML(Constant.getFinalFilePath());
		try {
			// On récupère la composition à modifier
			toModif = CompositionUtils.findByArtistTitreAndType(compositionList, selectedRow.get(indexArtist),
					selectedRow.get(indexTitre), selectedRow.get(indexType), true);
		} catch (MyException e1) {
			String log = "Erreur dans modificationCompositionAction, impossible de trouver la compo à modifier";
			LOG.error(log, e1);
			resultLabel.setText(log + e1.getMessage());
			return;
		}
		int indexOfXml = importXML.indexOf(CompositionUtils.findByFile(importXML, toModif.getFiles().get(0),
				Optional.of(selectedRow.get(indexArtist)), Optional.of(selectedRow.get(indexTitre))).get());
		int indexOfResult = SearchUtils.indexOf(compositionList, toModif);
		// Lancement de la popup de modification
		ModifyCompositionDialog md = new ModifyCompositionDialog(null, "Modifier une composition", true,
				new Dimension(950, 150), selectedRow, indexArtist, indexTitre, indexType, indexDeleted);
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

		// On modifie la composition
		toModif.setArtist(editedRow.get(indexArtist));
		toModif.setTitre(editedRow.get(indexTitre));
		toModif.setRecordType(RecordType.valueOf(editedRow.get(indexType)));
		toModif.setDeleted(Boolean.valueOf(editedRow.get(indexDeleted)));

		// Modification du fichier final
		importXML.remove(indexOfXml);
		compositionList.remove(indexOfResult);
		Composition compoExist = CompositionUtils.compoExist(importXML, toModif);
		boolean isDeleted = false;
		if (compoExist == null) {
			LOG.debug("Pas de regroupement");
			importXML.add(toModif);
			compositionList.add(toModif);
		} else {
			LOG.debug("La compo existe déjà, on regroupe");
			// regroupement avec une autre composition
			isDeleted = compoExist.isDeleted() || toModif.isDeleted();
			compoExist.getFiles().addAll(toModif.getFiles());
			compoExist.setDeleted(isDeleted);
			toModif.setDeleted(isDeleted);
			// Liste des compositions affichées
			Composition compoExistResult = CompositionUtils.compoExist(compositionList, toModif);
			if (compoExistResult != null) {
				// La compo apparait bien dans les resultats de recherche
				compoExistResult.getFiles().addAll(toModif.getFiles());
				compoExistResult.setDeleted(isDeleted);
			}
		}
		try {
			ExportXML.exportXML(importXML, Constant.getFinalFile());
			artistPanel.updateArtistPanel();
		} catch (IOException e1) {
			String log = "Erreur lors de l'export du fichier final !!";
			LOG.error(log, e1);
			label = log;
		}

		// On modifie les fichiers xml en conséquence
		try {
			CompositionUtils.modifyCompositionsInFiles(toModif, editedRow.get(indexArtist), editedRow.get(indexTitre),
					editedRow.get(indexType), isDeleted);
		} catch (MyException e1) {
			String log = "Erreur lors de la modification d'une composition";
			LOG.error(log, e1);
			resultLabel.setText(log + e1);
			return;
		}

		// Modification des données de fichier panel
		if (compoExist == null) {
			for (Fichier file : toModif.getFiles()) {
				try {
					List<Composition> compoListFichierPanel = fichierPanel.getCompoListFromData(file);
					Composition compoFichierPanel = CompositionUtils.findByArtistTitreAndType(compoListFichierPanel,
							selectedRow.get(indexArtist), selectedRow.get(indexTitre), selectedRow.get(indexType),
							false);
					compoListFichierPanel.set(SearchUtils.indexOf(compoListFichierPanel, compoFichierPanel), toModif);
					fichierPanel.setCompoListFromData(file, compoListFichierPanel);
				} catch (MyException e) {
					String log = "Impossible de mettre à jour les données de Fichier Panel";
					LOG.error(log, e);
					resultLabel.setText(log + e.getMessage());
				}
			}
		}
		resultLabel.setText(label);
		LOG.debug("End modificationCompositionAction");
	}

	/**
	 * Crée un {@link JPanel} avec un layout de type {@link BoxLayout}.
	 * 
	 * @param l'axe sur lequel les composants sont alignés.
	 * @see {@link BoxLayout#X_AXIS} et {@link BoxLayout#Y_AXIS}
	 * @return le panel crée
	 */
	public static JPanel createBoxLayoutPanel(int axis) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, axis));
		panel.add(Box.createRigidArea(new Dimension(100, 0)));
		return panel;
	}

	/**
	 * Modifie la taille du composant donné.
	 * 
	 * @param comp le composant
	 * @param width la nouvelle largeur
	 * @param height la nouvelle hauteur
	 */
	public static void setSize(JComponent comp, int width, int height) {
		comp.setMinimumSize(new Dimension(width, height));
		comp.setPreferredSize(new Dimension(width, height));
		comp.setMaximumSize(comp.getPreferredSize());
	}

	/**
	 * Ajoute un composant au panel.
	 * 
	 * @param panel le futur panel parent
	 * @param component le composant à ajouter
	 * @param alignement l'alignement vertical
	 * @param rigidSize la largeur de la rigid area
	 */
	public static void addComponent(JPanel panel, JComponent component, float alignement, int rigidSize) {
		component.setAlignmentX(alignement);
		component.setAlignmentY(Component.CENTER_ALIGNMENT);
		panel.add(component);
		panel.add(Box.createRigidArea(new Dimension(rigidSize, 0)));
	}

	public static JButton createJButton(String label, int width, FontAwesome icon) {
		JButton btn = new JButton(label, FontIcon.of(icon));
		btn.setBackground(Color.white);
		btn.setPreferredSize(new Dimension(width, PanelUtils.PANEL_HEIGHT));
		return btn;
	}

	public static void setBorder(JComponent comp, Color c) {
		comp.setBorder(BorderFactory.createLineBorder(c, 2));
	}

	public static JLabel createJLabel(String text, int width) {
		JLabel jLabel = new JLabel(text, SwingConstants.CENTER);
		setSize(jLabel, width, COMPONENT_HEIGHT);
		return jLabel;
	}
}
