package presentation;

import java.awt.BorderLayout;
// Panel réparations réparateur - CRUD et changement d'état
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import dao.Appareil;
import dao.Client;
import dao.Reparation;
import exception.ReparationException;

public class ReparationPanelReparateur extends JPanel {

    private Object parentFrame; // Peut être ReparateurFrame ou MainFrame
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Constructeur par défaut (WindowBuilder)
     * @wbp.parser.constructor
     */
    public ReparationPanelReparateur() {
        this.parentFrame = null;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
    }

    public ReparationPanelReparateur(ReparateurFrame reparateurFrame) {
        this.parentFrame = reparateurFrame;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
        loadReparations();
    }
    
    public ReparationPanelReparateur(MainFrame mainFrame) {
        this.parentFrame = mainFrame;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
        loadReparations();
    }
    
    // Méthodes helper pour accéder aux métiers de manière générique
    private dao.Reparateur getReparateur() {
        if (parentFrame instanceof ReparateurFrame) {
            return ((ReparateurFrame) parentFrame).getReparateur();
        } else if (parentFrame instanceof MainFrame) {
            return ((MainFrame) parentFrame).getReparateur();
        }
        return null;
    }
    
    private metier.IReparationMetier getReparationMetier() {
        if (parentFrame instanceof ReparateurFrame) {
            return ((ReparateurFrame) parentFrame).getReparationMetier();
        } else if (parentFrame instanceof MainFrame) {
            return ((MainFrame) parentFrame).getReparationMetier();
        }
        return null;
    }
    
    private metier.IClientMetier getClientMetier() {
        if (parentFrame instanceof ReparateurFrame) {
            return ((ReparateurFrame) parentFrame).getClientMetier();
        } else if (parentFrame instanceof MainFrame) {
            return ((MainFrame) parentFrame).getClientMetier();
        }
        return null;
    }
    
    private metier.IComposantMetier getComposantMetier() {
        if (parentFrame instanceof ReparateurFrame) {
            return ((ReparateurFrame) parentFrame).getComposantMetier();
        } else if (parentFrame instanceof MainFrame) {
            return ((MainFrame) parentFrame).getComposantMetier();
        }
        return null;
    }

