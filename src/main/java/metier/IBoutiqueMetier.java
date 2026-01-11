package metier;

import java.util.List;

import dao.Boutique;
import dao.Caisse;
import dao.Proprietaire;
import dao.Reparateur;
import dao.Reparation;
import dao.StatistiquesFinancieres;
import exception.BoutiqueException;

// Interface métier pour la gestion des boutiques et réparateurs
public interface IBoutiqueMetier {

    // Créer un réparateur dans une boutique
    Reparateur creerReparateur(
        Boutique boutique,
        String nom,
        String prenom,
        String email,
        String motDePasse,
        float pourcentageGain
    ) throws BoutiqueException;
    
    // Lister les réparateurs d'une boutique
    List<Reparateur> listerReparateurs(Boutique boutique) throws BoutiqueException;
    
    // Lister les boutiques d'un propriétaire
    List<Boutique> listerBoutiques(Proprietaire proprietaire) throws BoutiqueException;
    
    // Modifier une boutique
    Boutique modifierBoutique(
        Boutique boutique,
        String nom,
        String adresse,
        String numTelephone,
        String numPatente
    ) throws BoutiqueException;
    
    // Créer la caisse d'un réparateur
    Caisse creerCaisseReparateur(Reparateur reparateur) throws BoutiqueException;
    
    // Modifier le pourcentage de gain
    void modifierPourcentageGain(Reparateur reparateur, float nouveauPourcentage) throws BoutiqueException;
    
    // Modifier un réparateur
    void modifierReparateur(Reparateur reparateur) throws BoutiqueException;
    
    // Dernières réparations (propriétaire)
    List<Reparation> listerDernieresReparations(Proprietaire proprietaire, int limite) throws BoutiqueException;
    
    // Dernières réparations (boutique)
    List<Reparation> listerDernieresReparations(Boutique boutique, int limite) throws BoutiqueException;
    
    // Statistiques financières
    StatistiquesFinancieres obtenirStatistiquesFinancieres(Proprietaire proprietaire) throws BoutiqueException;
    
    // Lister les caisses (propriétaire)
    List<Caisse> listerCaissesReparateurs(Proprietaire proprietaire) throws BoutiqueException;
    
    // Lister les caisses (boutique)
    List<Caisse> listerCaissesReparateurs(Boutique boutique) throws BoutiqueException;
}
