/**
 * 
 */
package pmb.music.AllMusic.utils;

import java.util.List;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.model.Fichier;

/**
 * @author i2113mj
 *
 */
public class FichierUtils {

    
    public static Object[][] convertListForJTable(List<Fichier> fList) {
        if(fList == null) {
            return new String[0][9];
        }
        Object[][] result = new String[fList.size()][9];
        for (int i = 0; i < fList.size(); i++) {
            Fichier f = fList.get(i);
            result[i][0] = f.getAuthor();
            result[i][1] = f.getFileName();
            result[i][2] = String.valueOf(f.getPublishYear());
            result[i][3] = f.getCategorie().toString();
            result[i][4] = f.getRangeDateBegin() + " - " + f.getRangeDateEnd();
            result[i][5] = ExportXML.SDF_DTTM.format(f.getCreationDate());
            result[i][6] = String.valueOf(f.getSize());
            result[i][7] = String.valueOf(f.getClassement());
            result[i][8] = f.getSorted().toString().toUpperCase();
        }
        return result;
    }
}
