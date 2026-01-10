package presentation;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import dao.Composant;
import exception.ComposantException;

// Panel composants - Gestion stock pièces détachées
public class ComposantPanelReparateur extends JPanel {

    private Object parentFrame; // Peut être ReparateurFrame ou MainFrame
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    /**
     * Constructeur par défaut (WindowBuilder)
     * @wbp.parser.constructor
     */
    public ComposantPanelReparateur() {
        this.parentFrame = null;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
    }

    public ComposantPanelReparateur(ReparateurFrame reparateurFrame) {
        this.parentFrame = reparateurFrame;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
        loadComposants();
    }
    
    public ComposantPanelReparateur(MainFrame mainFrame) {
        this.parentFrame = mainFrame;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
        loadComposants();
    }
    
    // Méthode helper pour accéder aux métiers
    private metier.IComposantMetier getComposantMetier() {
        if (parentFrame instanceof ReparateurFrame) {
            return ((ReparateurFrame) parentFrame).getComposantMetier();
        } else if (parentFrame instanceof MainFrame) {
            return ((MainFrame) parentFrame).getComposantMetier();
        }
        return null;
    }
    
    // Méthode helper pour obtenir le réparateur actuel
    private dao.Reparateur getReparateur() {
        if (parentFrame instanceof ReparateurFrame) {
            return ((ReparateurFrame) parentFrame).getReparateur();
        } else if (parentFrame instanceof MainFrame) {
            return ((MainFrame) parentFrame).getReparateurActuel();
        }
        return null;
    }

    private void initComponents() {
        // Barre du haut
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        topPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Gestion des Composants");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(lblTitle);

        JButton btnNew = new JButton("Nouveau composant");
        btnNew.addActionListener(e -> ajouterComposant());
        topPanel.add(btnNew);

        JButton btnEdit = new JButton("Modifier");
        btnEdit.addActionListener(e -> modifierComposant());
        topPanel.add(btnEdit);

        JButton btnDelete = new JButton("Supprimer");
        btnDelete.addActionListener(e -> supprimerComposant());
        topPanel.add(btnDelete);

        JButton btnRefresh = new JButton("Actualiser");
        btnRefresh.addActionListener(e -> loadComposants());
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
                rechercherComposants();
            }
        });
        searchPanel.add(txtSearch);
        add(searchPanel, BorderLayout.SOUTH);

        // Table
        String[] cols = {"ID", "Nom", "Prix (DH)", "Quantité en stock", "Disponibilité"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void loadComposants() {
        model.setRowCount(0);
        if ( getComposantMetier() == null) return;
        
        try {
            // Pour les onglets "MES" (personnels), on affiche TOUS les composants
            // sans filtrage par boutique, car c'est un onglet personnel
            List<Composant> list = getComposantMetier().listerComposants();
            
            for (Composant c : list) {
                model.addRow(new Object[]{
                    c.getIdComposant(),
                    c.getNom(),
                    c.getPrix(),
                    c.getQuantite()
                });
            }
        } catch (ComposantException e) {
            e.printStackTrace();
        }
    }

    private void rechercherComposants() {
        String search = txtSearch.getText().trim();
        if (search.isEmpty()) {
            loadComposants();
            return;
        }
        
        model.setRowCount(0);
        try {
            List<Composant> list = getComposantMetier().chercherComposantsParNom(search);
            for (Composant c : list) {
                model.addRow(new Object[]{
                    c.getIdComposant(),
                    c.getNom(),
                    c.getPrix(),
                    c.getQuantite()
                });
            }
        } catch (ComposantException e) {
            e.printStackTrace();
        }
    }

    private void ajouterComposant() {
        JTextField txtNom = new JTextField();
        JTextField txtPrix = new JTextField();
        JTextField txtQuantite = new JTextField();

        Object[] message = {
            "Nom:", txtNom,
            "Prix (DH):", txtPrix,
            "Quantité:", txtQuantite
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Nouveau Composant", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Composant c = new Composant();
                c.setNom(txtNom.getText().trim());
                c.setPrix(Float.parseFloat(txtPrix.getText().trim()));
                c.setQuantite(Integer.parseInt(txtQuantite.getText().trim()));
                
                getComposantMetier().ajouterComposant(c, getReparateur());
                JOptionPane.showMessageDialog(this, "Composant ajouté avec succès");
                loadComposants();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Erreur: Prix et quantité doivent être des nombres");
            } catch (ComposantException e) {
                JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
            }
        }
    }

    private void modifierComposant() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un composant");
            return;
        }

        try {
            Long id = (Long) model.getValueAt(row, 0);
            Composant c = getComposantMetier().chercherComposant(id);

            JTextField txtNom = new JTextField(c.getNom());
            JTextField txtPrix = new JTextField(String.valueOf(c.getPrix()));
            JTextField txtQuantite = new JTextField(String.valueOf(c.getQuantite()));

            Object[] message = {
                "Nom:", txtNom,
                "Prix (DH):", txtPrix,
                "Quantité:", txtQuantite
            };

            int option = JOptionPane.showConfirmDialog(this, message, "Modifier Composant", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                c.setNom(txtNom.getText().trim());
                c.setPrix(Float.parseFloat(txtPrix.getText().trim()));
                c.setQuantite(Integer.parseInt(txtQuantite.getText().trim()));
                
                getComposantMetier().modifierComposant(c);
                JOptionPane.showMessageDialog(this, "Composant modifié avec succès");
                loadComposants();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Erreur: Prix et quantité doivent être des nombres");
        } catch (ComposantException e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
        }
    }

    private void supprimerComposant() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un composant");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Voulez-vous vraiment supprimer ce composant?", 
            "Confirmation", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Long id = (Long) model.getValueAt(row, 0);
                getComposantMetier().supprimerComposant(id);
                JOptionPane.showMessageDialog(this, "Composant supprimé avec succès");
                loadComposants();
            } catch (ComposantException e) {
                JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
            }
        }
    }
}


