/**
 * 
 */
package pmb.music.AllMusic.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;

import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;

/**
 * @author i2113mj
 *
 */
public class SearchUtils {

    public static List<Composition> search(List<Composition> compoList, final Map<String, String> criteria) {
        ArrayList<Composition> arrayList = new ArrayList<Composition>(compoList);
        CollectionUtils.filter(arrayList, new Predicate() {

            @Override
            public boolean evaluate(Object c) {
                Composition co = (Composition) c;
                String artist = criteria.get("artist");
                String oeuvre = criteria.get("oeuvre");
                String type = criteria.get("type");

                boolean result = true;
                if (StringUtils.isNotBlank(artist)) {
                    result = result && StringUtils.containsIgnoreCase(co.getArtist(), artist);
                }
                if (StringUtils.isNotBlank(oeuvre)) {
                    result = result && StringUtils.containsIgnoreCase(co.getOeuvre(), oeuvre);
                }
                if (type != null) {
                    result = result && co.getRecordType() == RecordType.valueOf(type);
                }
                if (CollectionUtils.isNotEmpty(co.getFiles())) {
                    List<Fichier> files = co.getFiles();
                    CollectionUtils.filter(files, new Predicate() {

                        @Override
                        public boolean evaluate(Object f) {
                            Fichier fi = (Fichier) f;
                            String publish = criteria.get("publish");
                            String fileName = criteria.get("fileName");
                            String auteur = criteria.get("auteur");
                            String cat = criteria.get("cat");
                            String dateB = criteria.get("dateB");
                            String dateE = criteria.get("dateE");
                            boolean result = true;

                            if (StringUtils.isNotBlank(publish)) {
                                result = result && fi.getPublishYear() == Integer.parseInt(publish);
                            }
                            if (StringUtils.isNotBlank(fileName)) {
                                result = result && StringUtils.containsIgnoreCase(fi.getFileName(), fileName);
                            }
                            if (StringUtils.isNotBlank(auteur)) {
                                result = result && StringUtils.containsIgnoreCase(fi.getAuthor(), auteur);
                            }
                            if (StringUtils.isNotBlank(cat)) {
                                result = result && fi.getCategorie() == Cat.valueOf(cat);
                            }
                            if (StringUtils.isNotBlank(dateB)) {
                                result = result && fi.getRangeDateBegin() == Integer.parseInt(dateB);
                            }
                            if (StringUtils.isNotBlank(dateE)) {
                                result = result && fi.getRangeDateEnd() == Integer.parseInt(dateE);
                            }
                            return result;
                        }

                    });
                }
                result = result && CollectionUtils.isNotEmpty(co.getFiles());

                return result;
            }
        });

        return arrayList;
    }

}
