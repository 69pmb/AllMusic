/**
 * 
 */
package pmb.music.AllMusic.XML;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
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
 * @author pmbroca
 */
public class CompoHandler extends DefaultHandler {

	private static final Logger LOG = Logger.getLogger(CompoHandler.class);

	private List<Composition> compoList;

	private Composition compo;

	private List<Fichier> files;

	private Fichier file;

	public static final String TAG_ROOT = "ListCompositions";
	public static final String TAG_COMPOSITION = "Composition";
	public static final String TAG_FILE = "File";
	public static final String TAG_PUBLISH_YEAR = "publishYear";
	public static final String TAG_RANGE_DATE_BEGIN = "rangeDateBegin";
	public static final String TAG_RANGE_DATE_END = "rangeDateEnd";
	public static final String TAG_CLASSEMENT = "classement";
	public static final String TAG_SORTED = "sorted";
	public static final String TAG_AUTHOR = "author";
	public static final String TAG_FILENAME = "fileName";
	public static final String TAG_CATEGORIE = "categorie";
	public static final String TAG_SIZE = "size";
	public static final String TAG_CREATION_DATE = "creationDate";
	public static final String TAG_ARTIST = "artist";
	public static final String TAG_TITRE = "titre";
	public static final String TAG_TYPE = "type";
	public static final String TAG_CAN_BE_MERGED = "canBeMerged";
	public static final String TAG_DELETED = "deleted";

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
			file.setCreationDate(new Constant().getSdfDttm().parse(attributes.getValue(TAG_CREATION_DATE)));
		} catch (ParseException e) {
			file.setCreationDate(new Date());
		}
	}

	private void getCompositionElem(Attributes attributes) throws SAXException {
		compo = new Composition();
		files = new ArrayList<>();
		try {
			compo.setArtist(attributes.getValue(TAG_ARTIST));
			compo.setTitre(attributes.getValue(TAG_TITRE));
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
