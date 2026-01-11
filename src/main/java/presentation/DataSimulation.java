package presentation;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import dao.Appareil;
import dao.Boutique;
import dao.Caisse;
import dao.Client;
import dao.Composant;
import dao.Emprunt;
import dao.MouvementCaisse;
import dao.Proprietaire;
import dao.Recu;
import dao.Reparateur;
import dao.Reparation;

// Insère des données de test complètes pour la démo
public class DataSimulation {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("repairPU");
        EntityManager em = emf.createEntityManager();

        try {
            System.out.println("=== INSERTION DES DONNEES DE DEMONSTRATION ===");

            // Vérifier si les données existent déjà
            Long count = em.createQuery("SELECT COUNT(p) FROM Proprietaire p WHERE p.email = :email", Long.class)
                    .setParameter("email", "admin@repairshop.ma")
                    .getSingleResult();
            
            if (count > 0) {
                System.out.println("\nLes donnees existent deja.");
                afficherComptesTest();
                return;
            }

            em.getTransaction().begin();

            // 1. PROPRIETAIRE (ADMIN)
            System.out.println("\nCreation du proprietaire...");
            
            Proprietaire proprietaire = new Proprietaire();
            proprietaire.setNom("Alami");
            proprietaire.setPrenom("Hassan");
            proprietaire.setEmail("admin@repairshop.ma");
            proprietaire.setMdp("admin123");
            proprietaire.setPourcentageGain(100f);
            
            Caisse caisseProprietaire = new Caisse();
            caisseProprietaire.setSoldeActuel(15000f);
            caisseProprietaire.setDernierMouvement(new Date());
            em.persist(caisseProprietaire);
            proprietaire.setCaisse(caisseProprietaire);
            em.persist(proprietaire);
            System.out.println("   Proprietaire: admin@repairshop.ma (mdp: admin123)");

            // 2. BOUTIQUES (2)
            System.out.println("\nCreation des boutiques...");
            
            Boutique boutique1 = new Boutique();
            boutique1.setNom("RepairShop1");
            boutique1.setAdresse("MA7AL1, Rabat");
            boutique1.setNumTelephone("0522-123456");
            boutique1.setNumP("PAT-2024-001");
            boutique1.setProprietaire(proprietaire);
            em.persist(boutique1);
            System.out.println("   Boutique 1: " + boutique1.getNom());

            Boutique boutique2 = new Boutique();
            boutique2.setNom("RepairShop2");
            boutique2.setAdresse("MA7AL, Rabat");
            boutique2.setNumTelephone("0522-654321");
            boutique2.setNumP("PAT-2024-002");
            boutique2.setProprietaire(proprietaire);
            em.persist(boutique2);
            System.out.println("   Boutique 2: " + boutique2.getNom());

            // 3. REPARATEURS (4)
            System.out.println("\nCreation des reparateurs...");
            
            // Réparateur 1 - Boutique 1
            Reparateur rep1 = creerReparateur(em, "Bohair", "Nabil", "Nabil@repairshop.ma", "rep123", 15f, boutique1, 3500f);
            System.out.println("   Reparateur 1: Nabil@repairshop.ma (15% gain)");
            
            // Réparateur 2 - Boutique 1
            Reparateur rep2 = creerReparateur(em, "Hadraoui", "Wiame", "Wiame@repairshop.ma", "rep123", 20f, boutique1, 4200f);
            System.out.println("   Reparateur 2: Wiame@repairshop.ma (20% gain)");
            
            // Réparateur 3 - Boutique 2
            Reparateur rep3 = creerReparateur(em, "Alla", "Karim", "karim@repairshop.ma", "rep123", 18f, boutique2, 2800f);
            System.out.println("   Reparateur 3: karim@repairshop.ma (18% gain)");
            
            // Réparateur 4 - Boutique 2
            Reparateur rep4 = creerReparateur(em, "Bouroumi", "Anass", "Anass@repairshop.ma", "rep123", 22f, boutique2, 3100f);
            System.out.println("   Reparateur 4: Anass@repairshop.ma (22% gain)");

            Reparateur[] reparateurs = {rep1, rep2, rep3, rep4};

            // 4. CLIENTS (12)
            System.out.println("\nCreation des clients...");
            
            String[][] clientsData = {
                {"Tazi", "Mohamed", "0661-234567", "45 Rue Allal Ben Abdellah, Rabat", "true"},
                {"Hassani", "Amina", "0662-345678", "78 Avenue Hassan II, Fès", "true"},
                {"Chraibi", "Youssef", "0663-456789", "12 Boulevard Zerktouni, Marrakech", "false"},
                {"Benjelloun", "Laila", "0664-567890", "90 Rue de la Liberté, Tanger", "true"},
                {"Ouazzani", "Omar", "0665-678901", "34 Avenue Mohammed VI, Agadir", "false"},
                {"Filali", "Nadia", "0666-789012", "56 Rue Fès, Meknès", "true"},
                {"Berrada", "Hamza", "0667-890123", "23 Boulevard Anfa, Casablanca", "true"},
                {"Sqalli", "Khadija", "0668-901234", "67 Avenue FAR, Rabat", "false"},
                {"Lamrani", "Amine", "0669-012345", "89 Rue Moulay Ismail, Oujda", "true"},
                {"Kettani", "Salma", "0670-123456", "12 Place Jamaa El Fna, Marrakech", "false"},
                {"Benkiran", "Rachid", "0671-234567", "45 Corniche, Mohammedia", "true"},
                {"Alaoui", "Zineb", "0672-345678", "78 Rue de Paris, Casablanca", "false"}
            };
            
            Client[] clients = new Client[clientsData.length];
            for (int i = 0; i < clientsData.length; i++) {
                clients[i] = creerClient(em, clientsData[i][0], clientsData[i][1], 
                    clientsData[i][2], clientsData[i][3], Boolean.parseBoolean(clientsData[i][4]));
            }
            System.out.println("   " + clients.length + " clients crees (6 fideles)");

            // 5. COMPOSANTS (15)
            System.out.println("\nCreation des composants...");
            
            // Écrans
            creerComposant(em, "Écran LCD iPhone 12", 450f, 280f, 15, "iPhone 12");
            creerComposant(em, "Écran OLED iPhone 13", 550f, 350f, 12, "iPhone 13");
            creerComposant(em, "Écran OLED iPhone 14", 650f, 420f, 8, "iPhone 14");
            creerComposant(em, "Écran Samsung Galaxy S21", 380f, 240f, 18, "Samsung S21");
            creerComposant(em, "Écran Samsung Galaxy S22", 420f, 280f, 14, "Samsung S22");
            
            // Batteries
            creerComposant(em, "Batterie iPhone (tous modèles)", 120f, 60f, 50, "iPhone");
            creerComposant(em, "Batterie Samsung Galaxy S21", 100f, 50f, 45, "Samsung");
            creerComposant(em, "Batterie Xiaomi Redmi", 80f, 35f, 60, "Xiaomi");
            
            // Autres composants
            creerComposant(em, "Caméra arrière iPhone 12/13", 280f, 180f, 20, "iPhone");
            creerComposant(em, "Caméra arrière Samsung", 220f, 140f, 25, "Samsung");
            creerComposant(em, "Connecteur USB-C universel", 45f, 15f, 100, "Universel");
            creerComposant(em, "Connecteur Lightning iPhone", 55f, 25f, 80, "iPhone");
            creerComposant(em, "Vitre de protection universelle", 25f, 8f, 200, "Universel");
            creerComposant(em, "Haut-parleur universel", 35f, 12f, 70, "Universel");
            creerComposant(em, "Kit vis + colle réparation", 15f, 5f, 150, "Universel");
            
            System.out.println("   15 composants crees");

            // 6. REPARATIONS (20) - Differents etats
            System.out.println("\nCreation des reparations...");
            
            Calendar cal = Calendar.getInstance();
            int repCount = 0;
            
            // --- 5 réparations LIVREE (historique payé) ---
            String[][] reparationsLivrees = {
                {"REP-2025-001", "Remplacement écran fissuré", "Apple", "iPhone 12 Pro", "356789000000001", "500"},
                {"REP-2025-002", "Changement batterie usée", "Samsung", "Galaxy S21", "356789000000002", "150"},
                {"REP-2025-003", "Réparation caméra arrière", "Apple", "iPhone 13", "356789000000003", "320"},
                {"REP-2025-004", "Connecteur de charge défectueux", "Xiaomi", "Redmi Note 10", "356789000000004", "85"},
                {"REP-2025-005", "Écran + vitre protection", "Samsung", "Galaxy S22", "356789000000005", "480"}
            };
            
            for (int i = 0; i < reparationsLivrees.length; i++) {
                cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, -(30 - i*5)); // Dates passées
                Date dateDepot = cal.getTime();
                cal.add(Calendar.DAY_OF_MONTH, 3);
                Date dateLivraison = cal.getTime();
                
                Reparation rep = creerReparation(em, reparationsLivrees[i][0], "LIVREE", 
                    reparationsLivrees[i][1], dateDepot, dateLivraison, 
                    Float.parseFloat(reparationsLivrees[i][5]), 
                    clients[i % clients.length], reparateurs[i % reparateurs.length]);
                
                creerAppareil(em, reparationsLivrees[i][4], reparationsLivrees[i][2], 
                    reparationsLivrees[i][3], "Smartphone", rep);
                
                // Créer reçu pour réparations livrées
                creerRecu(em, "REC-2025-00" + (i+1), dateLivraison, 
                    Float.parseFloat(reparationsLivrees[i][5]), "ESPECES", 
                    clients[i % clients.length], reparateurs[i % reparateurs.length], rep);
                
                // Mouvement de caisse avec répartition des gains
                float montantTotal = Float.parseFloat(reparationsLivrees[i][5]);
                Reparateur repActuel = reparateurs[i % reparateurs.length];
                float pourcentage = repActuel.getPourcentageGain();
                float gainReparateur = montantTotal * (pourcentage / 100f);
                float gainProprietaire = montantTotal - gainReparateur;
                
                // Part du réparateur
                creerMouvement(em, "ENTREE", gainReparateur,
                    "Paiement " + reparationsLivrees[i][0] + " (" + pourcentage + "% de " + montantTotal + " DH)", 
                    dateLivraison, repActuel.getCaisse(), rep);
                
                // Part du propriétaire
                creerMouvement(em, "ENTREE", gainProprietaire,
                    "Paiement " + reparationsLivrees[i][0] + " (part boutique: " + (100-pourcentage) + "%)", 
                    dateLivraison, caisseProprietaire, rep);
                
                repCount++;
            }
            System.out.println("   5 reparations LIVREE (payees)");

