package pmb.music.AllMusic.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
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
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.view.model.AbstractModel;
import pmb.music.AllMusic.view.panel.ArtistPanel;

public class PanelUtils {

	private static final Logger LOG = Logger.getLogger(PanelUtils.class);
	public static final int PANEL_HEIGHT = 70;
	public static final int COMPONENT_HEIGHT = 25;

	/**
	 * Dimensionne les colonnes du tableau et ajoute des couleurs aux lignes.
	 * 
	 * @see {@link EvenOddRenderer}
	 * @param table le tableau
	 * @param lastColumn si la dernière colonne doit être colorisée ou non
	 * @param deletedIndex index of the deleted column, use it to draw deleted row
	 *            with specific color
	 */
	public static void colRenderer(JTable table, boolean lastColumn, Integer deletedIndex) {
		LOG.debug("Start colRenderer");
		JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(table);
		setColumnsWidth(table, topFrame != null ? topFrame.getWidth() : table.getWidth(), "Init");

		DefaultTableCellRenderer renderer = new EvenOddRenderer(deletedIndex);
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

	@SuppressWarnings("unchecked")
	public static <T extends AbstractModel> Optional<Vector<String>> getSelectedRow(MouseEvent e) {
		LOG.debug("Start getSelectedRow");
		JTable target = (JTable) e.getSource();
		int rowAtPoint = target.rowAtPoint(SwingUtilities.convertPoint(target, new Point(e.getX(), e.getY()), target));
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
	public static int keyShortcutAction(KeyEvent e, int selectedRow, Integer sortedColumn) {
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
	 * @param compoList la liste de composition
	 * @param selected les lignes sélectionnées
	 * @param label le label de résultat
	 */
	@SuppressWarnings("unchecked")
	public static void deleteCompositionAction(final ArtistPanel artistPanel, List<Composition> compoList,
			List<Object> selected, JLabel label) {
		LOG.debug("Start deleteCompositionAction");
		if (selected.isEmpty()) {
			label.setText("Aucune composition sélectionnée !");
			LOG.debug("End deleteCompositionAction, no selected composition");
			return;
		}
		label.setText("");
		artistPanel.interruptUpdateArtist();
		List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
		for (Object o : selected) {
			Vector<String> v = (Vector<String>) o;
			try {
				Composition toRemoveToFinal = CompositionUtils.findByArtistTitreAndType(importXML, v.get(0), v.get(1),
						v.get(2), true);
				Composition toRemoveToTable = CompositionUtils.findByArtistTitreAndType(compoList, v.get(0), v.get(1),
						v.get(2), true);
				compoList.get(compoList.indexOf(toRemoveToTable)).setDeleted(true);
				importXML.get(importXML.indexOf(toRemoveToFinal)).setDeleted(true);
				CompositionUtils.removeCompositionsInFiles(toRemoveToFinal);
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
	 * @param panel
	 * @param component
	 * @param alignement
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
