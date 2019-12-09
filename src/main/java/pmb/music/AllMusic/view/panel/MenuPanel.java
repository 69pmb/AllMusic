package pmb.music.AllMusic.view.panel;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.XML.NgExportXml;
import pmb.music.AllMusic.exception.MajorException;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FilesUtils;
import pmb.music.AllMusic.utils.GetProperties;
import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.dialog.ExceptionDialog;

/**
 * Classe pour le menu de l'application.
 *
 * @author pmbroca
 */
public final class MenuPanel {
    private static final Logger LOG = LogManager.getLogger(MenuPanel.class);

    private MenuPanel() {
        throw new AssertionError("Must not be used");
    }

    /**
     * Création de la barre de menu.
     *
     * @param frame the frame where the menu will be added
     *
     * @return le {@link JMenuBar} crée
     */
    public static void buildMenu(final JFrame frame) {
        LOG.debug("Start menuBar");
        final JMenuBar menuBar = new JMenuBar();

        final JMenu fichier = fichierMenu();

        // Edition
        final JMenu edition = edititonMenu();

        // Affichage
        final JMenu aff = affichageMenu(frame);

        // Aide
        final JMenu aide = helpMenu();

        menuBar.add(fichier);
        menuBar.add(edition);
        menuBar.add(aff);
        menuBar.add(aide);

        frame.getContentPane().add(menuBar, BorderLayout.NORTH);
        LOG.debug("End menuBar");
    }

