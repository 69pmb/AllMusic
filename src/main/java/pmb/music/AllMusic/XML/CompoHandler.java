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
 * @author i2113mj
 * 
 */
public class CompoHandler extends DefaultHandler {

	private static final Logger LOG = Logger.getLogger(CompoHandler.class);

	private List<Composition> compoList;

	private Composition compo;

	private List<Fichier> files;

	private Fichier file;

	public CompoHandler() {
		super();
		compoList = new ArrayList<>();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		switch (qName) {
		case "ListCompositions":
			compoList = new ArrayList<>();
			break;
		case "Composition":
			getCompositionElem(attributes);
			break;
		case "File":
			getFileElem(attributes);
			break;
		default:
			break;
		}
	}

	private void getFileElem(Attributes attributes) {
		file = new Fichier();
		file.setPublishYear(Integer.parseInt(attributes.getValue("publishYear")));
		file.setRangeDateBegin(Integer.parseInt(attributes.getValue("rangeDateBegin")));
		file.setRangeDateEnd(Integer.parseInt(attributes.getValue("rangeDateEnd")));
		file.setClassement(Integer.parseInt(attributes.getValue("classement")));
		file.setSorted(Boolean.parseBoolean(attributes.getValue("sorted")));
		file.setAuthor(attributes.getValue("author"));
		file.setFileName(attributes.getValue("fileName"));
		file.setCategorie(Cat.valueOf(attributes.getValue("categorie")));
		try {
			file.setSize(Integer.parseInt(attributes.getValue("size")));
		} catch (NumberFormatException e) {
			file.setSize(0);
			LOG.error(file, e);
		}
		try {
			file.setCreationDate(Constant.SDF_DTTM.parse(attributes.getValue("creationDate")));
		} catch (ParseException e) {
			file.setCreationDate(new Date());
		}
	}

	private void getCompositionElem(Attributes attributes) throws SAXException {
		compo = new Composition();
		files = new ArrayList<>();
		try {
			compo.setArtist(attributes.getValue("artist"));
			compo.setTitre(attributes.getValue("titre"));
			compo.setRecordType(RecordType.valueOf(attributes.getValue("type")));
		} catch (Exception e) {
			throw new SAXException(e);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		switch (qName) {
		case "Composition":
			compo.setFiles(files);
			compoList.add(compo);
			compo = null;
			files = null;
			break;
		case "File":
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
