/**
 * 
 */
package pmb.music.AllMusic.XML;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.utils.Constant;

/**
 * @author i2113mj
 *
 */
public class ExportXML {

    public static void exportXML(List<Composition> compList, String fileName) throws IOException {
        System.out.println("Start exportXML");
        Document doc = DocumentHelper.createDocument();
        Element listComp = doc.addElement("ListCompositions");

        for (int i = 0; i < compList.size(); i++) {
            // Ajout element <Order/>
            Element comp = listComp.addElement("Composition");
            comp.addAttribute("artist", String.valueOf(compList.get(i).getArtist()));
            comp.addAttribute("titre", String.valueOf(compList.get(i).getTitre()));
            comp.addAttribute("type", String.valueOf(compList.get(i).getRecordType()));

            for (int j = 0; j < compList.get(i).getFiles().size(); j++) {
                Element file = comp.addElement("File");
                file.addAttribute("author", String.valueOf(compList.get(i).getFiles().get(j).getAuthor()));
                file.addAttribute("fileName", String.valueOf(compList.get(i).getFiles().get(j).getFileName()));
                file.addAttribute("publishYear", String.valueOf(compList.get(i).getFiles().get(j).getPublishYear()));
                file.addAttribute("categorie", String.valueOf(compList.get(i).getFiles().get(j).getCategorie()));
                file.addAttribute("rangeDateBegin", String.valueOf(compList.get(i).getFiles().get(j).getRangeDateBegin()));
                file.addAttribute("rangeDateEnd", String.valueOf(compList.get(i).getFiles().get(j).getRangeDateEnd()));
                file.addAttribute("sorted", String.valueOf(compList.get(i).getFiles().get(j).getSorted()));
                file.addAttribute("classement", String.valueOf(compList.get(i).getFiles().get(j).getClassement()));
                file.addAttribute("creationDate", Constant.SDF_DTTM.format(compList.get(i).getFiles().get(j).getCreationDate()));
                file.addAttribute("size", String.valueOf(compList.get(i).getFiles().get(j).getSize()));
            }
        }
        String nomDir = "src\\main\\resources\\";
        if (!FileUtils.fileExists(nomDir)) {
            FileUtils.mkdir(nomDir);
        }
        
        // Sauvegarde
        File source = new File(nomDir + fileName + ".xml");
        String history = nomDir + "history\\";
        if (!FileUtils.fileExists(history)) {
            FileUtils.mkdir(history);
        }
        File destination = new File(history + fileName + dateNow() + ".xml");
        source.renameTo(destination);

        // Enregistrement dans un fichier
        FileOutputStream fos = new FileOutputStream(nomDir + fileName + ".xml");
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setIndent(true);
        format.setNewlines(true);
        XMLWriter xmlOut = new XMLWriter(fos, format);
        xmlOut.write(doc);
        xmlOut.close();
        System.out.println("End exportXML");
    }

    private static String dateNow() {
        Calendar greg = new GregorianCalendar();
        Date date = greg.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(" dd-MM-yyyy HH-mm");
        return sdf.format(date);
    }
}
