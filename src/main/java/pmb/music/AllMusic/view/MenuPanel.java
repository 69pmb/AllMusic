package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;


public class MenuPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private BasicFrame myFrame;
    // private List<Groupe> groupes= new ArrayList<Groupe>();

    public MenuPanel(final BasicFrame myFrame) {
        this.myFrame = myFrame;
        initComponents();
    }

    public BasicFrame getFrame() {
        return myFrame;
    }

    public void setBasicFrame(final BasicFrame BasicFrame) {
        myFrame = BasicFrame;
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        final JMenuBar menu = MenuBar();
        myFrame.getContentPane().add(menu, BorderLayout.NORTH);
    }

    public JMenuBar MenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        final JMenu fichier = new JMenu("Fichier");
        final JMenu edition = new JMenu("Edition");
        final JMenu aff = new JMenu("Affichage");
        final JMenu aide = new JMenu("Aide");
        fichier.setMnemonic(KeyEvent.VK_F);
        edition.setMnemonic(KeyEvent.VK_E);
        aff.setMnemonic(KeyEvent.VK_A);
        aide.setMnemonic(KeyEvent.VK_H);

        final JMenuItem help = new JMenuItem("?");
        final JMenuItem excel = new JMenuItem("Ouvrir Fichier Excel");
        final JMenuItem exportXml = new JMenuItem("Exporter en XML");
        final JMenuItem close = new JMenuItem("Fermer");
        final JMenuItem calculStats = new JMenuItem("Calculer Statistique");
        final JMenuItem triDate = new JMenuItem("Trier par date");
        final JMenuItem add = new JMenuItem("Ajouter une ligne");
        final JMenuItem search = new JMenuItem("Rechercher");
        final JMenuItem remove = new JMenuItem(
                "Supprimer les lignes sélectionnées");
        final JMenuItem addItem = new JMenuItem("Ajouter une sortie");
        final JMenuItem addGroupe = new JMenuItem(
                "Ajouter un groupe à des sorties");
        final JMenuItem removeGroupe = new JMenuItem(
                "Supprimer tous les groupes des sorties sélectionnées");

        final JRadioButtonMenuItem tout = new JRadioButtonMenuItem(
                "Afficher tout les groupes");

        excel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                ActionEvent.CTRL_MASK));
        help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
                ActionEvent.CTRL_MASK));
        add.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                ActionEvent.CTRL_MASK + KeyEvent.SHIFT_DOWN_MASK));
        addItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                ActionEvent.CTRL_MASK));
        exportXml.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                ActionEvent.CTRL_MASK));
        addGroupe.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                ActionEvent.CTRL_MASK + KeyEvent.SHIFT_DOWN_MASK));
        remove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                ActionEvent.CTRL_MASK + KeyEvent.SHIFT_DOWN_MASK));
        calculStats.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                ActionEvent.CTRL_MASK));
        search.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                ActionEvent.CTRL_MASK));
        removeGroupe.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                ActionEvent.CTRL_MASK + KeyEvent.SHIFT_DOWN_MASK));

//        excel.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(final ActionEvent ae) {
//                Excel = new ExcelTool();
//                try {
//                    pmb.Toolkit.ImportExcel.importation(Excel.getFichier()
//                            .toString(), myFrame);
//                } catch (final IOException e) {
//                    LOG.error("", e);
//                }
//                if (myFrame.getTableau() != null)
//                    myFrame.redrawTable();
//            }
//        });
        fichier.add(excel);

//        exportXml.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(final ActionEvent ae) {
//                try {
//                    ExportXML.exportXML(WorkoutArray.getAllWorkout(), "workout");
//                } catch (Exception e) {
//                    LOG.error("", e);
//                }
//            }
//        });
        fichier.add(exportXml);

//        calculStats.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(final ActionEvent ae) {
//                JOptionPane.showMessageDialog(null,
//                        ToolkitWorkout.stats(WorkoutArray.getAllWorkout()),
//                        "STATISTIQUES", JOptionPane.INFORMATION_MESSAGE);
//            }
//        });
        fichier.add(calculStats);

