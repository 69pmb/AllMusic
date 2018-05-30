/**
 * 
 */
package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.model.RecordType;

/**
 * Une "pop-up" permettant de modifier une composition.
 * @see {@link JDialog}
 * @author pmbroca
 */
@SuppressWarnings("rawtypes")
public class ModifyDialog extends JDialog {
	
	private static final long serialVersionUID = 1304786661370052913L;
	private static final Logger LOG = Logger.getLogger(ModifyDialog.class);
	private final Vector compo;
	private JTextField artist;
	private JTextField titre;
	private JComboBox<RecordType> type;
	private boolean sendData;

	/**
	 * Constructeur.
	 * @param parent {@link JFrame} la fenetre parente
	 * @param header {@link String} les entetes de la popup
	 * @param modal {@code boolean} si la popup bloque l'utilisateur
	 * @param files {@code List<Fichier>} la liste des fichier à afficher
	 * @param dim {@link Dimension} les dimension de la popup
	 * @param compo {@link Vector} la compo à modifier
	 */
	public ModifyDialog(JFrame parent, String header, boolean modal, Dimension dim, Vector compo) {
		super(parent, header, modal);
		LOG.debug("Start DialogFileTable");
		this.setSize(dim);
		this.setLocationRelativeTo(null);
	    this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		this.compo = compo;
		this.setResizable(true);
		initComposant();
		LOG.debug("End DialogFileTable");
	}

	@SuppressWarnings("unchecked")
	private void initComposant() {
		LOG.debug("Start initComposant");
		// Artiste
		JPanel artistPanel = new JPanel();
		artistPanel.setPreferredSize(new Dimension(250, 60));
		JLabel artistLabel = new JLabel("Artiste : ");
		artist = new JTextField((String) compo.get(0));
		artist.setPreferredSize(new Dimension(230, 30));
		artistPanel.add(artistLabel);
		artistPanel.add(artist);
		
		// Titre
		JPanel titrePanel = new JPanel();
		titrePanel.setPreferredSize(new Dimension(300, 60));
		JLabel titreLabel = new JLabel("Titre : ");
		titre = new JTextField((String) compo.get(1));
		titre.setPreferredSize(new Dimension(270, 30));
		titrePanel.add(titreLabel);
		titrePanel.add(titre);

		// Type
		JPanel typePanel = new JPanel();
		typePanel.setPreferredSize(new Dimension(180, 60));
		JLabel typeLabel = new JLabel("Type : ");
		type = new JComboBox<>();
		RecordType[] valuesType = RecordType.values();
		for (int i = 0; i < valuesType.length; i++) {
			type.addItem(valuesType[i]);
		}
		type.setPreferredSize(new Dimension(150, 25));
		type.setSelectedItem(RecordType.valueOf((String) compo.get(2)));
		typePanel.add(typeLabel);
		typePanel.add(type);
		
	    JPanel content = new JPanel();
	    content.add(artistPanel);
	    content.add(titrePanel);
	    content.add(typePanel);
	    
	    JPanel control = new JPanel();
	    JButton okBouton = new JButton("OK");
	    
		okBouton.addActionListener((ActionEvent arg0) -> {
			setVisible(false);
			sendData = true;
			compo.set(0, artist.getText());
			compo.set(1, titre.getText());
			compo.set(2, type.getSelectedItem().toString());
		});

	    JButton cancelBouton = new JButton("Annuler");
	    cancelBouton.addActionListener((ActionEvent arg0) -> {
	    	setVisible(false);
	    	sendData = false;
	    });

	    control.add(okBouton);
	    control.add(cancelBouton);
	    
	    this.getContentPane().add(content, BorderLayout.CENTER);
	    this.getContentPane().add(control, BorderLayout.SOUTH);
		LOG.debug("End initComposant");
	}

	/**
	 * Affiche une {@link ModifyDialog}.
	 */
	public void showDialogFileTable() {
		LOG.debug("Start showDialogFileTable");
	    this.sendData = false;
	    this.setVisible(true);  
		LOG.debug("End showDialogFileTable");    
	}

	public Vector getCompo() {
		return compo;
	}

	public boolean isSendData() {
		return sendData;
	}
}
