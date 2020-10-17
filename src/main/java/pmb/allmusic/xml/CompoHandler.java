/**
 *
 */
package pmb.allmusic.xml;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import pmb.allmusic.model.Cat;
import pmb.allmusic.model.Composition;
import pmb.allmusic.model.Fichier;
import pmb.allmusic.model.RecordType;
import pmb.allmusic.utils.Constant;
import pmb.my.starter.utils.VariousUtils;

/**
 * Classe décrivant comment parser un fichier XML contenant des {@link Composition}.
 *
 * @see {@link DefaultHandler}
 * @author pmbroca
 */
public class CompoHandler extends DefaultHandler {

    private static final Logger LOG = LogManager.getLogger(CompoHandler.class);

    private List<Composition> compoList;

    private Composition compo;

    private List<Fichier> files;

    private Fichier file;

    public static final String TAG_ROOT = "Compositions";
    public static final String TAG_COMPOSITION = "C";
    public static final String TAG_FILE = "F";
    public static final String TAG_PUBLISH_YEAR = "publish";
    public static final String TAG_RANGE_DATE_BEGIN = "rangeB";
    public static final String TAG_RANGE_DATE_END = "rangeE";
    public static final String TAG_CLASSEMENT = "rank";
    public static final String TAG_SORTED = "sorted";
    public static final String TAG_AUTHOR = "author";
    public static final String TAG_FILENAME = "name";
    public static final String TAG_CATEGORIE = "cat";
    public static final String TAG_SIZE = "size";
    public static final String TAG_CREATION_DATE = "creation";
    public static final String TAG_ARTIST = "A";
    public static final String TAG_TITRE = "T";
    public static final String TAG_UUID = "uuid";
    public static final String TAG_TYPE = "type";
    public static final String TAG_CAN_BE_MERGED = "mergeable";
    public static final String TAG_DELETED = "del";

    /**
     * Constructeur.
     */
    public CompoHandler() {
        super();
        compoList = new ArrayList<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (qName) {
        case TAG_ROOT:
            compoList = new ArrayList<>();
            break;
        case TAG_COMPOSITION:
            getCompositionElem(attributes);
            break;
        case TAG_FILE:
            getFileElem(attributes);
            break;
        default:
            break;
        }
    }

    private void getFileElem(Attributes attributes) {
        file = new Fichier();
        file.setRangeDateBegin(Integer.parseInt(attributes.getValue(TAG_RANGE_DATE_BEGIN)));
        file.setRangeDateEnd(Integer.parseInt(attributes.getValue(TAG_RANGE_DATE_END)));
        file.setClassement(Integer.parseInt(attributes.getValue(TAG_CLASSEMENT)));
        file.setSorted(Boolean.parseBoolean(attributes.getValue(TAG_SORTED)));
        file.setFileName(attributes.getValue(TAG_FILENAME));
        file.setCategorie(Cat.getByValue(attributes.getValue(TAG_CATEGORIE)));
        if (StringUtils.equalsIgnoreCase(attributes.getValue(TAG_AUTHOR), Constant.VARIOUS_AUTHOR)) {
            file.setAuthor(attributes.getValue(TAG_AUTHOR));
            file.setPublishYear(Integer.parseInt(attributes.getValue(TAG_PUBLISH_YEAR)));
        } else {
            file.setAuthor(StringUtils.substringBefore(file.getFileName(), Constant.FILE_NAME_SEPARATOR));
            file.setPublishYear(Integer.parseInt(StringUtils.substringAfterLast(file.getFileName(), Constant.FILE_NAME_SEPARATOR)));
        }
        try {
            file.setSize(Integer.parseInt(attributes.getValue(TAG_SIZE)));
        } catch (NumberFormatException e) {
            file.setSize(0);
            LOG.error(file, e);
        }
        try {
            file.setCreationDate(
                    LocalDateTime.parse(attributes.getValue(TAG_CREATION_DATE), new Constant().getFullDTF()));
        } catch (DateTimeParseException | NumberFormatException e) {
            LOG.warn("Error when parsing creation date", e);
            file.setCreationDate(LocalDateTime.now());
        }
    }

    private void getCompositionElem(Attributes attributes) throws SAXException {
        compo = new Composition();
        files = new ArrayList<>();
        try {
            compo.setArtist(attributes.getValue(TAG_ARTIST));
            compo.setTitre(attributes.getValue(TAG_TITRE));
            compo.setUuids(VariousUtils.stringToUuids(attributes.getValue(TAG_UUID)));
            compo.setRecordType(RecordType.valueOf(attributes.getValue(TAG_TYPE)));
            compo.setCanBeMerged(Boolean.parseBoolean(attributes.getValue(TAG_CAN_BE_MERGED)));
            compo.setDeleted(Boolean.parseBoolean(attributes.getValue(TAG_DELETED)));
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
        case TAG_COMPOSITION:
            compo.setFiles(files);
            compoList.add(compo);
            compo = null;
            files = null;
            break;
        case TAG_FILE:
            files.add(file);
            file = null;
            break;
        default:
            break;
        }
    }

    public List<Composition> getCompoList() {
        return compoList;
    }

    public void setCompoList(List<Composition> compoList) {
        this.compoList = compoList;
    }

}