//        search.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(final ActionEvent ae) {
//                String inputValue = JOptionPane.showInputDialog(null,
//                        "Rechercher :", "Rechercher",
//                        JOptionPane.QUESTION_MESSAGE);
//                for (int i = 0; i < WorkoutArray.getAllWorkout().size(); i++) {
//                    if (WorkoutArray.getAllWorkout().get(i).getRemarque()
//                            .toLowerCase().contains(inputValue.toLowerCase())
//                            || WorkoutArray.getAllWorkout().get(i).getDetails()
//                                    .toLowerCase()
//                                    .contains(inputValue.toLowerCase())
//                            || WorkoutArray.getAllWorkout().get(i)
//                                    .getGroupesToString().toLowerCase()
//                                    .contains(inputValue.toLowerCase())) {
//                        WorkoutArray.getAllWorkout().get(i).setSearched(true);
//                    } else {
//                        WorkoutArray.getAllWorkout().get(i).setSearched(false);
//                    }
//                }
//                myFrame.setVisible(false);
//                myFrame = new BasicFrame();
//                myFrame.setVisible(true);
//            }
//        });
        fichier.add(search);

        close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                ActionEvent.CTRL_MASK));
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent ae) {
                final int option = JOptionPane.showConfirmDialog(null,
                        "Voulez-vous VRAIMENT quitter ?",
                        "Demande confirmation ",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (option == 0) {
                    System.exit(0);
                } else {
                }
            }
        });

//        triDate.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(final ActionEvent ae) {
//                Collections.sort(WorkoutArray.getAllWorkout());
//                myFrame.redrawTable();
//            }
//        });
        fichier.add(triDate);
        fichier.add(close);

//        add.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(final ActionEvent ae) {
//                myFrame.getTab().getTable().getMyData()
//                        .addWorkout(new Workout());
//            }
//        });
        edition.add(add);

//        addItem.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(final ActionEvent ae) {
//                AddWorkoutDialog zd = new AddWorkoutDialog(null,
//                        "Nouvelle Sortie", true);
//                Workout work = zd.showAddWorkoutDialog();
//                if (zd.ok)
//                    myFrame.getTab().getTable().getMyData().addWorkout(work);
//            }
//        });
        edition.add(addItem);

//        remove.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(final ActionEvent ae) {
//                final int[] selection = myFrame.getTab().getTable().getTable()
//                        .getSelectedRows();
//                for (int i = selection.length - 1; i >= 0; i--)
//                    myFrame.getTab().getTable().getMyData()
//                            .removeWorkout(selection[i]);
//            }
//        });
        edition.add(remove);

//        addGroupe.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(final ActionEvent ae) {
//                final int[] selection = myFrame.getTab().getTable().getTable()
//                        .getSelectedRows();
//                String inputValue = JOptionPane.showInputDialog(null,
//                        "Rentrer le nom du groupe :", "Nouveau Groupe",
//                        JOptionPane.QUESTION_MESSAGE);
//                Groupe group = new Groupe(inputValue, 0, false);
//                List<Workout> workoutToDisplay = new ArrayList<>();
//                for (Workout w : WorkoutArray.getAllWorkout()) {
//                    if (w.isSearched()) {
//                        workoutToDisplay.add(w);
//                    }
//                }
//                for (int i = selection.length - 1; i >= 0; i--) {
//                    group.setId(workoutToDisplay.get(selection[i]).getGroupe()
//                            .size());
//                    workoutToDisplay.get(selection[i]).addGroupe(group);
//                }
//            }
//        });
        edition.add(addGroupe);
        
//        removeGroupe.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(final ActionEvent ae) {
//                final int[] selection = myFrame.getTab().getTable().getTable()
//                        .getSelectedRows();
//                List<Workout> workoutToDisplay = new ArrayList<>();
//                for (Workout w : WorkoutArray.getAllWorkout()) {
//                    if (w.isSearched()) {
//                        workoutToDisplay.add(w);
//                    }
//                }
//                for (int i = selection.length - 1; i >= 0; i--) {
//                    workoutToDisplay.get(selection[i]).getGroupe().clear();
//                }
//            }
//        });
        edition.add(removeGroupe);
        
        help.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent ae) {
                JOptionPane
                        .showMessageDialog(
                                null,
                                "Ce logiciel permet de gérer les classements et palmarès de chansons et d'albums.\n"
                                        + "Il a été developpé par M. Pierre-Marie Broca de janvier 2017 à XXX.",
                                "HELP", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        aide.add(help);

        menuBar.add(fichier);
        menuBar.add(edition);
        menuBar.add(aff);
        menuBar.add(aide);
        aff.add(tout);
        // edition.add(jrm4);

        return menuBar;
    }

    public BasicFrame getMyFrame() {
        return myFrame;
    }

    public void setMyFrame(BasicFrame myFrame) {
        this.myFrame = myFrame;
    }

}
