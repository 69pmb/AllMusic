/**
 * 
 */
package pmb.music.AllMusic.XML;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import pmb.music.AllMusic.App;
import pmb.music.AllMusic.file.ImportFile;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.utils.CompositionUtils;

/**
 * @author i2113mj
 *
 */
public final class ImportXML {

    private static volatile ImportXML importXML = null;

    private static List<Composition> compoList = new ArrayList<Composition>();

    public static List<Composition> importXML(String uri) {
        System.out.println("Start importXML");
        SAXParserFactory fabrique = SAXParserFactory.newInstance();
        SAXParser parseur = null;
        try {
            parseur = fabrique.newSAXParser();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        File fichier = new File(uri);
        CompoHandler handler = new CompoHandler();
        if (fichier.length() > 0) {
            try {
                parseur.parse(fichier, handler);
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            int taille = handler.compoList.size();
//            for (int i = 0; i < taille; i++) {
//                compoList.add(handler.compoList.get(i));
//            }
        }
        System.out.println("End importXML");
        return handler.getCompoList();
    }

    public final static ImportXML getInstance(String fileName) {
        System.out.println("Start getInstance");
        // Le "Double-Checked Singleton"/"Singleton doublement vérifié" permet
        // d'éviter un appel coûteux à synchronized,
        // une fois que l'instanciation est faite.
        if (ImportXML.importXML == null) {
            // Le mot-clé synchronized sur ce bloc empêche toute instanciation
            // multiple même par différents "threads".
            // Il est TRES important.
            synchronized (ImportXML.class) {
                if (ImportXML.importXML == null) {
                    ImportXML.importXML = new ImportXML(fileName);
                }
            }
        }
//        CompositionUtils.printCompoList(ImportXML.importXML.compoList);
        System.out.println("End getInstance");
        return ImportXML.importXML;
    }

    private ImportXML(String file) {
        super();
        ImportXML.importXML(file);
    }

    public static List<Composition> getAllCompo(String fileName) {
        if(compoList.isEmpty()){
            getInstance(fileName);
        }
        return compoList;
    }
    
    public static void convertTxtToXml(String fileName, String exportName) {
        String path = System.getProperty("user.dir") + App.RESOURCES_DIRECTORY + "Music\\" + fileName;
        File file = new File(path);
        Fichier convertFile = ImportFile.convertOneFile(file);
        String line = ImportFile.randomLineAndLastLines(file).get(0);
        convertFile.setSorted(ImportFile.isSorted(line));
        try {
            List<Composition> compoList = ImportFile.getCompositionsFromFile(file, convertFile, ImportFile.determineType(file.getName()), ImportFile.getSeparator(line), false);
            ExportXML.exportXML(compoList, exportName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static List<Composition> fusionFiles(String fileName, boolean getFinal) throws Exception {
        System.out.println("Start fusionFiles");
        File dir = new File(fileName);
        List<File> files = new ArrayList<File>();
        CompositionUtils.listFilesForFolder(dir, files, ".xml", false);
        List<Composition> compoFusion = new ArrayList<Composition>();
        for (File fileXML : files) {
            boolean isFinal = fileXML.getName().equals("final.xml");
            if (!isFinal || (getFinal && isFinal)) {
                System.out.println(fileXML.getName());
                compoFusion.addAll(ImportXML.importXML(fileXML.getAbsolutePath()));
            }
        }
        List<Composition> compoFinal = new ArrayList<Composition>();
        for (Composition compo : compoFusion) {
            Composition compoExist = CompositionUtils.compoExist(compoFinal, compo);
            if (compoExist == null) {
                compoFinal.add(compo);
            } else {
                Composition composition = compoFusion.get(compoFusion.indexOf(compoExist));
                composition.getFiles().addAll(compo.getFiles());
                compoFinal.add(composition);
            }
        }
//        CompositionUtils.printCompoList(compoFinal);
        ExportXML.exportXML(compoFinal, "final");
        System.out.println("End fusionFiles");
        return compoFinal;
    }
    
}
