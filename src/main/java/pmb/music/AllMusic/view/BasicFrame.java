package pmb.music.AllMusic.view;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.view.panel.MenuPanel;
import pmb.music.AllMusic.view.panel.OngletPanel;

/**
 * La fenetre principale contenant tous les composants de l'application.
 * 
 * @see {@link JFrame}
 */
public class BasicFrame {
	private static final Logger LOG = Logger.getLogger(BasicFrame.class);
	private JFrame frame;
	private OngletPanel tab;

	/**
	 * Construit la fenetre principale, ajoute le menu et les onglets.
	 * 
	 * @param withArtist if true the artist panel is displayed
	 */
	public BasicFrame(boolean withArtist) {
		LOG.debug("Start BasicFrame");
		this.frame = new JFrame(Constant.DEFAULT_TITLE);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.tab = new OngletPanel(this, withArtist);
		MenuPanel.buildMenu(this);
		this.frame.getContentPane().add(tab, BorderLayout.EAST);
		LOG.debug("End BasicFrame");
	}

	public void setTab(final OngletPanel tab) {
		this.tab = tab;
	}

	public OngletPanel getTab() {
		return tab;
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setFrame(JFrame frame) {
		this.frame = frame;
	}
}
