package presentation;

import java.awt.*;
import javax.swing.*;

import dao.Boutique;
import dao.Proprietaire;
import dao.Reparateur;
import dao.User;
import exception.AuthException;
import metier.AuthMetierImpl;
import metier.IAuthMetier;

// Fenêtre de connexion - Point d'entrée de l'application
// Redirige vers MainFrame (propriétaire), ReparateurFrame (employé) ou ClientFrame
public class LoginFrame extends JFrame {

    // Champs de saisie
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnClient;
    
    // Service métier pour l'authentification
    private final IAuthMetier authMetier;

    public LoginFrame() {
        authMetier = new AuthMetierImpl();
        initComponents();
    }

    // Initialisation des composants graphiques
    private void initComponents() {
        setTitle("Connexion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        
        // Titre
        JLabel lblTitle = new JLabel("Connexion", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 20));
        lblTitle.setBackground(new Color(0, 0, 0));
        lblTitle.setBounds(111, 35, 157, 35);
        mainPanel.add(lblTitle);
        
        // Email
        JLabel label = new JLabel("Email:");
        label.setBounds(81, 97, 31, 14);
        mainPanel.add(label);
        txtEmail = new JTextField(20);
        txtEmail.setBounds(122, 95, 221, 20);
        mainPanel.add(txtEmail);
        
        // Mot de passe
        JLabel label_1 = new JLabel("Mot de passe:");
        label_1.setBounds(42, 127, 70, 14);
        mainPanel.add(label_1);
        txtPassword = new JPasswordField(20);
        txtPassword.setBounds(122, 124, 221, 20);
        mainPanel.add(txtPassword);
        
        // Bouton connexion
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setBounds(139, 159, 107, 33);
        btnLogin = new JButton("Se connecter");
        btnPanel.add(btnLogin);
        mainPanel.add(btnPanel);
        
        // Séparateur
        JSeparator separator = new JSeparator();
        separator.setBounds(50, 205, 300, 2);
        mainPanel.add(separator);
        
        // Bouton espace client
        btnClient = new JButton("Je suis un client");
        btnClient.setBounds(100, 220, 200, 35);
        btnClient.setFont(new Font("Tahoma", Font.PLAIN, 12));
        btnClient.setBackground(new Color(0, 153, 204));
        btnClient.setForeground(Color.WHITE);
        mainPanel.add(btnClient);

        getContentPane().add(mainPanel);

        // Écouteurs d'événements
        btnLogin.addActionListener(e -> login());
        txtPassword.addActionListener(e -> login());
        btnClient.addActionListener(e -> ouvrirInterfaceClient());
    }

    // Authentification et redirection selon le type d'utilisateur
    private void login() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (email.isEmpty() || password.isEmpty()) return;

        try {
            User user = authMetier.login(email, password);
            
            if (user instanceof Proprietaire) {
                // Propriétaire → MainFrame (admin)
                Proprietaire p = (Proprietaire) user;
                
                // Première connexion: créer une boutique
                if (p.getBoutiques() == null || p.getBoutiques().isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Vous devez créer au moins une boutique pour continuer.", 
                        "Première connexion", 
                        JOptionPane.INFORMATION_MESSAGE);
                    BoutiqueDialog d = new BoutiqueDialog(this, p, true);
                    d.setVisible(true);
                    
                    // Recharger le propriétaire pour obtenir la boutique créée
                    try {
                        user = authMetier.login(email, password);
                        p = (Proprietaire) user;
                        if (p.getBoutiques() == null || p.getBoutiques().isEmpty()) {
                            return; // Boutique non créée, rester sur login
                        }
                    } catch (AuthException ex) {
                        return;
                    }
                }
                
                // Sélection de boutique si plusieurs
                Boutique boutiqueChoisie = choisirBoutique(p);
                if (boutiqueChoisie == null) return;
                
                new MainFrame(p, boutiqueChoisie).setVisible(true);
                this.dispose();
                
            } else if (user instanceof Reparateur) {
                // Réparateur → ReparateurFrame (employé)
                Reparateur r = (Reparateur) user;
                new ReparateurFrame(r).setVisible(true);
                this.dispose();
                
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Type d'utilisateur non reconnu", 
                    "Erreur", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (AuthException e) {
            JOptionPane.showMessageDialog(this, 
                "Erreur : " + e.getMessage(), 
                "Erreur d'authentification", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Ouvrir l'interface de suivi client
    private void ouvrirInterfaceClient() {
        new ClientFrame().setVisible(true);
        this.dispose();
    }
    
    // Dialogue de sélection de boutique
    private Boutique choisirBoutique(Proprietaire p) {
        // Une seule boutique: sélection automatique
        if (p.getBoutiques().size() == 1) {
            return p.getBoutiques().get(0);
        }
        
        // Créer un dialogue personnalisé
        JDialog dialog = new JDialog(this, "Sélection de boutique", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        // Liste des boutiques
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Boutique b : p.getBoutiques()) {
            listModel.addElement(b.getNom());
        }
        JList<String> listBoutiques = new JList<>(listModel);
        listBoutiques.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listBoutiques.setSelectedIndex(0);
        
        JScrollPane scrollPane = new JScrollPane(listBoutiques);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Mes boutiques"));
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        // Panneau de boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnEntrer = new JButton("Entrer dans cette boutique");
        JButton btnNouvelle = new JButton("Créer nouvelle boutique");
        JButton btnAnnuler = new JButton("Annuler");
        
        btnEntrer.setBackground(new Color(51, 153, 255));
        btnEntrer.setForeground(Color.WHITE);
        btnNouvelle.setBackground(new Color(46, 204, 113));
        btnNouvelle.setForeground(Color.WHITE);
        
        final Boutique[] boutiqueSelectionnee = {null};
        
        btnEntrer.addActionListener(e -> {
            int index = listBoutiques.getSelectedIndex();
            if (index >= 0) {
                boutiqueSelectionnee[0] = p.getBoutiques().get(index);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Veuillez sélectionner une boutique");
            }
        });
        
        btnNouvelle.addActionListener(e -> {
            // Créer une nouvelle boutique
            BoutiqueDialog boutiqueDialog = new BoutiqueDialog(this, p, true);
            boutiqueDialog.setVisible(true);
            
            // Recharger les boutiques
            try {
                String email = txtEmail.getText().trim();
                String password = new String(txtPassword.getPassword());
                User user = authMetier.login(email, password);
                Proprietaire pRecharge = (Proprietaire) user;
                
                // Mettre à jour la liste
                listModel.clear();
                for (Boutique b : pRecharge.getBoutiques()) {
                    listModel.addElement(b.getNom());
                }
                
                JOptionPane.showMessageDialog(dialog, 
                    "Boutique créée ! Vous pouvez maintenant la sélectionner.", 
                    "Succès", 
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Erreur rechargement: " + ex.getMessage(), 
                    "Erreur", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        btnPanel.add(btnEntrer);
        btnPanel.add(btnNouvelle);
        btnPanel.add(btnAnnuler);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
        return boutiqueSelectionnee[0];
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    LoginFrame frame = new LoginFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}