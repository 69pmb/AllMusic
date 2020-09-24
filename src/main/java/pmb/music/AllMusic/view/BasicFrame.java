package pmb.music.AllMusic.view;

import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.view.panel.MenuPanel;
import pmb.music.AllMusic.view.panel.OngletPanel;

/**
 * La fenetre principale contenant tous les composants de l'application.
 *
 * @see {@link JFrame}
 * @author pmbroca
 */
public final class BasicFrame {
    private static final Logger LOG = LogManager.getLogger(BasicFrame.class);

    private BasicFrame() {
        throw new AssertionError("Must not be used");
    }

    /**
     * Construit la fenetre principale, ajoute le menu et les onglets.
     *
     * @param withArtist if true the artist panel is displayed
     */
    public static void buildFrame(boolean withArtist) {
        LOG.debug("Start BasicFrame");
        JFrame frame = new JFrame(Constant.DEFAULT_TITLE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        MenuPanel.buildMenu(frame);
        OngletPanel.buildTabs(frame, withArtist);
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        try {
            frame.setLocation(null);
        } catch (NullPointerException e) {
            // No need to handle this exception
        }
        frame.pack();
        frame.setVisible(true);
        LOG.debug("End BasicFrame");
    }
}
