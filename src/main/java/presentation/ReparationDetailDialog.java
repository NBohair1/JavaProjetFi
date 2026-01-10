package presentation;

import dao.Client;
import dao.Reparation;
import metier.ClientMetierImpl;
import metier.IReparationMetier;

import javax.swing.*;
import java.awt.*;
import java.util.List;

// Dialog détail/création réparation
public class ReparationDetailDialog extends JDialog {

    private final IReparationMetier reparationMetier;
    private final Reparation reparation; // null si création
    private final MainFrame mainFrame;
    
    private JComboBox<Client> comboClient;
    private JTextArea txtDescription;
    private JTextField txtAppareilModele; // Pour simplifier l'ajout
    private JButton btnSave;

    /**
     * @wbp.parser.constructor
     */
    public ReparationDetailDialog(MainFrame owner, IReparationMetier reparationMetier, Reparation reparation) {
        super(owner, reparation == null ? "Nouvelle Réparation" : "Détails Réparation", true);
        this.mainFrame = owner;
        this.reparationMetier = reparationMetier;
        this.reparation = reparation;
        
        initComponents();
        loadData();
        
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(500, 450));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        // 1. Client
        formPanel.add(new JLabel("Client:"));
        comboClient = new JComboBox<>();
        formPanel.add(comboClient);
        
        // 2. Appareil (Simplifié en texte pour ce prototype)
        formPanel.add(new JLabel("Appareil (Modèle):"));
        txtAppareilModele = new JTextField(20);
        formPanel.add(txtAppareilModele);
        
        // 3. Description (JScrollPane pour le TextArea)
        formPanel.add(new JLabel("Description Panne:"));
        txtDescription = new JTextArea(4, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        // Astuce layout: le scrollpane prend la place dans le grid
        formPanel.add(scrollDesc);
        
        // Champs info (Lecture seule si existant)
        if (reparation != null) {
            formPanel.add(new JLabel("État actuel:"));
            JTextField txtEtat = new JTextField(reparation.getEtat());
            txtEtat.setEditable(false);
            formPanel.add(txtEtat);
            
            formPanel.add(new JLabel("Prix Total:"));
            JTextField txtPrix = new JTextField(reparation.getPrixTotal() + " DH");
            txtPrix.setEditable(false);
            formPanel.add(txtPrix);
        } else {
            // Remplissage pour garder la grille alignée
            formPanel.add(new JLabel("")); 
            formPanel.add(new JLabel(""));
            formPanel.add(new JLabel("")); 
            formPanel.add(new JLabel(""));
        }

        add(formPanel, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        btnSave = new JButton("Enregistrer");
        btnSave.setBackground(new Color(51, 153, 255));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> save());
        
        JButton btnClose = new JButton("Annuler");
        btnClose.addActionListener(e -> dispose());
        
        btnPanel.add(btnSave);
        btnPanel.add(btnClose);
        
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        // Charger les clients
        try {
            List<Client> clients = new ClientMetierImpl().listerTousLesClients();
            for (Client c : clients) {
                comboClient.addItem(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Si modification, remplir les champs
        if (reparation != null) {
            comboClient.setSelectedItem(reparation.getClient());
            comboClient.setEnabled(false); // On ne change pas le client d'une réparation en cours
            
            if (reparation.getAppareils() != null && !reparation.getAppareils().isEmpty()) {
                txtAppareilModele.setText(reparation.getAppareils().get(0).getModele());
            }
            // Note: Comme on n'a pas stocké la description dans l'entité Reparation (selon ton modèle JPA), 
            // on la laisse vide ou on utilise un champ commentaire si ajouté.
        }
    }

    private void save() {
        try {
            Client client = (Client) comboClient.getSelectedItem();
            if (client == null) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un client.");
                return;
            }

            if (reparation == null) {
                // Création
                Reparation r = reparationMetier.creerReparation(client, mainFrame.getReparateurActuel());
                // Ici tu pourrais ajouter la logique pour créer l'appareil et lier la description
                // reparationMetier.ajouterAppareil(r, ...);
                JOptionPane.showMessageDialog(this, "Réparation créée avec succès !");
            } else {
                // Modification (si besoin)
            }
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}