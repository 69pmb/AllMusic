package pmb.music.AllMusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.StringJoiner;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.PanelUtils;

/**
 * Dialog editing composition with a slash in title to split them into 2
 * compositions.
 * 
 * @see {@link JDialog}
 * @author pmbroca
 */
public class SlashEditDialog {
	private static final Logger LOG = LogManager.getLogger(SlashEditDialog.class);

	private JDialog dialog;
	// Data
	private Boolean sendData;
	private int size;
	private List<Composition> finalFile;
	private Composition found;

	// Components
	private JTextPane compoFound;
	private JTextField title1;
	private JTextField title2;
	private JLabel label1;
	private JLabel label2;

	public final FocusListener searchForMerge = new FocusListener() {
		@Override
		public void focusLost(FocusEvent e) {
			setLabelsValues();
		}

		@Override
		public void focusGained(FocusEvent e) {
			JTextField source = (JTextField) e.getSource();
			source.selectAll();
		}
	};

	/**
	 * Constructeur de {@link SlashEditDialog}.
	 * 
	 * @param size amount of compositions to delete
	 */
	public SlashEditDialog(int size) {
		LOG.debug("Start SlashEditDialog");
		this.dialog = new JDialog((JFrame) null, "", true);
		PanelUtils.setSizeByScreenSize(dialog, 80, 60);
		this.dialog.setLocationRelativeTo(null);
		this.size = size;
		this.dialog.setResizable(true);
		this.initComponent();
		finalFile = ImportXML.importXML(Constant.getFinalFilePath());
		LOG.debug("End SlashEditDialog");
	}

	private void initComponent() {
		LOG.debug("Start initComponent");
		JPanel panel = PanelUtils.createBoxLayoutPanel(BoxLayout.Y_AXIS);
		PanelUtils.setSizeByScreenSize(panel, 78, 58);

		// Compo
		JPanel compoPanel = PanelUtils.createBoxLayoutPanel(BoxLayout.Y_AXIS);
		PanelUtils.setSizeByScreenSize(compoPanel, 60, 50);
		compoFound = ComponentBuilder.initJTextPaneComponent(new Color(21, 77, 153), 20);
		compoPanel.add(compoFound);

		// Text Fields
		title1 = (JTextField) new ComponentBuilder<JTextField>(JTextField.class).withComponentWidth(500).withPanelWidth(500)
				.withLabel("Titre 1").withParent(compoPanel).build();
		title1.addFocusListener(searchForMerge);
		label1 = (JLabel) new ComponentBuilder<JLabel>(JLabel.class).withLabelWidth(600).withPanelWidth(600).withParent(compoPanel).build();
		title2 = (JTextField) new ComponentBuilder<JTextField>(JTextField.class).withComponentWidth(500).withPanelWidth(500)
				.withLabel("Titre 2").withParent(compoPanel).build();
		title2.addFocusListener(searchForMerge);
		label2 = (JLabel) new ComponentBuilder<JLabel>(JLabel.class).withLabelWidth(600).withPanelWidth(600).withParent(compoPanel).build();
		panel.add(compoPanel);

		JPanel btnPanel = new JPanel();

		// Yes
		JButton yes = new JButton("Oui");
		yes.addActionListener((ActionEvent a) -> {
			if (StringUtils.isNotBlank(title1.getText()) && StringUtils.isNotBlank(title2.getText())) {
				dialog.setVisible(false);
				sendData = true;
				finalFile = ImportXML.importXML(Constant.getFinalFilePath());
			} else if (!StringUtils.endsWith(compoFound.getText(), "!")) {
				compoFound.setText(compoFound.getText() + Constant.NEW_LINE + "Input field empty !");
			}
		});
		btnPanel.add(yes);

		// No
		JButton no = new JButton("Non");
		no.addActionListener((ActionEvent a) -> {
			dialog.setVisible(false);
			sendData = false;
		});
		btnPanel.add(no);
		panel.add(btnPanel);

		this.dialog.setLayout(new BorderLayout());
		this.dialog.add(panel, BorderLayout.CENTER);
		LOG.debug("End initComponent");
	}

	/**
	 * Update dialog infos.
	 * 
	 * @param found composition found
	 * @param index index in all slashed composition
	 */
	public void updateDialog(Composition found, int index) {
		sendData = null;
		this.found = found;
		this.dialog.setTitle(
				index + "/" + size + " - " + BigDecimal.valueOf(100D).setScale(2).multiply(new BigDecimal(index))
						.divide(new BigDecimal(size), RoundingMode.HALF_UP).doubleValue() + "%");

		StringJoiner compoJoin = new StringJoiner(Constant.NEW_LINE).add(found.getArtist()).add(found.getTitre())
				.add(found.getRecordType().toString()).add("Deleted: " + Boolean.toString(found.isDeleted()))
				.add("CanBeMerged: " + Boolean.toString(found.isCanBeMerged()));
		Fichier fichier = found.getFiles().get(0);
		StringJoiner fileJoin = new StringJoiner(Constant.NEW_LINE).add(fichier.getFileName())
				.add("Category: " + fichier.getCategorie().toString())
				.add("Range date: " + fichier.getRangeDateBegin() + " - " + fichier.getRangeDateEnd())
				.add("Sorted: " + fichier.getSorted()).add("Rank: " + fichier.getClassement())
				.add("Size: " + fichier.getSize());
		compoFound.setText(compoJoin.toString() + Constant.NEW_LINE + fileJoin);

		String[] split = StringUtils.split(found.getTitre(), "/");
		title1.setText(split[0]);
		title2.setText(split[1]);
		setLabelsValues();
	}

	private void setLabelsValues() {
		label1.setText(findIfMergeable(found, title1.getText()));
		label2.setText(findIfMergeable(found, title2.getText()));
	}

	private String findIfMergeable(Composition c, String title) {
		Composition searched = new Composition(c);
		searched.setTitre(title);
		Composition compoExist = CompositionUtils.compoExist(finalFile, searched);
		String result = "Pas de fusion trouvée";
		if (compoExist != null) {
			result = "Fusion possible avec " + compoExist.getArtist() + " - " + compoExist.getTitre() + " (" + compoExist.getFiles().size() + " fichier(s))";
		}
		return result;
	}

	public Boolean getSendData() {
		return sendData;
	}

	public void setVisible(boolean b) {
		dialog.setVisible(b);
	}

	public String getTitle1() {
		return title1.getText();
	}

	public String getTitle2() {
		return title2.getText();
	}
}