    private static JMenu fichierMenu() {
        final JMenu fichier = new JMenu("Fichier");
        fichier.setMnemonic(KeyEvent.VK_F);

        ComponentBuilder.buildMenuItem(fichier, "Ouvrir le fichier de Log", KeyEvent.VK_L,
                (ActionEvent ae) -> openFile(Constant.FILE_LOG_PATH, "Error when opening log file"), null);

        ComponentBuilder.buildMenuItem(fichier, "Ouvrir le fichier de configuration", KeyEvent.VK_P,
                (ActionEvent ae) -> {
                    try {
                        FilesUtils.openFileInNotepad(Optional.of(Constant.getConfigPath()).orElse(null), null);
                    } catch (MajorException e) {
                        LOG.warn("Error when opening configuration file", e);
                        try {
                            Desktop.getDesktop().open(new File(Constant.getConfigPath()).getParentFile());
                        } catch (IOException e1) {
                            LOG.error("Error when opening configuration file directory", e1);
                        }
                    }
                }, null);

        ComponentBuilder.buildMenuItem(fichier, "Ouvrir le fichier de modification", KeyEvent.VK_D,
                (ActionEvent ae) -> openFile(Constant.MODIF_FILE_PATH, "Error when opening modif file"), null);

        ComponentBuilder.buildMenuItem(fichier, "Ouvrir le dossier de sortie", KeyEvent.VK_O,
                (ActionEvent ae) -> openDirectory(Constant.getOutputDir(), "Error when opening output directory"), null);

        ComponentBuilder.buildMenuItem(fichier, "Ouvrir le dossier des fichiers XML", KeyEvent.VK_L,
                (ActionEvent ae) -> openDirectory(Constant.getXmlPath(), "Error when opening XML files directory"), null);

        ComponentBuilder.buildMenuItem(fichier, "Ouvrir le dossier Musique", KeyEvent.VK_M,
                (ActionEvent ae) -> openDirectory(Constant.getMusicAbsDirectory(), "Error when opening txt files directory"), null);

        ComponentBuilder.buildMenuItem(fichier, "Fermer", KeyEvent.VK_Q, (ActionEvent ae) -> {
            if (JOptionPane.showConfirmDialog(null, "Voulez-vous VRAIMENT quitter ?",
                    "Demande confirmation ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == 0) {
                LOG.debug("Exit");
                System.exit(0);
            }
        }, null);
        return fichier;
    }

    private static JMenu edititonMenu() {
        final JMenu edition = new JMenu("Edition");
        edition.setMnemonic(KeyEvent.VK_E);

        ComponentBuilder.buildMenuItem(edition, "Exporter le fichier final", KeyEvent.VK_D,
                (ActionEvent ae) -> new Thread(() -> {
                    try {
                        NgExportXml.exportXML(ImportXML.importXML(Constant.getFinalFilePath()),
                                Constant.getFinalFile());
                        JOptionPane.showMessageDialog(null, "Final File successfully exported.", "",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (MajorException e) {
                        LOG.error("Export of final file for Angular failed", e);
                        ExceptionDialog exceptionDialog = new ExceptionDialog("Export of final file for Angular failed",
                                e.getMessage(), e);
                        exceptionDialog.setVisible(true);
                    }
                }).start(), ActionEvent.CTRL_MASK + InputEvent.SHIFT_DOWN_MASK);

        ComponentBuilder.buildMenuItem(edition, "Recharger le fichier de configuration", KeyEvent.VK_R,
                (ActionEvent ae) -> {
                    if (GetProperties.reloadProperties()) {
                        JOptionPane.showMessageDialog(null, "Properties successfully reloaded.", "",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Error when reloading properties.", "",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }, ActionEvent.CTRL_MASK + InputEvent.SHIFT_DOWN_MASK);

        ComponentBuilder.buildMenuItem(edition, "Recalculer les données de l'onglet Artiste", KeyEvent.VK_E,
                (ActionEvent ae) -> {
                    OngletPanel.getArtist().interruptUpdateArtist(true);
                    OngletPanel.getArtist().updateArtistPanel();
                }, ActionEvent.CTRL_MASK + InputEvent.SHIFT_DOWN_MASK);

        return edition;
    }

    private static JMenu affichageMenu(JFrame frame) {
        final JMenu aff = new JMenu("Affichage");
        aff.setMnemonic(KeyEvent.VK_A);
        Arrays.stream(UIManager.getInstalledLookAndFeels()).forEach(laf -> {
            JMenuItem mi = aff.add(new JMenuItem(laf.getName()));
            mi.addActionListener(e -> {
                try {
                    Optional<LookAndFeelInfo> found = Arrays.stream(UIManager.getInstalledLookAndFeels())
                            .filter(look -> laf.getName().equals(look.getName())).findFirst();
                    if (found.isPresent()) {
                        UIManager.setLookAndFeel(found.get().getClassName());
                        SwingUtilities.updateComponentTreeUI(frame);
                        frame.pack();
                    }
                } catch (Exception ex) {
                    // If error, fall back to cross-platform
                    LOG.error("Impossible d'appliquer le style choisi", ex);
                    try {
                        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                            | UnsupportedLookAndFeelException e1) {
                        LOG.error("Impossible d'appliquer le style par defaut", e1);
                    }
                }
            });
        });
        return aff;
    }

    private static JMenu helpMenu() {
        final JMenu aide = new JMenu("Aide");
        ComponentBuilder.buildMenuItem(aide, "?", KeyEvent.VK_H,
                (ActionEvent ae) -> JOptionPane.showMessageDialog(null,
                        "Ce logiciel permet de gérer les classements et palmarès de chansons et d'albums.\n"
                                + "Il a été developpé par M. Pierre-Marie Broca de janvier 2017 à septembre 2018.",
                                "HELP", JOptionPane.INFORMATION_MESSAGE),
                null);
        return aide;
    }

    private static void openDirectory(String path, String logMessage) {
        try {
            Desktop.getDesktop().open(new File(path));
        } catch (IOException e) {
            LOG.error(logMessage, e);
        }
    }

    private static void openFile(String path, String logMessage) {
        try {
            FilesUtils.openFileInNotepad(path, null);
        } catch (MajorException e) {
            LOG.error(logMessage, e);
        }
    }
}
