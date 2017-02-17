package pmb.music.AllMusic.view;

import java.awt.BorderLayout;

import javax.swing.JFrame;

public class BasicFrame extends JFrame{
    private static final long serialVersionUID = 3539279683629505967L;
    public static final String DEFAULT_TITLE="Ma Fenetre";
//    private TableTool tableau;
    private Onglet tab;
    private MenuPanel menuPanel;
    
    public BasicFrame(){
        super(DEFAULT_TITLE);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTab(new Onglet(this));
        this.getContentPane().add(tab,BorderLayout.EAST);
        associateMenuPanel();
    }
//    public void redrawTable(){
//        //getTab().remove(tableau.getTable());
//        tableau=new TableTool();
//        tableau.drawTable();
//    }
    public void associateMenuPanel(){
        this.setMenuPanel(new MenuPanel(this));
        this.getContentPane().add(getMenuPanel(),BorderLayout.WEST);
        pack();
        setVisible(true);
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
    
//    public TableTool getTableau() {
//        return tableau;
//    }
//    public void setTableau(TableTool tableau) {
//        this.tableau = tableau;
//    }
}
