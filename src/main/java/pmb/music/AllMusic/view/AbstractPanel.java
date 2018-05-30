package pmb.music.AllMusic.view;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

public abstract class AbstractPanel extends JPanel {
	private static final long serialVersionUID = -734333930764953994L;

	protected void colRenderer(JTable table) {
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

	/**
	 * Crée un {@link JPanel} avec un layout de type {@link BoxLayout} aligné sur
	 * l'abscisse.
	 * 
	 * @return le panel crée
	 */
	protected JPanel createBoxLayoutPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(Box.createRigidArea(new Dimension(100, 0)));
		return panel;
	}
	
	protected void setSize(JComponent comp, int width, int height) {
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
	protected void addComponent(JPanel panel, JComponent component, float alignement, int rigidSize) {
		component.setAlignmentX(alignement);
		component.setAlignmentY(Component.CENTER_ALIGNMENT);
		panel.add(component);
		panel.add(Box.createRigidArea(new Dimension(rigidSize, 0)));
	}
}
