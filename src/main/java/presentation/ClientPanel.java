package presentation;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import dao.Client;
import metier.ClientMetierImpl;
import metier.IClientMetier;

// Panel clients - CRUD et gestion fidélité
public class ClientPanel extends JPanel {

    private final MainFrame mainFrame;
    private JTable table;
    private DefaultTableModel model;
    private final IClientMetier metier;

    /**
     * Constructeur par défaut (WindowBuilder)
     * @wbp.parser.constructor
     */
    public ClientPanel() {
        this.mainFrame = null;
        this.metier = null;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
    }

    public ClientPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.metier = new ClientMetierImpl();
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
        loadClients();
    }

    private void initComponents() {
        // Top
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        topPanel.setBackground(Color.WHITE);
        JLabel lblTitle = new JLabel("Gestion des Clients");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(lblTitle);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Nom", "Prénom", "Téléphone", "Adresse", "Fidèle"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom
        JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botPanel.setBackground(Color.WHITE);
        
        JButton btnAdd = new JButton("Nouveau Client");
        JButton btnRefresh = new JButton("Actualiser");
        
        botPanel.add(btnAdd);
        botPanel.add(btnRefresh);
        add(botPanel, BorderLayout.SOUTH);

        // Actions
        btnRefresh.addActionListener(e -> loadClients());
        btnAdd.addActionListener(e -> ajouterClient());
    }

    private void ajouterClient() {
        // Formulaire simple via JPanel
        JTextField txtNom = new JTextField();
        JTextField txtPrenom = new JTextField();
        JTextField txtTel = new JTextField();
        JTextField txtAdr = new JTextField();
        
        Object[] message = {
            "Nom:", txtNom,
            "Prénom:", txtPrenom,
            "Téléphone:", txtTel,
            "Adresse:", txtAdr
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Nouveau Client", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                metier.creerClient(
                    txtNom.getText(), 
                    txtPrenom.getText(), 
                    txtTel.getText(), 
                    txtAdr.getText(), 
                    null // Image null pour l'instant
                );
                loadClients();
                JOptionPane.showMessageDialog(this, "Client ajouté !");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            }
        }
    }

    public void loadClients() {
        model.setRowCount(0);
        if (metier == null) return;
        try {
            List<Client> list = metier.listerTousLesClients();
            for(Client c : list) {
                model.addRow(new Object[]{
                    c.getIdClient(), c.getNom(), c.getPrenom(), c.getTelephone(), c.getAdresse(), c.isFidele() ? "Oui" : "Non"
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}