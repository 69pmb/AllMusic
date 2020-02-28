package pmb.music.AllMusic;

import java.awt.EventQueue;
import java.awt.Frame;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.XML.NgExportXml;
import pmb.music.AllMusic.exception.MajorException;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FilesUtils;
import pmb.music.AllMusic.utils.GetProperties;
import pmb.music.AllMusic.view.BasicFrame;
import pmb.music.AllMusic.view.dialog.ExceptionDialog;

/**
 * Root class of the application.
 */
public final class AllMusic {

    private static final Logger LOG = LogManager.getLogger(AllMusic.class);

    private AllMusic() {
        throw new AssertionError("Must not be used");
    }

    /**
     * La méthode d'entrée du programme.
     *
     * @param args if opens with artist panel
     */
    public static void main(String[] args) {
        LOG.debug("Start main");
        GetProperties.reloadProperties();
        Arrays.stream(args).forEach(LOG::debug);
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            LOG.error("An uncaught exception has been thrown: ", e);
            ExceptionDialog ed = new ExceptionDialog("An uncaught exception has been thrown", e.getMessage(), e);
            ed.setVisible(true);
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.debug("Start shutdownHook");
            if (ExportXML.isFinalFileChanged()) {
                LOG.debug("Final File Changed");
                try {
                    NgExportXml.exportXML(ImportXML.importXML(Constant.getFinalFilePath()), Constant.getFinalFile());
                } catch (MajorException e) {
                    LOG.error("Export of final file to Dropbox failed", e);
                }
            }
            Optional<String> firstLine = FilesUtils.readFirstLine(Constant.FILE_LOG_PATH);
            if (firstLine.isPresent() && StringUtils.isNotBlank(firstLine.get())) {
                LOG.debug("Log File not empty");
                try {
                    FilesUtils.openFileInNotepad(Constant.FILE_LOG_PATH, null);
                } catch (MajorException e1) {
                    LOG.error("Error opening log file", e1);
                }
            }
            LOG.debug("End shutdownHook");
        }));
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            LOG.debug("Look: {}", UIManager.getLookAndFeel());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            LOG.error("Can't apply the requested look & feel", e);
        }
        EventQueue.invokeLater(() -> {
            LOG.debug("Start invokeLater");
            boolean withArtist = args.length == 0 || Boolean.parseBoolean(args[0]);
            final BasicFrame f = new BasicFrame(withArtist);
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
            f.getFrame().setExtendedState(Frame.MAXIMIZED_BOTH);
            try {
                f.getFrame().setLocation(null);
            } catch (NullPointerException e) {
                // No need to handle this exception
            }
            f.getFrame().pack();
            f.getFrame().setVisible(true);
            LOG.debug("End invokeLater");
        });
        LOG.debug("End main");
    }
}
