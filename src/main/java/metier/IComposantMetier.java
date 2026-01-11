package metier;

import dao.Boutique;
import dao.Composant;
import dao.Reparateur;
import exception.ComposantException;
import java.util.List;

// Interface métier pour la gestion des composants (pièces détachées)
public interface IComposantMetier {
    // Ajouter un composant au stock
    void ajouterComposant(Composant composant, Reparateur reparateur) throws ComposantException;
    
    // Modifier un composant
    void modifierComposant(Composant composant) throws ComposantException;
    
    // Supprimer un composant
    void supprimerComposant(Long id) throws ComposantException;
    
    // Rechercher par ID
    Composant chercherComposant(Long id) throws ComposantException;
    
    // Lister tous les composants
    List<Composant> listerComposants() throws ComposantException;
    
    // Lister par boutique
    List<Composant> listerComposantsParBoutique(Boutique boutique) throws ComposantException;
    
    // Rechercher par nom
    List<Composant> chercherComposantsParNom(String nom) throws ComposantException;
}
