/**
 * 
 */
package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.utils.FichierUtils;

/**
 * @author i2113mj
 *
 */
public class DialogFileTable extends JDialog {

    private static final long serialVersionUID = 1304786661370052913L;

    private List<Fichier> files = new ArrayList<Fichier>();

    private static final String title[] = { "Auteur", "Nom du fichier", "Date de publication", "Categorie", "Dates", "Date de création", "Taille", "Classement" , "Classé"};
    
    private JTable fichiers;

    public DialogFileTable(JFrame parent, String title, boolean modal, List<Fichier> files) {
        super(parent, title, modal);
        this.setSize(1500, 300);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.files = files;
        this.setResizable(true);
        this.initComponent();
    }

    public void showDialogFileTable() {
        this.setVisible(true);
    }

    private void initComponent() {
        fichiers = new JTable();
        fichiers.setAutoCreateRowSorter(true);
        fichiers.setRowHeight(30);
        fichiers.setFillsViewportHeight(true);
        fichiers.setBackground(UIManager.getColor("Label.background"));
        fichiers.setFont(UIManager.getFont("Label.font"));
        fichiers.setBorder(UIManager.getBorder("Label.border"));
        fichiers.setModel(new FichierModel(FichierUtils.convertListForJTable(files), title));

        TableColumnModel modelecolonne = fichiers.getColumnModel();
        int total = modelecolonne.getColumnCount();
        for (int i = 0; i < total; i++) {
            int taille = 0;
            int total2 = fichiers.getRowCount();
            for (int j = 0; j < total2; j++) {
                int taille2 = fichiers.getValueAt(j, i).toString().length() * 7; 
                if (taille2 > taille) {
                    taille = taille2;
                }
            }
            modelecolonne.getColumn(i).setPreferredWidth(taille + 50);
        }
        
        DefaultTableCellRenderer renderer = new EvenOddRenderer();
        for (int i = 0; i < fichiers.getColumnCount(); i++) {
            renderer.setHorizontalAlignment(JLabel.CENTER);
            fichiers.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(fichiers), BorderLayout.CENTER);
    }
}
