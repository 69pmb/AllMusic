/**
 * 
 */
package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

/**
 * @author i2113mj
 *
 */
public class Onglet extends JPanel {
    private static final long serialVersionUID = -7235352581168930316L;
    private JTabbedPane onglets;
    private static int index;

    public Onglet(final BasicFrame myFrame) {
        onglets = new JTabbedPane(SwingConstants.TOP);
        final JPanel pannel = new JPanel();
        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        dim.height = 35 * dim.height / 40;
        dim.width = 98 * dim.width / 100;
        onglets.setPreferredSize(dim);

//        Table=new TableTool();
//        Table.drawTable();  
//        final JScrollPane scrollPane = new JScrollPane(Table.getTable());
        // scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        JPanel importFile = new ImportPanel();
        JPanel search = new SearchPanel();
        
//        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        onglets.addTab("Import", importFile);
        onglets.setMnemonicAt(0, KeyEvent.VK_1);
        onglets.addTab("Rechercher", search);
        onglets.setMnemonicAt(1, KeyEvent.VK_2);

//        OngletVitesse.buildOngletVitesse(onglets,dim);
//        OngletWeeks.buildOngletWeeks(onglets,dim);
//        OngletMonths.buildOngletMonths(onglets, dim);           
//        OngletYears.buildOngletYears(onglets, dim);         
//        OngletGroup.buildOngletGroup(onglets, dim);
//        
        onglets.setOpaque(true);
        pannel.add(onglets);
        pannel.validate();
        myFrame.getContentPane().add(pannel, BorderLayout.CENTER);
        myFrame.pack();
    }
    
    /**
     * Raffraichi un onglet
     * @param title le titre de l'onglet à raffraichier
     * @param onglets le conteneur de tous les onglets
     * @param ongletToRedraw l'onglet à raffraichir
     */
    public static void redrawTab(String title, JTabbedPane onglets, JPanel ongletToRedraw) {
        if (!isTabAlreadyExists(onglets, title)) {
            onglets.addTab(title, ongletToRedraw);
        } else {
            onglets.removeTabAt(index);
            onglets.insertTab(title, null, ongletToRedraw, null, index);
        }
    }
    
    public static boolean isTabAlreadyExists(JTabbedPane onglets, String title) {
        boolean isTabExists = false;
        for (int i = 0; i < onglets.getTabCount(); i++) {
            if (title.equals(onglets.getTitleAt(i))) {
                isTabExists = true;
                index = i;
                break;
            }
        }
        return isTabExists;
    }
    
//    public TableTool getTable() {
//        return Table;
//    }
//
//    public void setTable(TableTool table) {
//        Table = table;
//    }

    public JTabbedPane getOnglets() {
        return onglets;
    }

    public void setOnglets(JTabbedPane onglets) {
        this.onglets = onglets;
    }
}
