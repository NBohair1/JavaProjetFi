package presentation;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import dao.Reparation;
import metier.IReparationMetier;
import metier.ReparationMetierImpl;

// Panel réparations propriétaire - Liste et détails
public class ReparationPanel extends JPanel {

    private MainFrame mainFrame;
    private JTable table;
    private DefaultTableModel model;
    private IReparationMetier metier;
    private JTextField txtSearch;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * @wbp.parser.constructor
     */
    public ReparationPanel() {
        this.mainFrame = null;
        this.metier = null;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
    }

    public ReparationPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.metier = new ReparationMetierImpl();
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
        loadReparations();
    }

    private void initComponents() {
        // --- 1. Barre du haut ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        topPanel.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("Recherche :");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        txtSearch = new JTextField(20);
        
        JButton btnSearch = new JButton("Rechercher");
        JButton btnRefresh = new JButton("Actualiser");

        topPanel.add(lblSearch);
        topPanel.add(txtSearch);
        topPanel.add(btnSearch);
        topPanel.add(btnRefresh);
        
        add(topPanel, BorderLayout.NORTH);

        // --- 2. Tableau ---
        String[] cols = {"ID", "Code Suivi", "Client", "Date Dépôt", "État", "Prix", "Appareil"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        
        table = new JTable(model);
        table.setRowHeight(30);
        table.setShowGrid(false);
        
        // Couleur selon état
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col);
                String etat = (String) value;
                if(!isSel) {
                    if ("TERMINEE".equals(etat)) setForeground(new Color(0, 128, 0));
                    else if ("EN_COURS".equals(etat)) setForeground(new Color(255, 140, 0));
                    else setForeground(Color.BLACK);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. Barre du bas ---
        JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botPanel.setBackground(Color.WHITE);
        
        JButton btnAdd = new JButton("Nouvelle Réparation");
        JButton btnEdit = new JButton("Modifier");
        
        botPanel.add(btnAdd);
        botPanel.add(btnEdit);
        
        add(botPanel, BorderLayout.SOUTH);

        // --- Listeners ---
        btnRefresh.addActionListener(e -> loadReparations());
        btnAdd.addActionListener(e -> {
            ReparationDetailDialog dialog = new ReparationDetailDialog(mainFrame, metier, null);
            dialog.setVisible(true);
            loadReparations();
        });
        
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row != -1) {
                String code = (String) model.getValueAt(row, 1);
                try {
                    Reparation r = metier.rechercherParCodeSuivi(code);
                    ReparationDetailDialog dialog = new ReparationDetailDialog(mainFrame, metier, r);
                    dialog.setVisible(true);
                    loadReparations();
                } catch(Exception ex) {}
            }
        });
    }

    public void loadReparations() {
        model.setRowCount(0);
        if (metier == null || mainFrame == null) return; // For WindowBuilder preview
        try {
            // Le propriétaire voit TOUTES les réparations
            List<Reparation> list = metier.listerToutesLesReparations();
            for(Reparation r : list) {
                String client = r.getClient() != null ? r.getClient().getNom() : "?";
                String app = (r.getAppareils() != null && !r.getAppareils().isEmpty()) 
                           ? r.getAppareils().get(0).getModele() : "";
                
                model.addRow(new Object[]{
                    r.getIdReparation(),
                    r.getCodeSuivi(),
                    client,
                    r.getDateDepot() != null ? sdf.format(r.getDateDepot()) : "",
                    r.getEtat(),
                    r.getPrixTotal() + " DH",
                    app
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    JFrame frame = new JFrame("Reparation Panel Test");
                    ReparationPanel panel = new ReparationPanel(); // Use no-arg constructor
                    frame.setContentPane(panel);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setSize(800, 600);
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}