/**
 * 
 */
package pmb.music.AllMusic.XML;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.utils.Constant;

/**
 * Classe décrivant comment parser un fichier XML contenant des
 * {@link Composition}.
 * 
 * @see {@link DefaultHandler}
 */
public class CompoHandler extends DefaultHandler {

	private static final Logger LOG = LogManager.getLogger(CompoHandler.class);

	private List<Composition> compoList;

	private Composition compo;

	private List<Fichier> files;

	private Fichier file;

	public static final String TAG_ROOT = "ListCompositions";
	public static final String TAG_COMPOSITION = "compo";
	public static final String TAG_FILE = "file";
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
		file.setPublishYear(Integer.parseInt(attributes.getValue(TAG_PUBLISH_YEAR)));
		file.setRangeDateBegin(Integer.parseInt(attributes.getValue(TAG_RANGE_DATE_BEGIN)));
		file.setRangeDateEnd(Integer.parseInt(attributes.getValue(TAG_RANGE_DATE_END)));
		file.setClassement(Integer.parseInt(attributes.getValue(TAG_CLASSEMENT)));
		file.setSorted(Boolean.parseBoolean(attributes.getValue(TAG_SORTED)));
		file.setAuthor(attributes.getValue(TAG_AUTHOR));
		file.setFileName(attributes.getValue(TAG_FILENAME));
		file.setCategorie(Cat.valueOf(attributes.getValue(TAG_CATEGORIE)));
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
			compo.setUuids(Optional.ofNullable(attributes.getValue(TAG_UUID)).map(uuids -> new LinkedList<>(Arrays.asList(StringUtils.split(uuids, ","))))
					.orElse(new LinkedList<String>()));
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
