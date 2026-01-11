package presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import dao.Reparation;
import exception.ReparationException;
import metier.IReparationMetier;
import metier.ReparationMetierImpl;

// Cette fenetre permet aux clients de suivre leurs reparations
// Il suffit de saisir le code de suivi pour voir l'etat de la reparation
public class ClientFrame extends JFrame {
    
    private JTextField txtCodeSuivi;
    private JButton btnRechercher;
    private JButton btnRetour;
    private JTextArea txtResultat;
    private final IReparationMetier reparationMetier;
    
    
    // Creation de la fenetre client
    // On prepare tout ce dont on a besoin pour que l'utilisateur puisse rechercher ses reparations
    public ClientFrame() {
        reparationMetier = new ReparationMetierImpl();
        initComponents();
    }
    
    
    // On met en place toute l'interface utilisateur
    // Cela comprend la fenetre, les boutons, les zones de texte, etc.
    private void initComponents() {
        // On configure d'abord la fenetre
        setTitle("Suivi de Reparation - Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setMinimumSize(new Dimension(500, 400));
        setLocationRelativeTo(null); // La fenetre s'ouvre au centre de l'ecran
        
        // On cree le panneau qui va contenir tout le reste
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE); // Un joli fond blanc propre
        
        // En haut, on met le titre et les explications
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        JLabel lblTitle = new JLabel("SUIVI DE REPARATION", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Une belle police lisible
        lblTitle.setForeground(new Color(0, 102, 204));
        topPanel.add(lblTitle, BorderLayout.NORTH);
        
        JLabel lblSubtitle = new JLabel("Entrez votre code de suivi pour consulter l'etat de votre reparation", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        lblSubtitle.setForeground(Color.DARK_GRAY);
        topPanel.add(lblSubtitle, BorderLayout.CENTER);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Ici on met la zone ou l'utilisateur peut taper son code
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Code de Suivi"));
        searchPanel.setBackground(Color.WHITE);
        
        JLabel lblCode = new JLabel("Code :");
        lblCode.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchPanel.add(lblCode);
        
        // La boite ou l'utilisateur tape son code
        txtCodeSuivi = new JTextField(20);
        txtCodeSuivi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtCodeSuivi.setBorder(BorderFactory.createLoweredBevelBorder());
        searchPanel.add(txtCodeSuivi);
        
        // Le bouton pour lancer la recherche
        btnRechercher = new JButton("Rechercher");
        btnRechercher.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRechercher.setBackground(new Color(225, 225, 225));
        btnRechercher.setForeground(Color.BLACK);
        btnRechercher.setBorder(BorderFactory.createRaisedBevelBorder());
        searchPanel.add(btnRechercher);
        
        // On combine la zone de recherche avec celle des resultats
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        
        // La zone ou s'afficheront les informations de la reparation
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Resultat de la Recherche"));
        resultPanel.setBackground(Color.WHITE);
        
        // La grande zone de texte pour afficher tous les details
        txtResultat = new JTextArea(10, 30);
        txtResultat.setEditable(false);
        txtResultat.setFont(new Font("Consolas", Font.PLAIN, 11)); // Police facile a lire
        txtResultat.setBackground(Color.WHITE);
        txtResultat.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JScrollPane scrollPane = new JScrollPane(txtResultat);
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        centerPanel.add(resultPanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // En bas, on met le bouton pour revenir a l'accueil
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(Color.WHITE);
        btnRetour = new JButton("Retour a l'accueil");
        btnRetour.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRetour.setBackground(new Color(225, 225, 225));
        btnRetour.setForeground(Color.BLACK);
        btnRetour.setBorder(BorderFactory.createRaisedBevelBorder());
        btnPanel.add(btnRetour);
        
        // On assemble les resultats et les boutons ensemble
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(resultPanel, BorderLayout.CENTER);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        getContentPane().add(mainPanel);
        
        // On dit aux boutons ce qu'ils doivent faire quand on clique dessus
        btnRechercher.addActionListener(e -> rechercherReparation());
        txtCodeSuivi.addActionListener(e -> rechercherReparation()); // Quand on appuie sur Entree aussi
        btnRetour.addActionListener(e -> retourLogin());
    }
    
    
    // Cette methode s'occupe de chercher une reparation
    // L'utilisateur a tape un code, on va voir si on le trouve dans nos donnees
    private void rechercherReparation() {
        String codeSuivi = txtCodeSuivi.getText().trim();
        System.out.println("L'utilisateur cherche le code : " + codeSuivi);
        
        // On verifie que l'utilisateur a bien tape quelque chose
        if (codeSuivi.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Veuillez entrer un code de suivi",
                "Erreur - Saisie requise",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // On demande a la base de donnees si elle connait ce code
            Reparation reparation = reparationMetier.rechercherParCodeSuivi(codeSuivi);
            
            if (reparation != null) {
                System.out.println("Super ! J'ai trouve la reparation " + reparation.getCodeSuivi() + " qui est " + reparation.getEtat());
                afficherDetailsReparation(reparation);
                System.out.println("J'ai fini de preparer l'affichage pour l'utilisateur");
            } else {
                System.out.println("Dommage, je n'ai rien trouve avec ce code");
                txtResultat.setText("Aucune reparation trouvee pour ce code de suivi.");
            }
        } catch (ReparationException e) {
            System.out.println("Oups, quelque chose s'est mal passe : " + e.getMessage());
            txtResultat.setText("");
            JOptionPane.showMessageDialog(this,
                "Reparation non trouvee pour ce code de suivi.\n" +
                "Veuillez verifier le code et reessayer.",
                "Aucun resultat",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    
    // Cette methode prend une reparation et affiche tous ses details
    // C'est ici qu'on prepare le joli texte que verra l'utilisateur
    // @param rep La reparation dont on veut montrer les details
    private void afficherDetailsReparation(Reparation rep) {
        System.out.println("On va afficher les details de la reparation : " + (rep != null ? rep.getCodeSuivi() : "aucune"));
        
        // On s'assure qu'on a bien une reparation a afficher
        if (rep == null) {
            txtResultat.setText("Aucune reparation trouvee.");
            System.out.println("Pas de reparation a afficher, on met un message d'erreur");
            return;
        }
        
        // Maintenant on va construire le beau texte a montrer au client
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        sb.append("----------------------------------------------------------\n");
        sb.append("              DETAILS DE LA REPARATION\n");
        sb.append("----------------------------------------------------------\n\n");
        
        sb.append("Code de suivi:        ").append(rep.getCodeSuivi()).append("\n\n");
        
        sb.append("Etat actuel:          ").append(rep.getEtat()).append("\n\n");
        
        sb.append("Date de depot:        ").append(sdf.format(rep.getDateDepot())).append("\n");
        
        // Si on a une date de livraison, on l'affiche aussi
        if (rep.getDateLivraison() != null) {
            sb.append("Date de livraison:    ").append(sdf.format(rep.getDateLivraison())).append("\n");
        }
        
        sb.append("\n");
        sb.append("Prix total:           ").append(String.format("%.2f DH", rep.getPrixTotal())).append("\n\n");
        
        // S'il y a un petit mot du reparateur, on le met aussi
        if (rep.getCommentaire() != null && !rep.getCommentaire().isEmpty()) {
            sb.append("Commentaire:\n");
            sb.append("  ").append(rep.getCommentaire()).append("\n\n");
        }
        
        // S'il y a des appareils, on va les lister gentiment
        if (rep.getAppareils() != null && !rep.getAppareils().isEmpty()) {
            sb.append("----------------------------------------------------------\n");
            sb.append("APPAREILS EN REPARATION:\n");
            sb.append("----------------------------------------------------------\n");
            for (dao.Appareil app : rep.getAppareils()) {
                sb.append("  - ").append(app.getTypeAppareil()).append(" - ").append(app.getMarque());
                if (app.getModele() != null) {
                    sb.append(" (").append(app.getModele()).append(")");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }
        
        // Ici on traduit l'etat technique en quelque chose de comprehensible pour le client
        sb.append("----------------------------------------------------------\n");
        if ("EN_ATTENTE".equals(rep.getEtat())) {
            sb.append("Votre reparation est en attente de prise en charge.\n");
        } else if ("EN_COURS".equals(rep.getEtat())) {
            sb.append("Votre reparation est en cours.\n");
        } else if ("TERMINEE".equals(rep.getEtat())) {
            sb.append("Votre reparation est terminee.\n");
            sb.append("Vous pouvez venir recuperer votre appareil.\n");
        } else if ("LIVREE".equals(rep.getEtat())) {
            sb.append("Votre reparation a ete livree.\n");
        } else if ("ANNULEE".equals(rep.getEtat())) {
            sb.append("Cette reparation a ete annulee.\n");
        }
        sb.append("----------------------------------------------------------\n");
        
        // Allez hop ! On colle tout ca dans la zone de texte
        txtResultat.setText(sb.toString());
        txtResultat.setCaretPosition(0); // Et on remet tout en haut pour que ce soit joli
        System.out.println("Voila ! J'ai prepare un beau texte de " + sb.length() + " caracteres pour le client");
        
        // On jette un petit coup d'oeil pour voir si tout va bien
        if (txtResultat.isVisible() && txtResultat.getText().length() > 0) {
            System.out.println("Nickel ! L'utilisateur va voir ses " + txtResultat.getText().length() + " caracteres d'infos");
        } else {
            System.out.println("Aie aie aie, il y a un probleme, l'utilisateur ne va rien voir du tout");
        }
        
        // Bon, on secoue un peu l'interface pour que tout s'affiche comme il faut
        txtResultat.invalidate();
        if (txtResultat.getParent() != null) {
            txtResultat.getParent().revalidate();
            txtResultat.getParent().repaint();
        }
        this.revalidate();
        this.repaint();
        
        System.out.println("Parfait ! Normalement l'utilisateur devrait voir toutes ses infos maintenant");
    }
    
    
    // Quand l'utilisateur veut revenir a la page de connexion
    // On ferme cette fenetre et on ouvre celle du login
    private void retourLogin() {
        new LoginFrame().setVisible(true);
        this.dispose();
    }
}
