package presentation;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import dao.Client;
import exception.ClientException;

// Panel clients réparateur - CRUD simplifié
public class ClientPanelReparateur extends JPanel {

    private Object parentFrame; // Peut être ReparateurFrame ou MainFrame
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    // @wbp.parser.constructor
    public ClientPanelReparateur() {
        this.parentFrame = null;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
    }

    public ClientPanelReparateur(ReparateurFrame reparateurFrame) {
        this.parentFrame = reparateurFrame;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
        loadClients();
    }
    
    public ClientPanelReparateur(MainFrame mainFrame) {
        this.parentFrame = mainFrame;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
        loadClients();
    }
    
    // Méthodes helper pour accéder aux métiers
    private metier.IClientMetier getClientMetier() {
        if (parentFrame instanceof ReparateurFrame) {
            return ((ReparateurFrame) parentFrame).getClientMetier();
        } else if (parentFrame instanceof MainFrame) {
            return ((MainFrame) parentFrame).getClientMetier();
        }
        return null;
    }

    private void initComponents() {
        // Barre du haut
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        topPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Gestion des Clients");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(lblTitle);

        JButton btnNew = new JButton("Nouveau client");
        btnNew.addActionListener(e -> ajouterClient());
        topPanel.add(btnNew);

        JButton btnEdit = new JButton("Modifier");
        btnEdit.addActionListener(e -> modifierClient());
        topPanel.add(btnEdit);

        JButton btnDelete = new JButton("Supprimer");
        btnDelete.addActionListener(e -> supprimerClient());
        topPanel.add(btnDelete);

        JButton btnRefresh = new JButton("Actualiser");
        btnRefresh.addActionListener(e -> loadClients());
        topPanel.add(btnRefresh);

        add(topPanel, BorderLayout.NORTH);

        // Recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(new JLabel("Rechercher:"));
        txtSearch = new JTextField(20);
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                rechercherClients();
            }
        });
        searchPanel.add(txtSearch);
        add(searchPanel, BorderLayout.SOUTH);

        // Table
        String[] cols = {"ID", "Nom", "Prénom", "Téléphone", "Adresse", "Fidèle"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void loadClients() {
        model.setRowCount(0);
        if ( getClientMetier() == null) return;
        
        try {
            // Pour les onglets "MES" (personnels), on affiche TOUS les clients
            // sans filtrage par boutique, car c'est un onglet personnel
            List<Client> list = getClientMetier().listerTousLesClients();
            
            for (Client c : list) {
                String fideleStr;
                if (c.isFidele()) {
                    fideleStr = "Oui";
                } else {
                    fideleStr = "Non";
                }
                
                model.addRow(new Object[]{
                    c.getIdClient(),
                    c.getNom(),
                    c.getPrenom(),
                    c.getTelephone(),
                    c.getAdresse(),
                    fideleStr
                });
            }
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    private void rechercherClients() {
        String search = txtSearch.getText().trim().toLowerCase();
        if (search.isEmpty()) {
            loadClients();
            return;
        }
        
        model.setRowCount(0);
        try {
            // Pour les onglets "MES" (personnels), on affiche TOUS les clients
            // sans filtrage par boutique, car c'est un onglet personnel
            List<Client> list = getClientMetier().listerTousLesClients();
            
            for (Client c : list) {
                String fullText = (c.getNom() + " " + c.getPrenom() + " " + c.getTelephone()).toLowerCase();
                if (fullText.contains(search)) {
                    model.addRow(new Object[]{
                        c.getIdClient(),
                        c.getNom(),
                        c.getPrenom(),
                        c.getTelephone(),
                        c.getAdresse(),
                        c.isFidele() ? "Oui" : "Non"
                    });
                }
            }
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    private void ajouterClient() {
        JTextField txtNom = new JTextField();
        JTextField txtPrenom = new JTextField();
        JTextField txtTel = new JTextField();
        JTextField txtAdresse = new JTextField();
        JCheckBox cbFidele = new JCheckBox("Client fidèle");

        Object[] message = {
            "Nom:", txtNom,
            "Prénom:", txtPrenom,
            "Téléphone:", txtTel,
            "Adresse:", txtAdresse,
            cbFidele
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Nouveau Client", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String nom = txtNom.getText().trim();
                String prenom = txtPrenom.getText().trim();
                String telephone = txtTel.getText().trim();
                String adresse = txtAdresse.getText().trim();
                
                Client c = getClientMetier().creerClient(nom, prenom, telephone, adresse, null);
                
                if (cbFidele.isSelected()) {
                    getClientMetier().marquerClientFidele(c);
                }
                
                JOptionPane.showMessageDialog(this, "Client ajouté avec succès");
                loadClients();
            } catch (ClientException e) {
                JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
            }
        }
    }

    private void modifierClient() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un client");
            return;
        }

        try {
            Long id = (Long) model.getValueAt(row, 0);
            Client c = getClientMetier().rechercherClientParId(id);

            JTextField txtNom = new JTextField(c.getNom());
            JTextField txtPrenom = new JTextField(c.getPrenom());
            JTextField txtTel = new JTextField(c.getTelephone());
            JTextField txtAdresse = new JTextField(c.getAdresse());
            JCheckBox cbFidele = new JCheckBox("Client fidèle", c.isFidele());

            Object[] message = {
                "Nom:", txtNom,
                "Prénom:", txtPrenom,
                "Téléphone:", txtTel,
                "Adresse:", txtAdresse,
                cbFidele
            };

            int option = JOptionPane.showConfirmDialog(this, message, "Modifier Client", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String nom = txtNom.getText().trim();
                String prenom = txtPrenom.getText().trim();
                String telephone = txtTel.getText().trim();
                String adresse = txtAdresse.getText().trim();
                
                getClientMetier().modifierClient(c, nom, prenom, telephone, adresse, null);
                
                if (cbFidele.isSelected() && !c.isFidele()) {
                    getClientMetier().marquerClientFidele(c);
                }
                
                JOptionPane.showMessageDialog(this, "Client modifié avec succès");
                loadClients();
            }
        } catch (ClientException e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
        }
    }

    private void supprimerClient() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un client");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Voulez-vous vraiment supprimer ce client?", 
            "Confirmation", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Long id = (Long) model.getValueAt(row, 0);
                Client c = getClientMetier().rechercherClientParId(id);
                getClientMetier().supprimerClient(c);
                JOptionPane.showMessageDialog(this, "Client supprimé avec succès");
                loadClients();
            } catch (ClientException e) {
                JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
            }
        }
    }
}


