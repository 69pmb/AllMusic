package pmb.music.AllMusic.XML;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.utils.Constant;

/**
 * Created by PBR on 14 févr. 2019.
 */
public abstract class AbstractExportXML {

	protected static final Logger LOG = Logger.getLogger(AbstractExportXML.class);

	protected AbstractExportXML() {
		throw new AssertionError("Must not be used");
	}

	public static void exportXML(List<Composition> compList, String fileName) throws IOException {
	}

	protected static void exportFichier(List<Composition> compList, DateTimeFormatter fullDTF, int i, Element comp) {
		for (int j = 0; j < compList.get(i).getFiles().size(); j++) {
			Element file = comp.addElement(CompoHandler.TAG_FILE);
			try {
				file.addAttribute(CompoHandler.TAG_AUTHOR,
						String.valueOf(compList.get(i).getFiles().get(j).getAuthor()));
				file.addAttribute(CompoHandler.TAG_FILENAME,
						String.valueOf(compList.get(i).getFiles().get(j).getFileName()));
				file.addAttribute(CompoHandler.TAG_PUBLISH_YEAR,
						String.valueOf(compList.get(i).getFiles().get(j).getPublishYear()));
				file.addAttribute(CompoHandler.TAG_CATEGORIE,
						String.valueOf(compList.get(i).getFiles().get(j).getCategorie()));
				file.addAttribute(CompoHandler.TAG_RANGE_DATE_BEGIN,
						String.valueOf(compList.get(i).getFiles().get(j).getRangeDateBegin()));
				file.addAttribute(CompoHandler.TAG_RANGE_DATE_END,
						String.valueOf(compList.get(i).getFiles().get(j).getRangeDateEnd()));
				file.addAttribute(CompoHandler.TAG_SORTED,
						String.valueOf(compList.get(i).getFiles().get(j).getSorted()));
				file.addAttribute(CompoHandler.TAG_CLASSEMENT,
						String.valueOf(compList.get(i).getFiles().get(j).getClassement()));
				file.addAttribute(CompoHandler.TAG_CREATION_DATE,
						fullDTF.format(compList.get(i).getFiles().get(j).getCreationDate()));
				file.addAttribute(CompoHandler.TAG_SIZE, String.valueOf(compList.get(i).getFiles().get(j).getSize()));
			} catch (NullPointerException e) {
				LOG.error("comp: " + comp, e);
				LOG.error("file: " + file);
				LOG.error("compList: " + compList);
				LOG.error("compList.get(i): " + compList.get(i));
				LOG.error("compList.get(i).getFiles(): " + compList.get(i).getFiles());
			}
		}
	}

	protected static void saveFile(String fileName, Document doc) throws IOException {
	}

	protected static void writeCompositionInFile(Document doc, String fullFileName)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {
		FileOutputStream fos = new FileOutputStream(Constant.getXmlPath() + fullFileName);
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setIndent(true);
		format.setNewlines(true);
		XMLWriter xmlOut = new XMLWriter(fos, format);
		xmlOut.write(doc);
		xmlOut.close();
	}

}
