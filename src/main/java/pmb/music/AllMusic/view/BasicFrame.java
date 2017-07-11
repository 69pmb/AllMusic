package pmb.music.AllMusic.view;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.utils.Constant;

/**
 * La fenetre principale contenant tous les composants de l'application.
 * @see {@link JFrame}
 * @author pmbroca
 */
public class BasicFrame extends JFrame{
	private static final Logger LOG = Logger.getLogger(BasicFrame.class);
    private static final long serialVersionUID = 3539279683629505967L;
    private Onglet tab;
    private MenuPanel menuPanel;
    
    /**
     * Construit la fenetre principale, ajoute le menu et les onglets.
     */
    public BasicFrame(){
        super(Constant.DEFAULT_TITLE);
        LOG.debug("Start BasicFrame");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTab(new Onglet(this));
        this.getContentPane().add(tab,BorderLayout.EAST);
        associateMenuPanel();
        LOG.debug("End BasicFrame");
    }
    
    private void associateMenuPanel(){
        LOG.debug("Start associateMenuPanel");
        this.setMenuPanel(new MenuPanel(this));
        this.getContentPane().add(getMenuPanel(),BorderLayout.WEST);
        pack();
        setVisible(true);
        LOG.debug("End associateMenuPanel");
    }
    
    public void setTab(final Onglet tab) {
        this.tab = tab;
    }
    
    public Onglet getTab() {
        return tab;
    }   
    
    public MenuPanel getMenuPanel(){
        return menuPanel;
    }
    
    public void setMenuPanel(final MenuPanel myMenuPanel){
        menuPanel=myMenuPanel;
    }
}
