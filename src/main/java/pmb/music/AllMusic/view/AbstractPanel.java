package pmb.music.AllMusic.view;

import java.awt.Component;
import java.awt.Dimension;
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

import org.apache.log4j.Logger;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MyException;

public abstract class AbstractPanel extends JPanel {
	private static final long serialVersionUID = -734333930764953994L;

	private static final Logger LOG = Logger.getLogger(AbstractPanel.class);

	/**
	 * Dimensionne les colonnes du tableau et ajoute des couleurs aux lignes.
	 * @see {@link EvenOddRenderer}
	 * @param table le tableau
	 * @param lastColumn si la dernière colonne doit être colorisée ou non
	 */
	protected void colRenderer(JTable table, boolean lastColumn) {
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
	 * Supprime les compositions sélectionnées du tableau et des fichiers XML.
	 * @param artistPanel le panel artiste
	 * @param compoList la liste de composition
	 * @param selected les lignes sélectionnées
	 * @param label le label de résultat
	 */
	@SuppressWarnings("unchecked")
	protected void deleteCompositionAction(final ArtistPanel artistPanel, List<Composition> compoList, List<Object> selected, JLabel label) {
		LOG.debug("Start deleteCompositionAction");
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
	 * Crée un {@link JPanel} avec un layout de type {@link BoxLayout} aligné sur
	 * l'abscisse.
	 * @return le panel crée
	 */
	protected JPanel createBoxLayoutPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(Box.createRigidArea(new Dimension(100, 0)));
		return panel;
	}
	
	/**
	 * Modifie la taille du composant donné.
	 * @param comp le composant
	 * @param width la nouvelle largeur
	 * @param height la nouvelle hauteur
	 */
	protected void setSize(JComponent comp, int width, int height) {
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
	protected void addComponent(JPanel panel, JComponent component, float alignement, int rigidSize) {
		component.setAlignmentX(alignement);
		component.setAlignmentY(Component.CENTER_ALIGNMENT);
		panel.add(component);
		panel.add(Box.createRigidArea(new Dimension(rigidSize, 0)));
	}
}
