package pmb.music.AllMusic.view.popup;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.view.model.FichierPanelModel;

/**
 * Created by PBR on 12 oct. 2018.
 */
public class FichierPopupMenu extends JPopupMenu {
	private static final long serialVersionUID = 4954841294693242496L;
	private static final Logger LOG = Logger.getLogger(FichierPopupMenu.class);

	private String fileName;
	private String author;
	private int fileNameIndex;
	private int authorIndex;
	private JTable table;

	public FichierPopupMenu(JTable table, int fileNameIndex, int authorIndex) {
		super();
		LOG.debug("Start FichierPopupMenu");
		this.table = table;
		this.fileNameIndex = fileNameIndex;
		this.authorIndex = authorIndex;

		// Open XML file
		JMenuItem openXml = new JMenuItem("Ouvrir le fichier XML");
		openXml.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		openXml.addActionListener((ActionEvent e) -> {
			LOG.debug("Start openXml");
			try {
				FichierUtils.openFileInNotepad(FichierUtils.buildXmlFilePath(fileName));
			} catch (MyException e1) {
				LOG.error("Error when opening with notepad file : " + fileName, e1);
			}
			LOG.debug("End openXml");
		});
		this.add(openXml);

		// Open TXT file
		JMenuItem openTxt = new JMenuItem("Ouvrir le fichier TXT");
		openTxt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
		openTxt.addActionListener((ActionEvent e) -> {
			LOG.debug("Start openTxt");
			try {
				FichierUtils.openFileInNotepad(FichierUtils.buildTxtFilePath(fileName, author));
			} catch (MyException e1) {
				LOG.error("Error when opening with notepad file : " + fileName, e1);
			}
			LOG.debug("End openTxt");
		});
		this.add(openTxt);

		this.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						LOG.debug("Start popupMenuWillBecomeVisible");
						FichierPopupMenu popup = (FichierPopupMenu) e.getSource();
						int rowAtPoint = popup.getTable()
								.rowAtPoint(SwingUtilities.convertPoint(popup, new Point(0, 0), popup.getTable()));
						if (rowAtPoint > -1) {
							popup.getTable().setRowSelectionInterval(rowAtPoint, rowAtPoint);
						}
						@SuppressWarnings("unchecked")
						Vector<String> selectedRow = (Vector<String>) ((FichierPanelModel) popup.getTable().getModel())
								.getDataVector()
								.get(popup.getTable().getRowSorter().convertRowIndexToModel(rowAtPoint));
						popup.setFileName(selectedRow.get(popup.getFileNameIndex()));
						popup.setAuthor(selectedRow.get(popup.getAuthorIndex()));
						LOG.debug("End popupMenuWillBecomeVisible");
					}
				});
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				// TODO Auto-generated method stub

			}
		});
		LOG.debug("End FichierPopupMenu");
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public int getFileNameIndex() {
		return fileNameIndex;
	}

	public void setFileNameIndex(int fileNameIndex) {
		this.fileNameIndex = fileNameIndex;
	}

	public int getAuthorIndex() {
		return authorIndex;
	}

	public void setAuthorIndex(int authorIndex) {
		this.authorIndex = authorIndex;
	}

	public JTable getTable() {
		return table;
	}

	public void setTable(JTable table) {
		this.table = table;
	}
}
