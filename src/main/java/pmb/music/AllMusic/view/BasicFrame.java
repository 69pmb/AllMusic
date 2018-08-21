package pmb.music.AllMusic.view;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.utils.Constant;

/**
 * La fenetre principale contenant tous les composants de l'application.
 * 
 * @see {@link JFrame}
 * @author pmbroca
 */
public class BasicFrame extends JFrame {
	private static final Logger LOG = Logger.getLogger(BasicFrame.class);
	private static final long serialVersionUID = 3539279683629505967L;
	private Onglet tab;
	private MenuPanel menuPanel;

	/**
	 * Construit la fenetre principale, ajoute le menu et les onglets.
	 * @param withArtist if true the artist panel is displayed
	 */
	public BasicFrame(boolean withArtist) {
		super(Constant.DEFAULT_TITLE);
		LOG.debug("Start BasicFrame");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTab(new Onglet(this, withArtist));
		this.getContentPane().add(getTab(), BorderLayout.EAST);
		this.setMenuPanel(new MenuPanel(this));
		this.getContentPane().add(getMenuPanel(), BorderLayout.WEST);
		LOG.debug("End BasicFrame");
	}

	public void setTab(final Onglet tab) {
		this.tab = tab;
	}

	public Onglet getTab() {
		return tab;
	}

	public MenuPanel getMenuPanel() {
		return menuPanel;
	}

	public void setMenuPanel(final MenuPanel myMenuPanel) {
		menuPanel = myMenuPanel;
	}
}
