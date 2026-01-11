package presentation;

import java.awt.BorderLayout;

// Panel d'administration - Gérer réparateurs, boutiques et statistiques
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import dao.Boutique;
import dao.Reparateur;
import dao.StatistiquesCaisse;
import metier.BoutiqueMetierImpl;
import metier.CaisseMetierImpl;
import metier.ClientMetierImpl;
import metier.EmpruntMetierImpl;
import metier.IBoutiqueMetier;
import metier.ICaisseMetier;
import metier.IClientMetier;
import metier.IEmpruntMetier;
import metier.IReparationMetier;
import metier.ReparationMetierImpl;

public class AdminPanel extends JPanel {

    private final MainFrame mainFrame;
    private final IBoutiqueMetier boutiqueMetier;
    private final ICaisseMetier caisseMetier;
    private final IEmpruntMetier empruntMetier;
    private final IReparationMetier reparationMetier;
    private final IClientMetier clientMetier;
    
    private JTabbedPane tabbedPane;
    
    // Runnable pour rafraîchir les données
    private Runnable loadDataReparations;
    private Runnable loadDataCaisses;
    private Runnable loadDataEmprunts;
    private Runnable loadDataClients;

    /**
     * Constructeur par défaut (WindowBuilder)
     * @wbp.parser.constructor
     */
    public AdminPanel() {
        this.mainFrame = null;
        this.boutiqueMetier = null;
        this.caisseMetier = null;
        this.empruntMetier = null;
        this.reparationMetier = null;
        this.clientMetier = null;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        initComponents();
    }

    public AdminPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.boutiqueMetier = new BoutiqueMetierImpl();
        this.caisseMetier = new CaisseMetierImpl();
        this.empruntMetier = new EmpruntMetierImpl();
        this.reparationMetier = new ReparationMetierImpl();
        this.clientMetier = new ClientMetierImpl();
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        initComponents();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Onglet Réparations - TOUTES les réparations
        JPanel reparationPanelAdmin = createReparationPanelAdmin();
        tabbedPane.addTab("Réparations", reparationPanelAdmin);

        // Onglet Clients - TOUS les clients
        JPanel clientPanelAdmin = createClientPanelAdmin();
        tabbedPane.addTab("Clients", clientPanelAdmin);

        // Onglet Caisses - TOUTES les caisses
        JPanel caissePanelAdmin = createCaissePanelAdmin();
        tabbedPane.addTab("Caisses", caissePanelAdmin);
        
        // Onglet Emprunts - TOUS les emprunts
        JPanel empruntPanel = createEmpruntPanelAdmin();
        tabbedPane.addTab("Emprunts", empruntPanel);

        // Onglet Gestion Administrative
        JPanel gestionAdminPanel = createGestionAdministrativePanel();
        tabbedPane.addTab(" Gestion Administrative", gestionAdminPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }
    
    // Crée un faux ReparateurFrame pour réutiliser les panels existants
    private ReparateurFrame createMockReparateurFrame() {
        ReparateurFrame frame = new ReparateurFrame(mainFrame.getReparateurActuel());
        return frame;
    }
    
    // Panel pour afficher TOUTES les réparations (tous réparateurs)
    private JPanel createReparationPanelAdmin() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Titre
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        topPanel.setBackground(Color.WHITE);
        
        JLabel lblTitle = new JLabel("Toutes les Réparations");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(lblTitle);
        
        JButton btnRefresh = new JButton("Actualiser");
        JButton btnNouveau = new JButton("Nouvelle réparation");
        JButton btnDetails = new JButton("Voir détails");
        