            // --- 5 réparations TERMINEE (en attente de récupération) ---
            String[][] reparationsTerminees = {
                {"REP-2026-001", "Changement écran OLED", "Apple", "iPhone 14", "356789000000006", "700"},
                {"REP-2026-002", "Batterie + nettoyage", "Samsung", "Galaxy S21", "356789000000007", "180"},
                {"REP-2026-003", "Réparation haut-parleur", "Xiaomi", "Mi 11", "356789000000008", "95"},
                {"REP-2026-004", "Caméra + mise à jour", "Apple", "iPhone 12", "356789000000009", "350"},
                {"REP-2026-005", "Connecteur + test complet", "Huawei", "P40 Lite", "356789000000010", "120"}
            };
            
            for (int i = 0; i < reparationsTerminees.length; i++) {
                cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, -(7 - i)); // Récentes
                Date dateDepot = cal.getTime();
                cal.add(Calendar.DAY_OF_MONTH, 2);
                
                Date datePrevue = cal.getTime();
                
                Reparation rep = creerReparation(em, reparationsTerminees[i][0], "TERMINEE", 
                    reparationsTerminees[i][1], dateDepot, datePrevue, 
                    Float.parseFloat(reparationsTerminees[i][5]), 
                    clients[(i+5) % clients.length], reparateurs[i % reparateurs.length]);
                
