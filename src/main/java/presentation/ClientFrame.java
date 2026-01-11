package presentation;

import java.awt.BorderLayout;
import java.awt.Color;
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

// Fenêtre client - Suivi réparation par code
public class ClientFrame extends JFrame {
    
    private JTextField txtCodeSuivi;
    private JButton btnRechercher;
    private JButton btnRetour;
    private JTextArea txtResultat;
    private final IReparationMetier reparationMetier;
    
    public ClientFrame() {
        reparationMetier = new ReparationMetierImpl();
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Suivi de Réparation - Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel supérieur - Titre
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("SUIVI DE RÉPARATION", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 24));
        lblTitle.setForeground(new Color(0, 102, 204));
        topPanel.add(lblTitle, BorderLayout.NORTH);
        
        JLabel lblSubtitle = new JLabel("Entrez votre code de suivi pour consulter l'état de votre réparation", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Tahoma", Font.PLAIN, 12));
        lblSubtitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        topPanel.add(lblSubtitle, BorderLayout.CENTER);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Panel central - Recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Code de Suivi"));
        
        JLabel lblCode = new JLabel("Code:");
        lblCode.setFont(new Font("Tahoma", Font.BOLD, 12));
        searchPanel.add(lblCode);
        
        txtCodeSuivi = new JTextField(20);
        txtCodeSuivi.setFont(new Font("Tahoma", Font.PLAIN, 12));
        searchPanel.add(txtCodeSuivi);
        
        btnRechercher = new JButton("Rechercher");
        btnRechercher.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnRechercher.setBackground(new Color(0, 153, 51));
        btnRechercher.setForeground(Color.WHITE);
        searchPanel.add(btnRechercher);
        
        mainPanel.add(searchPanel, BorderLayout.CENTER);
        
        // Panel résultat
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Résultat de la Recherche"));
        
        txtResultat = new JTextArea(15, 40);
        txtResultat.setEditable(false);
        txtResultat.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtResultat.setBackground(new Color(245, 245, 245));
        JScrollPane scrollPane = new JScrollPane(txtResultat);
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(resultPanel, BorderLayout.SOUTH);
        
        // Panel boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRetour = new JButton("Retour à l'accueil");
        btnRetour.setFont(new Font("Tahoma", Font.PLAIN, 12));
        btnPanel.add(btnRetour);
        
        mainPanel.add(btnPanel, BorderLayout.PAGE_END);
        
        getContentPane().add(mainPanel);
        
        // Actions
        btnRechercher.addActionListener(e -> rechercherReparation());
        txtCodeSuivi.addActionListener(e -> rechercherReparation());
        btnRetour.addActionListener(e -> retourLogin());
    }
    
    private void rechercherReparation() {
        String codeSuivi = txtCodeSuivi.getText().trim();
        
        if (codeSuivi.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Veuillez entrer un code de suivi",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Reparation reparation = reparationMetier.rechercherParCodeSuivi(codeSuivi);
            afficherDetailsReparation(reparation);
        } catch (ReparationException e) {
            txtResultat.setText("");
            JOptionPane.showMessageDialog(this,
                "Réparation non trouvée pour ce code de suivi.\n" +
                "Veuillez vérifier le code et réessayer.",
                "Réparation non trouvée",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void afficherDetailsReparation(Reparation rep) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append("              DÉTAILS DE LA RÉPARATION\n");
        sb.append("═══════════════════════════════════════════════════════════\n\n");
        
        sb.append("Code de suivi:        ").append(rep.getCodeSuivi()).append("\n\n");
        
        // État avec indicateur visuel
        sb.append("État actuel:          ");
        switch (rep.getEtat()) {
            case "EN_ATTENTE":
                sb.append(" EN ATTENTE");
                break;
            case "EN_COURS":
                sb.append(" EN COURS");
                break;
            case "TERMINEE":
                sb.append(" TERMINÉE");
                break;
            case "LIVREE":
                sb.append(" LIVRÉE");
                break;
            case "ANNULEE":
                sb.append(" ANNULÉE");
                break;
            default:
                sb.append(rep.getEtat());
        }
        sb.append("\n\n");
        
        sb.append("Date de dépôt:        ").append(sdf.format(rep.getDateDepot())).append("\n");
        
        if (rep.getDateLivraison() != null) {
            sb.append("Date de livraison:    ").append(sdf.format(rep.getDateLivraison())).append("\n");
        }
        
        sb.append("\n");
        sb.append("Prix total:           ").append(String.format("%.2f DH", rep.getPrixTotal())).append("\n\n");
        
        if (rep.getCommentaire() != null && !rep.getCommentaire().isEmpty()) {
            sb.append("Commentaire:\n");
            sb.append("  ").append(rep.getCommentaire()).append("\n\n");
        }
        
        // Informations appareils
        if (rep.getAppareils() != null && !rep.getAppareils().isEmpty()) {
            sb.append("───────────────────────────────────────────────────────────\n");
            sb.append("APPAREILS EN RÉPARATION:\n");
            sb.append("───────────────────────────────────────────────────────────\n");
            rep.getAppareils().forEach(app -> {
                sb.append("  • ").append(app.getTypeAppareil()).append(" - ").append(app.getMarque());
                if (app.getModele() != null) {
                    sb.append(" (").append(app.getModele()).append(")");
                }
                sb.append("\n");

            });
            sb.append("\n");
        }
        
        // Message selon l'état
        sb.append("═══════════════════════════════════════════════════════════\n");
        switch (rep.getEtat()) {
            case "EN_ATTENTE":
                sb.append("Votre réparation est en attente de prise en charge.\n");
                sb.append("Vous serez contacté prochainement.\n");
                break;
            case "EN_COURS":
                sb.append("Votre réparation est actuellement en cours.\n");
                sb.append("Notre équipe travaille activement dessus.\n");
                break;
            case "TERMINEE":
                sb.append("Votre réparation est terminée !\n");
                sb.append("Vous pouvez venir récupérer votre appareil.\n");
                break;
            case "LIVREE":
                sb.append("Votre réparation a été livrée.\n");
                sb.append("Merci de votre confiance !\n");
                break;
            case "ANNULEE":
                sb.append("Cette réparation a été annulée.\n");
                sb.append("Contactez-nous pour plus d'informations.\n");
                break;
        }
        sb.append("═══════════════════════════════════════════════════════════\n");
        
        txtResultat.setText(sb.toString());
        txtResultat.setCaretPosition(0);
    }
    
    private void retourLogin() {
        new LoginFrame().setVisible(true);
        this.dispose();
    }
}
