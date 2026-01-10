package presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.text.SimpleDateFormat;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import dao.Boutique;
import dao.Proprietaire;
import dao.Reparateur;
import metier.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

// Fenêtre principale du propriétaire (administrateur)
// Contient les onglets: Mes Réparations, Mes Clients, Mes Composants, Ma Caisse, Administration
public class MainFrame extends JFrame {

    // Utilisateurs
    private Proprietaire proprietaire;
    private Boutique boutiqueActuelle;
    private Reparateur reparateurActuel;
    
    // Services métier
    private IBoutiqueMetier boutiqueMetier;
    private IReparationMetier reparationMetier;
    private ICaisseMetier caisseMetier;
    private IClientMetier clientMetier;
    private IComposantMetier composantMetier;
    private IEmpruntMetier empruntMetier;

    // Composants graphiques
    private JTabbedPane tabbedPane;
    
    // Onglets activités personnelles (comme ReparateurFrame)
    private ReparationPanelReparateur mesReparationsPanel;
    private ClientPanelReparateur mesClientsPanel;
    private ComposantPanelReparateur mesComposantsPanel;
    private CaissePanelReparateur maCaissePanel;
    
    // Onglet administration (propriétaire uniquement)
    private AdminPanel adminPanel;

    // Constructeur par défaut (WindowBuilder)
    public MainFrame() {
        this.proprietaire = null;
        this.reparateurActuel = null;
        this.boutiqueMetier = null;
        this.boutiqueActuelle = null;
        
        initComponents();
    }

    public MainFrame(Proprietaire proprietaire) {
        this(proprietaire, null);
    }
    
    // Constructeur principal avec propriétaire et boutique
    public MainFrame(Proprietaire proprietaire, Boutique boutique) {
        this.proprietaire = proprietaire;
        this.reparateurActuel = proprietaire;
        
        // Initialisation des services métier
        this.boutiqueMetier = new BoutiqueMetierImpl();
        this.reparationMetier = new ReparationMetierImpl();
        this.caisseMetier = new CaisseMetierImpl();
        this.clientMetier = new ClientMetierImpl();
        this.composantMetier = new ComposantMetierImpl();
        this.empruntMetier = new EmpruntMetierImpl();
        
        // Définir la boutique actuelle
        if (boutique != null) {
            this.boutiqueActuelle = boutique;
        } else if (proprietaire != null && proprietaire.getBoutiques() != null && !proprietaire.getBoutiques().isEmpty()) {
            this.boutiqueActuelle = proprietaire.getBoutiques().get(0);
        }
        
        initComponents();
    }

    // Initialisation de l'interface graphique
    private void initComponents() {
        String titleSuffix;
        if (boutiqueActuelle != null) {
            titleSuffix = boutiqueActuelle.getNom();
        } else {
            titleSuffix = "";
        }
        setTitle("RepairShop - " + titleSuffix);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(1200, 800);

        // Menu principal
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);
        JMenu menuFile = new JMenu("Options");
        JMenu menuBoutique = new JMenu("Boutique");
        
        JMenuItem itemQuit = new JMenuItem("Quitter");
        itemQuit.addActionListener(e -> System.exit(0));
        
        JMenuItem itemLogout = new JMenuItem("Déconnexion");
        itemLogout.addActionListener(e -> { new LoginFrame().setVisible(true); dispose(); });
        
        JMenuItem itemChange = new JMenuItem("Changer de Boutique");
        itemChange.addActionListener(e -> changerBoutique());

        menuFile.add(itemLogout);
        menuFile.addSeparator();
        menuFile.add(itemQuit);
        menuBoutique.add(itemChange);
        
        menuBar.add(menuFile);
        menuBar.add(menuBoutique);
        setJMenuBar(menuBar);

        // --- Onglets ---
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(Color.WHITE);

        // === Mes activités personnelles (IDENTIQUE à ReparateurFrame) ===
        mesReparationsPanel = new ReparationPanelReparateur(this);
        tabbedPane.addTab(" 🔧 Mes Réparations ", mesReparationsPanel);
        
        mesClientsPanel = new ClientPanelReparateur(this);
        tabbedPane.addTab(" 👤 Clients ", mesClientsPanel);
        
        mesComposantsPanel = new ComposantPanelReparateur(this);
        tabbedPane.addTab(" 🔩 Composants ", mesComposantsPanel);
        
        maCaissePanel = new CaissePanelReparateur(this);
        tabbedPane.addTab(" 💰 Ma Caisse ", maCaissePanel);
        
        JPanel mesEmpruntsPanel = createEmpruntPanel();
        tabbedPane.addTab(" 💳 Mes Emprunts ", mesEmpruntsPanel);

