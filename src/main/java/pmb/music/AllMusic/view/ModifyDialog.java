/**
 * 
 */
package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

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
	private boolean sendData;

	/**
	 * Constructeur.
	 * @param parent {@link JFrame} la fenetre parente
	 * @param header {@link String} les entetes de la popup
	 * @param modal {@code boolean} si la popup bloque l'utilisateur
	 * @param files {@code List<Fichier>} la liste des fichier à afficher
	 * @param dim {@link Dimension} les dimension de la popup
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

	private void initComposant() {
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
		
	    JPanel content = new JPanel();
	    content.add(artistPanel);
	    content.add(titrePanel);
	    
	    JPanel control = new JPanel();
	    JButton okBouton = new JButton("OK");
	    
	    okBouton.addActionListener(new ActionListener(){
	      @SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent arg0) {        
	        setVisible(false);
	        sendData = true;
	        compo.set(0, artist.getText());
	        compo.set(1, titre.getText());
	      }
	    });

	    JButton cancelBouton = new JButton("Annuler");
	    cancelBouton.addActionListener(new ActionListener(){
	      @Override
		public void actionPerformed(ActionEvent arg0) {
	        setVisible(false);
	        sendData = false;
	      }      
	    });

	    control.add(okBouton);
	    control.add(cancelBouton);
	    
	    this.getContentPane().add(content, BorderLayout.CENTER);
	    this.getContentPane().add(control, BorderLayout.SOUTH);
	}

	/**
	 * Affiche une {@link ModifyDialog}.
	 */
	public void showDialogFileTable() {
	    this.sendData = false;
	    this.setVisible(true);      
	}

	public Vector getCompo() {
		return compo;
	}

	public boolean isSendData() {
		return sendData;
	}
}
