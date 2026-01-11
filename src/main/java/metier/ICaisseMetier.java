package metier;

import java.util.Date;
import java.util.List;

import dao.MouvementCaisse;
import dao.Proprietaire;
import dao.Reparateur;
import dao.Reparation;
import dao.StatistiquesCaisse;
import exception.CaisseException;

// Interface métier pour la gestion de la caisse
// Caisse système = solde total | Caisse réelle = solde - emprunts non remboursés
public interface ICaisseMetier {
    // Alimenter la caisse (ajout d'argent)
    void alimenterCaisse(Reparateur reparateur, float montant, String description) throws CaisseException;
    
    // Retirer de l'argent
    void retirerCaisse(Reparateur reparateur, float montant, String description) throws CaisseException;
    
    // Consulter le solde
    float consulterSolde(Reparateur reparateur) throws CaisseException;
    
    // Mouvements sur une période
    List<MouvementCaisse> consulterCaisseHebdomadaire(Reparateur reparateur, Date d1, Date d2) throws CaisseException;
    
    // Total des caisses d'une boutique
    float consulterTotalCaissesBoutique(Proprietaire proprietaire) throws CaisseException;
    
    // Lister tous les mouvements
    List<MouvementCaisse> listerMouvements(Reparateur reparateur) throws CaisseException;
    
    // Enregistrer un paiement client avec répartition des gains:
    // - Réparateur reçoit son pourcentageGain% (ex: 15%)
    // - Propriétaire reçoit le reste (ex: 85%)
    void enregistrerPaiement(Reparateur reparateur, float montant, String description, Reparation reparation) throws CaisseException;
    
    // Obtenir les statistiques
    StatistiquesCaisse obtenirStatistiques(Reparateur reparateur) throws CaisseException;
    
    // Revenus sur une période
    float calculerRevenusPeriode(Reparateur reparateur, Date dateDebut, Date dateFin) throws CaisseException;
    
    // Nombre de réparations
    int calculerNombreReparations(Reparateur reparateur) throws CaisseException;
    
    // Caisse réelle (argent disponible)
    float consulterCaisseReelle(Reparateur reparateur) throws CaisseException;
    
    // Caisse système (solde théorique)
    float consulterCaisseSysteme(Reparateur reparateur) throws CaisseException;
}