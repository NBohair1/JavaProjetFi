package presentation;

import dao.Caisse;
import dao.Reparateur;
import metier.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;

// Fenêtre principale du réparateur (employé)
// Contient: Mes Réparations, Clients, Composants, Ma Caisse, Emprunts
public class ReparateurFrame extends JFrame {

    // Réparateur connecté
    private Reparateur reparateur;
    
    // Services métier
    private IReparationMetier reparationMetier;
    private ICaisseMetier caisseMetier;
    private IClientMetier clientMetier;
    private IComposantMetier composantMetier;
    private IEmpruntMetier empruntMetier;
    
    // Composants graphiques
    private JTabbedPane tabbedPane;
    private ReparationPanelReparateur reparationPanel;
    private ClientPanelReparateur clientPanel;
    private CaissePanelReparateur caissePanel;
    private ComposantPanelReparateur composantPanel;

    // Constructeur par défaut (WindowBuilder)
    public ReparateurFrame() {
        this.reparateur = null;
        initComponents();
    }

    // Constructeur avec réparateur connecté
    public ReparateurFrame(Reparateur reparateur) {
        this.reparateur = reparateur;
        if (reparateur != null) {
            // Initialisation des services métier
            this.reparationMetier = new ReparationMetierImpl();
            this.caisseMetier = new CaisseMetierImpl();
            this.clientMetier = new ClientMetierImpl();
            this.composantMetier = new ComposantMetierImpl();
            this.empruntMetier = new EmpruntMetierImpl();
        }
        initComponents();
    }

    // Initialisation de l'interface graphique
    private void initComponents() {
        setTitle("Interface Réparateur" + (reparateur != null ? " - " + reparateur.getPrenom() + " " + reparateur.getNom() : ""));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        if (reparateur != null) {
            // Onglet réparations du réparateur
            reparationPanel = new ReparationPanelReparateur(this);
            tabbedPane.addTab("Mes Réparations", reparationPanel);

            // Onglet clients
            clientPanel = new ClientPanelReparateur(this);
            tabbedPane.addTab("Clients", clientPanel);

            // Onglet composants (pièces détachées)
            composantPanel = new ComposantPanelReparateur(this);
            tabbedPane.addTab("Composants", composantPanel);

            // Onglet caisse personnelle
            caissePanel = new CaissePanelReparateur(this);
            tabbedPane.addTab("Ma Caisse", caissePanel);
            
            // Onglet emprunts
            JPanel empruntPanel = createEmpruntPanel();
            tabbedPane.addTab("Emprunts", empruntPanel);
        }

        // Menu
        JMenuBar menuBar = new JMenuBar();
        
        JMenu menuFichier = new JMenu("Options");
        
        JMenuItem itemRefresh = new JMenuItem("Actualiser");
        itemRefresh.addActionListener(e -> refreshAll());
        
        JMenuItem itemDeconnexion = new JMenuItem("Déconnexion");
        itemDeconnexion.addActionListener(e -> deconnexion());
        
        JMenuItem itemQuitter = new JMenuItem("Quitter");
        itemQuitter.addActionListener(e -> System.exit(0));
        
        menuFichier.add(itemRefresh);
        menuFichier.addSeparator();
        menuFichier.add(itemDeconnexion);
        menuFichier.addSeparator();
        menuFichier.add(itemQuitter);
        
        menuBar.add(menuFichier);
        setJMenuBar(menuBar);

        getContentPane().add(tabbedPane);
    }

    public void refreshAll() {
        if (reparationPanel != null) reparationPanel.loadReparations();
        if (clientPanel != null) clientPanel.loadClients();
        if (composantPanel != null) composantPanel.loadComposants();
        if (caissePanel != null) caissePanel.loadCaisseData();
    }

