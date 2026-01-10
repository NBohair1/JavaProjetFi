package metier;

import java.util.List;

import dao.CaisseAvecEmprunt;
import dao.Emprunt;
import dao.Reparateur;
import exception.CaisseException;
import exception.EmpruntException;

/**
 * Interface pour la gestion de la caisse avec emprunts
 * Hérite de toutes les opérations de base de ICaisseMetier
 * et ajoute les opérations spécifiques aux entrées/sorties d'emprunt
 */
public interface ICaisseAvecEmpruntMetier extends ICaisseMetier {
    
    // Opérations spécifiques aux emprunts
    Emprunt creerEmpruntSortie(Reparateur reparateur, float montant, String description) throws EmpruntException;
    Emprunt creerEmpruntEntree(Reparateur reparateur, float montant, String description) throws EmpruntException;
    void rembourserEmprunt(Long idEmprunt, Reparateur reparateur) throws EmpruntException;
    
    // Consultation des emprunts
    List<Emprunt> listerEmpruntsActifs(Reparateur reparateur) throws CaisseException;
    List<Emprunt> listerTousEmprunts(Reparateur reparateur) throws CaisseException;
    float calculerTotalEmpruntsSortieActifs(Reparateur reparateur) throws CaisseException;
    float calculerTotalEmpruntsEntreeActifs(Reparateur reparateur) throws CaisseException;
    
    // Obtenir l'objet complet caisse avec emprunts
    CaisseAvecEmprunt obtenirCaisseAvecEmprunts(Reparateur reparateur) throws CaisseException;
}
