package metier;

import java.util.List;

import dao.Appareil;
import dao.Client;
import dao.Composant;
import dao.Recu;
import dao.Reparateur;
import dao.Reparation;
import exception.ReparationException;

// Interface métier pour la gestion des réparations
// États: EN_ATTENTE → EN_COURS → TERMINEE → LIVREE (ou ANNULEE)
public interface IReparationMetier {

    // Créer une réparation (état initial: EN_ATTENTE)
    Reparation creerReparation(Client client, Reparateur reparateur) throws ReparationException;

    // Ajouter un appareil à réparer
    void ajouterAppareil(Reparation reparation, Appareil appareil) throws ReparationException;

    // Ajouter un composant utilisé
    void ajouterComposant(Reparation reparation, Composant composant) throws ReparationException;

    // Changer l'état de la réparation
    void changerEtat(Reparation reparation, String nouvelEtat) throws ReparationException;

    // Calculer le prix total (composants + main d'œuvre)
    float calculerPrixTotal(Reparation reparation) throws ReparationException;

    // Lister les réparations d'un client
    List<Reparation> listerReparationsParClient(Client client) throws ReparationException;

    // Générer un code de suivi unique
    void genererCodeSuivi(Reparation reparation) throws ReparationException;
    
    // Lister les réparations d'un réparateur
    List<Reparation> listerReparationsParReparateur(Reparateur reparateur) throws ReparationException;
    
    // Lister toutes les réparations
    List<Reparation> listerToutesLesReparations() throws ReparationException;
    
    // Livrer au client (passe à LIVREE + date livraison)
    void livrerReparation(Reparation reparation) throws ReparationException;
    
    // Générer le reçu de paiement
    Recu genererRecu(Reparation reparation, float montantPaye) throws ReparationException;
    
    // Générer le PDF du reçu
    String genererRecuPDF(Recu recu, String cheminSortie) throws ReparationException;
    
    // Rechercher par code de suivi
    Reparation rechercherParCodeSuivi(String codeSuivi) throws ReparationException;
    
    // Annuler une réparation
    void annulerReparation(Reparation reparation) throws ReparationException;
    
    // Modifier une réparation
    void modifierReparation(Reparation reparation) throws ReparationException;
    
    // Supprimer une réparation
    void supprimerReparation(Reparation reparation) throws ReparationException;
    
    // Supprimer un appareil
    void supprimerAppareil(Reparation reparation, Appareil appareil) throws ReparationException;
    
    // Supprimer un composant
    void supprimerComposant(Reparation reparation, Composant composant) throws ReparationException;
}