    private void deconnexion() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Voulez-vous vraiment vous déconnecter?", 
            "Déconnexion", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            this.dispose();
        }
    }

    public Reparateur getReparateur() { 
        return reparateur; 
    }

    public IReparationMetier getReparationMetier() {
        return reparationMetier;
    }

    public IClientMetier getClientMetier() {
        return clientMetier;
    }

    public ICaisseMetier getCaisseMetier() {
        return caisseMetier;
    }

    public IComposantMetier getComposantMetier() {
        return composantMetier;
    }
    
    public IEmpruntMetier getEmpruntMetier() {
        return empruntMetier;
    }
    
    private JPanel createEmpruntPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Titre et boutons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        topPanel.setBackground(Color.WHITE);
        
        JLabel lblTitle = new JLabel("Mes Emprunts");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(lblTitle);
        
        JButton btnRefresh = new JButton("Actualiser");
        JButton btnNouveau = new JButton("Nouveau emprunt");
        JButton btnModifier = new JButton("Modifier");
        JButton btnSupprimer = new JButton("Supprimer");
        btnSupprimer.setBackground(new Color(231, 76, 60));
        btnSupprimer.setForeground(Color.WHITE);
        JButton btnRembourser = new JButton("Rembourser");
        btnRembourser.setBackground(new Color(46, 204, 113));
        btnRembourser.setForeground(Color.WHITE);
        
        topPanel.add(btnRefresh);
        topPanel.add(btnNouveau);
        topPanel.add(btnModifier);
        topPanel.add(btnSupprimer);
        topPanel.add(btnRembourser);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Table
        String[] cols = {"ID", "Montant (DH)", "Type", "Date", "Remboursé", "État"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Charger données
        Runnable loadData = () -> {
            model.setRowCount(0);
            try {
                java.util.List<dao.Emprunt> emprunts = empruntMetier.listerEmpruntsParReparateur(reparateur);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                
                for (dao.Emprunt emp : emprunts) {
                    String etat = emp.isRembourse() ? "Rembourse" : "En cours";
                    model.addRow(new Object[]{
                        emp.getIdEmprunt(),
                        emp.getMontant(),
                        emp.getType(),
                        emp.getDate() != null ? sdf.format(emp.getDate()) : "",
                        emp.isRembourse() ? "Oui" : "Non",
                        etat
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        
        // Actions
        btnRefresh.addActionListener(e -> loadData.run());
        
        btnNouveau.addActionListener(e -> {
            try {
                JTextField txtMontant = new JTextField();
                JTextField txtCommentaire = new JTextField();
                JComboBox<String> cbType = new JComboBox<>(new String[]{"SORTIE", "ENTREE"});
                
                JPanel formPanel = new JPanel(new java.awt.GridLayout(0, 2, 10, 10));
                formPanel.add(new JLabel("Montant (DH):"));
                formPanel.add(txtMontant);
                formPanel.add(new JLabel("Type:"));
                formPanel.add(cbType);
                formPanel.add(new JLabel("Commentaire:"));
                formPanel.add(txtCommentaire);
                formPanel.add(new JLabel(""), new JLabel(""));
                formPanel.add(new JLabel("SORTIE = J'emprunte (argent entre)"));
                formPanel.add(new JLabel(""), new JLabel(""));
                formPanel.add(new JLabel("ENTREE = Je prête (argent sort)"));
                
                int option = JOptionPane.showConfirmDialog(this, formPanel, "Nouvel Emprunt", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    float montant = Float.parseFloat(txtMontant.getText());
                    String type = (String) cbType.getSelectedItem();
                    String commentaire = txtCommentaire.getText().trim();
                    
                    empruntMetier.creerEmprunt(reparateur, montant, type, commentaire.isEmpty() ? "Emprunt " + type : commentaire);
                    JOptionPane.showMessageDialog(this, "Emprunt créé avec succès!", "Succès", JOptionPane.INFORMATION_MESSAGE);
                    loadData.run();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Montant invalide", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        btnRembourser.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(panel, "Sélectionnez un emprunt");
                return;
            }
            
            try {
                Long idEmprunt = (Long) model.getValueAt(selectedRow, 0);
                java.util.List<dao.Emprunt> emprunts = empruntMetier.listerEmpruntsParReparateur(reparateur);
                dao.Emprunt emp = emprunts.stream().filter(emprunt -> emprunt.getIdEmprunt().equals(idEmprunt)).findFirst().orElse(null);
                
                if (emp == null) {
                    JOptionPane.showMessageDialog(panel, "Emprunt introuvable");
                    return;
                }
                
                if (emp.isRembourse()) {
                    JOptionPane.showMessageDialog(panel, "Cet emprunt est déjà remboursé");
                    return;
                }
                
                int confirm = JOptionPane.showConfirmDialog(panel, 
                    "Rembourser " + emp.getMontant() + " DH?", 
                    "Confirmation", 
                    JOptionPane.YES_NO_OPTION);
                    
                if (confirm == JOptionPane.YES_OPTION) {
                    empruntMetier.rembourserEmprunt(emp);
                    JOptionPane.showMessageDialog(panel, "Emprunt remboursé!");
                    loadData.run();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
                btnModifier.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(panel, "Veuillez sélectionner un emprunt à modifier", "Aucune sélection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                Long idEmprunt = (Long) model.getValueAt(selectedRow, 0);
                java.util.List<dao.Emprunt> emprunts = empruntMetier.listerEmpruntsParReparateur(reparateur);
                dao.Emprunt emp = emprunts.stream().filter(emprunt -> emprunt.getIdEmprunt().equals(idEmprunt)).findFirst().orElse(null);
                
                if (emp == null) {
                    JOptionPane.showMessageDialog(panel, "Emprunt introuvable", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (emp.isRembourse()) {
                    JOptionPane.showMessageDialog(panel, "Impossible de modifier un emprunt déjà remboursé", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                JTextField txtMontant = new JTextField(String.valueOf(emp.getMontant()));
                JTextField txtCommentaire = new JTextField(emp.getCommentaire() != null ? emp.getCommentaire() : "");
                JComboBox<String> cbType = new JComboBox<>(new String[]{"SORTIE", "ENTREE"});
                cbType.setSelectedItem(emp.getType());
                
                JPanel modifPanel = new JPanel(new java.awt.GridLayout(0, 2, 10, 10));
                modifPanel.add(new JLabel("Montant (DH):"));
                modifPanel.add(txtMontant);
                modifPanel.add(new JLabel("Type:"));
                modifPanel.add(cbType);
                modifPanel.add(new JLabel("Commentaire:"));
                modifPanel.add(txtCommentaire);
                
                int option = JOptionPane.showConfirmDialog(this, modifPanel, "Modifier Emprunt", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    float nouveauMontant = Float.parseFloat(txtMontant.getText());
                    String nouveauType = (String) cbType.getSelectedItem();
                    String nouveauCommentaire = txtCommentaire.getText().trim();
                    
                    // Calculer la différence de montant pour ajuster la caisse
                    float difference = nouveauMontant - emp.getMontant();
                    
                    emp.setMontant(nouveauMontant);
                    emp.setType(nouveauType);
                    emp.setCommentaire(nouveauCommentaire);
                    
                    // Mettre à jour l'emprunt
                    EntityManager em = JPAUtil.getInstance().getEntityManager();
                    EntityTransaction tx = em.getTransaction();
                    try {
                        tx.begin();
                        em.merge(emp);
                        
                        // Ajuster le solde de la caisse selon la différence
                        Caisse caisse = reparateur.getCaisse();
                        if ("SORTIE".equalsIgnoreCase(nouveauType)) {
                            caisse.setSoldeActuel(caisse.getSoldeActuel() + difference);
                        } else if ("ENTREE".equalsIgnoreCase(nouveauType)) {
                            caisse.setSoldeActuel(caisse.getSoldeActuel() - difference);
                        }
                        em.merge(caisse);
                        
                        tx.commit();
                        JOptionPane.showMessageDialog(this, "Emprunt modifié avec succès!", "Succès", JOptionPane.INFORMATION_MESSAGE);
                        loadData.run();
                    } catch (Exception ex) {
                        if (tx.isActive()) tx.rollback();
                        throw ex;
                    } finally {
                        if (em != null && em.isOpen()) em.close();
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Montant invalide", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        btnSupprimer.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(panel, "Veuillez sélectionner un emprunt à supprimer", "Aucune sélection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                Long idEmprunt = (Long) model.getValueAt(selectedRow, 0);
                java.util.List<dao.Emprunt> emprunts = empruntMetier.listerEmpruntsParReparateur(reparateur);
                dao.Emprunt emp = emprunts.stream().filter(emprunt -> emprunt.getIdEmprunt().equals(idEmprunt)).findFirst().orElse(null);
                
                if (emp == null) {
                    JOptionPane.showMessageDialog(panel, "Emprunt introuvable", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String message = "Voulez-vous vraiment supprimer cet emprunt?\n\n" +
                    "Montant: " + emp.getMontant() + " DH\n" +
                    "Type: " + emp.getType() + "\n" +
                    "Etat: " + (emp.isRembourse() ? "Rembourse" : "En cours") + "\n\n" +
                    "Cette action est irreversible.";
                
                int confirm = JOptionPane.showConfirmDialog(panel, 
                    message,
                    "Confirmation de suppression",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    EntityManager em = JPAUtil.getInstance().getEntityManager();
                    EntityTransaction tx = em.getTransaction();
                    try {
                        tx.begin();
                        
                        dao.Emprunt empManaged = em.find(dao.Emprunt.class, emp.getIdEmprunt());
                        if (empManaged == null) {
                            throw new Exception("Emprunt non trouvé");
                        }
                        
                        // Ajuster le solde de la caisse avant suppression
                        if (!empManaged.isRembourse()) {
                            Caisse caisse = reparateur.getCaisse();
                            if ("SORTIE".equalsIgnoreCase(empManaged.getType())) {
                                // SORTIE = j'avais emprunté → retirer de la caisse
                                caisse.setSoldeActuel(caisse.getSoldeActuel() - empManaged.getMontant());
                            } else if ("ENTREE".equalsIgnoreCase(empManaged.getType())) {
                                // ENTREE = j'avais prêté → remettre dans la caisse
                                caisse.setSoldeActuel(caisse.getSoldeActuel() + empManaged.getMontant());
                            }
                            em.merge(caisse);
                        }
                        
                        em.remove(empManaged);
                        tx.commit();
                        
                        JOptionPane.showMessageDialog(panel, "Emprunt supprimé avec succès!", "Succès", JOptionPane.INFORMATION_MESSAGE);
                        loadData.run();
                    } catch (Exception ex) {
                        if (tx.isActive()) tx.rollback();
                        throw ex;
                    } finally {
                        if (em != null && em.isOpen()) em.close();
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
                loadData.run();
        return panel;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ReparateurFrame frame = new ReparateurFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