    private void initComponents() {
        // Barre du haut
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        topPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Mes Réparations");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(lblTitle);

        JButton btnNew = new JButton("Nouvelle réparation");
        btnNew.addActionListener(e -> nouvelleReparation());
        topPanel.add(btnNew);
        
        JButton btnModifier = new JButton("Modifier");
        btnModifier.addActionListener(e -> modifierReparation());
        topPanel.add(btnModifier);
        
        JButton btnChangeEtat = new JButton("Changer l'état");
        btnChangeEtat.addActionListener(e -> changerEtatReparation());
        topPanel.add(btnChangeEtat);
        
        JButton btnSupprimer = new JButton("Supprimer");
        btnSupprimer.setBackground(new Color(231, 76, 60));
        btnSupprimer.setForeground(Color.WHITE);
        btnSupprimer.addActionListener(e -> supprimerReparation());
        topPanel.add(btnSupprimer);
        
        JButton btnPDF = new JButton("Générer PDF Reçu");
        btnPDF.addActionListener(e -> genererPDFRecu());
        topPanel.add(btnPDF);

        JButton btnRefresh = new JButton("Actualiser");
        btnRefresh.addActionListener(e -> loadReparations());
        topPanel.add(btnRefresh);

        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"Code", "Client", "Appareil", "État", "Prix", "Dépôt", "Livraison"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        
        // Coloration des lignes selon l'état
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    String etat = (String) model.getValueAt(row, 3);
                    if (etat != null) {
                        if (etat.equals("EN_COURS")) c.setBackground(new Color(255, 255, 200));
                        else if (etat.equals("TERMINEE")) c.setBackground(new Color(200, 255, 200));
                        else if (etat.equals("EN_ATTENTE")) c.setBackground(new Color(255, 220, 200));
                        else c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
        
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) afficherDetails(row);
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void loadReparations() {
        model.setRowCount(0);
        if (getReparateur() == null) return;
        
        try {
            // Pour les onglets "MES" (personnels), on affiche TOUTES les données du propriétaire
            // sans filtrage par boutique, car c'est un onglet personnel
            List<Reparation> list = getReparationMetier().listerReparationsParReparateur(getReparateur());
            
            for (Reparation r : list) {
                String client;
                if (r.getClient() != null) {
                    client = r.getClient().getNom() + " " + r.getClient().getPrenom();
                } else {
                    client = "N/A";
                }
                String appareil = "";
                if (r.getAppareils() != null && !r.getAppareils().isEmpty()) {
                    appareil = r.getAppareils().get(0).getMarque() + " " + r.getAppareils().get(0).getModele();
                }
                
                // Déterminer l'affichage de la colonne livraison
                String livraison = "";
                if ("LIVREE".equals(r.getEtat())) {
                    if (r.getDateLivraison() != null) {
                        livraison = sdf.format(r.getDateLivraison());
                    } else {
                        livraison = "LIVREE";
                    }
                } else {
                    livraison = "-"; // Pas encore livrée
                }
                
                String dateDepot;
                if (r.getDateDepot() != null) {
                    dateDepot = sdf.format(r.getDateDepot());
                } else {
                    dateDepot = "";
                }
                
                model.addRow(new Object[]{
                    r.getCodeSuivi(),
                    client,
                    appareil,
                    r.getEtat(),
                    r.getPrixTotal() + " DH",
                    dateDepot,
                    livraison
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void nouvelleReparation() {
        try {
            // Étape 1: Choisir le mode de sélection du client
            String[] options = {"Sélectionner un client existant", "Rechercher par téléphone", "Créer un nouveau client"};
            int choix = JOptionPane.showOptionDialog(this,
                "Comment voulez-vous sélectionner le client?",
                "Nouvelle Réparation - Étape 1/5: Client",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
            
            if (choix < 0) return; // Annulé
            
            Client client = null;
            
            if (choix == 0) {
                // Sélectionner depuis la liste
                List<Client> clients = getClientMetier().listerTousLesClients();
                if (clients.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Aucun client dans la base. Veuillez en créer un.");
                    return;
                }
                
                String[] clientNames = new String[clients.size()];
                for (int i = 0; i < clients.size(); i++) {
                    Client c = clients.get(i);
                    clientNames[i] = c.getNom() + " " + c.getPrenom() + " - " + c.getTelephone();
                }
                
                String selected = (String) JOptionPane.showInputDialog(this,
                    "Sélectionnez le client:",
                    "Choix du client",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    clientNames,
                    clientNames[0]);
                
                if (selected == null) return;
                
                int index = -1;
                for (int i = 0; i < clientNames.length; i++) {
                    if (clientNames[i].equals(selected)) {
                        index = i;
                        break;
                    }
                }
                client = clients.get(index);
                
            } else if (choix == 1) {
                // Rechercher par téléphone
                String telephone = JOptionPane.showInputDialog(this, 
                    "Entrez le téléphone du client:", 
                    "Recherche client", 
                    JOptionPane.QUESTION_MESSAGE);
                
                if (telephone == null || telephone.trim().isEmpty()) return;
                
                try {
                    client = getClientMetier().rechercherClientParTelephone(telephone.trim());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Client introuvable avec ce numéro", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
            } else {
                // Créer un nouveau client
                JTextField txtNom = new JTextField();
                JTextField txtPrenom = new JTextField();
                JTextField txtTelephone = new JTextField();
                JTextField txtAdresse = new JTextField();
                
                Object[] message = {
                    "Nom:", txtNom,
                    "Prénom:", txtPrenom,
                    "Téléphone:", txtTelephone,
                    "Adresse:", txtAdresse
                };
                
                int option = JOptionPane.showConfirmDialog(this, message, "Nouveau Client", JOptionPane.OK_CANCEL_OPTION);
                if (option != JOptionPane.OK_OPTION) return;
                
                client = getClientMetier().creerClient(
                    txtNom.getText().trim(),
                    txtPrenom.getText().trim(),
                    txtTelephone.getText().trim(),
                    txtAdresse.getText().trim(),
                    null
                );
                JOptionPane.showMessageDialog(this, "Client créé: " + client.getNom() + " " + client.getPrenom());
            }
            
            if (client == null) {
                JOptionPane.showMessageDialog(this, "Aucun client sélectionné", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Confirmation du client choisi
            int confirm = JOptionPane.showConfirmDialog(this,
                "Client sélectionné:\n" + 
                client.getNom() + " " + client.getPrenom() + "\n" +
                "Téléphone: " + client.getTelephone() + "\n\n" +
                "Continuer avec ce client?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm != JOptionPane.YES_OPTION) return;
            
            // Étape 2: Informations de l'appareil
            JTextField txtMarque = new JTextField();
            JTextField txtModele = new JTextField();
            JTextField txtIMEI = new JTextField();
            JComboBox<String> cbType = new JComboBox<>(new String[]{"Smartphone", "Tablette", "Ordinateur", "Montre", "Autre"});
            
            Object[] appareilMsg = {
                "Marque:", txtMarque,
                "Modèle:", txtModele,
                "IMEI/Série:", txtIMEI,
                "Type:", cbType
            };
            
            int optAppareil = JOptionPane.showConfirmDialog(this, appareilMsg, 
                "Nouvelle Réparation - Étape 2/5: Appareil", 
                JOptionPane.OK_CANCEL_OPTION);
            
            if (optAppareil != JOptionPane.OK_OPTION) return;
            
            // Étape 3: Problème et commentaire
            JTextArea txtCommentaire = new JTextArea(4, 20);
            JScrollPane scrollComment = new JScrollPane(txtCommentaire);
            
            Object[] commentMsg = {
                "Décrivez le problème / panne:", scrollComment
            };
            
            int optComment = JOptionPane.showConfirmDialog(this, commentMsg,
                "Nouvelle Réparation - Étape 3/5: Problème",
                JOptionPane.OK_CANCEL_OPTION);
            
            if (optComment != JOptionPane.OK_OPTION) return;
            
            // Étape 4: Sélection des composants
            List<dao.Composant> composantsDisponibles = getComposantMetier().listerComposants();
            JPanel composantPanel = new JPanel(new GridLayout(0, 1));
            JCheckBox[] checkBoxes = new JCheckBox[composantsDisponibles.size()];
            
            for (int i = 0; i < composantsDisponibles.size(); i++) {
                dao.Composant c = composantsDisponibles.get(i);
                checkBoxes[i] = new JCheckBox(c.getNom() + " - " + c.getPrix() + " DH (Stock: " + c.getQuantite() + ")");
                composantPanel.add(checkBoxes[i]);
            }
            
            JScrollPane scrollComp = new JScrollPane(composantPanel);
            scrollComp.setPreferredSize(new Dimension(400, 200));
            
            JTextArea txtDemandeComposant = new JTextArea(3, 30);
            txtDemandeComposant.setBorder(BorderFactory.createTitledBorder("Demande de composant non disponible:"));
            
            Object[] composantMsg = {
                "Sélectionnez les composants nécessaires:", 
                scrollComp,
                txtDemandeComposant
            };
            
            int optComp = JOptionPane.showConfirmDialog(this, composantMsg,
                "Nouvelle Réparation - Étape 4/5: Composants",
                JOptionPane.OK_CANCEL_OPTION);
            
            if (optComp != JOptionPane.OK_OPTION) return;
            
            // Étape 5: Prix estimé
            String prixStr = null;
            float prix = 0;
            boolean prixValide = false;
            
            while (!prixValide) {
                prixStr = JOptionPane.showInputDialog(this,
                    "Prix estimé (DH):\n(Entrez un nombre positif)",
                    "Nouvelle Réparation - Étape 5/5: Prix",
                    JOptionPane.QUESTION_MESSAGE);
                
                if (prixStr == null) return; // Annulé
                
                if (prixStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Le prix est obligatoire", 
                        "Erreur de saisie", 
                        JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                
                try {
                    prix = Float.parseFloat(prixStr.trim());
                    if (prix < 0) {
                        JOptionPane.showMessageDialog(this, 
                            "Le prix ne peut pas être négatif", 
                            "Erreur de saisie", 
                            JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    prixValide = true;
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, 
                        "Format de prix invalide. Utilisez un nombre (ex: 150.5)", 
                        "Erreur de saisie", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
            
            // Validation des données de l'appareil
            if (txtMarque.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "La marque de l'appareil est obligatoire", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (txtModele.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Le modèle de l'appareil est obligatoire", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
	            // Créer la réparation AVEC LE CLIENT
            Reparation reparation = getReparationMetier().creerReparation(
                client, 
                getReparateur()
            );
                
                // Créer et ajouter l'appareil
                Appareil appareil = new Appareil();
                appareil.setMarque(txtMarque.getText().trim());
                appareil.setModele(txtModele.getText().trim());
                appareil.setImei(txtIMEI.getText().trim());
                appareil.setTypeAppareil((String) cbType.getSelectedItem());
                
                getReparationMetier().ajouterAppareil(reparation, appareil);
                
                // Ajouter les composants sélectionnés
                for (int i = 0; i < checkBoxes.length; i++) {
                    if (checkBoxes[i].isSelected()) {
                        getReparationMetier().ajouterComposant(
                            reparation, 
                            composantsDisponibles.get(i)
                        );
                    }
                }
                
                // Ajouter la demande de composant au commentaire si nécessaire
                String commentaireFinal = txtCommentaire.getText().trim();
                String demandeComp = txtDemandeComposant.getText().trim();
                if (!demandeComp.isEmpty()) {
                    commentaireFinal += "\n\n[DEMANDE COMPOSANT]: " + demandeComp;
                }
                
                reparation.setCommentaire(commentaireFinal);
                reparation.setPrixTotal(prix);
                reparation.setEtat("EN_ATTENTE");
                
                // Générer code de suivi
                getReparationMetier().genererCodeSuivi(reparation);
                
                JOptionPane.showMessageDialog(this,
                    "Réparation créée avec succès!\n\n" +
                    "Code de suivi: " + reparation.getCodeSuivi() + "\n" +
                    "Client: " + client.getNom() + " " + client.getPrenom() + "\n" +
                    "Appareil: " + appareil.getMarque() + " " + appareil.getModele(),
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
                
                loadReparations();
                
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Prix invalide", "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void changerEtatReparation() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une réparation", "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String code = (String) model.getValueAt(selectedRow, 0);
            Reparation r = getReparationMetier().rechercherParCodeSuivi(code);
            
            String[] etats = {"EN_ATTENTE", "EN_COURS", "TERMINEE", "LIVREE", "ANNULEE"};
            String currentEtat = r.getEtat();
            
            String nouvelEtat = (String) JOptionPane.showInputDialog(
                this,
                "État actuel: " + currentEtat + "\n\nChoisissez le nouvel état:",
                "Changer l'état de la réparation " + code,
                JOptionPane.QUESTION_MESSAGE,
                null,
                etats,
                currentEtat
            );
            
            if (nouvelEtat != null && !nouvelEtat.equals(currentEtat)) {
                getReparationMetier().changerEtat(r, nouvelEtat);
                JOptionPane.showMessageDialog(this, 
                    "État changé de '" + currentEtat + "' à '" + nouvelEtat + "'",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
                loadReparations();
            }
        } catch (ReparationException e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void afficherDetails(int row) {
        try {
            String code = (String) model.getValueAt(row, 0);
            Reparation r = getReparationMetier().rechercherParCodeSuivi(code);
            
            if (r != null) {
                // Construire les détails complets
                StringBuilder details = new StringBuilder();
                details.append("=== DÉTAILS RÉPARATION ===\n\n");
                details.append("Code suivi: ").append(r.getCodeSuivi()).append("\n");
                details.append("État: ").append(r.getEtat()).append("\n\n");
                
                details.append("--- Client ---\n");
                details.append("Nom: ").append(r.getClient().getNom()).append(" ").append(r.getClient().getPrenom()).append("\n");
                details.append("Téléphone: ").append(r.getClient().getTelephone()).append("\n\n");
                
                details.append("--- Appareil(s) ---\n");
                if (r.getAppareils() != null && !r.getAppareils().isEmpty()) {
                    for (Appareil app : r.getAppareils()) {
                        details.append("• ").append(app.getMarque()).append(" ").append(app.getModele())
                               .append(" (").append(app.getTypeAppareil()).append(")\n");
                        details.append("  IMEI: ").append(app.getImei()).append("\n");
                    }
                } else {
                    details.append("Aucun appareil\n");
                }
                
                details.append("\n--- Composants ---\n");
                if (r.getComposants() != null && !r.getComposants().isEmpty()) {
                    for (dao.Composant comp : r.getComposants()) {
                        details.append("• ").append(comp.getNom()).append(" - ").append(comp.getPrix()).append(" DH\n");
                    }
                } else {
                    details.append("Aucun composant\n");
                }
                
                details.append("\n--- Informations ---\n");
                details.append("Prix total: ").append(r.getPrixTotal()).append(" DH\n");
                String dateDepotStr;
                if (r.getDateDepot() != null) {
                    dateDepotStr = sdf.format(r.getDateDepot());
                } else {
                    dateDepotStr = "N/A";
                }
                details.append("Date dépôt: ").append(dateDepotStr).append("\n");
                String dateLivraisonStr;
                if (r.getDateLivraison() != null) {
                    dateLivraisonStr = sdf.format(r.getDateLivraison());
                } else {
                    dateLivraisonStr = "N/A";
                }
                details.append("Date livraison: ").append(dateLivraisonStr).append("\n");
                
                if (r.getCommentaire() != null && !r.getCommentaire().isEmpty()) {
                    details.append("\nCommentaire:\n").append(r.getCommentaire()).append("\n");
                }
                
                JTextArea textArea = new JTextArea(details.toString());
                textArea.setEditable(false);
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(500, 400));
                
                JOptionPane.showMessageDialog(this, scrollPane, "Détails Réparation - " + code, JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (ReparationException e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
        }
    }
    
    private void modifierReparation() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une réparation à modifier", "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String code = (String) model.getValueAt(selectedRow, 0);
            Reparation r = getReparationMetier().rechercherParCodeSuivi(code);
            
            if (r == null) return;
            
            // Créer un panneau de modification
            JPanel panelModif = new JPanel(new GridLayout(0, 2, 10, 10));
            
            JTextField txtPrix = new JTextField(String.valueOf(r.getPrixTotal()));
            String commentaireText;
            if (r.getCommentaire() != null) {
                commentaireText = r.getCommentaire();
            } else {
                commentaireText = "";
            }
            JTextArea txtCommentaire = new JTextArea(commentaireText, 5, 20);
            JScrollPane scrollComment = new JScrollPane(txtCommentaire);
            
            String[] etatsDisponibles = {"EN_ATTENTE", "EN_COURS", "TERMINEE", "LIVREE", "ANNULEE"};
            JComboBox<String> cbEtat = new JComboBox<>(etatsDisponibles);
            cbEtat.setSelectedItem(r.getEtat());
            
            panelModif.add(new JLabel("Prix total (DH):"));
            panelModif.add(txtPrix);
            panelModif.add(new JLabel("État:"));
            panelModif.add(cbEtat);
            panelModif.add(new JLabel("Commentaire:"));
            panelModif.add(scrollComment);
            
            int option = JOptionPane.showConfirmDialog(this, panelModif, 
                "Modifier Réparation - " + code, 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE);
            
            if (option == JOptionPane.OK_OPTION) {
                // Mettre à jour les champs modifiables
                float nouveauPrix = Float.parseFloat(txtPrix.getText().trim());
                String nouvelEtat = (String) cbEtat.getSelectedItem();
                String nouveauCommentaire = txtCommentaire.getText().trim();
                
                r.setPrixTotal(nouveauPrix);
                r.setEtat(nouvelEtat);
                r.setCommentaire(nouveauCommentaire);
                
                getReparationMetier().modifierReparation(r);
                
                JOptionPane.showMessageDialog(this, 
                    "Réparation modifiée avec succès!", 
                    "Succès", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadReparations();
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Prix invalide", "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void supprimerReparation() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une réparation à supprimer", "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String code = (String) model.getValueAt(selectedRow, 0);
            Reparation r = getReparationMetier().rechercherParCodeSuivi(code);
            
            if (r == null) {
                JOptionPane.showMessageDialog(this, "Réparation non trouvée", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Construire le message de confirmation en gérant les valeurs nulles
            StringBuilder message = new StringBuilder("Voulez-vous vraiment supprimer cette réparation?\n\n");
            String codeSuivi;
            if (r.getCodeSuivi() != null) {
                codeSuivi = r.getCodeSuivi();
            } else {
                codeSuivi = "N/A";
            }
            message.append("Code: ").append(codeSuivi).append("\n");
            
            if (r.getClient() != null) {
                message.append("Client: ").append(r.getClient().getNom()).append(" ").append(r.getClient().getPrenom()).append("\n");
            } else {
                message.append("Client: Non spécifié\n");
            }
            
            String etat;
            if (r.getEtat() != null) {
                etat = r.getEtat();
            } else {
                etat = "N/A";
            }
            message.append("État: ").append(etat).append("\n");
            message.append("Prix: ").append(r.getPrixTotal()).append(" DH\n\n");
            message.append("Cette action est irréversible!");
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                message.toString(),
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                getReparationMetier().supprimerReparation(r);
                JOptionPane.showMessageDialog(this, 
                    "Réparation supprimée avec succès!", 
                    "Succès", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadReparations();
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void genererPDFRecu() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une réparation", "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String code = (String) model.getValueAt(selectedRow, 0);
            Reparation r = getReparationMetier().rechercherParCodeSuivi(code);
            
            if (r != null) {
                // Choix du répertoire de sauvegarde
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Enregistrer le reçu PDF");
                fileChooser.setSelectedFile(new java.io.File("Recu_" + code + ".pdf"));
                
                int userSelection = fileChooser.showSaveDialog(this);
                
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    String chemin = fileChooser.getSelectedFile().getAbsolutePath();
                    if (!chemin.toLowerCase().endsWith(".pdf")) {
                        chemin += ".pdf";
                    }
                    
                    // Créer un reçu à partir de la réparation
                    dao.Recu recu = getReparationMetier().genererRecu(r, r.getPrixTotal());
                    getReparationMetier().genererRecuPDF(recu, chemin);
                    
                    JOptionPane.showMessageDialog(this,
                        "Reçu PDF généré avec succès!\n\nChemin: " + chemin,
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la génération du PDF: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}