        topPanel.add(btnRefresh);
        topPanel.add(btnNouveau);
        topPanel.add(btnDetails);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Table
        String[] cols = {"Code", "Client", "Réparateur", "État", "Prix (DH)", "Date Dépôt", "Date Livraison"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Charger TOUTES les réparations
        loadDataReparations = () -> {
            model.setRowCount(0);
            try {
                // Utiliser la boutique actuelle dynamiquement
                Boutique boutiqueActuelle = mainFrame.getBoutiqueActuelle();
                if (boutiqueActuelle == null) return;
                
                java.util.List<dao.Reparation> reparations = boutiqueMetier.listerDernieresReparations(boutiqueActuelle, 1000);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                
                for (dao.Reparation rep : reparations) {
                    String clientNom = rep.getClient() != null ? rep.getClient().getNom() + " " + rep.getClient().getPrenom() : "N/A";
                    String reparateurNom = rep.getReparateur() != null ? rep.getReparateur().getPrenom() + " " + rep.getReparateur().getNom() : "N/A";
                    String dateDepot = rep.getDateDepot() != null ? sdf.format(rep.getDateDepot()) : "";
                    String dateLivraison = rep.getDateLivraison() != null ? sdf.format(rep.getDateLivraison()) : "";
                    
                    model.addRow(new Object[]{
                        rep.getCodeSuivi(),
                        clientNom,
                        reparateurNom,
                        rep.getEtat(),
                        rep.getPrixTotal(),
                        dateDepot,
                        dateLivraison
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        
        btnRefresh.addActionListener(e -> loadDataReparations.run());
        
        btnDetails.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(panel, "Sélectionnez une réparation");
                return;
            }
            
            try {
                String codeSuivi = (String) model.getValueAt(row, 0);
                dao.Reparation rep = reparationMetier.rechercherParCodeSuivi(codeSuivi);
                if (rep != null) {
                    new ReparationDetailDialog(mainFrame, reparationMetier, rep).setVisible(true);
                    loadDataReparations.run();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Erreur: " + ex.getMessage());
            }
        });
        
        btnNouveau.addActionListener(e -> {
            JOptionPane.showMessageDialog(panel, "Utilisez l'interface Réparateur pour créer une nouvelle réparation");
        });
        
        loadDataReparations.run();
        return panel;
    }
    
    // Panel pour afficher TOUTES les caisses
    private JPanel createCaissePanelAdmin() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        topPanel.setBackground(Color.WHITE);
        
        JLabel lblTitle = new JLabel("Toutes les Caisses");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(lblTitle);
        
        JButton btnRefresh = new JButton("Actualiser");
        topPanel.add(btnRefresh);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        String[] cols = {"ID Caisse", "Réparateur", "Solde (DH)", "Dernier Mouvement"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        loadDataCaisses = () -> {
            model.setRowCount(0);
            try {
                // Utiliser la boutique actuelle dynamiquement
                Boutique boutiqueActuelle = mainFrame.getBoutiqueActuelle();
                if (boutiqueActuelle == null) return;
                
                java.util.List<dao.Caisse> caisses = boutiqueMetier.listerCaissesReparateurs(boutiqueActuelle);
                List<dao.Reparateur> reparateurs = boutiqueMetier.listerReparateurs(boutiqueActuelle);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                
                for (dao.Caisse caisse : caisses) {
                    String reparateurNom = "N/A";
                    for (dao.Reparateur rep : reparateurs) {
                        if (rep.getCaisse() != null && rep.getCaisse().getIdCaisse().equals(caisse.getIdCaisse())) {
                            reparateurNom = rep.getPrenom() + " " + rep.getNom();
                            break;
                        }
                    }
                    
                    String dernierMvt = caisse.getDernierMouvement() != null ? sdf.format(caisse.getDernierMouvement()) : "";
                    
                    model.addRow(new Object[]{
                        caisse.getIdCaisse(),
                        reparateurNom,
                        String.format("%.2f", caisse.getSoldeActuel()),
                        dernierMvt
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        
        btnRefresh.addActionListener(e -> loadDataCaisses.run());
        loadDataCaisses.run();
        
        return panel;
    }
    
    // Panel pour TOUS les emprunts
    private JPanel createEmpruntPanelAdmin() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        topPanel.setBackground(Color.WHITE);
        
        JLabel lblTitle = new JLabel("Tous les Emprunts");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(lblTitle);
        
        JButton btnRefresh = new JButton("Actualiser");
        topPanel.add(btnRefresh);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        String[] cols = {"ID", "Réparateur", "Montant (DH)", "Type", "Date", "Remboursé", "État"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        loadDataEmprunts = () -> {
            model.setRowCount(0);
            try {
                // Utiliser la boutique actuelle dynamiquement
                Boutique boutiqueActuelle = mainFrame.getBoutiqueActuelle();
                if (boutiqueActuelle == null) return;
                
                List<dao.Reparateur> reparateurs = boutiqueMetier.listerReparateurs(boutiqueActuelle);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                
                for (dao.Reparateur rep : reparateurs) {
                    java.util.List<dao.Emprunt> emprunts = empruntMetier.listerEmpruntsParReparateur(rep);
                    for (dao.Emprunt emp : emprunts) {
                        String etat = emp.isRembourse() ? " Remboursé" : " En cours";
                        String repNom = rep.getPrenom() + " " + rep.getNom();
                        
                        model.addRow(new Object[]{
                            emp.getIdEmprunt(),
                            repNom,
                            emp.getMontant(),
                            emp.getType(),
                            emp.getDate() != null ? sdf.format(emp.getDate()) : "",
                            emp.isRembourse() ? "Oui" : "Non",
                            etat
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        
        btnRefresh.addActionListener(e -> loadDataEmprunts.run());
        loadDataEmprunts.run();
        
        return panel;
    }
    
    // Panel pour afficher TOUS les clients
    private JPanel createClientPanelAdmin() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        topPanel.setBackground(Color.WHITE);
        
        JLabel lblTitle = new JLabel("Tous les Clients");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(lblTitle);
        
        JButton btnRefresh = new JButton("Actualiser");
        topPanel.add(btnRefresh);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        String[] cols = {"ID", "Nom", "Prénom", "Téléphone", "Adresse", "Fidèle"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        loadDataClients = () -> {
            model.setRowCount(0);
            try {
                // Utiliser la boutique actuelle dynamiquement
                Boutique boutiqueActuelle = mainFrame.getBoutiqueActuelle();
                if (boutiqueActuelle == null) return;
                
                java.util.List<dao.Client> clients = clientMetier.listerClientsParBoutique(boutiqueActuelle);
                
                for (dao.Client client : clients) {
                    model.addRow(new Object[]{
                        client.getIdClient(),
                        client.getNom(),
                        client.getPrenom(),
                        client.getTelephone(),
                        client.getAdresse(),
                        client.isFidele() ? "Oui" : "Non"
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        
        btnRefresh.addActionListener(e -> loadDataClients.run());
        loadDataClients.run();
        
        return panel;
    }
    
    private JPanel createGestionAdministrativePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // En-tête
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        topPanel.setBackground(Color.WHITE);
        
        JLabel lblTitle = new JLabel(" Administration Avancée");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        topPanel.add(lblTitle);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Panneau central avec boutons
        JPanel centerPanel = new JPanel(new GridLayout(4, 2, 15, 15));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        JButton btnToutesCaisses = createAdminButton(" Consulter Toutes les Caisses", new Color(142, 68, 173));
        JButton btnDetailsReparations = createAdminButton(" Détails Caisses + Réparations", new Color(52, 152, 219));
        JButton btnDernieresRep = createAdminButton(" Dernières Réparations", new Color(46, 204, 113));
        JButton btnGestionRep = createAdminButton(" Gestion CRUD Réparations", new Color(241, 196, 15));
        JButton btnGestionReparateurs = createAdminButton(" Gestion des Réparateurs", new Color(231, 76, 60));
        JButton btnGestionBoutiques = createAdminButton(" Gestion des Boutiques", new Color(230, 126, 34));
        JButton btnStats = createAdminButton(" Statistiques Globales", new Color(155, 89, 182));
        
        centerPanel.add(btnToutesCaisses);
        centerPanel.add(btnDetailsReparations);
        centerPanel.add(btnDernieresRep);
        centerPanel.add(btnGestionRep);
        centerPanel.add(btnGestionReparateurs);
        centerPanel.add(btnGestionBoutiques);
        centerPanel.add(btnStats);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        // Actions
        btnToutesCaisses.addActionListener(e -> afficherToutesCaisses());
        btnDetailsReparations.addActionListener(e -> afficherCaissesAvecDetailsReparations());
        btnDernieresRep.addActionListener(e -> afficherDernieresReparations());
        btnGestionRep.addActionListener(e -> afficherGestionReparations());
        btnGestionReparateurs.addActionListener(e -> afficherGestionReparateurs());
        btnGestionBoutiques.addActionListener(e -> afficherGestionBoutiques());
        btnStats.addActionListener(e -> afficherStatistiquesGlobales());
        
        return panel;
    }
    
    private JButton createAdminButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        return btn;
    }
    
    // === MÉTHODES DE GESTION ADMINISTRATIVE ===
    
    // 1. Consulter toutes les caisses (simple liste)
    private void afficherToutesCaisses() {
        try {
            java.util.List<dao.Caisse> caisses = boutiqueMetier.listerCaissesReparateurs(mainFrame.getProprietaire());
            
            if (caisses == null || caisses.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucune caisse disponible", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Toutes les Caisses (Réelles)", true);
            dialog.setSize(650, 400);
            dialog.setLocationRelativeTo(this);
            
            String[] cols = {"ID Caisse", "Caisse Réelle (DH)", "Réparateur"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
            JTable table = new JTable(model);
            
            // Charger tous les réparateurs
            List<Reparateur> reparateurs = boutiqueMetier.listerReparateurs(mainFrame.getBoutiqueActuelle());
            
            for (dao.Caisse caisse : caisses) {
                String reparateurNom = "N/A";
                float caisseReelle = caisse.getSoldeActuel(); // Caisse réelle = montant actuel
                
                // Trouver le réparateur qui possède cette caisse
                for (Reparateur rep : reparateurs) {
                    if (rep.getCaisse() != null && rep.getCaisse().getIdCaisse().equals(caisse.getIdCaisse())) {
                        reparateurNom = rep.getPrenom() + " " + rep.getNom();
                        // Utiliser ICaisseMetier pour obtenir la caisse réelle
                        try {
                            caisseReelle = caisseMetier.consulterCaisseReelle(rep);
                        } catch (Exception e) {
                            // Garder la valeur par défaut
                        }
                        break;
                    }
                }
                model.addRow(new Object[]{
                    caisse.getIdCaisse(),
                    String.format("%.2f", caisseReelle),
                    reparateurNom
                });
            }
            
            dialog.add(new JScrollPane(table));
            dialog.setVisible(true);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    // 2. Consulter caisses avec détails des réparations
    private void afficherCaissesAvecDetailsReparations() {
        try {
            java.util.List<dao.Caisse> caisses = boutiqueMetier.listerCaissesReparateurs(mainFrame.getProprietaire());
            
            if (caisses == null || caisses.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucune caisse disponible");
                return;
            }
            
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Caisses avec Détails Réparations", true);
            dialog.setSize(900, 500);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());
            
            StringBuilder sb = new StringBuilder();
            sb.append("=== CAISSES AVEC DÉTAILS RÉPARATIONS ===\n\n");
            
            // Charger tous les réparateurs
            List<Reparateur> reparateurs = boutiqueMetier.listerReparateurs(mainFrame.getBoutiqueActuelle());
            
            for (dao.Caisse caisse : caisses) {
                Reparateur rep = null;
                String repNom = "N/A";
                
                // Trouver le réparateur qui possède cette caisse
                for (Reparateur r : reparateurs) {
                    if (r.getCaisse() != null && r.getCaisse().getIdCaisse().equals(caisse.getIdCaisse())) {
                        rep = r;
                        repNom = r.getPrenom() + " " + r.getNom();
                        break;
                    }
                }
                
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                sb.append(" Réparateur: ").append(repNom).append("\n");
                sb.append(" Solde Caisse: ").append(String.format("%.2f DH", caisse.getSoldeActuel())).append("\n\n");
                
                // Lister les réparations de ce réparateur
                if (rep != null) {
                    try {
                        java.util.List<dao.Reparation> reparations = reparationMetier.listerReparationsParReparateur(rep);
                        sb.append("    Réparations (").append(reparations.size()).append("):\n");
                        
                        for (dao.Reparation reparation : reparations) {
                            sb.append("      • ").append(reparation.getCodeSuivi())
                              .append(" - ").append(reparation.getEtat())
                              .append(" - ").append(String.format("%.2f DH", reparation.getPrixTotal()))
                              .append("\n");
                        }
                    } catch (Exception e) {
                        sb.append("   Erreur lors du chargement des réparations\n");
                    }
                } else {
                    sb.append("   Aucune réparation (pas de réparateur associé)\n");
                }
                
                sb.append("\n");
            }
            
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            dialog.add(scrollPane, BorderLayout.CENTER);
            
            dialog.setVisible(true);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // 3. Consulter les dernières réparations
    private void afficherDernieresReparations() {
        try {
            // Lister tous les réparateurs de la boutique SAUF le proprio
            List<Reparateur> reparateurs = boutiqueMetier.listerReparateurs(mainFrame.getBoutiqueActuelle());
            
            if (reparateurs == null || reparateurs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucun réparateur trouvé");
                return;
            }
            
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Toutes les Réparations par Réparateur", true);
            dialog.setSize(900, 600);
            dialog.setLocationRelativeTo(this);
            
            String[] cols = {"Code", "Client", "Réparateur", "État", "Prix (DH)", "Date Dépôt", "Date Livraison"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
            JTable table = new JTable(model);
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            
            // Parcourir tous les réparateurs SAUF le proprio
            for (Reparateur rep : reparateurs) {
                // Exclure le proprio (qui est aussi un réparateur)
                if (rep.getId().equals(mainFrame.getProprietaire().getId())) {
                    continue; // Sauter le proprio
                }
                
                try {
                    // Lister TOUTES les réparations de ce réparateur
                    java.util.List<dao.Reparation> reparations = reparationMetier.listerReparationsParReparateur(rep);
                    
                    for (dao.Reparation reparation : reparations) {
                        String clientNom = reparation.getClient() != null ? 
                            reparation.getClient().getNom() + " " + reparation.getClient().getPrenom() : "N/A";
                        String reparateurNom = rep.getPrenom() + " " + rep.getNom();
                        String dateDepot = reparation.getDateDepot() != null ? sdf.format(reparation.getDateDepot()) : "";
                        String dateLivraison = reparation.getDateLivraison() != null ? sdf.format(reparation.getDateLivraison()) : "";
                        
                        model.addRow(new Object[]{
                            reparation.getCodeSuivi(),
                            clientNom,
                            reparateurNom,
                            reparation.getEtat(),
                            reparation.getPrixTotal(),
                            dateDepot,
                            dateLivraison
                        });
                    }
                } catch (Exception e) {
                    // Ignorer les erreurs pour un réparateur spécifique
                }
            }
            
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Aucune réparation trouvée");
                dialog.dispose();
                return;
            }
            
            dialog.add(new JScrollPane(table));
            dialog.setVisible(true);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // 4. Gestion CRUD des réparations avec modification des gains
    private void afficherGestionReparations() {
        try {
            // Récupérer toutes les réparations
            java.util.List<dao.Reparation> reparations = boutiqueMetier.listerDernieresReparations(mainFrame.getProprietaire(), 100);
            
            if (reparations == null || reparations.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucune réparation disponible");
                return;
            }
            
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Gestion CRUD des Réparations", true);
            dialog.setSize(1000, 600);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());
            
            // Table
            String[] cols = {"Code", "Client", "Réparateur", "État", "Prix (DH)", "% Gain", "Date"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
            JTable table = new JTable(model);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            
            Runnable loadData = () -> {
                model.setRowCount(0);
                try {
                    java.util.List<dao.Reparation> reps = boutiqueMetier.listerDernieresReparations(mainFrame.getProprietaire(), 100);
                    for (dao.Reparation rep : reps) {
                        String clientNom = rep.getClient() != null ? rep.getClient().getNom() + " " + rep.getClient().getPrenom() : "N/A";
                        String reparateurNom = rep.getReparateur() != null ? rep.getReparateur().getPrenom() + " " + rep.getReparateur().getNom() : "N/A";
                        float pourcentage = rep.getReparateur() != null ? rep.getReparateur().getPourcentageGain() : 0;
                        String dateDepot = rep.getDateDepot() != null ? sdf.format(rep.getDateDepot()) : "";
                        
                        model.addRow(new Object[]{
                            rep.getCodeSuivi(),
                            clientNom,
                            reparateurNom,
                            rep.getEtat(),
                            rep.getPrixTotal(),
                            pourcentage + "%",
                            dateDepot
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            
            loadData.run();
            
            // Boutons
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton btnRefresh = new JButton("Actualiser");
            JButton btnModifierPrix = new JButton("Modifier Prix");
            JButton btnSupprimer = new JButton("Supprimer");
            btnSupprimer.setBackground(new Color(231, 76, 60));
            btnSupprimer.setForeground(Color.WHITE);
            
            btnPanel.add(btnRefresh);
            btnPanel.add(btnModifierPrix);
            btnPanel.add(btnSupprimer);
            
            dialog.add(new JScrollPane(table), BorderLayout.CENTER);
            dialog.add(btnPanel, BorderLayout.SOUTH);
            
            // Actions
            btnRefresh.addActionListener(e -> loadData.run());
            
            btnModifierPrix.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(dialog, "Sélectionnez une réparation");
                    return;
                }
                
                try {
                    String codeSuivi = (String) model.getValueAt(row, 0);
                    dao.Reparation rep = reparationMetier.rechercherParCodeSuivi(codeSuivi);
                    
                    if (rep == null) {
                        JOptionPane.showMessageDialog(dialog, "Réparation introuvable");
                        return;
                    }
                    
                    String input = JOptionPane.showInputDialog(dialog, "Nouveau prix total (DH):", rep.getPrixTotal());
                    if (input != null && !input.trim().isEmpty()) {
                        float nouveauPrix = Float.parseFloat(input.trim());
                        rep.setPrixTotal(nouveauPrix);
                        reparationMetier.modifierReparation(rep);
                        JOptionPane.showMessageDialog(dialog, "Prix modifié avec succès!");
                        loadData.run();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Prix invalide");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Erreur: " + ex.getMessage());
                }
            });
            
            btnSupprimer.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(dialog, "Sélectionnez une réparation");
                    return;
                }
                
                try {
                    String codeSuivi = (String) model.getValueAt(row, 0);
                    int confirm = JOptionPane.showConfirmDialog(dialog, 
                        "Voulez-vous vraiment supprimer la réparation " + codeSuivi + "?", 
                        "Confirmation", 
                        JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        dao.Reparation rep = reparationMetier.rechercherParCodeSuivi(codeSuivi);
                        if (rep != null) {
                            reparationMetier.supprimerReparation(rep);
                            JOptionPane.showMessageDialog(dialog, "Réparation supprimée!");
                            loadData.run();
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Erreur: " + ex.getMessage());
                }
            });
            
            dialog.setVisible(true);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // 5. Gestion des boutiques
    private void afficherGestionBoutiques() {
        try {
            java.util.List<dao.Boutique> boutiques = boutiqueMetier.listerBoutiques(mainFrame.getProprietaire());
            
            if (boutiques == null || boutiques.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucune boutique disponible");
                return;
            }
            
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Gestion des Boutiques", true);
            dialog.setSize(700, 400);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());
            
            String[] cols = {"ID", "Nom", "Adresse", "Téléphone", "Nb Réparateurs"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
            JTable table = new JTable(model);
            
            for (dao.Boutique b : boutiques) {
                int nbRep = b.getReparateurs() != null ? b.getReparateurs().size() : 0;
                model.addRow(new Object[]{
                    b.getIdBoutique(),
                    b.getNom(),
                    b.getAdresse(),
                    "",
                    nbRep
                });
            }
            
            JPanel btnPanel = new JPanel(new FlowLayout());
            JButton btnModifier = new JButton("Modifier");
            JButton btnAjouter = new JButton("Ajouter Boutique");
            
            btnPanel.add(btnAjouter);
            btnPanel.add(btnModifier);
            
            dialog.add(new JScrollPane(table), BorderLayout.CENTER);
            dialog.add(btnPanel, BorderLayout.SOUTH);
            
            btnModifier.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(dialog, "Sélectionnez une boutique");
                    return;
                }
                
                try {
                    Long idBoutique = (Long) model.getValueAt(row, 0);
                    dao.Boutique boutique = null;
                    for (dao.Boutique b : boutiques) {
                        if (b.getIdBoutique().equals(idBoutique)) {
                            boutique = b;
                            break;
                        }
                    }
                    
                    if (boutique != null) {
                        BoutiqueDialog boutiqueDialog = new BoutiqueDialog((Frame) SwingUtilities.getWindowAncestor(this), boutiqueMetier, boutique);
                        boutiqueDialog.setVisible(true);
                        dialog.dispose();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Erreur: " + ex.getMessage());
                }
            });
            
            btnAjouter.addActionListener(e -> {
                BoutiqueDialog boutiqueDialog = new BoutiqueDialog((Frame) SwingUtilities.getWindowAncestor(this), mainFrame.getProprietaire(), true);
                boutiqueDialog.setVisible(true);
                dialog.dispose();
                // Rafraîchir l'affichage
                afficherGestionBoutiques();
            });
            
            dialog.setVisible(true);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // 6. Gestion des Réparateurs (pourcentage, modif infos)
    private void afficherGestionReparateurs() {
        try {
            List<Reparateur> reparateurs = boutiqueMetier.listerReparateurs(mainFrame.getBoutiqueActuelle());
            
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Gestion des Réparateurs", true);
            dialog.setSize(800, 500);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());
            
            String[] cols = {"ID", "Nom", "Prénom", "Email", "% Gains", "Boutique"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
            JTable table = new JTable(model);
            
            // Charger les réparateurs
            Runnable loadData = () -> {
                model.setRowCount(0);
                try {
                    List<Reparateur> reps = boutiqueMetier.listerReparateurs(mainFrame.getBoutiqueActuelle());
                    for (Reparateur rep : reps) {
                        String boutiqueName = rep.getBoutique() != null ? rep.getBoutique().getNom() : "N/A";
                        model.addRow(new Object[]{
                            rep.getId(),
                            rep.getNom(),
                            rep.getPrenom(),
                            rep.getEmail(),
                            String.format("%.1f%%", rep.getPourcentageGain()),
                            boutiqueName
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            
            loadData.run();
            
            JPanel btnPanel = new JPanel(new FlowLayout());
            JButton btnAjouter = new JButton("Ajouter Réparateur");
            JButton btnModifierPourcentage = new JButton("Modifier % Gains");
            JButton btnModifierInfos = new JButton("Modifier Informations");
            
            btnAjouter.setBackground(new Color(46, 204, 113));
            btnAjouter.setForeground(Color.WHITE);
            
            btnPanel.add(btnAjouter);
            btnPanel.add(btnModifierPourcentage);
            btnPanel.add(btnModifierInfos);
            
            dialog.add(new JScrollPane(table), BorderLayout.CENTER);
            dialog.add(btnPanel, BorderLayout.SOUTH);
            
            // Ajouter un nouveau réparateur
            btnAjouter.addActionListener(e -> {
                JTextField txtNom = new JTextField(20);
                JTextField txtPrenom = new JTextField(20);
                JTextField txtEmail = new JTextField(20);
                JPasswordField txtMdp = new JPasswordField(20);
                JTextField txtPourcentage = new JTextField("50", 20);
                
                JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
                panel.add(new JLabel("Nom:"));
                panel.add(txtNom);
                panel.add(new JLabel("Prénom:"));
                panel.add(txtPrenom);
                panel.add(new JLabel("Email:"));
                panel.add(txtEmail);
                panel.add(new JLabel("Mot de passe:"));
                panel.add(txtMdp);
                panel.add(new JLabel("% Gains (0-100):"));
                panel.add(txtPourcentage);
                
                int result = JOptionPane.showConfirmDialog(dialog, panel, 
                    "Ajouter un Réparateur", JOptionPane.OK_CANCEL_OPTION);
                
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        String nom = txtNom.getText().trim();
                        String prenom = txtPrenom.getText().trim();
                        String email = txtEmail.getText().trim();
                        String mdp = new String(txtMdp.getPassword());
                        float pourcentage = Float.parseFloat(txtPourcentage.getText().trim());
                        
                        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || mdp.isEmpty()) {
                            JOptionPane.showMessageDialog(dialog, "Tous les champs sont obligatoires", "Erreur", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        
                        boutiqueMetier.creerReparateur(
                            mainFrame.getBoutiqueActuelle(),
                            nom,
                            prenom,
                            email,
                            mdp,
                            pourcentage
                        );
                        
                        JOptionPane.showMessageDialog(dialog, 
                            "Réparateur créé avec succès !\nEmail: " + email + "\nMot de passe: " + mdp, 
                            "Succès", 
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        loadData.run(); // Recharger la table
                        
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(dialog, "Pourcentage invalide", "Erreur", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            
            // Modifier le pourcentage de gains
            btnModifierPourcentage.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(dialog, "Sélectionnez un réparateur");
                    return;
                }
                
                try {
                    Long idRep = (Long) model.getValueAt(row, 0);
                    List<Reparateur> reps = boutiqueMetier.listerReparateurs(mainFrame.getBoutiqueActuelle());
                    Reparateur reparateur = reps.stream()
                        .filter(r -> r.getId().equals(idRep))
                        .findFirst()
                        .orElse(null);
                    
                    if (reparateur != null) {
                        String input = JOptionPane.showInputDialog(dialog,
                            "Nouveau % de gain pour " + reparateur.getPrenom() + " " + reparateur.getNom() + ":", 
                            reparateur.getPourcentageGain());
                        
                        if (input != null && !input.trim().isEmpty()) {
                            float nouveauPourcentage = Float.parseFloat(input);
                            boutiqueMetier.modifierPourcentageGain(reparateur, nouveauPourcentage);
                            JOptionPane.showMessageDialog(dialog, "Pourcentage modifié avec succès !");
                            loadData.run(); 
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Erreur: " + ex.getMessage());
                }
            });
            
            // Modifier les informations du réparateur
            btnModifierInfos.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(dialog, "Sélectionnez un réparateur");
                    return;
                }
                
                try {
                    Long idRep = (Long) model.getValueAt(row, 0);
                    List<Reparateur> reps = boutiqueMetier.listerReparateurs(mainFrame.getBoutiqueActuelle());
                    Reparateur reparateur = reps.stream()
                        .filter(r -> r.getId().equals(idRep))
                        .findFirst()
                        .orElse(null);
                    
                    if (reparateur != null) {
                        // Formulaire de modification
                        JTextField txtNom = new JTextField(reparateur.getNom(), 20);
                        JTextField txtPrenom = new JTextField(reparateur.getPrenom(), 20);
                        JTextField txtEmail = new JTextField(reparateur.getEmail(), 20);
                        
                        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
                        panel.add(new JLabel("Nom:"));
                        panel.add(txtNom);
                        panel.add(new JLabel("Prénom:"));
                        panel.add(txtPrenom);
                        panel.add(new JLabel("Email:"));
                        panel.add(txtEmail);
                        
                        int result = JOptionPane.showConfirmDialog(dialog, panel, 
                            "Modifier Informations", JOptionPane.OK_CANCEL_OPTION);
                        
                        if (result == JOptionPane.OK_OPTION) {
                            reparateur.setNom(txtNom.getText().trim());
                            reparateur.setPrenom(txtPrenom.getText().trim());
                            reparateur.setEmail(txtEmail.getText().trim());
                            
                            boutiqueMetier.modifierReparateur(reparateur);
                            JOptionPane.showMessageDialog(dialog, "Informations modifiées avec succès !");
                            loadData.run(); 
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Erreur: " + ex.getMessage());
                }
            });
            
            dialog.setVisible(true);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // 7. Statistiques globales PAR BOUTIQUE
    private void afficherStatistiquesGlobales() {
        try {
            // Filtrer les statistiques pour la boutique actuelle
            Boutique boutiqueActuelle = mainFrame.getBoutiqueActuelle();
            
            if (boutiqueActuelle == null) {
                JOptionPane.showMessageDialog(this, "Aucune boutique sélectionnée");
                return;
            }
            
            // Récupérer les statistiques filtrées par boutique
            List<Reparateur> reparateurs = boutiqueMetier.listerReparateurs(boutiqueActuelle);
            
            if (reparateurs == null || reparateurs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucun réparateur dans cette boutique");
                return;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("=== STATISTIQUES FINANCIÈRES ===\n");
            sb.append(" Boutique : ").append(boutiqueActuelle.getNom()).append("\n\n");
            
            float revenuTotalBoutique = 0;
            float totalCaissesBoutique = 0;
            int totalReparations = 0;
            
            sb.append("--- Réparateurs ---\n");
            
            for (Reparateur rep : reparateurs) {
                try {
                    // Statistiques par réparateur
                    StatistiquesCaisse statsCaisse = caisseMetier.obtenirStatistiques(rep);
                    int nbReparations = caisseMetier.calculerNombreReparations(rep);
                    
                    float revenuRep = statsCaisse.getRevenuTotal();
                    float caisseRep = statsCaisse.getCaisse();
                    
                    revenuTotalBoutique += revenuRep;
                    totalCaissesBoutique += caisseRep;
                    totalReparations += nbReparations;
                    
                    sb.append("\n ").append(rep.getPrenom()).append(" ").append(rep.getNom()).append("\n");
                    sb.append("    Revenu Total : ").append(String.format("%.2f DH", revenuRep)).append("\n");
                    sb.append("    Caisse : ").append(String.format("%.2f DH", caisseRep)).append("\n");
                    sb.append("    Caisse Réelle : ").append(String.format("%.2f DH", statsCaisse.getCaisseReelle())).append("\n");
                    sb.append("    % Gain : ").append(String.format("%.1f%%", rep.getPourcentageGain())).append("\n");
                    sb.append("    Réparations : ").append(nbReparations).append("\n");
                    
                } catch (Exception e) {
                    sb.append("\n ").append(rep.getPrenom()).append(" ").append(rep.getNom()).append(" : Erreur de calcul\n");
                }
            }
            
            sb.append("\n\n=== TOTAUX BOUTIQUE ===\n");
            sb.append(" Revenu Total : ").append(String.format("%.2f DH", revenuTotalBoutique)).append("\n");
            sb.append(" Total Caisses : ").append(String.format("%.2f DH", totalCaissesBoutique)).append("\n");
            sb.append(" Total Réparations : ").append(totalReparations).append("\n");
            
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(550, 450));
            
            JOptionPane.showMessageDialog(this, scrollPane, 
                "Statistiques - " + boutiqueActuelle.getNom(), 
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // Méthode pour rafraîchir toutes les données lors du changement de boutique
    public void refreshData() {
        // Rafraîchir les onglets avec tables
        if (loadDataReparations != null) {
            loadDataReparations.run();
        }
        if (loadDataCaisses != null) {
            loadDataCaisses.run();
        }
        if (loadDataEmprunts != null) {
            loadDataEmprunts.run();
        }
        if (loadDataClients != null) {
            loadDataClients.run();
        }
    }
}
