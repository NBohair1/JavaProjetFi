package presentation;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import dao.MouvementCaisse;
import metier.CaisseMetierImpl;
import metier.ICaisseMetier;

// Panel caisse propriétaire - Mouvements et solde
public class CaissePanel extends JPanel {

    private final MainFrame mainFrame;
    private JTable table;
    private DefaultTableModel model;
    private final ICaisseMetier metier;
    private JLabel lblSolde;

    // @wbp.parser.constructor
    public CaissePanel() {
        this.mainFrame = null;
        this.metier = null;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
    }

    public CaissePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.metier = new CaisseMetierImpl();
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
        loadCaisseData();
    }

    private void initComponents() {
        // Top
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        topPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Gestion de la Caisse");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        lblSolde = new JLabel("Solde: ...");
        lblSolde.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSolde.setForeground(new Color(0, 128, 0));
        
        topPanel.add(lblTitle);
        topPanel.add(Box.createHorizontalStrut(50));
        topPanel.add(lblSolde);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Type", "Montant", "Description", "Date"};
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
        
        JButton btnAlimenter = new JButton("Alimenter");
        JButton btnRetirer = new JButton("Retirer");
        JButton btnRefresh = new JButton("Actualiser");
        
        botPanel.add(btnAlimenter);
        botPanel.add(btnRetirer);
        botPanel.add(btnRefresh);
        add(botPanel, BorderLayout.SOUTH);

        // Actions
        btnRefresh.addActionListener(e -> loadCaisseData());
        btnAlimenter.addActionListener(e -> gererCaisse("ALIMENTATION"));
        btnRetirer.addActionListener(e -> gererCaisse("SORTIE"));
    }

    private void gererCaisse(String type) {
        String input = JOptionPane.showInputDialog(this, "Entrez le montant :");
        if (input != null && !input.isEmpty()) {
            try {
                float montant = Float.parseFloat(input);
                String desc = JOptionPane.showInputDialog(this, "Description :");
                
                if ("ALIMENTATION".equals(type)) {
                    metier.alimenterCaisse(mainFrame.getReparateurActuel(), montant, desc);
                } else {
                    metier.retirerCaisse(mainFrame.getReparateurActuel(), montant, desc);
                }
                
                loadCaisseData();
                JOptionPane.showMessageDialog(this, "Opération réussie");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Montant invalide");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            }
        }
    }

    public void loadCaisseData() {
        model.setRowCount(0);
        if (metier == null || mainFrame == null) return;
        try {
            float solde = metier.consulterSolde(mainFrame.getReparateurActuel());
            lblSolde.setText("Solde: " + solde + " DH");
            
            List<MouvementCaisse> list = metier.listerMouvements(mainFrame.getReparateurActuel());
            for(MouvementCaisse mv : list) {
                model.addRow(new Object[]{
                    mv.getIdMouvement(), mv.getTypeMouvement(), mv.getMontant() + " DH", mv.getDescription(), mv.getDateMouvement()
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}