package presentation;

import dao.Boutique;
import dao.Proprietaire;
import metier.AuthMetierImpl;
import metier.BoutiqueMetierImpl;
import metier.IBoutiqueMetier;

import javax.swing.*;
import java.awt.*;

// Dialog création/modification boutique
public class BoutiqueDialog extends JDialog {

    private final IBoutiqueMetier boutiqueMetier;
    private Boutique boutique;
    private Proprietaire proprietaire; // Nécessaire pour la création initiale
    private boolean isCreationMode = false;

    private JTextField txtNom;
    private JTextField txtAdresse;
    private JTextField txtTelephone;
    private JTextField txtPatente;

    // @wbp.parser.constructor
    public BoutiqueDialog(Frame owner, IBoutiqueMetier boutiqueMetier, Boutique boutique) {
        super(owner, "Gestion Boutique", true);
        this.boutiqueMetier = (boutiqueMetier != null) ? boutiqueMetier : new BoutiqueMetierImpl();
        this.boutique = boutique;
        this.isCreationMode = (boutique == null);
        
        initComponents();
        if (boutique != null) loadBoutique();
        pack();
        setLocationRelativeTo(owner);
    }
    
    // Constructeur pour CRÉATION INITIALE (Login)
    public BoutiqueDialog(Frame owner, Proprietaire proprietaire, boolean isFirstCreation) {
        super(owner, "Créer votre première Boutique", true);
        this.boutiqueMetier = new BoutiqueMetierImpl();
        this.proprietaire = proprietaire;
        this.isCreationMode = true;
        
        initComponents();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Formulaire
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        formPanel.add(new JLabel("Nom Boutique:"));
        txtNom = new JTextField(20);
        formPanel.add(txtNom);
        
        formPanel.add(new JLabel("Adresse:"));
        txtAdresse = new JTextField(20);
        formPanel.add(txtAdresse);
        
        formPanel.add(new JLabel("Téléphone:"));
        txtTelephone = new JTextField(20);
        formPanel.add(txtTelephone);
        
        formPanel.add(new JLabel("N° Patente:"));
        txtPatente = new JTextField(20);
        formPanel.add(txtPatente);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        JButton btnSave = new JButton(isCreationMode ? "Créer et Démarrer" : "Enregistrer");
        btnSave.setBackground(new Color(51, 153, 255));
        btnSave.setForeground(Color.WHITE);
        
        JButton btnCancel = new JButton("Annuler");
        
        btnSave.addActionListener(e -> save());
        btnCancel.addActionListener(e -> dispose());
        
        btnPanel.add(btnSave);
        if(!isCreationMode) btnPanel.add(btnCancel); // Pas d'annulation si création obligatoire
        
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadBoutique() {
        if (boutique != null) {
            txtNom.setText(boutique.getNom());
            txtAdresse.setText(boutique.getAdresse());
            txtTelephone.setText(boutique.getNumTelephone());
            txtPatente.setText(boutique.getNumP());
        }
    }

    private void save() {
        String nom = txtNom.getText().trim();
        String adresse = txtAdresse.getText().trim();
        
        if (nom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom est obligatoire", "Erreur", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            if (isCreationMode && proprietaire != null) {
                // Création initiale via AuthMetier (pour lier au proprio)
                new AuthMetierImpl().creerBoutique(proprietaire, nom, adresse, txtTelephone.getText(), txtPatente.getText());
                JOptionPane.showMessageDialog(this, "Boutique créée ! Bienvenue.");
            } else if (boutique != null) {
                // Modification
                boutiqueMetier.modifierBoutique(boutique, nom, adresse, txtTelephone.getText(), txtPatente.getText());
                JOptionPane.showMessageDialog(this, "Modifications enregistrées.");
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}