        // === Onglets supplémentaires pour le PROPRIO ===
        adminPanel = new AdminPanel(this);
        tabbedPane.addTab(" ⚙️ Gestion Globale ", adminPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private void changerBoutique() {
        // Logique changement boutique (idem ton code)
        try {
            List<Boutique> list = boutiqueMetier.listerBoutiques(proprietaire);
            if(list.isEmpty()) return;
            String[] noms = list.stream().map(Boutique::getNom).toArray(String[]::new);
            String choix = (String) JOptionPane.showInputDialog(this, "Choisir boutique", "Choix", JOptionPane.QUESTION_MESSAGE, null, noms, noms[0]);
            if(choix != null) {
                for(Boutique b : list) {
                    if(b.getNom().equals(choix)) {
                        boutiqueActuelle = b;
                        // NE PAS persister le changement - gardons l'historique intact
                        // On utilise seulement boutiqueActuelle pour les nouvelles créations
                        break;
                    }
                }
                setTitle("RepairShop - " + boutiqueActuelle.getNom());
                refreshAll();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void refreshAll() {
        // Rafraîchir mes activités personnelles
        if (mesReparationsPanel != null) mesReparationsPanel.loadReparations();
        if (mesClientsPanel != null) mesClientsPanel.loadClients();
        if (mesComposantsPanel != null) mesComposantsPanel.loadComposants();
        if (maCaissePanel != null) maCaissePanel.loadCaisseData();
        // Rafraîchir l'AdminPanel (gestion globale)
        if (adminPanel != null) adminPanel.refreshData();
    }

    public Proprietaire getProprietaire() { return proprietaire; }
    public Reparateur getReparateurActuel() { return reparateurActuel; }
    public Reparateur getReparateur() { return proprietaire; } // Pour compatibilité avec panels réparateur
    public Boutique getBoutiqueActuelle() { return boutiqueActuelle; }
    
    // Getters pour les métiers (utilisés par les panels réparateur)
    public IReparationMetier getReparationMetier() { return reparationMetier; }
    public IClientMetier getClientMetier() { return clientMetier; }
    public ICaisseMetier getCaisseMetier() { return caisseMetier; }
    public IComposantMetier getComposantMetier() { return composantMetier; }
    public IEmpruntMetier getEmpruntMetier() { return empruntMetier; }
    
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
                java.util.List<dao.Emprunt> emprunts = empruntMetier.listerEmpruntsParReparateur(proprietaire);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                
                // Pour "Mes Emprunts", on affiche TOUS les emprunts du propriétaire
                // sans filtrage par boutique car c'est un onglet personnel
                for (dao.Emprunt emp : emprunts) {
                    String etat;
                if (emp.isRembourse()) {
                    etat = "✓ Remboursé";
                } else {
                    etat = "⏳ En cours";
                }
                    String dateStr;
                    if (emp.getDate() != null) {
                        dateStr = sdf.format(emp.getDate());
                    } else {
                        dateStr = "";
                    }
                    
                    String rembourseStr;
                    if (emp.isRembourse()) {
                        rembourseStr = "Oui";
                    } else {
                        rembourseStr = "Non";
                    }
                    
                    model.addRow(new Object[]{
                        emp.getIdEmprunt(),
                        emp.getMontant(),
                        emp.getType(),
                        dateStr,
                        rembourseStr,
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
                
                int option = JOptionPane.showConfirmDialog(this, formPanel, "Nouvel Emprunt", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    float montant = Float.parseFloat(txtMontant.getText());
                    String type = (String) cbType.getSelectedItem();
                    String commentaire = txtCommentaire.getText().trim();
                    
                    String commentaireEmprunt;
                if (commentaire.isEmpty()) {
                    commentaireEmprunt = "Emprunt " + type;
                } else {
                    commentaireEmprunt = commentaire;
                }
                empruntMetier.creerEmprunt(proprietaire, montant, type, commentaireEmprunt);
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
                java.util.List<dao.Emprunt> emprunts = empruntMetier.listerEmpruntsParReparateur(proprietaire);
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
                JOptionPane.showMessageDialog(panel, "Veuillez sélectionner un emprunt à modifier");
                return;
            }
            
            try {
                Long idEmprunt = (Long) model.getValueAt(selectedRow, 0);
                java.util.List<dao.Emprunt> emprunts = empruntMetier.listerEmpruntsParReparateur(proprietaire);
                dao.Emprunt emp = emprunts.stream().filter(emprunt -> emprunt.getIdEmprunt().equals(idEmprunt)).findFirst().orElse(null);
                
                if (emp == null || emp.isRembourse()) {
                    JOptionPane.showMessageDialog(panel, "Impossible de modifier cet emprunt");
                    return;
                }
                
                JTextField txtMontant = new JTextField(String.valueOf(emp.getMontant()));
                String commentaireValue;
                if (emp.getCommentaire() != null) {
                    commentaireValue = emp.getCommentaire();
                } else {
                    commentaireValue = "";
                }
                JTextField txtCommentaire = new JTextField(commentaireValue);
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
                    
                    emp.setMontant(nouveauMontant);
                    emp.setType(nouveauType);
                    emp.setCommentaire(nouveauCommentaire);
                    
                    EntityManager em = JPAUtil.getInstance().getEntityManager();
                    EntityTransaction tx = em.getTransaction();
                    try {
                        tx.begin();
                        em.merge(emp);
                        tx.commit();
                        JOptionPane.showMessageDialog(this, "Emprunt modifié avec succès!");
                        loadData.run();
                    } catch (Exception ex) {
                        if (tx.isActive()) tx.rollback();
                        throw ex;
                    } finally {
                        if (em != null && em.isOpen()) em.close();
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        btnSupprimer.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(panel, "Veuillez sélectionner un emprunt à supprimer");
                return;
            }
            
            try {
                Long idEmprunt = (Long) model.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(panel, 
                    "Voulez-vous vraiment supprimer cet emprunt?\\n⚠️ Cette action est irréversible!",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    EntityManager em = JPAUtil.getInstance().getEntityManager();
                    EntityTransaction tx = em.getTransaction();
                    try {
                        tx.begin();
                        dao.Emprunt empManaged = em.find(dao.Emprunt.class, idEmprunt);
                        if (empManaged != null) {
                            em.remove(empManaged);
                        }
                        tx.commit();
                        JOptionPane.showMessageDialog(panel, "Emprunt supprimé avec succès!");
                        loadData.run();
                    } catch (Exception ex) {
                        if (tx.isActive()) tx.rollback();
                        throw ex;
                    } finally {
                        if (em != null && em.isOpen()) em.close();
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Erreur: " + ex.getMessage());
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
                    LoginFrame loginFrame = new LoginFrame();
                    loginFrame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
