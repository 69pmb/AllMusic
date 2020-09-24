package pmb.music.AllMusic;

import java.awt.EventQueue;
import java.util.Arrays;

import javax.swing.UIManager;

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
import pmb.music.AllMusic.view.PanelUtils;
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
        PanelUtils.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        LOG.debug("Look: {}", UIManager.getLookAndFeel());
        EventQueue.invokeLater(() -> {
            boolean withArtist = args.length == 0 || Boolean.parseBoolean(args[0]);
            BasicFrame.buildFrame(withArtist);
        });
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
            if (StringUtils.isNotBlank(FilesUtils.readFirstLine(Constant.FILE_LOG_PATH))) {
                LOG.debug("Log File not empty");
                try {
                    FilesUtils.openFileInNotepad(Constant.FILE_LOG_PATH, null);
                } catch (MajorException e1) {
                    LOG.error("Error opening log file", e1);
                }
            }
            LOG.debug("End shutdownHook");
        }));
        LOG.debug("End main");
    }
}
