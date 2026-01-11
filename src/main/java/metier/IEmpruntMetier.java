package metier;

import java.util.List;

import dao.Emprunt;
import dao.Reparateur;
import exception.EmpruntException;

// Interface métier pour la gestion des emprunts
// Types: ENTREE (prêt reçu) | SORTIE (prêt accordé)
public interface IEmpruntMetier {

    // Créer un emprunt
    Emprunt creerEmprunt(
            Reparateur reparateur,
            float montant,
            String type,
            String commentaire
    ) throws EmpruntException;

    // Lister les emprunts d'un réparateur
    List<Emprunt> listerEmpruntsParReparateur(Reparateur reparateur) throws EmpruntException;
    
    // Marquer un emprunt comme remboursé
    void rembourserEmprunt(Emprunt emprunt) throws EmpruntException;
    
    // Calculer le solde avec emprunts
    float calculerSoldeAvecEmprunts(Reparateur reparateur) throws EmpruntException;
}
