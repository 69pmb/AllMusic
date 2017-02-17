package pmb.music.AllMusic;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import pmb.music.AllMusic.view.BasicFrame;

/**
 * Hello world!
 *
 */
public class App {

    public static final String RESOURCES_DIRECTORY = "\\src\\main\\resources\\";
    
    public static final String RESOURCES_ABS_DIRECTORY = System.getProperty("user.dir") + RESOURCES_DIRECTORY;
    
    public static final String FINAL_FILE_PATH = System.getProperty("user.dir") + App.RESOURCES_DIRECTORY + "final.xml";
    
    public static final String MUSIC_DIRECTORY = System.getProperty("user.dir") + App.RESOURCES_DIRECTORY + "Music\\";
    
    public static final String ANSI_ENCODING = "Cp1252";

    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
//                try {
//                    ExportXML.exportXML(ImportXML.getAllCompo(FINAL_FILE_PATH), "final");
//                } catch (NullPointerException e) {
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
        }));
        try {
            UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
//        ImportXML.getInstance(FINAL_FILE_PATH);
        // List<Composition> fusionFiles = ImportXML.fusionFiles(System.getProperty("user.dir") + RESOURCES_DIRECTORY);
        final BasicFrame f = new BasicFrame();
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        dim.height = 19 * dim.height / 20;
        // f.setPreferredSize(dim);
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
        try {
            f.setLocation(null);
        } catch (NullPointerException e) {}
        f.pack();
        f.setVisible(true);

        // ImportXML.convertTxtToXml("Rolling Stone\\The 100 Best Singles Of 65-90 songs - 1989.txt","exemple6");
    }
}
