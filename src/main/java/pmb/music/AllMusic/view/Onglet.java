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

import pmb.music.AllMusic.utils.Constant;

/**
 * Classe gérant les onglet de l'appli.
 */
public class Onglet extends JPanel {
    private static final long serialVersionUID = -7235352581168930316L;
    private JTabbedPane onglets;
    private static int index;

    /**
     * Génère les onglets
     * @param myFrame la fenetre principale
     */
    public Onglet(final BasicFrame myFrame) {
        onglets = new JTabbedPane(SwingConstants.TOP);
        final JPanel pannel = new JPanel();
        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        dim.height = 35 * dim.height / 40;
        dim.width = 98 * dim.width / 100;
        onglets.setPreferredSize(dim);

        ArtistPanel artist = new ArtistPanel();
        ImportPanel importFile = new ImportPanel(artist);
        SearchPanel search = new SearchPanel(artist);
        
        
		onglets.addTab(Constant.ONGLET_IMPORT, importFile);
        onglets.setMnemonicAt(0, KeyEvent.VK_1);
		onglets.addTab(Constant.ONGLET_SEARCH, search);
        onglets.setMnemonicAt(1, KeyEvent.VK_2);
		onglets.addTab(Constant.ONGLET_ARTIST, artist);
        onglets.setMnemonicAt(2, KeyEvent.VK_3);

        onglets.setOpaque(true);
        pannel.add(onglets);
        pannel.validate();
        myFrame.getContentPane().add(pannel, BorderLayout.CENTER);
        myFrame.pack();
    }
    
    /**
     * Rafraichi un onglet.
     * @param title le titre de l'onglet à rafraichir
     * @param onglets le conteneur de tous les onglets
     * @param ongletToRedraw l'onglet à rafraichir
     */
    public static void redrawTab(String title, JTabbedPane onglets, JPanel ongletToRedraw) {
        if (!isTabAlreadyExists(onglets, title)) {
            onglets.addTab(title, ongletToRedraw);
        } else {
            onglets.removeTabAt(index);
            onglets.insertTab(title, null, ongletToRedraw, null, index);
        }
    }
    
    private static boolean isTabAlreadyExists(JTabbedPane onglets, String title) {
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
    
    public JTabbedPane getOnglets() {
        return onglets;
    }

    public void setOnglets(JTabbedPane onglets) {
        this.onglets = onglets;
    }
}