                creerAppareil(em, reparationsTerminees[i][4], reparationsTerminees[i][2], 
                    reparationsTerminees[i][3], "Smartphone", rep);
                repCount++;
            }
            System.out.println("   5 reparations TERMINEE (a recuperer)");

            // --- 5 réparations EN_COURS ---
            String[][] reparationsEnCours = {
                {"REP-2026-006", "Diagnostic + écran", "Apple", "iPhone 13 Pro", "356789000000011", "580"},
                {"REP-2026-007", "Batterie gonflée - urgent", "Samsung", "Galaxy A52", "356789000000012", "130"},
                {"REP-2026-008", "Problème tactile", "Xiaomi", "Redmi Note 11", "356789000000013", "220"},
                {"REP-2026-009", "Écran noir après chute", "Apple", "iPhone 12 Mini", "356789000000014", "420"},
                {"REP-2026-010", "Caméra floue", "Samsung", "Galaxy S22 Ultra", "356789000000015", "280"}
            };
            
            for (int i = 0; i < reparationsEnCours.length; i++) {
                cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, -(3 - i));
                Date dateDepot = cal.getTime();
                cal.add(Calendar.DAY_OF_MONTH, 5);
                Date datePrevue = cal.getTime();
                
                Reparation rep = creerReparation(em, reparationsEnCours[i][0], "EN_COURS", 
                    reparationsEnCours[i][1], dateDepot, datePrevue, 
                    Float.parseFloat(reparationsEnCours[i][5]), 
                    clients[(i+3) % clients.length], reparateurs[(i+1) % reparateurs.length]);
                
                creerAppareil(em, reparationsEnCours[i][4], reparationsEnCours[i][2], 
                    reparationsEnCours[i][3], "Smartphone", rep);
                repCount++;
            }
            System.out.println("   5 reparations EN_COURS");

            // --- 3 réparations EN_ATTENTE ---
            String[][] reparationsAttente = {
                {"REP-2026-011", "Attente pièce écran spécial", "OnePlus", "9 Pro", "356789000000016", "450"},
                {"REP-2026-012", "Client contacté - confirme", "Apple", "iPhone SE", "356789000000017", "180"},
                {"REP-2026-013", "Devis en cours", "Google", "Pixel 6", "356789000000018", "350"}
            };
            
            for (int i = 0; i < reparationsAttente.length; i++) {
                cal = Calendar.getInstance();
                Date dateDepot = cal.getTime();
                cal.add(Calendar.DAY_OF_MONTH, 7);
                Date datePrevue = cal.getTime();
                
                Reparation rep = creerReparation(em, reparationsAttente[i][0], "EN_ATTENTE", 
                    reparationsAttente[i][1], dateDepot, datePrevue, 
                    Float.parseFloat(reparationsAttente[i][5]), 
                    clients[(i+8) % clients.length], reparateurs[(i+2) % reparateurs.length]);
                
                creerAppareil(em, reparationsAttente[i][4], reparationsAttente[i][2], 
                    reparationsAttente[i][3], "Smartphone", rep);
                repCount++;
            }
            System.out.println("   3 reparations EN_ATTENTE");

            // --- 2 réparations ANNULEE ---
            String[][] reparationsAnnulees = {
                {"REP-2025-099", "Client ne répond plus", "Nokia", "G50", "356789000000019", "150"},
                {"REP-2025-098", "Appareil irréparable", "Sony", "Xperia 5", "356789000000020", "200"}
            };
            
            for (int i = 0; i < reparationsAnnulees.length; i++) {
                cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, -45);
                Date dateDepot = cal.getTime();
                cal.add(Calendar.DAY_OF_MONTH, 7);
                Date dateAnnulation = cal.getTime();
                
                Reparation rep = creerReparation(em, reparationsAnnulees[i][0], "ANNULEE", 
                    reparationsAnnulees[i][1], dateDepot, dateAnnulation, 
                    Float.parseFloat(reparationsAnnulees[i][5]), 
                    clients[(i+10) % clients.length], reparateurs[i % reparateurs.length]);
                
                creerAppareil(em, reparationsAnnulees[i][4], reparationsAnnulees[i][2], 
                    reparationsAnnulees[i][3], "Smartphone", rep);
                repCount++;
            }
            System.out.println("   2 reparations ANNULEE");
            System.out.println("   Total: " + repCount + " reparations");

            // 7. EMPRUNTS (6)
            System.out.println("\nCreation des emprunts...");
            
            // Emprunts SORTIE (prêts accordés par le réparateur)
            creerEmprunt(em, rep1, 500f, "SORTIE", "Avance pour Ahmed - urgence familiale", false);
            creerEmprunt(em, rep2, 300f, "SORTIE", "Prêt pour achat matériel", false);
            creerEmprunt(em, rep3, 200f, "SORTIE", "Dépannage temporaire", true); // Remboursé
            
            // Emprunts ENTREE (prêts reçus par le réparateur)
            creerEmprunt(em, rep1, 1000f, "ENTREE", "Avance sur salaire", false);
            creerEmprunt(em, rep4, 400f, "ENTREE", "Prêt du propriétaire", true); // Remboursé
            creerEmprunt(em, rep2, 250f, "ENTREE", "Aide exceptionnelle", false);
            
            System.out.println("   6 emprunts crees (2 rembourses)");

            System.out.println("\nMouvements de caisse: 10 entrees creees (paiements reparations)");

            // COMMIT
            em.getTransaction().commit();
            
            System.out.println("\n=== DONNEES INSEREES AVEC SUCCES ===");
            
            afficherComptesTest();
            afficherStatistiques();
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("\nErreur lors de l'insertion: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }

    // METHODES UTILITAIRES
    
    private static Reparateur creerReparateur(EntityManager em, String nom, String prenom, 
            String email, String mdp, float pourcentage, Boutique boutique, float soldeCaisse) {
        Caisse caisse = new Caisse();
        caisse.setSoldeActuel(soldeCaisse);
        caisse.setDernierMouvement(new Date());
        em.persist(caisse);
        
        Reparateur rep = new Reparateur();
        rep.setNom(nom);
        rep.setPrenom(prenom);
        rep.setEmail(email);
        rep.setMdp(mdp);
        rep.setPourcentageGain(pourcentage);
        rep.setBoutique(boutique);
        rep.setCaisse(caisse);
        em.persist(rep);
        return rep;
    }
    
    private static Client creerClient(EntityManager em, String nom, String prenom, 
            String telephone, String adresse, boolean fidele) {
        Client client = new Client();
        client.setNom(nom);
        client.setPrenom(prenom);
        client.setTelephone(telephone);
        client.setAdresse(adresse);
        client.setFidele(fidele);
        em.persist(client);
        return client;
    }
    
    private static Composant creerComposant(EntityManager em, String nom, float prix, 
            float prixAchat, int quantite, String typeAppareil) {
        Composant comp = new Composant();
        comp.setNom(nom);
        comp.setPrix(prix);
        comp.setPrixAchat(prixAchat);
        comp.setQuantite(quantite);
        comp.setTypeAppareil(typeAppareil);
        em.persist(comp);
        return comp;
    }
    
    private static Reparation creerReparation(EntityManager em, String code, String etat, 
            String commentaire, Date dateDepot, Date dateLivraison, float prix, 
            Client client, Reparateur reparateur) {
        Reparation rep = new Reparation();
        rep.setCodeSuivi(code);
        rep.setEtat(etat);
        rep.setCommentaire(commentaire);
        rep.setDateDepot(dateDepot);
        rep.setDateLivraison(dateLivraison);
        rep.setPrixTotal(prix);
        rep.setClient(client);
        rep.setReparateur(reparateur);
        em.persist(rep);
        return rep;
    }
    
    private static Appareil creerAppareil(EntityManager em, String imei, String marque, 
            String modele, String type, Reparation reparation) {
        Appareil app = new Appareil();
        app.setImei(imei);
        app.setMarque(marque);
        app.setModele(modele);
        app.setTypeAppareil(type);
        app.setReparation(reparation);
        em.persist(app);
        return app;
    }
    
    private static Recu creerRecu(EntityManager em, String numero, Date date, float montant, 
            String modePaiement, Client client, Reparateur reparateur, Reparation reparation) {
        Recu recu = new Recu();
        recu.setNumeroRecu(numero);
        recu.setDate(date);
        recu.setMontant(montant);
        recu.setModePaiement(modePaiement);
        recu.setClient(client);
        recu.setReparateur(reparateur);
        recu.setReparation(reparation);
        em.persist(recu);
        return recu;
    }
    
    private static MouvementCaisse creerMouvement(EntityManager em, String type, float montant, 
            String description, Date date, Caisse caisse, Reparation reparation) {
        MouvementCaisse mv = new MouvementCaisse();
        mv.setTypeMouvement(type);
        mv.setMontant(montant);
        mv.setDescription(description);
        mv.setDateMouvement(date);
        mv.setCaisse(caisse);
        mv.setReparation(reparation);
        em.persist(mv);
        return mv;
    }
    
    private static Emprunt creerEmprunt(EntityManager em, Reparateur reparateur, float montant, 
            String type, String commentaire, boolean rembourse) {
        Emprunt emp = new Emprunt();
        emp.setReparateur(reparateur);
        emp.setMontant(montant);
        emp.setType(type);
        emp.setCommentaire(commentaire);
        emp.setDate(new Date());
        emp.setRembourse(rembourse);
        if (rembourse) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -5);
            emp.setDateRemboursement(cal.getTime());
        }
        em.persist(emp);
        return emp;
    }
    
    private static void afficherComptesTest() {
        System.out.println("\n--- COMPTES DE TEST DISPONIBLES ---");
        System.out.println("PROPRIETAIRE (Admin complet):");
        System.out.println("   Email: admin@repairshop.ma");
        System.out.println("   Mot de passe: admin123");
        System.out.println("REPARATEURS:");
    }
    
    private static void afficherStatistiques() {
        System.out.println("\n--- DONNEES DE DEMONSTRATION ---");
        System.out.println("  - 1 Proprietaire");
        System.out.println("  - 2 Boutiques");
        System.out.println("  - 4 Reparateurs");
        System.out.println("  - 12 Clients (6 fideles)");
        System.out.println("  - 15 Composants");
        System.out.println("  - 20 Reparations:");
        System.out.println("      5 LIVREE (payees)");
        System.out.println("      5 TERMINEE (a recuperer)");
        System.out.println("      5 EN_COURS");
        System.out.println("      3 EN_ATTENTE");
        System.out.println("      2 ANNULEE");
        System.out.println("  - 6 Emprunts (2 rembourses)");
        System.out.println("  - 10 Mouvements caisse (entrees paiements)");
        System.out.println("  - 5 Recus de paiement");
    }
}
