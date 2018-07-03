package pmb.music.AllMusic.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.view.model.AbstractModel;

public class PanelUtils {

	private static final Logger LOG = Logger.getLogger(PanelUtils.class);

	/**
	 * Dimensionne les colonnes du tableau et ajoute des couleurs aux lignes.
	 * @see {@link EvenOddRenderer}
	 * @param table le tableau
	 * @param lastColumn si la dernière colonne doit être colorisée ou non
	 */
	public static void colRenderer(JTable table, boolean lastColumn) {
		setColumnsPreferredWidth(table);

		DefaultTableCellRenderer renderer = new EvenOddRenderer();
		int columnCount = table.getColumnCount();
		if (!lastColumn) {
			columnCount--;
		}
		for (int i = 0; i < columnCount; i++) {
			renderer.setHorizontalAlignment(JLabel.CENTER);
			table.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}
	}

	/**
	 * Recherche la longueur maximum pour chaque colonne et la set pour la largeur de cette colonne. 
	 * @param table le tableau
	 */
	public static void setColumnsPreferredWidth(JTable table) {
		TableColumnModel columnModel = table.getColumnModel();
		for (int i = 0; i < columnModel.getColumnCount(); i++) {
			int maximum = 0;
			for (int j = 0; j < table.getRowCount(); j++) {
				Object currentValue = table.getValueAt(j, i);
				if(currentValue == null) {
					continue;
				}
				int longueurCourante = currentValue.toString().length();
				if (longueurCourante > maximum) {
					maximum = longueurCourante;
				}
			}
			columnModel.getColumn(i).setPreferredWidth(maximum * 7 + 50); // valeur arbitraire
		}
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
		return -1;
	}

	/**
	 * Supprime les compositions sélectionnées du tableau et des fichiers XML.
	 * @param artistPanel le panel artiste
	 * @param compoList la liste de composition
	 * @param selected les lignes sélectionnées
	 * @param label le label de résultat
	 */
	@SuppressWarnings("unchecked")
	public static void deleteCompositionAction(final ArtistPanel artistPanel, List<Composition> compoList, List<Object> selected, JLabel label) {
		LOG.debug("Start deleteCompositionAction");
		if(selected.isEmpty()) {
			label.setText("Aucune composition sélectionnée !");
			LOG.debug("End deleteCompositionAction, no selected composition");
			return;
		}
		label.setText("");
		artistPanel.interruptUpdateArtist();
		List<Composition> importXML = ImportXML.importXML(Constant.FINAL_FILE_PATH);
		for (Object o : selected) {
			Vector<String> v = (Vector<String>) o;
			try {
				Composition toRemoveToFinal = CompositionUtils.findByArtistTitreAndType(importXML, v.get(0), v.get(1), v.get(2), true);
				Composition toRemoveToTable = CompositionUtils.findByArtistTitreAndType(compoList, v.get(0), v.get(1), v.get(2), true);
				compoList.remove(compoList.indexOf(toRemoveToTable));
				importXML.remove(importXML.indexOf(toRemoveToFinal));
				CompositionUtils.removeCompositionsInFiles(toRemoveToFinal);
			} catch (MyException e1) {
				LOG.error("Erreur lors de la suppression d'une composition", e1);
				return;
			}
		}
		try {
			ExportXML.exportXML(importXML, Constant.FINAL_FILE);
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
}
