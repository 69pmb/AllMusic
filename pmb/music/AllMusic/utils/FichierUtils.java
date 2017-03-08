/**
 * 
 */
package pmb.music.AllMusic.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;

/**
 * @author i2113mj
 *
 */
public class FichierUtils {

    public static Object[][] convertListForJTable(List<Fichier> fList) {
        System.out.println("Start convertListForJTable");
        if (fList == null) {
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
            result[i][5] = Constant.SDF_DTTM.format(f.getCreationDate());
            result[i][6] = String.valueOf(f.getSize());
            result[i][7] = String.valueOf(f.getClassement());
            result[i][8] = f.getSorted().toString().toUpperCase();
        }
        System.out.println("End convertListForJTable");
        return result;
    }

    public static void findDuplicateFichierInCompo(List<Composition> allCompo) {
        for (Composition composition : allCompo) {
            List<Fichier> files = composition.getFiles();
            List<String> names = new ArrayList<>();
            for (Fichier fichier : files) {
                names.add(fichier.getFileName());
            }
            Set<String> set = new HashSet<String>(names);
            if (set.size() < names.size()) {
                System.out.println("Duplicates for: " + composition);
                System.out.println("");
            }
        }
    }
}
