/** */
package pmb.allmusic.view.model;

import java.util.List;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 * Décrit la façon dont les tableaux seront affichées.
 *
 * @see {@link DefaultTableModel}
 */
public abstract class AbstractModel extends DefaultTableModel {

  private static final long serialVersionUID = 1L;

  /**
   * Constructeur de {@link AbstractModel}.
   *
   * @param data {@code Object[][]} les données
   * @param title {@code String[]} les entetes du tableau
   */
  public AbstractModel(Object[][] data, String[] title) {
    super(data, title);
  }

  /**
   * Constructeur de {@link AbstractModel}.
   *
   * @param data {@link Vector} les données
   * @param title {@link Vector} les entetes du tableau
   */
  public AbstractModel(Vector<? extends Vector<?>> data, Vector<?> title) {
    super(data, title);
  }

  @Override
  public boolean isCellEditable(int i, int i1) {
    return false;
  }

  @Override
  public abstract Class<?> getColumnClass(int col);

  @Override
  public int getRowCount() {
    if (this.getDataVector() != null) {
      return this.getDataVector().size();
    } else {
      return 0;
    }
  }

  @Override
  public int getColumnCount() {
    return this.columnIdentifiers.size();
  }

  /**
   * Retourne les lignes selectionnées avec les cases à cocher.
   *
   * @return la liste des objects selectionés
   */
  public abstract List<Object> getSelected();

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return ((Vector<?>) this.getDataVector().elementAt(rowIndex)).elementAt(columnIndex);
  }
}
